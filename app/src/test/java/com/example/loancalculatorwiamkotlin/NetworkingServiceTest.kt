package com.example.loancalculatorwiamkotlin

import android.content.Context
import android.net.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*
import com.example.loancalculatorwiamkotlin.data.network.NetworkingService
import com.example.loancalculatorwiamkotlin.data.dto.LoanResponse

class NetworkingServiceTest {

    private val service = NetworkingService.forTesting()

    @Test
    fun `sendRequest - success response`() = runTest {
        val expectedResponse = LoanResponse("123")
        coEvery { service.sendRequest(any()) } returns expectedResponse
        val jsonBody = """{"amount":10000}"""

        val response = service.sendRequest(jsonBody)
        assertEquals("123", response.id)
    }

    @Test
    fun `sendRequest - network failure`() = runTest {
        val jsonBody = """{"amount":10000}"""

        coEvery { service.sendRequest(jsonBody) } throws RuntimeException("Error")
        try {
            service.sendRequest(jsonBody)
            fail("Should throw exception")
        } catch (e: RuntimeException) {
            assertEquals("Error", e.message)
        }
    }
}