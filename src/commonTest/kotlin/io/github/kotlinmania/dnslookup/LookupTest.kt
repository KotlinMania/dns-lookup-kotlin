// port-lint: source lookup.rs
package io.github.kotlinmania.dnslookup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LookupTest {
    @Test
    fun testLocalhost() {
        if (!DnsLookupPlatform.supportsLookup) {
            return
        }

        val ips = lookupHost("localhost").getOrThrow().asSequence().toList()
        assertTrue(ips.contains(IpAddr.v4("127.0.0.1")))
        assertFalse(ips.contains(IpAddr.v4("10.0.0.1")))
    }

    @Test
    fun testRevLocalhost() {
        if (!DnsLookupPlatform.supportsLookup) {
            return
        }

        val name = lookupAddr(IpAddr.v4("127.0.0.1"))
        assertEquals("localhost", name.getOrThrow())
    }

    @Test
    fun testHostname() {
        if (!DnsLookupPlatform.supportsLookup || !DnsLookupPlatform.loopbackReverseLookupUsesHostname) {
            return
        }

        val hostname = getHostname().getOrThrow()
        val revName = lookupAddr(IpAddr.v4("127.0.0.1"))
        assertEquals(hostname, revName.getOrThrow())
    }
}
