// port-lint: source src/types.rs
package io.github.kotlinmania.dnslookup

/**
 * Socket Type
 *
 * Cross platform enum of common Socket Types. For missing types use
 * the platform's libc / winsock socket constants directly.
 */
enum class SockType {
    /** Sequenced, reliable, connection-based byte streams. */
    Stream,
    /** Connectionless, unreliable datagrams of fixed max length. */
    DGram,
    /** Raw protocol interface. */
    Raw,
    /** Reliably-delivered messages. */
    RDM,
    ;

    /** Convert to the platform's libc / winsock socket-type integer. */
    fun toCInt(): Int = when (this) {
        Stream -> Sys.SOCK_STREAM
        DGram -> Sys.SOCK_DGRAM
        Raw -> Sys.SOCK_RAW
        RDM -> Sys.SOCK_RDM
    }
}

/**
 * Socket Protocol
 *
 * Cross platform enum of common Socket Protocols. For missing types use
 * the platform's libc / winsock protocol constants directly.
 */
enum class Protocol {
    /** Internet Control Message Protocol. */
    ICMP,
    /** Transmission Control Protocol. */
    TCP,
    /** User Datagram Protocol. */
    UDP,
    ;

    /** Convert to the platform's libc / winsock protocol integer. */
    fun toCInt(): Int = when (this) {
        ICMP -> Sys.IPPROTO_ICMP
        TCP -> Sys.IPPROTO_TCP
        UDP -> Sys.IPPROTO_UDP
    }
}

/**
 * Address Family
 *
 * Cross platform enum of common Address Families. For missing types use
 * the platform's libc / winsock address-family constants directly.
 */
enum class AddrFamily {
    /** Local to host (pipes and file-domain) */
    Unix,
    /** IP protocol family. */
    Inet,
    /** IP version 6. */
    Inet6,
    ;

    /** Convert to the platform's libc / winsock address-family integer. */
    fun toCInt(): Int = when (this) {
        Unix -> Sys.AF_UNIX
        Inet -> Sys.AF_INET
        Inet6 -> Sys.AF_INET6
    }
}

internal expect object Sys {
    val SOCK_STREAM: Int
    val SOCK_DGRAM: Int
    val SOCK_RAW: Int
    val SOCK_RDM: Int
    val IPPROTO_ICMP: Int
    val IPPROTO_TCP: Int
    val IPPROTO_UDP: Int
    val AF_UNIX: Int
    val AF_INET: Int
    val AF_INET6: Int
}
