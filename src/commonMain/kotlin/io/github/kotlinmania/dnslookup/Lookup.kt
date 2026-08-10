// port-lint: source lookup.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.dnslookup

import kotlin.native.HiddenFromObjC

/**
 * Lookup the address for a given hostname via DNS.
 *
 * Returns an iterator of IP addresses, or a failure on lookup errors.
 */
@HiddenFromObjC
fun lookupHost(host: String): Result<Iterator<IpAddr>> {
    val hints =
        AddrInfoHints(
            socktype = SockType.Stream.toCInt(),
        )

    val addrs =
        getaddrinfo(host, null, hints).getOrElse { err ->
            reloadDnsNameserver()
            return Result.failure(err)
        }

    val ips = mutableListOf<IpAddr>()
    while (addrs.hasNext()) {
        val addr =
            addrs.next().getOrElse { err ->
                reloadDnsNameserver()
                return Result.failure(err)
            }
        ips += addr.sockaddr.ip
    }
    return Result.success(ips.iterator())
}

/**
 * Lookup the hostname of a given IP address via DNS.
 *
 * Returns the hostname as a [String], or a failure if lookup fails or the
 * hostname cannot be determined.
 */
@HiddenFromObjC
fun lookupAddr(addr: IpAddr): Result<String> {
    val sock = SocketAddr(addr, 0)
    val flags = DnsLookupPlatform.NI_NUMERICSERV or DnsLookupPlatform.NI_NAMEREQD
    return getnameinfo(sock, flags).fold(
        onSuccess = { (name, _) -> Result.success(name) },
        onFailure = { err ->
            reloadDnsNameserver()
            Result.failure(err)
        },
    )
}

// The lookup failure could be caused by using a stale resolver configuration.
// See https://github.com/rust-lang/rust/issues/41570.
// We therefore force a reload of the nameserver information where the target
// exposes an equivalent hook. Apple platforms don't seem to have this problem.
internal fun reloadDnsNameserver() {
    DnsLookupPlatform.reloadDnsNameserver()
}
