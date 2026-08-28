#if canImport(Testing)
import Testing
import DnsLookup

@Suite("DnsLookup Swift Export Tests")
struct DnsLookupExportTests {
    @Test("Swift module loads")
    func testSwiftModuleLoads() {
        #expect(Bool(true), "DnsLookup swift module imported cleanly")
    }
}
#elseif canImport(XCTest)
import XCTest
import DnsLookup

final class DnsLookupExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "DnsLookup swift module imported cleanly")
    }
}
#endif
