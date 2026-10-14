// port-lint: source lib.rs
package io.github.kotlinmania.dnslookup

import io.github.kotlinmania.libc.Addrinfo
import io.github.kotlinmania.libc.COpaquePointer
import io.github.kotlinmania.libc.Sockaddr
import io.github.kotlinmania.libc.Utsname
import io.github.kotlinmania.libc.toAddrinfo
import io.github.kotlinmania.libc.toSockaddr
import io.github.kotlinmania.libc.unix.bsd.apple.NI_NAMEREQD as APPLE_NI_NAMEREQD
import io.github.kotlinmania.libc.unix.bsd.apple.NI_NUMERICHOST as APPLE_NI_NUMERICHOST
import io.github.kotlinmania.libc.unix.bsd.apple.NI_NUMERICSERV as APPLE_NI_NUMERICSERV
import io.github.kotlinmania.libc.unix.bsd.apple.getenv
import io.github.kotlinmania.libc.unix.bsd.apple.uname
import io.github.kotlinmania.libc.unix.freeaddrinfo
import io.github.kotlinmania.libc.unix.linuxlike.NI_NAMEREQD as LINUX_NI_NAMEREQD
import io.github.kotlinmania.libc.unix.linuxlike.NI_NUMERICHOST as LINUX_NI_NUMERICHOST
import io.github.kotlinmania.libc.unix.linuxlike.NI_NUMERICSERV as LINUX_NI_NUMERICSERV
import io.github.kotlinmania.libc.unix.resInit
import io.github.kotlinmania.libc.unix.getaddrinfo as cGetAddrInfo
import io.github.kotlinmania.libc.unix.getnameinfo as cGetNameInfo

/**
 * A small wrapper for libc to perform simple DNS lookups.
 *
 * Two main functions are provided: [lookupHost], which returns the IP
 * addresses associated with a hostname, and [lookupAddr], which returns the
 * reverse DNS entry for an IP address.
 *
 * If you only need a single result, consider the host platform's standard
 * socket-address conversion API.
 *
 * The upstream Rust crate re-exports its module members from `lib.rs`; Kotlin
 * keeps these declarations in this package directly rather than introducing
 * central alias APIs.
 */

internal object LibModule {
    const val ADDRINFO_EXPORTS: String = "getaddrinfo, AddrInfo, AddrInfoHints, AddrInfoIter"
    const val ERR_EXPORTS: String = "LookupError, LookupErrorKind"
    const val HOSTNAME_EXPORTS: String = "getHostname"
    const val LOOKUP_EXPORTS: String = "lookupAddr, lookupHost"
    const val NAMEINFO_EXPORTS: String = "getnameinfo"
    const val TYPES_EXPORTS: String = "AddrFamily, Protocol, SockType"
}

internal enum class OsKind {
    DARWIN,
    LINUX,
    ANDROID,
    WINDOWS,
    WASI,
}

internal object DnsLookupPlatform {
    val osKind: OsKind by lazy {
        val uts = Utsname()
        val rc = uname(uts)
        if (rc != 0) {
            OsKind.WINDOWS
        } else {
            val end = uts.sysname.indexOf(0.toByte()).let { if (it < 0) uts.sysname.size else it }
            val sys = uts.sysname.decodeToString(0, end)
            when {
                sys.startsWith("Darwin", ignoreCase = true) -> OsKind.DARWIN
                sys.startsWith("WASI", ignoreCase = true) -> OsKind.WASI
                sys.startsWith("Windows", ignoreCase = true) -> OsKind.WINDOWS
                sys.startsWith("Linux", ignoreCase = true) ->
                    if (getenv("ANDROID_ROOT") != null) {
                        OsKind.ANDROID
                    } else {
                        OsKind.LINUX
                    }
                else -> OsKind.LINUX
            }
        }
    }

    const val supportsLookup: Boolean = true

    val loopbackReverseLookupUsesHostname: Boolean
        get() = osKind == OsKind.WINDOWS

    val NI_NAMEREQD: Int
        get() = if (osKind == OsKind.LINUX) LINUX_NI_NAMEREQD else APPLE_NI_NAMEREQD

    val NI_NUMERICHOST: Int
        get() = if (osKind == OsKind.LINUX) LINUX_NI_NUMERICHOST else APPLE_NI_NUMERICHOST

    val NI_NUMERICSERV: Int
        get() = if (osKind == OsKind.LINUX) LINUX_NI_NUMERICSERV else APPLE_NI_NUMERICSERV

    fun getAddrInfo(
        host: String?,
        service: String?,
        hints: AddrInfoHints,
    ): Result<List<AddrInfo>> =
        dnsLookupResult {
            val cHints =
                Addrinfo(
                    aiFlags = hints.flags,
                    aiFamily = hints.address,
                    aiSocktype = hints.socktype,
                    aiProtocol = hints.protocol,
                )
            val result = COpaquePointer()
            val code = cGetAddrInfo(host, service, cHints, result)
            LookupError.matchGaiError(code).getOrThrow()
            val head = result.toAddrinfo()
            try {
                val entries = mutableListOf<AddrInfo>()
                var current = head
                while (current != null) {
                    val sockaddr = current.aiSockaddr ?: current.aiAddr.toSockaddr()
                    entries +=
                        AddrInfo(
                            flags = 0,
                            address = current.aiFamily,
                            socktype = current.aiSocktype,
                            protocol = current.aiProtocol,
                            sockaddr = socketAddressFrom(sockaddr, current.aiAddrlen),
                            canonname = current.aiCanonname,
                        )
                    current = current.aiNext
                }
                entries
            } finally {
                if (head != null) {
                    freeaddrinfo(head)
                }
            }
        }

    fun getNameInfo(
        sock: SocketAddr,
        flags: Int,
    ): Result<Pair<String, String>> =
        dnsLookupResult {
            val cHints = Addrinfo(aiFamily = sock.ip.addressFamily())
            val result = COpaquePointer()
            val code = cGetAddrInfo(sock.ip.address, sock.port.toString(), cHints, result)
            LookupError.matchGaiError(code).getOrThrow()
            val head = result.toAddrinfo() ?: throw LookupError.of(ErrSys.EAI_NONAME)
            try {
                val sockaddr = head.aiSockaddr ?: head.aiAddr.toSockaddr()
                getNameInfoFrom(sockaddr, head.aiAddrlen, flags)
            } finally {
                freeaddrinfo(head)
            }
        }

    fun getHostname(): Result<String> =
        dnsLookupResult {
            if (loopbackReverseLookupUsesHostname) {
                val loopback = SocketAddr(IpAddr.v4("127.0.0.1"), 0)
                val (host, _) = getNameInfo(loopback, 0).getOrThrow()
                if (host.isNotEmpty()) {
                    return@dnsLookupResult host
                }
            }
            val info = Utsname()
            val code = uname(info)
            if (code == 0) {
                val end = info.nodename.indexOf(0.toByte()).let { if (it < 0) info.nodename.size else it }
                val name = info.nodename.decodeToString(0, end)
                if (name.isNotEmpty()) {
                    return@dnsLookupResult name
                }
            }
            val envName = getenv("COMPUTERNAME") ?: getenv("HOSTNAME")
            if (!envName.isNullOrEmpty()) {
                return@dnsLookupResult envName
            }
            throw ErrSys.lastOsError()
        }

    fun reloadDnsNameserver() {
        resInit()
    }

    fun initWinsock() {
    }

    private fun socketAddressFrom(
        address: Sockaddr?,
        length: UInt,
    ): SocketAddr {
        val (host, service) = getNameInfoFrom(address, length, NI_NUMERICHOST or NI_NUMERICSERV)
        return SocketAddr(IpAddr.parse(host), service.toIntOrNull() ?: 0)
    }

    private fun getNameInfoFrom(
        address: Sockaddr?,
        length: UInt,
        flags: Int,
    ): Pair<String, String> {
        if (address == null) {
            throw LookupError.fromIoError(IllegalArgumentException("Supplied socket address is null"))
        }
        val host = ByteArray(1024)
        val service = ByteArray(32)
        val code =
            cGetNameInfo(
                address,
                length,
                host,
                host.size.toUInt(),
                service,
                service.size.toUInt(),
                flags,
            )
        LookupError.matchGaiError(code).getOrThrow()
        val hostEnd = host.indexOf(0.toByte()).let { if (it < 0) host.size else it }
        val servEnd = service.indexOf(0.toByte()).let { if (it < 0) service.size else it }
        return host.decodeToString(0, hostEnd) to service.decodeToString(0, servEnd)
    }
}

private fun IpAddr.addressFamily(): Int =
    when (version) {
        IpVersion.V4 -> Sys.AF_INET
        IpVersion.V6 -> Sys.AF_INET6
    }

internal inline fun <T> dnsLookupResult(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (err: Throwable) {
        Result.failure(if (err is LookupError) err else LookupError.fromIoError(err))
    }

internal fun unsupportedDnsLookupError(operation: String): LookupError =
    LookupError.fromIoError(UnsupportedOperationException("$operation is not available on this target"))
