// port-lint: source hostname.rs
package io.github.kotlinmania.dnslookup

import kotlin.test.Test
import kotlin.test.assertTrue

class HostnameTest {
    @Test
    fun testGetHostname() {
        if (!DnsLookupPlatform.supportsLookup) {
            return
        }

        assertTrue(getHostname().getOrThrow().isNotEmpty())
    }
}
