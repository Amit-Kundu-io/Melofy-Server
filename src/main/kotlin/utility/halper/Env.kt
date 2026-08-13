package com.studycore.util

import io.github.cdimascio.dotenv.dotenv

/** Centralized environment reader for runtime configuration that is required by the server. */
object Env {

    private val dotenv = dotenv {
        ignoreIfMissing = true
        ignoreIfMalformed = true
    }

    /** Reads one mandatory setting from the process environment or local .env file. */
    private fun get(name: String): String = System.getenv(name) ?: dotenv[name] ?: error("$name is missing")

    val jwtSecret = get("JWT_SECRET")
    val jwtIssuer = get("JWT_ISSUER")
    val jwtAudience = get("JWT_AUDIENCE")
    val jwtRealm = get("JWT_REALM")

    val accessExpireHr = get("JWT_ACCESS_EXPIRE_HOURS").toLong()

    val refreshExpireDay = get("JWT_REFRESH_EXPIRE_DAYS").toLong()

    val dbPoolSize: Int = (System.getenv("DB_POOL_SIZE") ?: "20").toInt()

    val KEY_ID = get("B2_KEY_ID")
    val B2_BUCKET_ID = get("B2_BUCKET_ID")
    val B2_APP_KEY = get("B2_APP_KEY")

}
