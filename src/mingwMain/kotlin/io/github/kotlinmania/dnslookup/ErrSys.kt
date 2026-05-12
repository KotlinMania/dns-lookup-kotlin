// port-lint: ignore — actual for the commonMain `ErrSys` expect-object; values come from Win32 winsock2.h.
package io.github.kotlinmania.dnslookup

internal actual object ErrSys {
    // WinSock numerics from Win32 winsock2.h. The upstream Rust port
    // translates between these and the EAI_ enum on Windows; this Kotlin
    // surface keeps the same shape by re-using the EAI_ names while
    // holding the WSA numeric values. `EAI_SYSTEM` has no winsock
    // counterpart, so it surfaces as the `EAI_ABSENT` sentinel.
    actual val EAI_AGAIN: Int = 11002      // WSATRY_AGAIN
    actual val EAI_BADFLAGS: Int = 10022   // WSAEINVAL
    actual val EAI_FAIL: Int = 11003       // WSANO_RECOVERY
    actual val EAI_FAMILY: Int = 10047     // WSAEAFNOSUPPORT
    actual val EAI_MEMORY: Int = 8         // WSA_NOT_ENOUGH_MEMORY
    actual val EAI_NONAME: Int = 11001     // WSAHOST_NOT_FOUND
    actual val EAI_NODATA: Int = 11004     // WSANO_DATA
    actual val EAI_SERVICE: Int = 10109    // WSATYPE_NOT_FOUND
    actual val EAI_SOCKTYPE: Int = 10044   // WSAESOCKTNOSUPPORT
    actual val EAI_ABSENT: Int = Int.MIN_VALUE
    actual val EAI_SYSTEM: Int = EAI_ABSENT

    actual fun gaiStrError(err: Int): String = when (err) {
        EAI_AGAIN -> "A temporary failure in name resolution occurred"
        EAI_BADFLAGS -> "An invalid value was provided for the ai_flags member of the pHints parameter"
        EAI_FAIL -> "A non-recoverable failure in name resolution occurred"
        EAI_FAMILY -> "The ai_family member of the pHints parameter is not supported"
        EAI_MEMORY -> "A memory allocation failure occurred"
        EAI_NONAME -> "The name does not resolve for the supplied parameters"
        EAI_NODATA -> "No address associated with nodename"
        EAI_SERVICE -> "The pServiceName parameter is not supported for the specified ai_socktype member of the pHints parameter"
        EAI_SOCKTYPE -> "The ai_socktype member of the pHints parameter is not supported"
        else -> "Unknown error: $err"
    }

    actual fun lastOsError(): Throwable = Exception("system error from getaddrinfo / getnameinfo")
}
