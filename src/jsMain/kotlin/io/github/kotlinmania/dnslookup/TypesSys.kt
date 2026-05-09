// port-lint: ignore — actual for the commonMain `Sys` expect-object; JS has no native libc, so values follow Linux POSIX conventions.
package io.github.kotlinmania.dnslookup

internal actual object Sys {
    actual val SOCK_STREAM: Int = 1
    actual val SOCK_DGRAM: Int = 2
    actual val SOCK_RAW: Int = 3
    actual val SOCK_RDM: Int = 4
    actual val IPPROTO_ICMP: Int = 1
    actual val IPPROTO_TCP: Int = 6
    actual val IPPROTO_UDP: Int = 17
    actual val AF_UNIX: Int = 1
    actual val AF_INET: Int = 2
    actual val AF_INET6: Int = 10
}
