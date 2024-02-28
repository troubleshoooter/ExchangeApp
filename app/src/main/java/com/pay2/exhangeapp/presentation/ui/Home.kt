package com.pay2.exhangeapp.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pay2.exhangeapp.common.formatDecimal
import com.pay2.exhangeapp.presentation.ui.compose.AmountTextField
import com.pay2.exhangeapp.presentation.ui.compose.CurrencyDropDown
import com.pay2.exhangeapp.presentation.viewmodel.MainViewModel


private const val GRID_SPAN = 3
private const val DEFAULT_CONVERSION_RATE = 1.0

@Composable
fun Home(mainViewModel: MainViewModel = viewModel()) {
    var amountValue by remember {
        mutableStateOf("")
    }

    val currencyItems by mainViewModel.getCurrencies().observeAsState()

    val exchangeRates by mainViewModel.getExchangeRates().observeAsState()

    LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Fixed(GRID_SPAN)) {
        item(span = StaggeredGridItemSpan.FullLine) {
            AmountTextField(
                onValueChange = {
                    amountValue = it
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
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            CurrencyDropDown(
                currencyItems = currencyItems.orEmpty(),
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
        }
        items(exchangeRates.orEmpty()) {
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