package com.amit_kundu_io.utility.validation_exception

/** Signals invalid playlist input supplied by a client. */
class PlaylistValidationException(message: String) : IllegalArgumentException(message)