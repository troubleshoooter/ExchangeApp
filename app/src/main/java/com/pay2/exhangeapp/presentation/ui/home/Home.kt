package com.pay2.exhangeapp.presentation.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pay2.exhangeapp.common.formatDecimal
import com.pay2.exhangeapp.presentation.ui.compose.AmountTextField
import com.pay2.exhangeapp.presentation.ui.compose.CurrencyDropDown
import com.pay2.exhangeapp.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch


private const val GRID_SPAN = 3

@Composable
fun Home(mainViewModel: MainViewModel = viewModel()) {
    var amountValue by rememberSaveable {
        mutableStateOf("")
    }

    val currencyState by mainViewModel.getCurrencies().collectAsStateWithLifecycle()
    val exchangeRatesState by mainViewModel.getExchangeRates().collectAsStateWithLifecycle()
    val errorState by mainViewModel.getError().collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        mainViewModel.fetchData()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { paddingValues ->
        if (!errorState.isNullOrEmpty()) {
            LaunchedEffect(key1 = snackBarHostState) {
                scope.launch {
                    snackBarHostState.showSnackbar(
                        message = errorState.toString()
                    )
                }
            }
        }
        Column(Modifier.padding(paddingValues)) {
            AmountTextField(
                onValueChange = { value ->
                    amountValue = value
                    if (mainViewModel.getSelectedCurrency() != null && amountValue.isNotBlank()) {
                        mainViewModel.getConversions(amountValue.toDouble())
                    } else {
                        mainViewModel.clearList()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
            CurrencyDropDown(
                isLoading = currencyState == null,
                currencyItems = currencyState,
                onCurrencySelected = { currency ->
                    val prevCurrency = mainViewModel.getSelectedCurrency()
                    mainViewModel.setSelectedCurrency(currency)
                    currency?.let {
                        if (amountValue.isBlank()) {
                            mainViewModel.clearList()
                        } else if (amountValue.isNotBlank() && prevCurrency != currency) {
                            mainViewModel.getConversions(amountValue.toDouble())
                        }


                    } ?: mainViewModel.clearList()
                }
            )
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(GRID_SPAN),
                modifier = Modifier.testTag("LazyVerticalStaggeredGrid")
            ) {
                if (!exchangeRatesState.isNullOrEmpty()) {
                    items(exchangeRatesState.orEmpty(), key = { it.code }) {
                        ElevatedCard(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            modifier = Modifier
                                .padding(8.dp)
                        ) {
                            Text(
                                text = it.code,
                                style = TextStyle(fontWeight = FontWeight.Medium),
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 4.dp)
                            )
                            Text(
                                text = it.rate.formatDecimal(),
                                style = TextStyle(fontWeight = FontWeight.Normal),
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

    }
}