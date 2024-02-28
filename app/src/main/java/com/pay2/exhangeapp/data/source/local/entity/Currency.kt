package com.pay2.exhangeapp.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency")
data class Currency(
    @PrimaryKey
    val code: String,
    val name: String
){
    override fun toString(): String {
        return "$code ($name)"
    }
}