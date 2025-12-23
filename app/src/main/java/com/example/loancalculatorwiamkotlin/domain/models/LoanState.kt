package com.example.loancalculatorwiamkotlin.domain.models

data class LoanState(
    val loan: LoanModel = LoanModel(),
    val isInternetAvailable: Boolean? = null,
    val notifyOnRestoreInternetConnection: Boolean? = null
)