package com.example.loancalculatorwiamkotlin.domain.models

sealed class LoanError(val description: String) : Exception(description) {
    data object InvalidAmount : LoanError("Amount must be between 5000 and 50000")
    data object JsonEncoding : LoanError("JSON encoding failed")
    data object JsonDecoding : LoanError("JSON decoding failed")
    data object RequestFailed : LoanError("Request failed")
    data object Undefined : LoanError("An unexpected error occurred")

    val localizedDescription: String
        get() = description
}
