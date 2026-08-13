package com.plugins.Backblaze_B2.storage

/**
 * A chunk of bytes read off the source channel. [bytes] is a reused backing
 * array that is larger than the meaningful data on the final chunk -- always
 * read exactly [length] bytes from it, never bytes.size.
 */
data class Chunk(val bytes: ByteArray, val length: Int)
