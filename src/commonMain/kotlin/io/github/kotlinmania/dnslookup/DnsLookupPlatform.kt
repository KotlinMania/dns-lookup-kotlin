// port-lint: ignore - platform bridge used by the translated lookup wrappers.
package io.github.kotlinmania.dnslookup

internal expect object DnsLookupPlatform {
    val supportsLookup: Boolean
    val loopbackReverseLookupUsesHostname: Boolean

    val NI_NAMEREQD: Int
    val NI_NUMERICSERV: Int

    fun getAddrInfo(host: String?, service: String?, hints: AddrInfoHints): Result<List<AddrInfo>>

    fun getNameInfo(sock: SocketAddr, flags: Int): Result<Pair<String, String>>

    fun getHostname(): Result<String>

    fun reloadDnsNameserver()

    fun initWinsock()
}

internal inline fun <T> dnsLookupResult(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (err: Throwable) {
        Result.failure(if (err is LookupError) err else LookupError.fromIoError(err))
    }

internal fun unsupportedDnsLookupError(operation: String): LookupError =
    LookupError.fromIoError(UnsupportedOperationException("$operation is not available on this target"))

internal fun servicePort(service: String?): Int =
    when (service?.lowercase()) {
        null -> 0
        "ssh" -> 22
        "http" -> 80
        "https" -> 443
        "domain" -> 53
        "dns" -> 53
        else ->
            service.toIntOrNull()
                ?: throw LookupError.fromIoError(IllegalArgumentException("Unknown network service: $service"))
    }

internal fun serviceName(port: Int): String =
    when (port) {
        22 -> "ssh"
        80 -> "http"
        443 -> "https"
        53 -> "domain"
        else -> port.toString()
    }
