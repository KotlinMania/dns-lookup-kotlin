// port-lint: source nameinfo.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.dnslookup

import kotlin.native.HiddenFromObjC

/**
 * Retrieve the name for a given IP and service. Acts as a thin wrapper around
 * the platform getnameinfo.
 *
 * Returned names may be encoded in punycode for International Domain Names
 * (UTF-8 DNS names). Decode these to their actual UTF-8 representation before
 * presenting them to users when that matters.
 *
 * Retrieving names or services that contain non-UTF-8 locales is currently not
 * supported because [String] is returned. Raise an issue if this is a concern
 * for you.
 */
@HiddenFromObjC
fun getnameinfo(sock: SocketAddr, flags: Int): Result<Pair<String, String>> {
    initWinsock()
    return DnsLookupPlatform.getNameInfo(sock, flags)
}
