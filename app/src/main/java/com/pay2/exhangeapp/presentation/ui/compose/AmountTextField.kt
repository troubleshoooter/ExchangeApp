package com.pay2.exhangeapp.presentation.ui.compose

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AmountTextField(onValueChange: (String) -> Unit, modifier: Modifier) {
    var amountValue by remember {
        mutableStateOf("")
    }
    OutlinedTextField(
        maxLines = 1,
        singleLine = true,
        placeholder = { Text(text = "Enter Amount") },
        value = amountValue,
        onValueChange = { value ->
            // Regex that only allows non zero leading digits and one decimal
            val regex = "^(?!0)(?:0|[1-9][0-9]*)(\\.)?[0-9]*$".toRegex()
            if (value.length < amountValue.length || value.matches(regex)) {
                amountValue = value
                onValueChange(amountValue)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = modifier
    )
}