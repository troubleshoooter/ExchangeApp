package com.pay2.exhangeapp.presentation.ui.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AmountTextFieldTest {


    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun initialValueIsEmpty() {
        composeTestRule.setContent {
            AmountTextField(onValueChange = {}, modifier = Modifier)
        }

        composeTestRule.onNodeWithTag("AmountTextField").assertIsDisplayed()
    }

    @Test
    fun valueChangesOnInput() {
        var capturedValue = ""
        composeTestRule.setContent {
            AmountTextField(onValueChange = { capturedValue = it }, modifier = Modifier)
        }

        composeTestRule.onNodeWithTag("AmountTextField").performTextInput("123.45")
        composeTestRule.waitForIdle()

        assertEquals("123.45", capturedValue)
    }

    @Test
    fun inputIsRestrictedByRegex() {
        composeTestRule.setContent {
            AmountTextField(onValueChange = { }, modifier = Modifier)
        }

        composeTestRule.onNodeWithTag("AmountTextField").performTextInput("..")
        composeTestRule.onNodeWithText("").assertIsDisplayed()

        composeTestRule.onNodeWithTag("AmountTextField").performTextInput("00000")
        composeTestRule.onNodeWithText("").assertIsDisplayed()

        composeTestRule.onNodeWithTag("AmountTextField").performTextInput("00.0000")
        composeTestRule.onNodeWithText("").assertIsDisplayed()

        composeTestRule.onNodeWithTag("AmountTextField").performTextInput("123")
        composeTestRule.onNodeWithText("123").assertIsDisplayed()
    }
}