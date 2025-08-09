package com.example.eventmanagement

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: EventRepository
    private val _selectedDate = MutableLiveData<Long>()

    // Use switchMap extension (import androidx.lifecycle.switchMap)
    val eventsByDate: LiveData<List<Event>>

    val upcomingEvents: LiveData<List<Event>>

    init {
        val dao = EventDatabase.getDatabase(application).eventDao()
        repo = EventRepository(dao)

        eventsByDate = _selectedDate.switchMap { date ->
            repo.getEventsByDate(date)
        }

        val today = System.currentTimeMillis()
        upcomingEvents = repo.getUpcomingEvents(today)
    }

    fun setSelectedDate(startOfDayMillis: Long) {
        _selectedDate.value = startOfDayMillis
    }

    fun insert(event: Event) = viewModelScope.launch { repo.insert(event) }
    fun update(event: Event) = viewModelScope.launch { repo.update(event) }
    fun delete(event: Event) = viewModelScope.launch { repo.delete(event) }
}