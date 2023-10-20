package com.example.meditrack.homeActivity.medicine.addMedicine

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.databinding.ActivityHomeBinding
import com.example.meditrack.databinding.FragmentAddMedicineBinding
import com.example.meditrack.homeActivity.HomeActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.mlkit.vision.text.TextRecognition
import java.util.Calendar

class AddMedicineFragment : Fragment() {

    companion object {
        fun newInstance() = AddMedicineFragment()
    }

    private lateinit var viewModel: AddMedicineViewModel
    private lateinit var binding: ActivityHomeBinding
    private lateinit var addMedicineBinding: FragmentAddMedicineBinding
    private lateinit var expiryDatePicker: TextInputEditText
    private lateinit var startDatePicker: TextInputEditText
    private val calendar = Calendar.getInstance()
    /*private lateinit var homeActivity: HomeActivity*/
    private val REQUEST_IMAGE_CAPTURE = 1

    /*override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure that the parent activity is HomeActivity
        if (context is HomeActivity) {
            homeActivity = context
        } else {
            throw IllegalStateException("Parent activity must be HomeActivity")
        }
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_medicine, container, false)

        expiryDatePicker = view.findViewById(R.id.fragment_medicine_expirydate_TextInputEditText)
        startDatePicker = view.findViewById(R.id.fragment_medicine_startdate_TextInputEditText)

        startDatePicker.setOnClickListener {
            startDatePickerDialog(view)
        }
        expiryDatePicker.setOnClickListener {
            expiryDatePickerDialog(view)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addMedicineBinding = FragmentAddMedicineBinding.bind(view)

        /*homeActivity.getToolbarMenuLayout().visibility = View.GONE*/

        addMedicineBinding.medicineImage.setOnClickListener {
            findNavController().navigate(R.id.OCRFragment)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddMedicineViewModel::class.java)
        // TODO: Use the ViewModel
    }

    fun expiryDatePickerDialog(view: View) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Handle the selected date here
                val selectedDate = "$selectedDay/${selectedMonth}/${selectedYear}"
                expiryDatePicker.setText(selectedDate)
            },
            year,
            month,
            day
        )

        // Optional: Set a minimum date if needed
        // datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        datePickerDialog.show()
    }
    fun startDatePickerDialog(view: View) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Handle the selected date here
                val selectedDate = "$selectedDay/${selectedMonth}/${selectedYear}"
                startDatePicker.setText(selectedDate)
            },
            year,
            month,
            day
        )

        // Optional: Set a minimum date if needed
        // datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

}