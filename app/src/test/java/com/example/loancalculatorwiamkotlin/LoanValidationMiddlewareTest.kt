package com.example.loancalculatorwiamkotlin

import com.example.loancalculatorwiamkotlin.data.dto.LoanRequest
import com.example.loancalculatorwiamkotlin.data.dto.LoanResponse
import com.example.loancalculatorwiamkotlin.data.validation.LoanValidationMiddleware
import com.example.loancalculatorwiamkotlin.data.network.NetworkingService
import com.example.loancalculatorwiamkotlin.domain.models.*
import com.example.loancalculatorwiamkotlin.redux.LoanAction
import com.example.loancalculatorwiamkotlin.redux.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class LoanValidationMiddlewareTest {
    @Mock
    private lateinit var networkingService: NetworkingService

    @Mock
    private lateinit var mockStore: Store<LoanState, LoanAction>

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var middleware: LoanValidationMiddleware

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        middleware = LoanValidationMiddleware(networkingService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `UpdateAmount - valid amount - no action dispatched`() = runTest {
        val action = LoanAction.UpdateAmount(10_000.0)
        val state = createTestState()

        middleware.process(mockStore, state, action)

        verify(mockStore, never()).dispatch(any())
    }

    @Test
    fun `UpdateAmount - invalid amount - dispatches IncorrectAmount`() = runTest {
        val action = LoanAction.UpdateAmount(1_000.0) // < 5000
        val state = createTestState()

        middleware.process(mockStore, state, action)

        verify(mockStore).dispatch(LoanAction.IncorrectAmount)
    }

    @Test
    fun `StartProcessing - saves data but no re-dispatch`() = runTest {
        val action = LoanAction.StartProcessing(createTestLoanModel())
        val state = createTestState()

        middleware.process(mockStore, state, action)

        verify(mockStore, never()).dispatch(any()) // ✅ Не диспатчит снова
    }

    @Test
    fun `SendLoan - success - dispatches SubmitLoanSuccess`() = runTest {
        val loan = createTestLoanModel()
        val state = createTestState(loan = loan)
        val action = LoanAction.SendLoan
        val mockResponse = LoanResponse("123")

        `when`(networkingService.sendRequest(anyString())).thenReturn(mockResponse)

        middleware.process(mockStore, state, action)

        testDispatcher.scheduler.advanceUntilIdle()
        verify(mockStore).dispatch(LoanAction.SubmitLoanSuccess(mockResponse))
    }

    @Test
    fun `SendLoan - failure - dispatches SubmitLoanFailure`() = runTest {
        val loan = createTestLoanModel()
        val state = createTestState(loan = loan)
        val action = LoanAction.SendLoan

        `when`(networkingService.sendRequest(anyString())).thenThrow(RuntimeException("Network error"))

        middleware.process(mockStore, state, action)

        testDispatcher.scheduler.advanceUntilIdle()
        verify(mockStore).dispatch(LoanAction.SubmitLoanFailure(LoanError.RequestFailed))
    }

    @Test
    fun `SubmitLoanFailure - no re-dispatch`() = runTest {
        val action = LoanAction.SubmitLoanFailure(LoanError.RequestFailed)
        val state = createTestState()

        middleware.process(mockStore, state, action)

        verify(mockStore, never()).dispatch(any())
    }

    @Test
    fun `Reset - no action dispatched`() = runTest {
        val action = LoanAction.Reset
        val state = createTestState()

        middleware.process(mockStore, state, action)

        verify(mockStore, never()).dispatch(any())
    }

    private fun createTestLoanModel(): LoanModel = LoanModel(
        amount = 10_000.0,
        period = 14,
        creditRate = 15.0,
        processState = LoanProcessState.Idle
    )

    private fun createTestState(loan: LoanModel = createTestLoanModel()): LoanState = LoanState(
        loan = loan,
        isInternetAvailable = true,
        notifyOnRestoreInternetConnection = null
    )
}