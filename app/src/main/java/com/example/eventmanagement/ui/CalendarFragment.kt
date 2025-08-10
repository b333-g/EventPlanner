package com.example.eventmanagement.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.databinding.FragmentCalendarBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import androidx.appcompat.view.ActionMode
import com.example.eventmanagement.viewmodel.EventViewModel
import com.example.eventmanagement.R
import com.example.eventmanagement.data.Event
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val vm: EventViewModel by activityViewModels()
    private lateinit var adapter: EventAdapter
    private var actionMode: ActionMode? = null
    private var editMenuItem: MenuItem? = null
    private var deleteMenuItem: MenuItem? = null
    private var selectedDateStartMillis: Long = startOfDayMillis(System.currentTimeMillis())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)

        adapter = EventAdapter(onActionModeStart = { startActionMode() }, onSelectionChanged = { count -> updateMenuState(count) },
            onEdit = { openDialog(it) }, onDelete = { selectedEvents -> vm.delete(selectedEvents) })
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // initialize
        vm.setSelectedDate(selectedDateStartMillis)
        vm.eventsByDate.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        binding.calendarView.setOnDateChangeListener { _: CalendarView, y, m, d ->
            val cal = Calendar.getInstance()
            cal.set(y, m, d, 0, 0, 0)
            selectedDateStartMillis = startOfDayMillis(cal.timeInMillis)
            vm.setSelectedDate(selectedDateStartMillis)
        }

        binding.fabAdd.setOnClickListener { openDialog(null) }

        return binding.root
    }

    private fun updateMenuState(selectedCount: Int) {
        editMenuItem?.isVisible = selectedCount == 1
        deleteMenuItem?.isVisible = selectedCount >= 1
        actionMode?.title = "$selectedCount selected"
    }

    private fun openDialog(event: Event?) {
        val builder = AlertDialog.Builder(requireContext())
        val v = layoutInflater.inflate(R.layout.dialog_event, null)
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
                date = pickedDate,
                time = eventTime.text.toString().trim()
            )
            
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun startOfDayMillis(ts: Long): Long {
            val c = Calendar.getInstance()
            c.timeInMillis = ts
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            return c.timeInMillis
        }
    }
}