package com.pay2.exhangeapp.presentation.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.pay2.exhangeapp.data.models.Currency
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropDown(
    isLoading: Boolean,
    currencyItems: ImmutableList<Currency>?,
    onCurrencySelected: (currency: Currency?) -> Unit
) {
    var mExpanded by rememberSaveable { mutableStateOf(false) }
    var currencyValue by rememberSaveable {
        mutableStateOf("")
    }
    var selectedCurrencyValue by rememberSaveable {
        mutableStateOf("")
    }
    ExposedDropdownMenuBox(
        expanded = mExpanded,
        onExpandedChange = { mExpanded = !mExpanded },
        modifier = Modifier
            .padding(12.dp)
            .testTag("ExposedDropdownMenuBox"),
    ) {
        OutlinedTextField(
            value = currencyValue,
            onValueChange = {
                if (!mExpanded){
                    mExpanded = true
                }
                currencyValue = it
            },
            maxLines = 1,
            readOnly = selectedCurrencyValue.isNotBlank() || isLoading,
            singleLine = true,
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 4.dp,
                        modifier = Modifier
                            .size(22.dp)
                            .testTag("CircularProgressIndicator")
                    )
                } else {
                    if (currencyValue.isBlank()) {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = mExpanded
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier
                                .clickable {
                                    currencyValue = ""
                                    selectedCurrencyValue = ""
                                    onCurrencySelected(null)
                                }
                                .testTag("Close")
                        )
                    }
                }
            },
            placeholder = { Text(text = "Select Your Currency") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .testTag("OutlinedTextField")
        )
        val filteringOptions = currencyItems?.filter {
            it.toString().contains(currencyValue, ignoreCase = true)
        }
        ExposedDropdownMenu(
            expanded = mExpanded,
            onDismissRequest = { mExpanded = false },
            modifier = Modifier
                .heightIn(0.dp, 240.dp)
                .fillMaxWidth()
                .testTag("ExposedDropdownMenu")
        ) {
            filteringOptions?.map { currency ->
                DropdownMenuItem(
                    text = { Text(currency.toString()) },
                    onClick = {
                        currencyValue = currency.toString()
                        onCurrencySelected(currency)
                        selectedCurrencyValue = currency.toString()
                        mExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}