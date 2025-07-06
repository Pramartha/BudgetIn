package com.example.budgetin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.budgetin.R

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Set click listeners for navigation
        view.findViewById<View>(R.id.btnNotificationSettings).setOnClickListener {
            navigateToFragment(NotificationSettingsFragment())
        }

        view.findViewById<View>(R.id.btnPinSettings).setOnClickListener {
            navigateToFragment(PinSettingsFragment())
        }

        view.findViewById<View>(R.id.btnFontSettings).setOnClickListener {
            navigateToFragment(FontSettingsFragment())
        }

        view.findViewById<View>(R.id.btnThemeSettings).setOnClickListener {
            navigateToFragment(ThemeSettingsFragment())
        }

        return view
    }

    private fun navigateToFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null) // This allows back navigation
        transaction.commit()
    }
}