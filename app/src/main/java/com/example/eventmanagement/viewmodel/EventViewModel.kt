package com.example.eventmanagement.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.eventmanagement.data.Event
import com.example.eventmanagement.data.EventDatabase
import com.example.eventmanagement.data.EventRepository
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: EventRepository
    private val _selectedDate = MutableLiveData<Long>()

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
    fun delete(event: List<Event>) = viewModelScope.launch { repo.delete(event) }
}