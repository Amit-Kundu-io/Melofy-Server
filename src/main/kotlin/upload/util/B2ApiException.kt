package upload.util

/** Thrown for any non-2xx response from B2, carrying enough to log/report cleanly upstream. */
class B2ApiException(
    val httpStatus: Int,
    val b2Code: String?,
    val b2Message: String?,
    rawBody: String
) : Exception("B2 error $httpStatus${b2Code?.let { " [$it]" } ?: ""}: ${b2Message ?: rawBody}")
