package com.example.eventmanagement.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.data.Event
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(
    private val onActionModeStart: () -> Unit,
    private val onSelectionChanged: (selectedCount: Int) -> Unit,
    val onEdit: (Event) -> Unit,
    val onDelete: (List<Event>)-> Unit,
    ) : RecyclerView.Adapter<EventAdapter.VH>() {

    private val selectedItems = mutableSetOf<Int>()
    var actionModeActive = false
    private var events = mutableListOf<Event>()

    fun submitList(list: List<Event>) {
        events = list.toMutableList()
        notifyDataSetChanged()
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.eventTitle)
        val desc: TextView = itemView.findViewById(R.id.eventDesc)
        val time: TextView = itemView.findViewById(R.id.eventTime)
        val date: TextView = itemView.findViewById(R.id.eventDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = events[position]
        holder.title.text = e.title
        holder.desc.text = e.description
        holder.time.text = e.time
        holder.date.text = getDate(e.date)

        if (selectedItems.contains(position)) {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.item_selected)
            )
        } else {
            holder.itemView.setBackgroundResource(R.drawable.item_bg)
        }


        holder.itemView.setOnLongClickListener {
            if (!actionModeActive) {
                actionModeActive = true
                onActionModeStart()
            }
            toggleSelection(position)
            true
        }

        holder.itemView.setOnClickListener {
            if (actionModeActive) {
                toggleSelection(position)
            }
        }
    }
    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
        notifyItemChanged(position)
        onSelectionChanged(selectedItems.size)

    }

    private fun getSelectedEvents(): List<Event> {
        return selectedItems.map { events[it] }
    }

    fun deleteSelectedEvents() {
        val toDelete = getSelectedEvents()
        onDelete(toDelete)
        clearSelection()
    }

    fun getSelectedCount(): Int = selectedItems.size

    fun getSingleSelectedEvent(): Event? {
        return if (selectedItems.size == 1) events[selectedItems.first()] else null
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
        onSelectionChanged(0)
    }


    private fun getDate(d:Long): String {
       val date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault()).format(Date(d)).replace("00:00","")
        return date
    }

    override fun getItemCount(): Int = events.size
}