package com.amit_kundu_io.config

import io.ktor.server.application.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.studycore.util.Env

/** Configures JWT authentication for future protected routes; it is not currently installed. */
fun Application.configureSecurity() {
    authentication {
        jwt("auth-jwt") {
            realm = Env.jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(Env.jwtSecret))
                    .withAudience(Env.jwtAudience)
                    .withIssuer(Env.jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(Env.jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
