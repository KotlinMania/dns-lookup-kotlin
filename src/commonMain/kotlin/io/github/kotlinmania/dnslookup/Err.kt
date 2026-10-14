// port-lint: source err.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.dnslookup

import io.github.kotlinmania.libc.unix.bsd.apple.EAI_AGAIN as APPLE_EAI_AGAIN
import io.github.kotlinmania.libc.unix.bsd.apple.EAI_BADFLAGS as APPLE_EAI_BADFLAGS
import io.github.kotlinmania.libc.unix.bsd.apple.EAI_FAIL as APPLE_EAI_FAIL
import io.github.kotlinmania.libc.unix.bsd.apple.EAI_FAMILY as APPLE_EAI_FAMILY
import io.github.kotlinmania.libc.unix.bsd.apple.EAI_MEMORY as APPLE_EAI_MEMORY
import io.github.kotlinmania.libc.unix.bsd.apple.EAI_NODATA as APPLE_EAI_NODATA
import io.github.kotlinmania.libc.unix.bsd.apple.EAI_NONAME as APPLE_EAI_NONAME
import io.github.kotlinmania.libc.unix.bsd.apple.EAI_SERVICE as APPLE_EAI_SERVICE
import io.github.kotlinmania.libc.unix.bsd.apple.EAI_SOCKTYPE as APPLE_EAI_SOCKTYPE
import io.github.kotlinmania.libc.unix.bsd.apple.EAI_SYSTEM as APPLE_EAI_SYSTEM
import io.github.kotlinmania.libc.unix.gaiStrerror
import io.github.kotlinmania.libc.unix.linuxlike.EAI_AGAIN as LINUX_EAI_AGAIN
import io.github.kotlinmania.libc.unix.linuxlike.EAI_BADFLAGS as LINUX_EAI_BADFLAGS
import io.github.kotlinmania.libc.unix.linuxlike.EAI_FAIL as LINUX_EAI_FAIL
import io.github.kotlinmania.libc.unix.linuxlike.EAI_FAMILY as LINUX_EAI_FAMILY
import io.github.kotlinmania.libc.unix.linuxlike.EAI_MEMORY as LINUX_EAI_MEMORY
import io.github.kotlinmania.libc.unix.linuxlike.EAI_NODATA as LINUX_EAI_NODATA
import io.github.kotlinmania.libc.unix.linuxlike.EAI_NONAME as LINUX_EAI_NONAME
import io.github.kotlinmania.libc.unix.linuxlike.EAI_SERVICE as LINUX_EAI_SERVICE
import io.github.kotlinmania.libc.unix.linuxlike.EAI_SOCKTYPE as LINUX_EAI_SOCKTYPE
import io.github.kotlinmania.libc.unix.linuxlike.EAI_SYSTEM as LINUX_EAI_SYSTEM
import kotlin.native.HiddenFromObjC

/**
 * Stores a lookup error from `getaddrinfo` or `getnameinfo`. The upstream
 * Rust type converts into an IO error, which on this Kotlin side is
 * expressed by extending [Exception]: the embedded inner exception's
 * message and cause are surfaced through the standard
 * [Throwable] API.
 */
@HiddenFromObjC
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
        fun matchGaiError(err: Int): Result<Unit> =
            when (err) {
                0 -> Result.success(Unit)
                else -> Result.failure(of(err))
            }

        /**
         * Create a new [LookupError] from a `gai` error, returned by
         * `getaddrinfo` and `getnameinfo`.
         */
        fun of(err: Int): LookupError =
            LookupError(
                kind = LookupErrorKind.fromGaiErr(err),
                errorNum = err,
                inner = gaiErrToIoError(err),
            )

        /**
         * Create a new [LookupError] from a `gai` error, returned by
         * `getaddrinfo` and `getnameinfo`.
         */
        fun new(err: Int): LookupError = of(err)

        /**
         * Build a [LookupError] from an existing IO-shaped throwable. The
         * resulting error has [kind] of [LookupErrorKind.IO] and
         * [errorNum] of `0`. This is the Kotlin analogue of upstream's
         * IO-error conversion.
         */
        fun fromIoError(err: Throwable): LookupError =
            LookupError(
                kind = LookupErrorKind.IO,
                errorNum = 0,
                inner = err,
            )

        /**
         * Build a [LookupError] from an existing IO-shaped throwable.
         */
        fun from(err: Throwable): LookupError = fromIoError(err)
    }

    /** Get the error kind explicitly. */
    fun kind(): LookupErrorKind = kind

    /**
     * Get the actual error number. This can be used to find non-standard
     * return codes from some implementations; be careful of portability here.
     */
    fun errorNum(): Int = errorNum
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

    /** Invalid value for the address-information flags field. */
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

    /** Address family not supported. */
    Family,

    /** Socket type not supported. */
    Socktype,

    /** SERVICE not supported for the socket type. */
    Service,

    /** Memory allocation failure. */
    Memory,

    /** System error returned in `errno`. */
    System,

    /**
     * An unknown result code was returned.
     *
     * For some platforms, you may wish to match on an unknown value
     * directly. Note that platform address-info error text is used to
     * build messages, so the generated IO error should contain the
     * correct error message for the platform.
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
         * a real error code returned by the system. This preserves the
         * upstream platform gates around no-data and system-error symbols
         * without conditional compilation.
         */
        fun fromGaiErr(err: Int): LookupErrorKind =
            when (err) {
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
internal fun gaiErrToIoError(err: Int): Throwable =
    when (err) {
        0 -> Exception("address information lookup success")
        ErrSys.EAI_SYSTEM -> ErrSys.lastOsError()
        else -> Exception("failed to lookup address information: ${ErrSys.gaiStrError(err)}")
    }

internal object ErrSys {
    val EAI_AGAIN: Int
        get() =
            when (DnsLookupPlatform.osKind) {
                OsKind.LINUX, OsKind.WASI -> LINUX_EAI_AGAIN
                OsKind.WINDOWS -> 11002
                else -> APPLE_EAI_AGAIN
            }

    val EAI_BADFLAGS: Int
        get() =
            when (DnsLookupPlatform.osKind) {
                OsKind.LINUX, OsKind.WASI -> LINUX_EAI_BADFLAGS
                OsKind.WINDOWS -> 10022
                else -> APPLE_EAI_BADFLAGS
            }

    val EAI_FAIL: Int
        get() =
            when (DnsLookupPlatform.osKind) {
                OsKind.LINUX, OsKind.WASI -> LINUX_EAI_FAIL
                OsKind.WINDOWS -> 11003
                else -> APPLE_EAI_FAIL
            }

    val EAI_FAMILY: Int
        get() =
            when (DnsLookupPlatform.osKind) {
                OsKind.LINUX, OsKind.WASI -> LINUX_EAI_FAMILY
                OsKind.WINDOWS -> 10047
                else -> APPLE_EAI_FAMILY
            }

    val EAI_MEMORY: Int
        get() =
            when (DnsLookupPlatform.osKind) {
                OsKind.LINUX, OsKind.WASI -> LINUX_EAI_MEMORY
                OsKind.WINDOWS -> 8
                else -> APPLE_EAI_MEMORY
            }

    val EAI_NONAME: Int
        get() =
            when (DnsLookupPlatform.osKind) {
                OsKind.LINUX, OsKind.WASI -> LINUX_EAI_NONAME
                OsKind.WINDOWS -> 11001
                else -> APPLE_EAI_NONAME
            }

    val EAI_NODATA: Int
        get() =
            when (DnsLookupPlatform.osKind) {
                OsKind.LINUX, OsKind.WASI -> LINUX_EAI_NODATA
                OsKind.WINDOWS -> 11004
                else -> APPLE_EAI_NODATA
            }

    val EAI_SERVICE: Int
        get() =
            when (DnsLookupPlatform.osKind) {
                OsKind.LINUX, OsKind.WASI -> LINUX_EAI_SERVICE
                OsKind.WINDOWS -> 10109
                else -> APPLE_EAI_SERVICE
            }

    val EAI_SOCKTYPE: Int
        get() =
            when (DnsLookupPlatform.osKind) {
                OsKind.LINUX, OsKind.WASI -> LINUX_EAI_SOCKTYPE
                OsKind.WINDOWS -> 10044
                else -> APPLE_EAI_SOCKTYPE
            }

    val EAI_SYSTEM: Int
        get() =
            when (DnsLookupPlatform.osKind) {
                OsKind.LINUX, OsKind.WASI -> LINUX_EAI_SYSTEM
                OsKind.WINDOWS -> 10093
                else -> APPLE_EAI_SYSTEM
            }

    const val EAI_ABSENT: Int = Int.MIN_VALUE

    fun gaiStrError(err: Int): String {
        val known =
            when (err) {
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
                else -> null
            }
        if (known != null) return known
        val raw = gaiStrerror(err)
        return if (!raw.isNullOrEmpty() && err in -100..15000) raw else "Unknown error: $err"
    }

    fun lastOsError(): Throwable = Exception("system error from getaddrinfo / getnameinfo")
}
