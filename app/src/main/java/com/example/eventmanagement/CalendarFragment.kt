package com.example.eventmanagement

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.databinding.FragmentCalendarBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val vm: EventViewModel by activityViewModels()
    private lateinit var adapter: EventAdapter
    private var selectedDateStartMillis: Long = startOfDayMillis(System.currentTimeMillis())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)

        adapter = EventAdapter(onEdit = { openDialog(it) }, onDelete = { vm.delete(it) })
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

    private fun openDialog(event: Event?) {
        val builder = AlertDialog.Builder(requireContext())
        val v = layoutInflater.inflate(R.layout.dialog_event, null)
        val titleInput = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputTitle)
        val descInput = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputDesc)
        val dateInput = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputDate)
        val timeInput = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputTime)

        val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var pickedDate = event?.date ?: selectedDateStartMillis
        var pickedTime = event?.time ?: ""

        dateInput.setText(dateFmt.format(Date(pickedDate)))
        timeInput.setText(pickedTime)
        titleInput.setText(event?.title ?: "")
        descInput.setText(event?.description ?: "")

        dateInput.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker().setSelection(pickedDate).build()
            picker.addOnPositiveButtonClickListener { ts ->
                pickedDate = startOfDayMillis(ts)
                dateInput.setText(dateFmt.format(Date(pickedDate)))
            }
            picker.show(parentFragmentManager, "datePicker")
        }

        timeInput.setOnClickListener {
            val cal = Calendar.getInstance()
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(cal.get(Calendar.HOUR_OF_DAY))
                .setMinute(cal.get(Calendar.MINUTE))
                .build()
            picker.addOnPositiveButtonClickListener {
                pickedTime = String.format(Locale.getDefault(), "%02d:%02d", picker.hour, picker.minute)
                timeInput.setText(pickedTime)

                // merge picked time into date millis for storage convenience (optional)
                // We keep date millis as start-of-day for queries; time string stored separately.
            }
            picker.show(parentFragmentManager, "timePicker")
        }

        builder.setView(v)
        builder.setTitle(if (event == null) "Add Event" else "Edit Event")
        builder.setPositiveButton("Save") { dialog, _ ->
            val newEvent = Event(
                id = event?.id ?: 0,
                title = titleInput.text.toString().trim(),
                description = descInput.text.toString().trim(),
                date = pickedDate, // start-of-day millis for that date
                time = timeInput.text.toString().trim()
            )
            if (event == null) vm.insert(newEvent) else vm.update(newEvent)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
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