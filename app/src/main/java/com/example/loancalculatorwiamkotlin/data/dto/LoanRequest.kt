package com.example.loancalculatorwiamkotlin.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoanRequest(
    val amount: Double,
    val period: Int,
    val totalRepayment: Double
)
