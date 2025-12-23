package com.example.loancalculatorwiamkotlin.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.pow
import kotlin.math.round

fun Double.roundedToTwoDecimalPlaces(fractionDigits: Int): Double {
    val multiplier = 10.0.pow(fractionDigits)
    return round(this * multiplier) / multiplier
}

fun Double.formatAmount(): String {
    val decimalFormatSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
    }
    val formatter = DecimalFormat("###,##0.##", decimalFormatSymbols)
    return formatter.format(this)
}

fun Double.formatNumberWithSpaces(): String {
    val decimalFormatSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ' '
    }
    val formatter = DecimalFormat("###,##0", decimalFormatSymbols)
    return formatter.format(this)
}
