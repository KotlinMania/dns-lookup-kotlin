// port-lint: ignore — actual for the commonMain `ErrSys` expect-object; values come from Apple/BSD netdb.h.
package io.github.kotlinmania.dnslookup

internal actual object ErrSys {
    actual val EAI_AGAIN: Int = 2
    actual val EAI_BADFLAGS: Int = 3
    actual val EAI_FAIL: Int = 4
    actual val EAI_FAMILY: Int = 5
    actual val EAI_MEMORY: Int = 6
    actual val EAI_NODATA: Int = 7
    actual val EAI_NONAME: Int = 8
    actual val EAI_SERVICE: Int = 9
    actual val EAI_SOCKTYPE: Int = 10
    actual val EAI_SYSTEM: Int = 11
    actual val EAI_ABSENT: Int = Int.MIN_VALUE

    actual fun gaiStrError(err: Int): String = when (err) {
        EAI_AGAIN -> "Temporary failure in name resolution"
        EAI_BADFLAGS -> "Invalid value for ai_flags"
        EAI_FAIL -> "Non-recoverable failure in name resolution"
        EAI_FAMILY -> "ai_family not supported"
        EAI_MEMORY -> "Memory allocation failure"
        EAI_NODATA -> "No address associated with nodename"
        EAI_NONAME -> "nodename nor servname provided, or not known"
        EAI_SERVICE -> "servname not supported for ai_socktype"
        EAI_SOCKTYPE -> "ai_socktype not supported"
        EAI_SYSTEM -> "System error"
        else -> "Unknown error: $err"
    }

    actual fun lastOsError(): Throwable = Exception("system error from getaddrinfo / getnameinfo")
}
