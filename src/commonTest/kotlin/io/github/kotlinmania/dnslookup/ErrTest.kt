// port-lint: source err.rs
package io.github.kotlinmania.dnslookup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ErrTest {

    @Test
    fun matchGaiErrorTreatsZeroAsSuccess() {
        val r = LookupError.matchGaiError(0)
        assertTrue(r.isSuccess)
    }

    @Test
    fun matchGaiErrorWrapsNonZero() {
        val r = LookupError.matchGaiError(ErrSys.EAI_NONAME)
        assertTrue(r.isFailure)
        val err = r.exceptionOrNull()
        assertIs<LookupError>(err)
        assertSame(LookupErrorKind.NoName, err.kind)
        assertEquals(ErrSys.EAI_NONAME, err.errorNum)
    }

    @Test
    fun fromGaiErrCoversTheStandardCodes() {
        assertSame(LookupErrorKind.Again, LookupErrorKind.fromGaiErr(ErrSys.EAI_AGAIN))
        assertSame(LookupErrorKind.Badflags, LookupErrorKind.fromGaiErr(ErrSys.EAI_BADFLAGS))
        assertSame(LookupErrorKind.Fail, LookupErrorKind.fromGaiErr(ErrSys.EAI_FAIL))
        assertSame(LookupErrorKind.Family, LookupErrorKind.fromGaiErr(ErrSys.EAI_FAMILY))
        assertSame(LookupErrorKind.Memory, LookupErrorKind.fromGaiErr(ErrSys.EAI_MEMORY))
        assertSame(LookupErrorKind.NoName, LookupErrorKind.fromGaiErr(ErrSys.EAI_NONAME))
        assertSame(LookupErrorKind.Service, LookupErrorKind.fromGaiErr(ErrSys.EAI_SERVICE))
        assertSame(LookupErrorKind.Socktype, LookupErrorKind.fromGaiErr(ErrSys.EAI_SOCKTYPE))
    }

    @Test
    fun fromGaiErrFallsBackToIoForUnknownCodes() {
        // 999_999 is far outside any platform's real EAI numeric
        // range, both Linux glibc (negative one-digit values) and
        // WinSock (low 5-digit values), so it should always be
        // classified as IO.
        assertSame(LookupErrorKind.IO, LookupErrorKind.fromGaiErr(999_999))
    }

    @Test
    fun fromIoErrorPreservesMessageAndKind() {
        val cause = Exception("network unreachable")
        val err = LookupError.fromIoError(cause)
        assertSame(LookupErrorKind.IO, err.kind)
        assertEquals(0, err.errorNum)
        assertEquals("network unreachable", err.message)
        assertSame(cause, err.cause)
    }

    @Test
    fun gaiErrToIoErrorIncludesDetailForUnknownCodes() {
        val message = gaiErrToIoError(999_999).message
        assertNotNull(message)
        assertTrue(message.startsWith("failed to lookup address information:"))
        assertTrue(message.contains("999999") || message.contains("999_999"))
    }

    @Test
    fun gaiErrToIoErrorTreatsZeroAsSuccessMessage() {
        assertEquals("address information lookup success", gaiErrToIoError(0).message)
    }
}
