package com.example.meditrack.utility.ownDialogs

import android.app.AlertDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.example.meditrack.R
import java.util.*


class MonthYearPickerDialog : DialogFragment() {

    private var listener: OnDateSetListener? = null

    fun setListener(listener: OnDateSetListener?) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())

        val inflater = requireActivity().layoutInflater

        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)

        val dialog: View = inflater.inflate(R.layout.date_picker_dialog, null)
        val monthPicker = dialog.findViewById<NumberPicker>(R.id.picker_month)
        val yearPicker = dialog.findViewById<NumberPicker>(R.id.picker_year)

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = cal.get(Calendar.MONTH)

        val year: Int = currentYear - 4
        val maxYears = 2099 // Define your MAX_YEAR value
        yearPicker.minValue = year
        yearPicker.maxValue = maxYears
        yearPicker.value = year

        builder.setView(dialog)
            // Add action buttons
            .setPositiveButton("OK") { _, _ ->
                listener?.onDateSet(null, yearPicker.value, monthPicker.value, 0)
            }
            .setNegativeButton("Cancel") { monthYearPickerDialog, _ ->
                monthYearPickerDialog.cancel()
            }
        return builder.create()
    }
}