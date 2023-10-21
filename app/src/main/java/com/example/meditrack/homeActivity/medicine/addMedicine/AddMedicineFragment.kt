package com.example.meditrack.homeActivity.medicine.addMedicine

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.dataModel.MedicineFrequency
import com.example.meditrack.databinding.FragmentAddMedicineBinding
import com.example.meditrack.regularExpression.ListPattern
import com.example.meditrack.regularExpression.MatchPattern.Companion.validate
import com.example.meditrack.utility.MonthYearPickerDialog
import com.example.meditrack.utility.UtilityFunction
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class AddMedicineFragment : Fragment() {

    companion object {
        fun newInstance() = AddMedicineFragment()
    }

    private lateinit var viewModel: AddMedicineViewModel
    private lateinit var binding: FragmentAddMedicineBinding
    private lateinit var expiryDatePicker: TextInputEditText
    private lateinit var startDatePicker: TextInputEditText
    private val calendar = Calendar.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "AddMedicineFragment"
    /*private lateinit var homeActivity: HomeActivity*/
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var medName:String
    private lateinit var dosage:String
    private lateinit var freqChipGroup: ChipGroup
    private lateinit var breakFastChipGroup: ChipGroup
    private lateinit var launchChipGroup: ChipGroup
    private lateinit var dinnerChipGroup: ChipGroup


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
        viewModel = ViewModelProvider(this)[AddMedicineViewModel::class.java]

        expiryDatePicker = view.findViewById(R.id.fragment_medicine_expirydate_TextInputEditText)
        startDatePicker = view.findViewById(R.id.fragment_medicine_startdate_TextInputEditText)


        freqChipGroup = view.findViewById(R.id.freqChipGroup)
        breakFastChipGroup = view.findViewById(R.id.breakFastChipGroup)
        launchChipGroup = view.findViewById(R.id.launchChipGroup)
        dinnerChipGroup = view.findViewById(R.id.dinnerChipGroup)

        for (tag in viewModel.freqTags) {
            val chip = Chip(requireContext())
            chip.text = tag
            chip.isCheckable = true
            chip.isClickable = true
            chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    val selectedChip = Chip(requireContext())
                    selectedChip.text = tag
                    selectedChip.isCloseIconVisible = true
                    selectedChip.setOnCloseIconClickListener {
                        selectedChip.isChecked = false
                    }
                    viewModel.selectedfreqTags= selectedChip.text as String
                }
                else{
                    viewModel.selectedfreqTags=null
                }
                Toast.makeText(requireContext(),"${viewModel.selectedfreqTags}",Toast.LENGTH_SHORT).show()
            }
            freqChipGroup.addView(chip)
        }

        for (tag in viewModel.dinnerTags) {
            val chip = Chip(requireContext())
            chip.text = tag
            chip.isCheckable = true
            chip.isClickable = true
            chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    val selectedChip = Chip(requireContext())
                    selectedChip.text = tag
                    selectedChip.isCloseIconVisible = true
                    selectedChip.setOnCloseIconClickListener {
                        selectedChip.isChecked = false
                    }
                    viewModel.selecteddinnerTags= selectedChip.text as String
                }
                else{
                    viewModel.selecteddinnerTags=null
                }
            }
            dinnerChipGroup.addView(chip)
        }

        for (tag in viewModel.breakFastTags) {
            val chip = Chip(requireContext())
            chip.text = tag
            chip.isCheckable = true
            chip.isClickable = true
            chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    val selectedChip = Chip(requireContext())
                    selectedChip.text = tag
                    selectedChip.isCloseIconVisible = true
                    selectedChip.setOnCloseIconClickListener {
                        selectedChip.isChecked = false
                    }
                    viewModel.selectedbreakFastTags= selectedChip.text as String
                }
                else{
                    viewModel.selectedbreakFastTags=null
                }
            }
            breakFastChipGroup.addView(chip)
        }

        for (tag in viewModel.launchTags) {
            val chip = Chip(requireContext())
            chip.text = tag
            chip.isCheckable = true
            chip.isClickable = true
            chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    val selectedChip = Chip(requireContext())
                    selectedChip.text = tag
                    selectedChip.isCloseIconVisible = true
                    selectedChip.setOnCloseIconClickListener {
                        selectedChip.isChecked = false
                    }
                    viewModel.selectedlaunchTags= selectedChip.text as String
                }
                else{
                    viewModel.selectedlaunchTags=null
                }
            }
            launchChipGroup.addView(chip)
        }


        startDatePicker.setOnClickListener {
            MonthYearPickerDialog().also {
                it.setListener { _, year, month, _ ->
                    viewModel.mfgDate="${month}/${year}"
                    if(viewModel.expDate!=null)
                    {
                        if(UtilityFunction.validateMedicine(viewModel.mfgDate!!,viewModel.expDate!!))
                        {
                            startDatePicker.setText(viewModel.mfgDate)
                            binding.fragmentMedicineStartdateTextInputLayout.helperText=null
                        }
                        else{
                            viewModel.mfgDate=null
                            startDatePicker.setText("")
                            binding.fragmentMedicineStartdateTextInputLayout.helperText="Invalid"
                        }
                    }
                    else{
                        startDatePicker.setText(viewModel.mfgDate)
                        binding.fragmentMedicineStartdateTextInputLayout.helperText=null
                    }
                }
                it.show(requireFragmentManager(), "MonthYearPickerDialog")
            }
            //mfgDatePicker()
        }
        expiryDatePicker.setOnClickListener {
            MonthYearPickerDialog().also {
                it.setListener { _, year, month, _ ->
                    viewModel.expDate = "${month}/${year}"
                    if(viewModel.mfgDate!=null)
                    {
                        if(UtilityFunction.validateMedicine(viewModel.mfgDate!!,viewModel.expDate!!))
                        {
                            expiryDatePicker.setText(viewModel.expDate)
                            binding.fragmentMedicineExpirydateTextInputLayout.helperText=null
                        }
                        else{
                            viewModel.expDate=null
                            expiryDatePicker.setText("")
                            binding.fragmentMedicineExpirydateTextInputLayout.helperText="Invalid"
                        }
                    }
                    else{
                        expiryDatePicker.setText(viewModel.expDate)
                        binding.fragmentMedicineExpirydateTextInputLayout.helperText=null
                    }
                }
                it.show(requireFragmentManager(), "MonthYearPickerDialog")
            }
            /*expiryDatePickerDialog()*/
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAddMedicineBinding.bind(view)

        /*homeActivity.getToolbarMenuLayout().visibility = View.GONE*/



        binding.apply {
            medicineImage.setOnClickListener {
                findNavController().navigate(R.id.OCRFragment)
            }
            fragmentMedicineNameTextInputEditText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    viewModel.medName = s.toString()
                    if(viewModel.medName=="")
                    {
                        fragmentMedicineNameTextInputLayout.helperText="Required"
                    }
                    else if(viewModel.medName!!.validate(ListPattern.getMedNameRegex()))
                    {
                        fragmentMedicineNameTextInputLayout.helperText="Invalid"
                    }
                    else {
                        // Input is valid, clear the error
                        fragmentMedicineNameTextInputLayout.helperText=null
                        medName = fragmentMedicineNameTextInputEditText.text.toString()
                    }
                }
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun afterTextChanged(s: Editable?) {}
            })

            fragmentDosageInfoTextInputEditText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    viewModel.dosage = s.toString()
                    if(viewModel.dosage=="")
                    {
                        fragmentDosageInfoTextInputLayout.helperText="Required"
                    }
                    else if (viewModel.dosage!!.validate(ListPattern.getDosageRegex()))
                    {
                        fragmentDosageInfoTextInputLayout.helperText="Include"
                    }
                    else {
                        // Input is valid, clear the error
                        fragmentDosageInfoTextInputLayout.helperText=null
                        dosage = fragmentDosageInfoTextInputEditText.text.toString()
                    }
                }
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun afterTextChanged(s: Editable?) {}
            })

            viewModel.medFreq=MedicineFrequency.DAILY
        }

        /*val medicineData = MedicineInfo()


        db.collection("user_medicines").document(MediTrackUserReference.getUserId()).collection("medicine_data")
            .add(medicineData)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }*/
    }


    /*fun expiryDatePickerDialog() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Handle the selected date here
                viewModel.expDate = "${selectedMonth}/${selectedYear}"
                if(viewModel.mfgDate!=null)
                {
                    if(UtilityFunction.validateMedicine(viewModel.mfgDate!!,viewModel.mfgDate!!))
                    {
                        expiryDatePicker.setText(viewModel.expDate)
                        binding.fragmentMedicineExpirydateTextInputLayout.helperText=null
                    }
                    else{
                        viewModel.expDate=null
                        binding.fragmentMedicineExpirydateTextInputLayout.helperText="Invalid"
                    }
                }

            },
            year,
            month,
            day
        )

        // Optional: Set a minimum date if needed
        // datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        datePickerDialog.show()
    }*/
    /*fun mfgDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Handle the selected date here
                viewModel.mfgDate = "${selectedMonth}/${selectedYear}"
                startDatePicker.setText(viewModel.mfgDate)
            },
            year,
            month,
            day
        )

        // Optional: Set a minimum date if needed
        // datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        datePickerDialog.show()
    }*/

}