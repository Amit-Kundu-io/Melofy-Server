package com.amit_kundu_io.database

import org.flywaydb.core.Flyway

object FlywayFactory {
    fun migrate() {
        Flyway.configure()
            .dataSource(
                DatabaseConfig.url,
                DatabaseConfig.user,
                DatabaseConfig.password
            )
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }
}