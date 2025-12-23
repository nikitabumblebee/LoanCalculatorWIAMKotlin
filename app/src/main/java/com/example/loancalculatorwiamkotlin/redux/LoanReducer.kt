package com.example.loancalculatorwiamkotlin.redux

import com.example.loancalculatorwiamkotlin.domain.models.LoanError
import com.example.loancalculatorwiamkotlin.domain.models.LoanProcessState
import com.example.loancalculatorwiamkotlin.domain.models.LoanState
import com.example.loancalculatorwiamkotlin.domain.models.LoanModel
import android.util.Log

fun loanReducer(state: LoanState, action: LoanAction): LoanState {
    val result = when (action) {
        is LoanAction.UpdateAmount -> {
            val newAmount = action.amount
            val newReturnAmount = LoanModel.calculateReturnAmount(
                newAmount,
                state.loan.period,
                state.loan.creditRate
            )
            state.copy(
                loan = state.loan.copy(
                    amount = newAmount,
                    returnAmount = newReturnAmount
                )
            )
        }

        is LoanAction.UpdateDays -> {
            val newPeriod = action.days
            val newReturnAmount = LoanModel.calculateReturnAmount(
                state.loan.amount,
                newPeriod,
                state.loan.creditRate
            )
            val newReturnDate = LoanModel.calculateReturnDate(newPeriod, state.loan.baseDate)
            state.copy(
                loan = state.loan.copy(
                    period = newPeriod,
                    returnAmount = newReturnAmount,
                    returnDate = newReturnDate
                )
            )
        }

        is LoanAction.StartProcessing -> {
            state.copy(loan = state.loan.copy(processState = LoanProcessState.Processing))
        }

        is LoanAction.SendLoan -> {
            state.copy(loan = state.loan)
        }

        is LoanAction.SubmitLoanFailure -> {
            state.copy(
                loan = state.loan.copy(
                    processState = LoanProcessState.Error(action.error)
                )
            )
        }

        is LoanAction.SubmitLoanSuccess -> {
            state.copy(loan = state.loan.copy(processState = LoanProcessState.Finish))
        }

        is LoanAction.Reset -> {
            state.copy(loan = state.loan.copy(processState = LoanProcessState.Idle))
        }

        LoanAction.CheckInternet -> state

        LoanAction.InternetConnectionFailed -> {
            state.copy(
                isInternetAvailable = false,
                notifyOnRestoreInternetConnection = true
            )
        }

        LoanAction.InternetConnectionRestored -> {
            if (state.isInternetAvailable == false) {
                state.copy(
                    isInternetAvailable = true,
                    notifyOnRestoreInternetConnection = true
                )
            } else {
                state
            }
        }

        LoanAction.ResetInternetNotification -> {
            state.copy(notifyOnRestoreInternetConnection = null)
        }

        LoanAction.IncorrectAmount -> {
            state.copy(
                loan = state.loan.copy(
                    processState = LoanProcessState.Error(LoanError.InvalidAmount)
                )
            )
        }
    }
    return result
}
