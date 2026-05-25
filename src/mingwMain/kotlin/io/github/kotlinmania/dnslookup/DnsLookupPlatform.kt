// port-lint: ignore - mingw actual for the common lookup platform bridge.
package io.github.kotlinmania.dnslookup

internal actual object DnsLookupPlatform {
    actual val supportsLookup: Boolean = false
    actual val loopbackReverseLookupUsesHostname: Boolean = true

    actual val NI_NAMEREQD: Int = 4
    actual val NI_NUMERICSERV: Int = 8

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
