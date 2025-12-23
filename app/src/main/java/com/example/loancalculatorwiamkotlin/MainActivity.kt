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
import com.example.loancalculatorwiamkotlin.redux.LoanAction
import com.example.loancalculatorwiamkotlin.redux.Store
import com.example.loancalculatorwiamkotlin.redux.loanReducer
import com.example.loancalculatorwiamkotlin.ui.screens.ConverterScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var store: Store<LoanState, LoanAction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val networkingService = NetworkingService.getInstance(this)
            val validationMiddleware = LoanValidationMiddleware(networkingService)
            val middleware: List<suspend (Store<LoanState, LoanAction>, LoanState, LoanAction) -> Unit> =
                listOf(
                    { store, state, action ->
                        validationMiddleware.process(store, state, action)
                    }
                )

            store = Store(
                initialState = LoanState(),
                reducer = ::loanReducer,
                middleware = middleware
            )

            setContent {
                LoanCalculatorWIAMKotlinTheme {
                    ConverterScreen(
                        stateFlow = store.state,
                        onAction = store::dispatch
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