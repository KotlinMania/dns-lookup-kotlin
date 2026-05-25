// port-lint: source win.rs
package io.github.kotlinmania.dnslookup

// Start windows socket library, following the upstream socket2-derived shape.
internal fun initWinsock() {
    DnsLookupPlatform.initWinsock()
}
