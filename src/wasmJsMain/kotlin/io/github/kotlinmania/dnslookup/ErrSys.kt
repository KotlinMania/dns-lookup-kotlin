// port-lint: ignore — actual for the commonMain `ErrSys` expect-object; WasmJS has no native libc, so values follow Linux glibc EAI conventions.
package io.github.kotlinmania.dnslookup

internal actual object ErrSys {
    actual val EAI_AGAIN: Int = -3
    actual val EAI_BADFLAGS: Int = -1
    actual val EAI_FAIL: Int = -4
    actual val EAI_FAMILY: Int = -6
    actual val EAI_MEMORY: Int = -10
    actual val EAI_NONAME: Int = -2
    actual val EAI_NODATA: Int = -5
    actual val EAI_SERVICE: Int = -8
    actual val EAI_SOCKTYPE: Int = -7
    actual val EAI_SYSTEM: Int = -11
    actual val EAI_ABSENT: Int = Int.MIN_VALUE

    actual fun gaiStrError(err: Int): String = when (err) {
        EAI_BADFLAGS -> "Bad value for ai_flags"
        EAI_NONAME -> "Name or service not known"
        EAI_AGAIN -> "Temporary failure in name resolution"
        EAI_FAIL -> "Non-recoverable failure in name resolution"
        EAI_NODATA -> "No address associated with hostname"
        EAI_FAMILY -> "ai_family not supported"
        EAI_SOCKTYPE -> "ai_socktype not supported"
        EAI_SERVICE -> "Servname not supported for ai_socktype"
        EAI_MEMORY -> "Memory allocation failure"
        EAI_SYSTEM -> "System error"
        else -> "Unknown error: $err"
    }

    actual fun lastOsError(): Throwable = Exception("system error from getaddrinfo / getnameinfo")
}
