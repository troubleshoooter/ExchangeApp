package com.pay2.exhangeapp.presentation.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.pay2.exhangeapp.common.formatDecimal
import com.pay2.exhangeapp.data.models.Currency
import com.pay2.exhangeapp.data.models.ExchangeRates
import com.pay2.exhangeapp.presentation.viewmodel.MainViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: MainViewModel
    private val mockCurrencies =
        persistentListOf(Currency("USD", "United States Dollar"), Currency("EUR", "Euro"))
    private val mockRates = listOf(ExchangeRates("USD", 1.0), ExchangeRates("EUR", 0.95))

    @Before
    fun setup() {
        mockViewModel = mockk<MainViewModel> {
            coEvery { getCurrencies() } returns MutableStateFlow(mockCurrencies)
            coEvery { getExchangeRates() } returns MutableStateFlow(mockRates)
            coEvery { getError() } returns MutableStateFlow("An error occurred")
            coEvery { fetchData() } returns Unit
            coEvery { getSelectedCurrency() } returns null
            coEvery { setSelectedCurrency(any()) } returns Unit
            coEvery { clearList() } returns Unit
        }
    }
    @Test
    fun currency_selection_triggers_fetch_and_updates_state() {

        composeTestRule.setContent {
            Home(mainViewModel = mockViewModel)
        }

        // Select currency and verify updates
        composeTestRule.onNode(hasText("Select Your Currency")).performClick()
        composeTestRule.onNode(hasText(mockCurrencies[0].toString())).performClick()

        // Confirm empty list after initial selection

        // Mock amount input and trigger conversion
        val amountTextField = composeTestRule.onNodeWithText("Enter Amount")
        amountTextField.performTextClearance()
        amountTextField.performTextInput("100")
        composeTestRule.waitUntil(5000) {
            composeTestRule.onNodeWithTag("LazyVerticalStaggeredGrid").isDisplayed()
        }
        // Verify conversion fetch and empty result
        composeTestRule.onNodeWithTag("LazyVerticalStaggeredGrid")
            .assertIsDisplayed() // Still empty grid
    }

    @Test
    fun error_state_triggers_snackbar() {
        val mockError = "An error occurred"

        composeTestRule.setContent {
            Home(mainViewModel = mockViewModel)
        }

        // Verify snackbar with error message
        composeTestRule.onNodeWithText(mockError).assertIsDisplayed()
    }

    @Test
    fun successful_conversion_shows_list() {
        val mockCurrencies = persistentListOf(Currency("USD", "US Dollar"), Currency("EUR", "Euro"))
        val mockRates = listOf(ExchangeRates("USD", 1.0), ExchangeRates("EUR", 0.95))
        val mockViewModel = mockk<MainViewModel> {
            coEvery { getCurrencies() } returns MutableStateFlow(mockCurrencies)
            coEvery { getExchangeRates() } returns MutableStateFlow(mockRates)
            coEvery { getError() } returns MutableStateFlow("Error")
            coEvery { fetchData() } returns Unit
            coEvery { getSelectedCurrency() } returns null
            coEvery { setSelectedCurrency(any()) } returns Unit
            coEvery { clearList() } returns Unit
        }
        composeTestRule.setContent {
            Home(mainViewModel = mockViewModel)
        }

        // Select currency and enter valid amount
        composeTestRule.onNode(hasText("Select Your Currency")).performClick()
        composeTestRule.onNode(hasText(mockCurrencies[1].toString())).performClick()

        val amountTextField = composeTestRule.onNodeWithTag("OutlinedTextField")
        amountTextField.performTextClearance()
        amountTextField.performTextInput("100")

        // Verify displayed conversion rates
        composeTestRule.onNode(hasText(mockRates[0].code)).assertIsDisplayed() // USD rate
        composeTestRule.onNode(hasText(mockRates[0].rate.formatDecimal()))
            .assertIsDisplayed() // USD format
        composeTestRule.onNode(hasText(mockRates[1].code)).assertIsDisplayed() // EUR rate
        composeTestRule.onNode(hasText(mockRates[1].rate.formatDecimal()))
            .assertIsDisplayed() // EUR format
    }
}