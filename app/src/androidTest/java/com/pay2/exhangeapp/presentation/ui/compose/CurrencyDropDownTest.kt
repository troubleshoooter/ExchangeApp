package com.pay2.exhangeapp.presentation.ui.compose

import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.pay2.exhangeapp.data.models.Currency
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CurrencyDropDownTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockCurrency = Currency("USD", "US Dollar")
    private val mockCurrencies = persistentListOf(mockCurrency)

    @Test
    fun initial_state_shows_placeholder() {
        composeTestRule.setContent {
            Surface {
                CurrencyDropDown(
                    isLoading = true,
                    currencyItems = null,
                    onCurrencySelected = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Select Your Currency").assertIsDisplayed()
        composeTestRule.onNodeWithTag("OutlinedTextField").performClick()
        composeTestRule.onNodeWithTag("OutlinedTextField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("OutlinedTextField").performTextInput("test")
        composeTestRule.onNodeWithText("").assertIsDisplayed()
    }

    @Test
    fun typing_in_textfield_enables_close_icon() {
        composeTestRule.setContent {
            Surface {
                CurrencyDropDown(
                    isLoading = false,
                    currencyItems = mockCurrencies,
                    onCurrencySelected = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Select Your Currency").assertIsDisplayed()
        composeTestRule.onNodeWithTag("OutlinedTextField").performClick()
        composeTestRule.onNodeWithTag("OutlinedTextField").performTextInput("test")
        composeTestRule.onNodeWithTag("Close").assertIsDisplayed()
        composeTestRule.onNodeWithTag("Close").performClick()
        composeTestRule.onNodeWithText("Select Your Currency").assertIsDisplayed()
    }

    @Test
    fun clicking_textfield_opens_dropdown() {
        composeTestRule.setContent {
            Surface {
                CurrencyDropDown(
                    isLoading = false,
                    currencyItems = mockCurrencies,
                    onCurrencySelected = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Select Your Currency").assertIsDisplayed()
        composeTestRule.onNodeWithTag("OutlinedTextField").performClick()
        composeTestRule.onNode(hasText(mockCurrency.toString())).assertIsDisplayed()
    }

    @Test
    fun selecting_dropdown_item_sets_selected_value() {
        composeTestRule.setContent {
            Surface {
                CurrencyDropDown(
                    isLoading = false,
                    currencyItems = mockCurrencies,
                    onCurrencySelected = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Select Your Currency").assertIsDisplayed()
        composeTestRule.onNodeWithTag("j").performClick()
        composeTestRule.onNode(hasText(mockCurrency.toString())).performClick()
        composeTestRule.onNodeWithText(mockCurrency.toString()).assertIsDisplayed()
    }

    @Test
    fun filtering_works_correctly() = runTest {
        composeTestRule.setContent {
            Surface {
                CurrencyDropDown(
                    isLoading = false,
                    currencyItems = persistentListOf(
                        Currency("USD", "US Dollar"),
                        Currency("EUR", "Euro"),
                        Currency("GBP", "British Pound")
                    ),
                    onCurrencySelected = {}
                )
            }
        }


        composeTestRule.onNodeWithText("Select Your Currency").assertIsDisplayed()
        composeTestRule.onNodeWithTag("OutlinedTextField").performClick()
        composeTestRule.onNodeWithTag("OutlinedTextField").performTextInput("eur")
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasText("Euro", substring = true)).assertIsDisplayed()
        composeTestRule.onNode(hasText("US Dollar", substring = true)).assertIsNotDisplayed()
        composeTestRule.onNode(hasText("British Pound", substring = true)).assertIsNotDisplayed()
    }
}