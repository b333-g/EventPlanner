package com.example.eventmanagement

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event)

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    // Query events happening that day (date stored as start-of-day millis)
    @Query("SELECT * FROM events WHERE date BETWEEN :start AND :end ORDER BY date ASC, time ASC")
    fun getEventsByDate(start: Long, end: Long): LiveData<List<Event>>

    // Upcoming events from today (inclusive)
    @Query("SELECT * FROM events WHERE date >= :today ORDER BY date ASC, time ASC")
    fun getUpcomingEvents(today: Long): LiveData<List<Event>>

    // All distinct dates that have events (used for calendar decorator)
    @Query("SELECT DISTINCT date FROM events")
    fun getAllEventDates(): LiveData<List<Long>>
}