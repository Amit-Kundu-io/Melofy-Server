package com.amit_kundu_io.database

import io.github.cdimascio.dotenv.dotenv

/** Reads database credentials from environment variables first, then the local .env file. */
object DatabaseConfig {

    private val dotenv = dotenv {
        ignoreIfMissing = true
        ignoreIfMalformed = true
    }

    val url = System.getenv("DB_URL")
        ?: dotenv["DB_URL"]
        ?: error("DB_URL is missing")

    val user = System.getenv("DB_USER")
        ?: dotenv["DB_USER"]
        ?: error("DB_USER is missing")

    val password = System.getenv("DB_PASSWORD")
        ?: dotenv["DB_PASSWORD"]
        ?: error("DB_PASSWORD is missing")

    val driver = System.getenv("DB_DRIVER")
        ?: dotenv["DB_DRIVER"]
        ?: "org.postgresql.Driver"
}
