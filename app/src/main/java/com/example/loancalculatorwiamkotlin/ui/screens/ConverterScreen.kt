package com.example.loancalculatorwiamkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loancalculatorwiamkotlin.domain.models.LoanProcessState
import com.example.loancalculatorwiamkotlin.domain.models.LoanState
import com.example.loancalculatorwiamkotlin.redux.LoanAction
import com.example.loancalculatorwiamkotlin.ui.components.ConverterSliderView
import com.example.loancalculatorwiamkotlin.utils.formatAmount
import com.example.loancalculatorwiamkotlin.utils.toDayMonthAndYear
import kotlinx.coroutines.flow.StateFlow
import android.util.Log

@Composable
fun ConverterScreen(
    stateFlow: StateFlow<LoanState>,
    onAction: (LoanAction) -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()
    var amountValue by remember { mutableFloatStateOf(state.loan.amount.toFloat()) }
    var durationValue by remember { mutableFloatStateOf(state.loan.period.toFloat()) }
    var alertTitle by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf("") }

    var lastDispatchedAmount by remember { mutableFloatStateOf(state.loan.amount.toFloat()) }
    var lastDispatchedDuration by remember { mutableFloatStateOf(state.loan.period.toFloat()) }
    var sendLoanDispatched by remember { mutableStateOf(false) }
    val showAlert = state.loan.processState is LoanProcessState.Error ||
            state.loan.processState is LoanProcessState.Finish ||
            state.notifyOnRestoreInternetConnection == true

    LaunchedEffect(state.loan.processState) {
        snapshotFlow { state.loan.processState }
            .collect { processState ->
                Log.d("ConverterScreen", "processState changed to: ${state.loan.processState}")
                when (processState) {
                    is LoanProcessState.Error -> {
                        Log.d("ConverterScreen", "Error")
                        alertTitle = "Error"
                        alertMessage = processState.error.localizedDescription
                        sendLoanDispatched = false
                    }

                    LoanProcessState.Finish -> {
                        Log.d("ConverterScreen", "Finish")
                        alertTitle = "Success"
                        alertMessage = "Your request for loan was successfully sent"
                        sendLoanDispatched = false
                    }

                    LoanProcessState.Processing -> {
                        Log.d("ConverterScreen", "Processing - sending loan")
                        onAction(LoanAction.SendLoan)
                        if (!sendLoanDispatched) {
                            Log.d("ConverterScreen", "Processing - sending loan")
                            onAction(LoanAction.SendLoan)
                            sendLoanDispatched = true
                        }
                    }

                    LoanProcessState.Idle -> {
                        sendLoanDispatched = false
                    }
                }
            }
    }

    LaunchedEffect(state.isInternetAvailable) {
        if (state.notifyOnRestoreInternetConnection == true) {
            if (state.isInternetAvailable == false) {
                alertTitle = "No internet connection"
                alertMessage = "Internet connection was failed. Please try again later"
                onAction(LoanAction.ResetInternetNotification)
            } else if (state.isInternetAvailable == true) {
                alertTitle = "Internet connection restored"
                alertMessage = ""
                onAction(LoanAction.ResetInternetNotification)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDarkTheme) "Dark theme" else "Light theme",
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onThemeToggle
                )
            }

            // Amount Slider
            ConverterSliderView(
                value = amountValue,
                onValueChange = { newValue ->
                    amountValue = newValue
                    if (newValue.toInt() != lastDispatchedAmount.toInt()) {
                        lastDispatchedAmount = newValue
                        onAction(LoanAction.UpdateAmount(newValue.toDouble()))
                    }
                },
                title = "How much?",
                valueLabel = "$${amountValue.toDouble().formatAmount()}",
                accentColor = Color(0xFF32B8C6),
                rangeValue = 5_000f..50_000f,
                step = 1f
            )

            // Duration Slider
            ConverterSliderView(
                value = durationValue,
                onValueChange = { newValue ->
                    Log.d("ConverterScreen", "Slider changed: $newValue, lastDispatched: $lastDispatchedDuration")
                    durationValue = newValue
                    if (newValue.toInt() != lastDispatchedDuration.toInt()) {
                        Log.d("ConverterScreen", "Dispatching UpdateDays: ${newValue.toInt()}")
                        lastDispatchedDuration = newValue.toFloat()
                        onAction(LoanAction.UpdateDays(newValue.toInt()))
                    } else {
                        Log.d("ConverterScreen", "Skipping dispatch - value not changed")
                    }
                },
                title = "How long?",
                valueLabel = "${durationValue.toInt()} days",
                accentColor = Color(0xFFE6815D),
                rangeValue = 7f..28f,
                step = 7f
            )

            // Info Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Currency rate: ${state.loan.creditRate.formatAmount()}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Return date: ${state.loan.returnDate.toDayMonthAndYear()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Return amount: ${state.loan.returnAmount.formatAmount()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Submit Button
            Button(
                onClick = {
                    onAction(LoanAction.StartProcessing(state.loan))
                },
                enabled = state.loan.processState != LoanProcessState.Processing && state.isInternetAvailable != false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(50.dp)
            ) {
                if (state.loan.processState == LoanProcessState.Processing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Processing...", fontWeight = FontWeight.Bold)
                } else {
                    Text("Submit an application", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Alert Dialog
        if (showAlert) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(alertTitle) },
                text = { Text(alertMessage) },
                confirmButton = {
                    Button(onClick = {
                        onAction(LoanAction.Reset)
                    }) {
                        Text("Ok")
                    }
                }
            )
        }
    }
}

@Composable
fun rememberScrollState(): androidx.compose.foundation.ScrollState {
    return remember { androidx.compose.foundation.ScrollState(0) }
}
