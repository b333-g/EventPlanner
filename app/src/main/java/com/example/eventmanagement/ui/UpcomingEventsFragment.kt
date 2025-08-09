package com.example.eventmanagement.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.viewmodel.EventViewModel
import com.example.eventmanagement.R
import com.example.eventmanagement.ui.CalendarFragment.Companion.startOfDayMillis
import com.example.eventmanagement.data.Event
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UpcomingEventsFragment : Fragment() {

    private val vm: EventViewModel by activityViewModels()
    private lateinit var adapter: EventAdapter
    private lateinit var recyclerView: RecyclerView
    private var selectedDateStartMillis: Long = startOfDayMillis(System.currentTimeMillis())

    private var actionMode: ActionMode? = null
    private var editMenuItem: MenuItem? = null
    private var deleteMenuItem: MenuItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_upcoming_events, container, false)
        recyclerView = root.findViewById(R.id.recyclerView)
        adapter = EventAdapter(onActionModeStart = { startActionMode() }, onSelectionChanged = { count -> updateMenuState(count) },onEdit = {openDialog(it)}, onDelete = { selectedEvents -> vm.delete(selectedEvents) })
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        vm.upcomingEvents.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        return root
    }

    private fun updateMenuState(selectedCount: Int) {
        editMenuItem?.isVisible = selectedCount == 1
        deleteMenuItem?.isVisible = selectedCount >= 1
        actionMode?.title = "$selectedCount selected"
    }

    private fun openDialog(event: Event?) {
        val builder = AlertDialog.Builder(requireContext())
        val v = layoutInflater.inflate(R.layout.dialog_event, null)
        val titleInput = v.findViewById<LinearLayout>(R.id.inputTitle)
        val descInput = v.findViewById<LinearLayout>(R.id.inputDesc)
        val dateInput = v.findViewById<LinearLayout>(R.id.inputDate)
        val timeInput = v.findViewById<LinearLayout>(R.id.inputTime)

        val eventTitle = v.findViewById<EditText>(R.id.eventTitle)
        val eventDesc = v.findViewById<EditText>(R.id.eventDesc)
        val eventDate = v.findViewById<EditText>(R.id.eventDate)
        val eventTime = v.findViewById<EditText>(R.id.eventTime)

        val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var pickedDate = event?.date ?: selectedDateStartMillis
        var pickedTime = event?.time ?: "00:00"


        eventDate.setText(dateFmt.format(Date(pickedDate)))
        eventTime.setText(pickedTime)
        eventTitle.setText(event?.title ?: "")
        eventDesc.setText(event?.description ?: "")

        eventDate.setOnClickListener {
            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(System.currentTimeMillis())) // No past dates

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select event date")
                .setSelection(pickedDate ?: MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

            picker.addOnPositiveButtonClickListener { ts ->
                pickedDate = startOfDayMillis(ts)
                eventDate.setText(dateFmt.format(Date(pickedDate)))
            }

            picker.show(parentFragmentManager, "datePicker")
        }

        eventTime.setOnClickListener {
            val cal = Calendar.getInstance()

            // If time was already picked, use that
            pickedTime.let {
                val parts = it.split(":")
                if (parts.size == 2) {
                    cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                    cal.set(Calendar.MINUTE, parts[1].toInt())
                }
            }

            val picker = MaterialTimePicker.Builder()
                .setTitleText("Select event time")
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(cal.get(Calendar.HOUR_OF_DAY))
                .setMinute(cal.get(Calendar.MINUTE))
                .build()

            picker.addOnPositiveButtonClickListener {
                pickedTime = String.format(Locale.getDefault(), "%02d:%02d", picker.hour, picker.minute)
                eventTime.setText(pickedTime)
            }

            picker.show(parentFragmentManager, "timePicker")
        }

        builder.setView(v)
        builder.setTitle(if (event == null) "Add Event" else "Edit Event")
        builder.setPositiveButton("Save") { dialog, _ ->
            val newEvent = Event(
                id = event?.id ?: 0,
                title = eventTitle.text.toString().trim(),
                description = eventDesc.text.toString().trim(),
                date = pickedDate, // start-of-day millis for that date
                time = eventTime.text.toString().trim()
            )
            if(eventTitle.text.toString().trim().isEmpty()){
                Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (event == null) vm.insert(newEvent) else vm.update(newEvent)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun startActionMode() {
        actionMode = (requireActivity() as AppCompatActivity)
            .startSupportActionMode(object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    mode.menuInflater.inflate(R.menu.menu_action_mode, menu)
                    editMenuItem = menu.findItem(R.id.action_edit)
                    deleteMenuItem = menu.findItem(R.id.action_delete)
                    updateMenuState(adapter.getSelectedCount())
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

                override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                    return when (item.itemId) {
                        R.id.action_edit -> {
                            adapter.getSingleSelectedEvent()?.let { adapter.onEdit(it) }
                            mode.finish()
                            true
                        }
                        R.id.action_delete -> {
                            adapter.deleteSelectedEvents()
                            mode.finish()
                            true
                        }
                        R.id.action_unselect -> {
                            adapter.clearSelection()
                            mode.finish()
                            true
                        }
                        else -> false
                    }
                }

                override fun onDestroyActionMode(mode: ActionMode) {
                    adapter.actionModeActive = false
                    adapter.clearSelection()
                }
            })
    }
}