package com.pay2.exhangeapp.presentation.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
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

    val homeUiState by mainViewModel.getHomeUiState().collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { paddingValues ->
        if (!homeUiState.errorMessage.isNullOrEmpty()) {
            LaunchedEffect(key1 = snackBarHostState) {
                scope.launch {
                    snackBarHostState.showSnackbar(
                        message = homeUiState.errorMessage.toString()
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
                homeUiState.isLoading && homeUiState.currencies.isEmpty(),
                currencyItems = homeUiState.currencies,
                onCurrencySelected = { currency ->
                    mainViewModel.setSelectedCurrency(currency)
                    currency?.let {
                        if (amountValue.isNotBlank()) {
                            mainViewModel.getConversions(amountValue.toDouble())
                        } else {
                            mainViewModel.clearList()
                        }
                    } ?: mainViewModel.clearList()
                }
            )
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(GRID_SPAN),
            ) {
                if (homeUiState.isLoading
                    && homeUiState.currencies.isNotEmpty()
                    && homeUiState.exchangeRates.isEmpty()
                ) {
                    item { LinearProgressIndicator() }
                } else {
                    items(homeUiState.exchangeRates) {
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