package com.amit_kundu_io.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

import com.studycore.util.Env
import org.jetbrains.exposed.v1.core.DatabaseConfig as ExposedDatabaseConfig
import org.slf4j.LoggerFactory

object DatabaseFactory {

    private val logger = LoggerFactory.getLogger("DatabaseFactory")

    lateinit var database: Database
        private set

    private lateinit var dataSource: HikariDataSource

    fun init() {
        val config = HikariConfig().apply {
            poolName = "app-db-pool"

            jdbcUrl = DatabaseConfig.url
            username = DatabaseConfig.user
            password = DatabaseConfig.password
            driverClassName = DatabaseConfig.driver

            // Fixed pool size (Hikari's own recommendation: min == max) - avoids
            // grow/shrink overhead exactly when you're under load. Tune via env,
            // not a redeploy: start at (cpu_cores * 2) + spindle_count, then load-test.
            maximumPoolSize = Env.dbPoolSize
            minimumIdle = Env.dbPoolSize

            isAutoCommit = false

            // Fail fast on pool exhaustion (3s) instead of Hikari's default 30s hang -
            // visible backpressure beats a silently frozen request.
            connectionTimeout = 3_000

            idleTimeout = 600_000
            maxLifetime = 1_800_000   // keep below  DB server's own connection recycling window

            validationTimeout = 5_000

            // Logs a stack trace if a connection is held longer than this without being
            // returned - the standard way pool-exhaustion incidents get diagnosed instead
            // of just "the app hung, no idea why."
            leakDetectionThreshold = 15_000

        }

        dataSource = HikariDataSource(config)

        database = Database.connect(
            dataSource,
            databaseConfig = ExposedDatabaseConfig {
                // Logs any single query that takes longer than this - your early warning
                // for missing indexes / bad query plans before users notice slowness.
                warnLongQueriesDuration = 1000

                // Exposed's per-statement SQL logging is expensive at real traffic volume -
                // make sure it's off. Turn on only ad-hoc, locally, when debugging one query.
                sqlLogger = null
            }
        )

        transaction {
            exec("SELECT 1")
        }

        logger.info("Database initialized - pool='{}' size={}", config.poolName, config.maximumPoolSize)
    }

    fun close() {
        dataSource.close()
    }
}