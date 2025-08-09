package com.example.eventmanagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(
    private val onEdit: (Event) -> Unit,
    private val onDelete: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.VH>() {

    private var items = listOf<Event>()

    fun submitList(list: List<Event>) {
        items = list
        notifyDataSetChanged()
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.eventTitle)
        val desc: TextView = itemView.findViewById(R.id.eventDesc)
        val time: TextView = itemView.findViewById(R.id.eventTime)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = items[position]
        holder.title.text = e.title
        holder.desc.text = e.description
        holder.time.text = e.time
        holder.btnEdit.setOnClickListener { onEdit(e) }
        holder.btnDelete.setOnClickListener { onDelete(e) }
    }

    override fun getItemCount(): Int = items.size
}