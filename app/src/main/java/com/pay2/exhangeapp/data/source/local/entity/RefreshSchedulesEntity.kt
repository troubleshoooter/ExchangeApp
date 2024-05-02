package com.pay2.exhangeapp.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("refresh_schedules")
data class RefreshSchedulesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val resource: String,
    val timestamp: Long
)