// port-lint: source nameinfo.rs
package io.github.kotlinmania.dnslookup

import kotlin.test.Test
import kotlin.test.assertEquals

class NameinfoTest {
    @Test
    fun testGetnameinfo() {
        if (!DnsLookupPlatform.supportsLookup) {
            return
        }

        val ip = IpAddr.v4("127.0.0.1")
        val port = 22
        val socket = SocketAddr(ip, port)

        val (name, service) = getnameinfo(socket, 0).getOrThrow()

        assertEquals("ssh", service)
        assertEquals("localhost", name)
    }
}
