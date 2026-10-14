// port-lint: source types.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.dnslookup

import kotlin.native.HiddenFromObjC
import io.github.kotlinmania.libc.unix.IPPROTO_ICMP as APPLE_IPPROTO_ICMP
import io.github.kotlinmania.libc.unix.IPPROTO_TCP as APPLE_IPPROTO_TCP
import io.github.kotlinmania.libc.unix.IPPROTO_UDP as APPLE_IPPROTO_UDP
import io.github.kotlinmania.libc.unix.bsd.apple.AF_INET as APPLE_AF_INET
import io.github.kotlinmania.libc.unix.bsd.apple.AF_INET6 as APPLE_AF_INET6
import io.github.kotlinmania.libc.unix.bsd.apple.AF_UNIX as APPLE_AF_UNIX
import io.github.kotlinmania.libc.unix.bsd.apple.SOCK_DGRAM as APPLE_SOCK_DGRAM
import io.github.kotlinmania.libc.unix.bsd.apple.SOCK_RAW as APPLE_SOCK_RAW
import io.github.kotlinmania.libc.unix.bsd.apple.SOCK_RDM as APPLE_SOCK_RDM
import io.github.kotlinmania.libc.unix.bsd.apple.SOCK_STREAM as APPLE_SOCK_STREAM
import io.github.kotlinmania.libc.unix.linuxlike.AF_INET6 as LINUX_AF_INET6

/** Parameters of address information structures are 32-bit signed integers on every supported target. */
typealias CInt = Int

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
    fun toCInt(): CInt =
        when (this) {
            Stream -> Sys.SOCK_STREAM
            DGram -> Sys.SOCK_DGRAM
            Raw -> Sys.SOCK_RAW
            RDM -> Sys.SOCK_RDM
        }

    /** Return whether this socket type has the supplied platform integer value. */
    fun matches(other: CInt): Boolean = other == toCInt()

    companion object {
        /** Convert a socket type to its platform integer value. */
        fun from(sock: SockType): CInt = sock.toCInt()
    }
}

/** Return whether this platform integer value represents [sock]. */
fun CInt.matches(sock: SockType): Boolean = this == sock.toCInt()

/**
 * Socket Protocol
 *
 * Cross platform enum of common Socket Protocols. For missing types use
 * the platform's libc / winsock protocol constants directly.
 */
@HiddenFromObjC
enum class Protocol {
    /** Internet Control Message Protocol. */
    ICMP,

    /** Transmission Control Protocol. */
    TCP,

    /** User Datagram Protocol. */
    UDP,
    ;

    /** Convert to the platform's libc / winsock protocol integer. */
    fun toCInt(): CInt =
        when (this) {
            ICMP -> Sys.IPPROTO_ICMP
            TCP -> Sys.IPPROTO_TCP
            UDP -> Sys.IPPROTO_UDP
        }

    /** Return whether this protocol has the supplied platform integer value. */
    fun matches(other: CInt): Boolean = other == toCInt()

    companion object {
        /** Convert a protocol to its platform integer value. */
        fun from(sock: Protocol): CInt = sock.toCInt()
    }
}

/** Return whether this platform integer value represents [sock]. */
@HiddenFromObjC
fun CInt.matches(sock: Protocol): Boolean = this == sock.toCInt()

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
    fun toCInt(): CInt =
        when (this) {
            Unix -> Sys.AF_UNIX
            Inet -> Sys.AF_INET
            Inet6 -> Sys.AF_INET6
        }

    /** Return whether this address family has the supplied platform integer value. */
    fun matches(other: CInt): Boolean = other == toCInt()

    companion object {
        /** Convert an address family to its platform integer value. */
        fun from(sock: AddrFamily): CInt = sock.toCInt()
    }
}

/** Return whether this platform integer value represents [sock]. */
fun CInt.matches(sock: AddrFamily): Boolean = this == sock.toCInt()

internal object Sys {
    val SOCK_STREAM: CInt = APPLE_SOCK_STREAM
    val SOCK_DGRAM: CInt = APPLE_SOCK_DGRAM
    val SOCK_RAW: CInt = APPLE_SOCK_RAW
    val SOCK_RDM: CInt = APPLE_SOCK_RDM
    val IPPROTO_ICMP: CInt = APPLE_IPPROTO_ICMP
    val IPPROTO_TCP: CInt = APPLE_IPPROTO_TCP
    val IPPROTO_UDP: CInt = APPLE_IPPROTO_UDP
    val AF_UNIX: CInt = APPLE_AF_UNIX
    val AF_INET: CInt = APPLE_AF_INET
    val AF_INET6: CInt
        get() =
            when (DnsLookupPlatform.osKind) {
                OsKind.DARWIN -> APPLE_AF_INET6
                OsKind.WINDOWS -> 23
                else -> LINUX_AF_INET6
            }
}
