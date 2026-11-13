package com.example.chariiptv

import com.example.chariiptv.player.IptvLoadErrorPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IptvLoadErrorPolicyTest {

    @Test
    fun permanentClientErrors_areMarkedAsFinal() {
        listOf(400, 401, 403, 404, 405, 410, 422).forEach { code ->
            assertTrue("Code $code should be marked as final", IptvLoadErrorPolicy.isFinalStatus(code))
        }
    }

    @Test
    fun temporaryErrors_areNotFinalAndShouldRetry() {
        // 408 Request Timeout and 429 Too Many Requests should be retried
        assertFalse("408 Timeout should not be final", IptvLoadErrorPolicy.isFinalStatus(408))
        assertFalse("429 Too Many Requests should not be final", IptvLoadErrorPolicy.isFinalStatus(429))

        // 5xx Server errors should be retried
        listOf(500, 502, 503, 504).forEach { code ->
            assertFalse("Server error $code should not be final", IptvLoadErrorPolicy.isFinalStatus(code))
        }

        // 2xx and 3xx are not errors
        assertFalse("200 OK should not be final", IptvLoadErrorPolicy.isFinalStatus(200))
    }
}
