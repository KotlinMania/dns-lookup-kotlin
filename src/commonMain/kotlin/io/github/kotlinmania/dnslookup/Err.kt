// port-lint: source src/err.rs
package io.github.kotlinmania.dnslookup

/**
 * Stores a lookup error from `getaddrinfo` or `getnameinfo`. The upstream
 * Rust type implements `From<LookupError> for io::Error`, which on this
 * Kotlin side is expressed by extending [Exception]: the embedded inner
 * exception's message and cause are surfaced through the standard
 * [Throwable] API.
 */
class LookupError internal constructor(
    val kind: LookupErrorKind,
    val errorNum: Int,
    inner: Throwable,
) : Exception(inner.message, inner) {

    companion object {
        /**
         * Match a `gai` error, returning a success [Result] if it is `0`.
         * Otherwise return a failure containing a [LookupError] with the
         * specific error details.
         */
        fun matchGaiError(err: Int): Result<Unit> = when (err) {
            0 -> Result.success(Unit)
            else -> Result.failure(of(err))
        }

        /**
         * Create a new [LookupError] from a `gai` error, returned by
         * `getaddrinfo` and `getnameinfo`.
         */
        fun of(err: Int): LookupError = LookupError(
            kind = LookupErrorKind.fromGaiErr(err),
            errorNum = err,
            inner = gaiErrToIoError(err),
        )

        /**
         * Build a [LookupError] from an existing IO-shaped throwable. The
         * resulting error has [kind] of [LookupErrorKind.IO] and
         * [errorNum] of `0`. This is the Kotlin analogue of upstream's
         * `From<io::Error> for LookupError` impl.
         */
        fun fromIoError(err: Throwable): LookupError = LookupError(
            kind = LookupErrorKind.IO,
            errorNum = 0,
            inner = err,
        )
    }
}

/**
 * Different kinds of lookup errors that `getaddrinfo` and `getnameinfo`
 * can return. These can be a little inconsistent between platforms, so
 * it's recommended not to rely on them.
 */
enum class LookupErrorKind {
    /**
     * Temporary failure in name resolution.
     *
     * May also be returned when the DNS server returns a SERVFAIL.
     */
    Again,

    /** Invalid value for the `ai_flags` field. */
    Badflags,

    /**
     * NAME or SERVICE is unknown.
     *
     * May also be returned when domain doesn't exist (NXDOMAIN) or domain
     * exists but contains no address records (NODATA).
     */
    NoName,

    /**
     * The specified network host exists, but has no data defined.
     *
     * This is no longer a POSIX standard, however it's still returned by
     * some platforms. Be warned that FreeBSD does not include the
     * corresponding `EAI_NODATA` symbol.
     */
    NoData,

    /** Non-recoverable failure in name resolution. */
    Fail,

    /** `ai_family` not supported. */
    Family,

    /** `ai_socktype` not supported. */
    Socktype,

    /** SERVICE not supported for `ai_socktype`. */
    Service,

    /** Memory allocation failure. */
    Memory,

    /** System error returned in `errno`. */
    System,

    /**
     * An unknown result code was returned.
     *
     * For some platforms, you may wish to match on an unknown value
     * directly. Note that `gai_strerror` is used to get error messages,
     * so the generated IO error should contain the correct error message
     * for the platform.
     */
    Unknown,

    /**
     * A generic C error or IO error occurred.
     *
     * You should convert this [LookupError] into an IO error directly.
     * Note that the error code is set to `0` in the case this is
     * returned.
     */
    IO,
    ;

    companion object {
        /**
         * Create a [LookupErrorKind] from a `gai` error.
         *
         * Platforms that lack a particular EAI symbol surface it as the
         * [ErrSys.EAI_ABSENT] sentinel — those branches then never match
         * a real error code returned by the system. This is how the
         * upstream `cfg(not(any(target_os = "freebsd", target_os =
         * "emscripten")))` gate around `EAI_NODATA` (and the absent
         * `EAI_SYSTEM` on Windows) is preserved without conditional
         * compilation.
         */
        fun fromGaiErr(err: Int): LookupErrorKind = when (err) {
            ErrSys.EAI_AGAIN -> Again
            ErrSys.EAI_BADFLAGS -> Badflags
            ErrSys.EAI_FAIL -> Fail
            ErrSys.EAI_FAMILY -> Family
            ErrSys.EAI_MEMORY -> Memory
            ErrSys.EAI_NONAME -> NoName
            ErrSys.EAI_NODATA -> NoData
            ErrSys.EAI_SERVICE -> Service
            ErrSys.EAI_SOCKTYPE -> Socktype
            ErrSys.EAI_SYSTEM -> System
            else -> IO
        }
    }
}

/**
 * Given a `gai` error, return a [Throwable] with the appropriate error
 * message. Note `0` is not an error, but will still map to an error.
 */
internal fun gaiErrToIoError(err: Int): Throwable = when (err) {
    0 -> Exception("address information lookup success")
    ErrSys.EAI_SYSTEM -> ErrSys.lastOsError()
    else -> Exception("failed to lookup address information: ${ErrSys.gaiStrError(err)}")
}

/**
 * Platform-specific `gai` error code surface. Each target supplies the
 * numeric values from its libc or winsock headers, along with the local
 * equivalents of `gai_strerror` and `io::Error::last_os_error`.
 *
 * Platforms that lack a particular symbol expose [EAI_ABSENT] in its
 * place — a sentinel chosen so that real error codes never collide with
 * it, preserving the upstream `cfg(...)` gates around `EAI_NODATA` and
 * `EAI_SYSTEM` without conditional compilation.
 */
internal expect object ErrSys {
    val EAI_AGAIN: Int
    val EAI_BADFLAGS: Int
    val EAI_FAIL: Int
    val EAI_FAMILY: Int
    val EAI_MEMORY: Int
    val EAI_NONAME: Int
    val EAI_NODATA: Int
    val EAI_SERVICE: Int
    val EAI_SOCKTYPE: Int
    val EAI_SYSTEM: Int

    /**
     * Sentinel used by platforms that lack a particular `gai` error
     * symbol. Set to a value that no real platform error code is known
     * to take, so that `when` branches against it never fire for real
     * input.
     */
    val EAI_ABSENT: Int

    /** Equivalent of libc / winsock `gai_strerror(err)`. */
    fun gaiStrError(err: Int): String

    /**
     * Equivalent of `io::Error::last_os_error()`. Wraps the platform's
     * current `errno` / `WSAGetLastError` value, where one is available;
     * on Kotlin targets that do not expose an OS errno, this returns a
     * generic IO-shaped throwable.
     */
    fun lastOsError(): Throwable
}
