package com.example.meditrack.homeActivity.reminder.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.meditrack.R
import com.example.meditrack.databinding.FragmentNotificationBinding
import com.example.meditrack.homeActivity.HomeActivity
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotificationFragment : Fragment() {


    companion object {
        fun newInstance() = NotificationFragment()
    }

    private lateinit var viewModel: NotificationViewModel
    private lateinit var binding: FragmentNotificationBinding
    private lateinit var progressDialog: CustomProgressDialog

//    override fun onPrepareOptionsMenu(menu: Menu) {
//        for(i in 0 until menu.size()){
//            menu.getItem(i).isVisible = false
//        }
//
//        super.onPrepareOptionsMenu(menu)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        binding = FragmentNotificationBinding.bind(view)

//        setHasOptionsMenu(true)

        progressDialog = CustomProgressDialog(requireContext())

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
            val dialog = MedicineReminderDialog(requireActivity())
            dialog.show(childFragmentManager, "MedicineReminderDialog")
        }

        return binding.root
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

    override fun onPause() {
        super.onPause()
        (activity as? HomeActivity)?.clearNavigationDrawerSelection(R.id.nav_notifications)
    }

    override fun onResume() {
        super.onResume()
        (activity as? HomeActivity)?.setNavigationDrawerSelection(R.id.nav_notifications)
    }



}