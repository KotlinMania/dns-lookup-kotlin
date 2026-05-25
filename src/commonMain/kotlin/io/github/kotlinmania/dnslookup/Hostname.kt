// port-lint: source hostname.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.dnslookup

import kotlin.native.HiddenFromObjC

/**
 * Fetch the local hostname.
 */
@HiddenFromObjC
fun getHostname(): Result<String> {
    initWinsock()
    return DnsLookupPlatform.getHostname()
}
