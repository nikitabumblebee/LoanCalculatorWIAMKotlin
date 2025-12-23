package com.example.loancalculatorwiamkotlin

import com.example.loancalculatorwiamkotlin.data.dto.LoanRequest
import com.example.loancalculatorwiamkotlin.data.dto.LoanResponse
import com.example.loancalculatorwiamkotlin.data.validation.LoanValidationMiddleware
import com.example.loancalculatorwiamkotlin.data.network.NetworkingService
import com.example.loancalculatorwiamkotlin.domain.models.*
import com.example.loancalculatorwiamkotlin.redux.LoanAction
import com.example.loancalculatorwiamkotlin.redux.Store
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LoanValidationMiddlewareTest {
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var middleware: LoanValidationMiddleware
    private lateinit var mockNetworkingService: NetworkingService
    private lateinit var mockStore: Store<LoanState, LoanAction>

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        mockNetworkingService = mockk<NetworkingService>(relaxed = true)
        mockStore = mockk<Store<LoanState, LoanAction>>(relaxed = true)

        val mockContext = mockk<android.content.Context>(relaxed = true)
        middleware = LoanValidationMiddleware(mockNetworkingService, mockContext)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `UpdateAmount valid - no dispatch`() = runTest {
        val action = LoanAction.UpdateAmount(10_000.0)
        val state = createTestState()

        middleware.process(mockStore, state, action)

        verify(exactly = 0) { mockStore.dispatch(any()) }
    }

    @Test
    fun `UpdateAmount invalid low - dispatches IncorrectAmount`() = runTest {
        val action = LoanAction.UpdateAmount(1_000.0)
        val state = createTestState()

        middleware.process(mockStore, state, action)

        verify { mockStore.dispatch(LoanAction.IncorrectAmount) }
    }

    @Test
    fun `UpdateAmount invalid high - dispatches IncorrectAmount`() = runTest {
        val action = LoanAction.UpdateAmount(100_000.0)
        val state = createTestState()

        middleware.process(mockStore, state, action)

        verify { mockStore.dispatch(LoanAction.IncorrectAmount) }
    }

    @Test
    fun `UpdateDays - no dispatch`() = runTest {
        val action = LoanAction.UpdateDays(30)
        val state = createTestState()

        middleware.process(mockStore, state, action)

        verify(exactly = 0) { mockStore.dispatch(any()) }
    }

    @Test
    fun `StartProcessing - no re-dispatch`() = runTest {
        val loan = createTestLoanModel()
        val action = LoanAction.StartProcessing(loan)
        val state = createTestState(loan)

        middleware.process(mockStore, state, action)

        verify(exactly = 0) { mockStore.dispatch(any()) }
    }

    @Test
    fun `CheckInternet - no dispatch`() = runTest {
        val action = LoanAction.CheckInternet
        val state = createTestState()

        middleware.process(mockStore, state, action)

        verify(exactly = 0) { mockStore.dispatch(any()) }
    }

    @Test
    fun `Reset - no dispatch`() = runTest {
        val action = LoanAction.Reset
        val state = createTestState()

        middleware.process(mockStore, state, action)

        verify(exactly = 0) { mockStore.dispatch(any()) }
    }

    @Test
    fun `SubmitLoanSuccess - no re-dispatch`() = runTest {
        val action = LoanAction.SubmitLoanSuccess(LoanResponse(101))
        val state = createTestState()

        middleware.process(mockStore, state, action)

        verify(exactly = 0) { mockStore.dispatch(any()) }
    }

    @Test
    fun `SubmitLoanFailure - no re-dispatch`() = runTest {
        val action = LoanAction.SubmitLoanFailure(LoanError.RequestFailed)
        val state = createTestState()

        middleware.process(mockStore, state, action)

        verify(exactly = 0) { mockStore.dispatch(any()) }
    }

    private fun createTestLoanModel(): LoanModel = LoanModel(
        amount = 10_000.0,
        period = 14,
        creditRate = 15.0,
        processState = LoanProcessState.Idle,
        returnAmount = 10_500.0
    )

    private fun createTestState(loan: LoanModel = createTestLoanModel()): LoanState = LoanState(
        loan = loan,
        isInternetAvailable = true,
        notifyOnRestoreInternetConnection = null
    )
}