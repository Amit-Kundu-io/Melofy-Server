package com.amit_kundu_io.config

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/articles")
/** Example type-safe route retained from the Ktor project template. */
class Articles(val sort: String? = "new")
