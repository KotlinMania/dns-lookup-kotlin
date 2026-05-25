// port-lint: source addrinfo.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.dnslookup

import kotlin.native.HiddenFromObjC

/**
 * A struct used as the hints argument to getaddrinfo.
 */
data class AddrInfoHints(
    /**
     * Optional bitmask arguments. Bitwise OR bitflags to change the
     * behaviour of getaddrinfo. Zero for none. Address-information flags
     * in libc.
     *
     * Values are defined by the libc on your system.
     */
    val flags: Int = 0,
    /**
     * Address family for this socket. Zero for none. Address family in libc.
     *
     * Values are defined by the libc on your system.
     */
    val address: Int = 0,
    /**
     * Type of this socket. Zero for none. Socket type in libc.
     *
     * Values are defined by the libc on your system.
     */
    val socktype: Int = 0,
    /**
     * Protocol for this socket. Zero for none. Protocol in libc.
     *
     * Values are defined by the libc on your system.
     */
    val protocol: Int = 0,
) {
    companion object {
        /**
         * Generate a blank [AddrInfoHints] struct, so new values can easily
         * be specified.
         */
        fun default(): AddrInfoHints = AddrInfoHints()

        /**
         * Create a new [AddrInfoHints] using built-in types.
         *
         * Included enums only provide common values; for anything else
         * create this struct directly using appropriate values from the
         * platform libc or winsock surface.
         */
        @HiddenFromObjC
        fun new(
            flags: Int? = null,
            address: AddrFamily? = null,
            socktype: SockType? = null,
            protocol: Protocol? = null,
        ): AddrInfoHints = AddrInfoHints(
            flags = flags ?: 0,
            address = address?.toCInt() ?: 0,
            socktype = socktype?.toCInt() ?: 0,
            protocol = protocol?.toCInt() ?: 0,
        )
    }
}

/**
 * Struct that stores socket information, as returned by getaddrinfo.
 */
data class AddrInfo(
    /** Optional bitmask arguments, usually set to zero. Address-information flags in libc. */
    val flags: Int,
    /**
     * Address family for this socket, usually matches protocol family.
     *
     * Values are defined by the libc on your system.
     */
    val address: Int,
    /**
     * Type of this socket.
     *
     * Values are defined by the libc on your system.
     */
    val socktype: Int,
    /**
     * Protocol family for this socket.
     *
     * Values are defined by the libc on your system.
     */
    val protocol: Int,
    /**
     * Socket address for this socket, usually containing an actual
     * IP address and port.
     */
    val sockaddr: SocketAddr,
    /** If requested, this is the canonical name for this socket or host. */
    val canonname: String?,
)

/**
 * An iterator of [AddrInfo] structs, wrapping the linked-list returned by
 * getaddrinfo on platforms that expose it.
 *
 * It's recommended to collect this iterator and collapse possible errors.
 */
@HiddenFromObjC
class AddrInfoIter internal constructor(
    private val entries: List<Result<AddrInfo>>,
) : Iterator<Result<AddrInfo>> {
    private var index: Int = 0

    override fun hasNext(): Boolean = index < entries.size

    override fun next(): Result<AddrInfo> {
        if (!hasNext()) {
            throw NoSuchElementException("AddrInfoIter exhausted")
        }
        val entry = entries[index]
        index += 1
        return entry
    }
}

/**
 * Retrieve socket information for a host, service, or both. Acts as a thin
 * wrapper around the platform getaddrinfo.
 *
 * The only portable way to support International Domain Names (UTF-8 DNS
 * names) is to manually convert to punycode before calling this function.
 * However some libc backends may support this natively, or by using bitflags
 * in the hints argument.
 *
 * Resolving names from non-UTF-8 locales is currently not supported because
 * the interface uses [String]. Raise an issue if this is a concern for you.
 */
@HiddenFromObjC
fun getaddrinfo(
    host: String?,
    service: String?,
    hints: AddrInfoHints? = null,
): Result<AddrInfoIter> {
    if (host == null && service == null) {
        return Result.failure(
            LookupError.fromIoError(IllegalArgumentException("Either host or service must be supplied")),
        )
    }

    initWinsock()
    return DnsLookupPlatform.getAddrInfo(host, service, hints ?: AddrInfoHints())
        .map { entries -> AddrInfoIter(entries.map { Result.success(it) }) }
}
