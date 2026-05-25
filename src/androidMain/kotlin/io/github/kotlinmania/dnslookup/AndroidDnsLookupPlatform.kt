// port-lint: ignore - Android actual for the common lookup platform bridge.
package io.github.kotlinmania.dnslookup

internal actual object DnsLookupPlatform {
    actual val supportsLookup: Boolean = false
    actual val loopbackReverseLookupUsesHostname: Boolean = false

    actual val NI_NAMEREQD: Int = 8
    actual val NI_NUMERICSERV: Int = 2

    actual fun getAddrInfo(host: String?, service: String?, hints: AddrInfoHints): Result<List<AddrInfo>> =
        Result.failure(unsupportedDnsLookupError("getaddrinfo"))

    actual fun getNameInfo(sock: SocketAddr, flags: Int): Result<Pair<String, String>> =
        Result.failure(unsupportedDnsLookupError("getnameinfo"))

    actual fun getHostname(): Result<String> =
        Result.failure(unsupportedDnsLookupError("gethostname"))

    actual fun reloadDnsNameserver() {
    }

    actual fun initWinsock() {
    }
}
