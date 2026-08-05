package com.amit_kundu_io.database

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory

/** Applies versioned PostgreSQL schema changes before the API accepts requests. */
object FlywayFactory {
    private val logger = LoggerFactory.getLogger(FlywayFactory::class.java)

    /** Repairs history only when explicitly requested, then validates and migrates normally. */
    fun migrate() {
        val flyway = Flyway.configure()
            .dataSource(
                DatabaseConfig.url,
                DatabaseConfig.user,
                DatabaseConfig.password
            )
            .locations("classpath:db/migration")
            .load()

        // Use only to recover a development database after an already-applied
        // migration was edited. Keep this disabled in normal and production runs.
        if (System.getenv("FLYWAY_REPAIR_ON_STARTUP")?.toBoolean() == true) {
            logger.warn("Running requested Flyway repair before migration")
            flyway.repair()
        }

        flyway
            .migrate()
    }
}
