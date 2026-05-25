// port-lint: source addrinfo.rs
package io.github.kotlinmania.dnslookup

import kotlin.test.Test
import kotlin.test.assertEquals

class AddrinfoTest {
    @Test
    fun testAddrinfohints() {
        assertEquals(
            AddrInfoHints(
                flags = 1,
                address = AddrFamily.Inet.toCInt(),
                socktype = SockType.Stream.toCInt(),
            ),
            AddrInfoHints.new(
                flags = 1,
                address = AddrFamily.Inet,
                socktype = SockType.Stream,
            ),
        )

        assertEquals(
            AddrInfoHints(
                address = AddrFamily.Inet.toCInt(),
                socktype = SockType.Stream.toCInt(),
            ),
            AddrInfoHints.new(
                address = AddrFamily.Inet,
                socktype = SockType.Stream,
            ),
        )
    }
}
