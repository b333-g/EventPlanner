package com.example.eventmanagement
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.eventmanagement.databinding.MainActivityBinding
import com.example.eventmanagement.ui.CalendarFragment
import com.example.eventmanagement.ui.UpcomingEventsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(CalendarFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calendar -> { loadFragment(CalendarFragment()); true }
                R.id.nav_upcoming -> { loadFragment(UpcomingEventsFragment()); true }
                else -> false
            }
        }
    }

    private fun loadFragment(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, f)
            .commit()
    }
}