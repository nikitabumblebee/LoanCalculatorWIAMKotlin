package com.example.loancalculatorwiamkotlin.data.validation

import android.content.SharedPreferences
import com.example.loancalculatorwiamkotlin.data.dto.LoanRequest
import com.example.loancalculatorwiamkotlin.data.dto.LoanResponse
import com.example.loancalculatorwiamkotlin.data.network.NetworkingService
import com.example.loancalculatorwiamkotlin.domain.models.LoanError
import com.example.loancalculatorwiamkotlin.domain.models.LoanModel
import com.example.loancalculatorwiamkotlin.domain.models.LoanState
import com.example.loancalculatorwiamkotlin.redux.LoanAction
import com.example.loancalculatorwiamkotlin.redux.Store
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoanValidationMiddleware(
    private val networkingService: NetworkingService
) {

    private object Constants {
        const val MAX_RETRY_COUNT = 3
        const val MIN_AMOUNT = 5_000.0
        const val MAX_AMOUNT = 50_000.0
    }

    suspend fun process(
        store: Store<LoanState, LoanAction>,
        state: LoanState,
        action: LoanAction
    ) {
        when (action) {
            is LoanAction.CheckInternet -> {
                Log.d("LoanValidationMiddleware", "CheckInternet action received, not processing")
            }

            is LoanAction.UpdateAmount -> {
                if (action.amount < Constants.MIN_AMOUNT || action.amount > Constants.MAX_AMOUNT) {
                    store.dispatch(LoanAction.IncorrectAmount)
                }
            }

            is LoanAction.UpdateDays -> {
                Log.d("LoanValidationMiddleware", "UpdateDays: ${action.days} processed")
            }

            is LoanAction.StartProcessing -> {
                saveLastData(action.loan)
            }

            is LoanAction.SendLoan -> {
                CoroutineScope(Dispatchers.Default).launch {
                    val result = tryToSendRequest(state.loan, 0)
                    store.dispatch(result)
                }
            }

            is LoanAction.SubmitLoanFailure -> {
                Log.d("LoanValidationMiddleware", "SubmitLoanFailure received")
            }

            is LoanAction.SubmitLoanSuccess -> {
                Log.d("LoanValidationMiddleware", "SubmitLoanSuccess received")
            }

            is LoanAction.Reset -> {
                Log.d("LoanValidationMiddleware", "Reset action received")
            }

            LoanAction.InternetConnectionFailed -> {
                Log.d("LoanValidationMiddleware", "InternetConnectionFailed")
            }

            LoanAction.InternetConnectionRestored -> {
                Log.d("LoanValidationMiddleware", "InternetConnectionRestored")
            }

            LoanAction.ResetInternetNotification -> {
                Log.d("LoanValidationMiddleware", "ResetInternetNotification")
            }

            LoanAction.IncorrectAmount -> {
                Log.d("LoanValidationMiddleware", "IncorrectAmount")
            }
        }
    }

    private suspend fun tryToSendRequest(
        loanModel: LoanModel,
        retryCount: Int
    ): LoanAction {
        return try {
            val loanRequest = LoanRequest(
                amount = loanModel.amount,
                period = loanModel.period,
                totalRepayment = loanModel.returnAmount
            )

            val jsonBody = Json.encodeToString(loanRequest)

            val response = networkingService.sendRequest(jsonBody)

            LoanAction.SubmitLoanSuccess(response)
        } catch (_: Exception) {
            if (retryCount < Constants.MAX_RETRY_COUNT) {
                delay(2000)
                tryToSendRequest(loanModel, retryCount + 1)
            } else {
                LoanAction.SubmitLoanFailure(LoanError.RequestFailed)
            }
        }
    }

    private fun saveLastData(loanModel: LoanModel) {
        
        // Здесь позже добавишь сохранение в SharedPreferences через отдельный helper
    }
}
