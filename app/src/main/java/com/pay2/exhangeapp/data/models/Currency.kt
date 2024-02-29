package com.pay2.exhangeapp.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Currency(
    val code: String,
    val name: String
) : Parcelable {
    override fun toString(): String {
        return "$code ($name)"
    }
}