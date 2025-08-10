# Event Planner App (Android - MVVM)

A simple Android Event Planner app built using Kotlin, MVVM Architecture, Room Database, ViewModel, LiveData, and Coroutines.  
The app allows you to add, edit, delete, and view events in a calendar and upcoming events list.

# Features
- Add Events :  with title, description, date, and time.
- Calendar View : Tap on a date to view scheduled events.
- Edit & Delete Events :  with multi-selection support.
- Offline Storage :  with Room Database.
- Upcoming Events :  list sorted by date.
- Material Date & Time Picker :  for selecting event time.
- MVVM Architecture :  with Repository pattern.

-------

##  Tech Stack
- Language: Kotlin
- UI: XML, Material Components
- Architecture: MVVM
- Database: Room
- Async: Kotlin Coroutines
- UI Binding: ViewBinding
- Calendar: MaterialDatePicker

--------

## Project Structure

app/
├── data/
│ ├── Event.kt # Entity
│ ├── EventDao.kt # DAO
│ ├── EventDatabase.kt # Room Database
│ ├── EventRepository.kt # Repository
│
├── ui/
│ ├── MainActivity.kt # Main activity
│ ├── CalendarFragment.kt # Calendar UI
│ ├── UpcomingEventsFragment.kt # Upcoming list
│ ├── EventAdapter.kt # RecyclerView Adapter
│
├── viewmodel/
│ ├── EventViewModel.kt # ViewModel
│
│
├── res/
│ ├── layout/ # XML layouts
│ ├── values/ # Strings, colors, styles
│
└── AndroidManifest.xml
