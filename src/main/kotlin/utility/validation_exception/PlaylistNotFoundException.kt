package com.amit_kundu_io.utility.validation_exception

/** Signals a syntactically valid playlist identifier that is absent from storage. */
class PlaylistNotFoundException : NoSuchElementException("Playlist not found")


