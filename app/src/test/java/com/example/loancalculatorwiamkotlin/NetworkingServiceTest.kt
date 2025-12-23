package com.example.loancalculatorwiamkotlin

import io.mockk.*
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException
import com.example.loancalculatorwiamkotlin.data.network.NetworkingService
import com.example.loancalculatorwiamkotlin.data.dto.LoanResponse

class NetworkingServiceTest {

    private lateinit var mockService: NetworkingService

    @Before
    fun setup() {
        mockService = mockk<NetworkingService>(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `sendRequest mock - returns LoanResponse with id`() = runTest {
        val expectedResponse = LoanResponse(101)
        coEvery { mockService.sendRequest(any()) } returns expectedResponse

        val response = mockService.sendRequest("""{"amount":10000}""")

        assertEquals(101, response.id)
        coVerify { mockService.sendRequest(any()) }
    }

    @Test
    fun `sendRequest mock - called with correct JSON`() = runTest {
        val jsonBody = """{"amount":10000,"period":14}"""
        coEvery { mockService.sendRequest(jsonBody) } returns LoanResponse(456)

        mockService.sendRequest(jsonBody)

        coVerify { mockService.sendRequest(jsonBody) }
    }

    @Test
    fun `sendRequest mock - throws exception on error`() = runTest {
        coEvery { mockService.sendRequest(any()) } throws IOException("Network error")

        try {
            mockService.sendRequest("""{"amount":10000}""")
            fail("Should throw IOException")
        } catch (e: IOException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `monitorInternetAvailability mock - returns flow of boolean`() = runTest {
        coEvery { mockService.monitorInternetAvailability() } returns
                kotlinx.coroutines.flow.flowOf(true)

        mockService.monitorInternetAvailability()
            .take(1)
            .collect { isConnected ->
                assertTrue(isConnected)
            }
    }
}