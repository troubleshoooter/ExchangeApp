package com.pay2.exhangeapp.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.pay2.exhangeapp.data.source.local.entity.RefreshSchedulesEntity

@Dao
interface RefreshSchedulesDao {

    @Upsert
    suspend fun upsert(scheduled: RefreshSchedulesEntity)

    @Query("Select timestamp from refresh_schedules where resource = :resource")
    suspend fun getLastUpdatedTimestamp(resource: String): Long?
}