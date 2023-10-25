package com.example.meditrack.homeActivity.reminder.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.meditrack.R
import com.example.meditrack.databinding.FragmentNotificationBinding
import com.example.meditrack.homeActivity.home.HomeViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotificationFragment : Fragment() {
    private lateinit var binding: FragmentNotificationBinding

    companion object {
        fun newInstance() = NotificationFragment()
    }

    private lateinit var viewModel: NotificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "1",
                "Medicine Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context?.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }



        val reminderDialogBox = view.findViewById<FloatingActionButton>(R.id.add_new_reminder)

        reminderDialogBox.setOnClickListener {
            val dialog = MedicineReminderDialog()
            dialog.show(childFragmentManager, "MedicineReminderDialog")
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNotificationBinding.bind(view)

//        binding.apply {
//            addNewReminder.setOnClickListener {
//                val dialog = MedicineReminderDialog()
//                dialog.show(childFragmentManager, "MedicineReminderDialog")
//            }
//        }
    }



}