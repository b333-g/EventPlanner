package com.example.eventmanagement

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.example.eventmanagement.databinding.DialogAddEventBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*

class AddEventDialog(
    context: Context,
    private val onSave: (String, String, Long, String) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogAddEventBinding
    private var selectedDate: Long? = null
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pick Date
        binding.btnPickDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { dateInMillis ->
                selectedDate = dateInMillis
                val formatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dateInMillis))
                binding.tvSelectedDate.text = formatted
            }

            datePicker.show((context as MainActivity).supportFragmentManager, "DATE_PICKER")
        }

        // Pick Time
        binding.btnPickTime.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Time")
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val hour = timePicker.hour
                val minute = timePicker.minute
                val amPm = if (hour >= 12) "PM" else "AM"
                val formattedHour = if (hour % 12 == 0) 12 else hour % 12
                selectedTime = String.format("%02d:%02d %s", formattedHour, minute, amPm)
                binding.tvSelectedTime.text = selectedTime
            }

            timePicker.show((context as MainActivity).supportFragmentManager, "TIME_PICKER")
        }

        // Save Event
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val desc = binding.etDescription.text.toString()

            if (title.isEmpty() || selectedDate == null || selectedTime == null) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            onSave(title, desc, selectedDate!!, selectedTime!!)
            dismiss()
        }

        binding.btnCancel.setOnClickListener { dismiss() }
    }
}