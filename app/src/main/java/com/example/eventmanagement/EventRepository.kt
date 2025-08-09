package com.example.eventmanagement

import androidx.lifecycle.LiveData
import java.util.concurrent.TimeUnit

class EventRepository(private val dao: EventDao) {

    /** date must represent start-of-day millis; we'll query start..end */
    fun getEventsByDate(dateStartMillis: Long): LiveData<List<Event>> {
        val start = dateStartMillis
        val end = dateStartMillis + TimeUnit.DAYS.toMillis(1) - 1
        return dao.getEventsByDate(start, end)
    }

    fun getUpcomingEvents(fromMillis: Long): LiveData<List<Event>> = dao.getUpcomingEvents(fromMillis)

    suspend fun insert(event: Event) = dao.insert(event)
    suspend fun update(event: Event) = dao.update(event)
    suspend fun delete(event: Event) = dao.delete(event)
}