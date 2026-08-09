package com.amit_kundu_io.utility.validation_exception

/** Signals client input that cannot be accepted as a song. */
class SongValidationException(message: String) : IllegalArgumentException(message)

