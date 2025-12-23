package com.example.loancalculatorwiamkotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.loancalculatorwiamkotlin.ui.theme.LoanCalculatorWIAMKotlinTheme
import com.example.loancalculatorwiamkotlin.data.network.NetworkingService
import com.example.loancalculatorwiamkotlin.data.validation.LoanValidationMiddleware
import com.example.loancalculatorwiamkotlin.domain.models.LoanState
import com.example.loancalculatorwiamkotlin.domain.models.createInitialLoanModel
import com.example.loancalculatorwiamkotlin.redux.LoanAction
import com.example.loancalculatorwiamkotlin.redux.Store
import com.example.loancalculatorwiamkotlin.redux.loanReducer
import com.example.loancalculatorwiamkotlin.ui.screens.ConverterScreen
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private lateinit var store: Store<LoanState, LoanAction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val networkingService = NetworkingService.getInstance(this)
            val validationMiddleware = LoanValidationMiddleware(networkingService, this)
            val middleware: List<suspend (Store<LoanState, LoanAction>, LoanState, LoanAction) -> Unit> =
                listOf(
                    { store, state, action ->
                        validationMiddleware.process(store, state, action)
                    }
                )

            val initialLoan = createInitialLoanModel(this)
            val initialState = LoanState(
                loan = initialLoan,
                isInternetAvailable = true,
                notifyOnRestoreInternetConnection = null
            )

            store = Store(
                initialState = initialState,
                reducer = ::loanReducer,
                middleware = middleware
            )

            setContent {
                var isDarkTheme: Boolean by remember { mutableStateOf(false) }
                LoanCalculatorWIAMKotlinTheme(darkTheme = isDarkTheme) {
                    ConverterScreen(
                        stateFlow = store.state,
                        onAction = store::dispatch,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { isDarkTheme = it }
                    )
                }
            }

            lifecycleScope.launch {
                store.dispatch(LoanAction.CheckInternet)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LoanCalculatorWIAMKotlinTheme {
        Greeting("Android")
    }
}