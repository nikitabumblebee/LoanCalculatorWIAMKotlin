package com.example.loancalculatorwiamkotlin.domain.models

import java.time.LocalDate
import java.util.Calendar
import com.example.loancalculatorwiamkotlin.utils.roundedToTwoDecimalPlaces
import android.content.Context
import com.example.loancalculatorwiamkotlin.utils.PreferencesManager

data class LoanModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val amount: Double = 10_000.0,
    val period: Int = 14,
    val creditRate: Double = 15.0,
    val baseDate: LocalDate = LocalDate.now(),
    val returnAmount: Double = calculateReturnAmount(10_000.0, 14, 15.0),
    val returnDate: LocalDate = calculateReturnDate(14, LocalDate.now()),
    val processState: LoanProcessState = LoanProcessState.Idle
) {
    companion object {
        fun calculateReturnAmount(amount: Double, period: Int, creditRate: Double): Double {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.MONTH, Calendar.DECEMBER)
                set(Calendar.DAY_OF_MONTH, 31)
            }
            val endOfYear = calendar.timeInMillis

            val startOfYear = Calendar.getInstance().apply {
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis

            val daysInYear = ((endOfYear - startOfYear) / (1000 * 60 * 60 * 24)).toInt() + 1
            val creditDuration = period.toDouble() / daysInYear
            val rate = creditRate / 100.0

            return (amount * (1 + creditDuration * rate)).roundedToTwoDecimalPlaces(2)
        }

        fun calculateReturnDate(period: Int, baseDate: LocalDate = LocalDate.now()): LocalDate {
            return baseDate.plusDays(period.toLong())
        }
    }
}

fun createInitialLoanModel(context: Context): LoanModel {
    val amount = PreferencesManager.getLastAmount(context)
    val period = PreferencesManager.getLastPeriod(context)
    val creditRate = 15.0

    return LoanModel(
        amount = amount,
        period = period,
        creditRate = creditRate,
        returnAmount = LoanModel.calculateReturnAmount(amount, period, creditRate),
        returnDate = LoanModel.calculateReturnDate(period),
        processState = LoanProcessState.Idle
    )
}

sealed interface LoanProcessState {
    data object Idle : LoanProcessState
    data object Processing : LoanProcessState
    data object Finish : LoanProcessState
    data class Error(val error: LoanError) : LoanProcessState
}