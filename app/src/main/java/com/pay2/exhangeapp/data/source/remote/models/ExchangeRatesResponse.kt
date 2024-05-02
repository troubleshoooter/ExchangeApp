package com.pay2.exhangeapp.data.source.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRatesResponse(
    val timestamp: Long,
    val rates: Map<String, Double>
)