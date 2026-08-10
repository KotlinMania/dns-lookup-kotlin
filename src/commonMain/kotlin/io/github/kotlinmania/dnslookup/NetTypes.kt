// port-lint: ignore - Kotlin value types backing the upstream standard-library network values.
package io.github.kotlinmania.dnslookup

/**
 * Kotlin representation of an IP address for this multiplatform port.
 */
class IpAddr private constructor(
    val address: String,
    val version: IpVersion,
) {
    override fun equals(other: Any?): Boolean =
        other is IpAddr && address == other.address && version == other.version

    override fun hashCode(): Int = 31 * address.hashCode() + version.hashCode()

    override fun toString(): String = address

    companion object {
        /** Build an IPv4 address. */
        fun v4(address: String): IpAddr {
            require(address.isIpv4Address()) { "Invalid IPv4 address: $address" }
            return IpAddr(address, IpVersion.V4)
        }

        /** Build an IPv6 address. */
        fun v6(address: String): IpAddr {
            require(':' in address) { "Invalid IPv6 address: $address" }
            return IpAddr(address, IpVersion.V6)
        }

        /** Parse a textual IP address into an [IpAddr]. */
        fun parse(address: String): IpAddr {
            require(address.isNotBlank()) { "IP address must not be blank" }
            return if (':' in address) {
                v6(address)
            } else {
                v4(address)
            }
        }
    }
}

/** IP address family. */
enum class IpVersion {
    V4,
    V6,
}

/**
 * Kotlin representation of an IP socket address.
 */
data class SocketAddr(
    val ip: IpAddr,
    val port: Int,
) {
    init {
        require(port in 0..65535) { "Socket port out of range: $port" }
    }
}

private fun String.isIpv4Address(): Boolean {
    val parts = split('.')
    return parts.size == 4 &&
        parts.all { part ->
            part.isNotEmpty() && part.all(Char::isDigit) && part.toIntOrNull() in 0..255
        }
}
