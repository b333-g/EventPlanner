package com.example.eventmanagement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.databinding.FragmentUpcomingEventsBinding

class UpcomingEventsFragment : Fragment() {

    private val vm: EventViewModel by activityViewModels()
    private lateinit var adapter: EventAdapter
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_upcoming_events, container, false)
        recyclerView = root.findViewById(R.id.recyclerView)
        adapter = EventAdapter(onEdit = { /* open edit if you want */ }, onDelete = { vm.delete(it) })
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        vm.upcomingEvents.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        return root
    }
}