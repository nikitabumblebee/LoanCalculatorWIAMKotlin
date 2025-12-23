package com.example.loancalculatorwiamkotlin.redux

import com.example.loancalculatorwiamkotlin.domain.models.LoanError
import com.example.loancalculatorwiamkotlin.domain.models.LoanModel
import com.example.loancalculatorwiamkotlin.data.dto.LoanResponse

sealed interface LoanAction {
    data object CheckInternet : LoanAction
    data object InternetConnectionFailed : LoanAction
    data object InternetConnectionRestored : LoanAction
    data object ResetInternetNotification : LoanAction
    data class StartProcessing(val loan: LoanModel) : LoanAction
    data object SendLoan : LoanAction
    data class UpdateAmount(val amount: Double) : LoanAction
    data class UpdateDays(val days: Int) : LoanAction
    data class SubmitLoanSuccess(val response: LoanResponse) : LoanAction
    data class SubmitLoanFailure(val error: LoanError) : LoanAction
    data object Reset : LoanAction
    data object IncorrectAmount : LoanAction
}
