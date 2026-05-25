@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

// port-lint: ignore - Apple actual for the common lookup platform bridge.
package io.github.kotlinmania.dnslookup

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.posix.NI_NAMEREQD
import platform.posix.NI_NUMERICHOST
import platform.posix.NI_NUMERICSERV
import platform.posix.addrinfo
import platform.posix.freeaddrinfo
import platform.posix.gethostname
import platform.posix.sockaddr
import platform.posix.getaddrinfo as cGetAddrInfo
import platform.posix.getnameinfo as cGetNameInfo

internal actual object DnsLookupPlatform {
    actual val supportsLookup: Boolean = true
    actual val loopbackReverseLookupUsesHostname: Boolean = false

    actual val NI_NAMEREQD: Int = platform.posix.NI_NAMEREQD
    actual val NI_NUMERICSERV: Int = platform.posix.NI_NUMERICSERV

    actual fun getAddrInfo(host: String?, service: String?, hints: AddrInfoHints): Result<List<AddrInfo>> =
        dnsLookupResult {
            memScoped {
                val cHints = allocAddrInfoHints(hints)
                val result = allocPointerTo<addrinfo>()
                val code = cGetAddrInfo(
                    host,
                    service,
                    cHints.ptr,
                    result.ptr,
                )
                LookupError.matchGaiError(code).getOrThrow()
                val head = result.value
                try {
                    val entries = mutableListOf<AddrInfo>()
                    var current = head
                    while (current != null) {
                        val item = current.pointed
                        entries += AddrInfo(
                            flags = 0,
                            address = item.ai_family,
                            socktype = item.ai_socktype,
                            protocol = item.ai_protocol,
                            sockaddr = socketAddressFrom(item.ai_addr, item.ai_addrlen.convert()),
                            canonname = item.ai_canonname?.toKString(),
                        )
                        current = item.ai_next
                    }
                    entries
                } finally {
                    if (head != null) {
                        freeaddrinfo(head)
                    }
                }
            }
        }

    actual fun getNameInfo(sock: SocketAddr, flags: Int): Result<Pair<String, String>> =
        dnsLookupResult {
            memScoped {
                val hints = allocAddrInfoHints(AddrInfoHints(address = sock.ip.addressFamily()))
                val result = allocPointerTo<addrinfo>()
                val code = cGetAddrInfo(
                    sock.ip.address,
                    sock.port.toString(),
                    hints.ptr,
                    result.ptr,
                )
                LookupError.matchGaiError(code).getOrThrow()
                val head = result.value ?: throw LookupError.of(ErrSys.EAI_NONAME)
                try {
                    getNameInfoFrom(head.pointed.ai_addr, head.pointed.ai_addrlen.convert(), flags)
                } finally {
                    freeaddrinfo(head)
                }
            }
        }

    actual fun getHostname(): Result<String> =
        dnsLookupResult {
            val buffer = ByteArray(256)
            buffer.usePinned { pinned ->
                val code = gethostname(pinned.addressOf(0), buffer.size.convert())
                if (code != 0) {
                    throw ErrSys.lastOsError()
                }
                pinned.addressOf(0).toKString()
            }
        }

    actual fun reloadDnsNameserver() {
    }

    actual fun initWinsock() {
    }
}

private fun kotlinx.cinterop.MemScope.allocAddrInfoHints(hints: AddrInfoHints): addrinfo =
    alloc<addrinfo> {
        ai_flags = hints.flags
        ai_family = hints.address
        ai_socktype = hints.socktype
        ai_protocol = hints.protocol
        ai_addrlen = 0.convert()
        ai_addr = null
        ai_canonname = null
        ai_next = null
    }

private fun socketAddressFrom(address: CPointer<sockaddr>?, length: UInt): SocketAddr {
    val (host, service) = getNameInfoFrom(address, length, NI_NUMERICHOST or platform.posix.NI_NUMERICSERV)
    return SocketAddr(IpAddr.parse(host), service.toIntOrNull() ?: 0)
}

private fun getNameInfoFrom(address: CPointer<sockaddr>?, length: UInt, flags: Int): Pair<String, String> {
    if (address == null) {
        throw LookupError.fromIoError(IllegalArgumentException("Supplied socket address is null"))
    }
    val host = ByteArray(1024)
    val service = ByteArray(32)
    host.usePinned { hostPinned ->
        service.usePinned { servicePinned ->
            val code = cGetNameInfo(
                address,
                length.convert(),
                hostPinned.addressOf(0),
                host.size.convert(),
                servicePinned.addressOf(0),
                service.size.convert(),
                flags,
            )
            LookupError.matchGaiError(code).getOrThrow()
            return hostPinned.addressOf(0).toKString() to servicePinned.addressOf(0).toKString()
        }
    }
}

private fun IpAddr.addressFamily(): Int =
    when (version) {
        IpVersion.V4 -> Sys.AF_INET
        IpVersion.V6 -> Sys.AF_INET6
    }
