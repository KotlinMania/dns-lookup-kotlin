// port-lint: source lib.rs
package io.github.kotlinmania.dnslookup

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
