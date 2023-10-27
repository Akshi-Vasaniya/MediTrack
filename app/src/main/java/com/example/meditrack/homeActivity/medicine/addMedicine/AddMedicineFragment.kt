package com.example.meditrack.homeActivity.medicine.addMedicine

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.adapter.MedicineTypeItemAdapter
import com.example.meditrack.dataModel.dataClasses.MedicineData
import com.example.meditrack.dataModel.enumClasses.medicine.MedicineFrequency
import com.example.meditrack.dataModel.enumClasses.medicine.MedicineTimeOfDayType1
import com.example.meditrack.dataModel.enumClasses.medicine.MedicineTimeOfDayType2
import com.example.meditrack.dataModel.enumClasses.medicine.MedicineType
import com.example.meditrack.databinding.FragmentAddMedicineBinding
import com.example.meditrack.firebase.fBase
import com.example.meditrack.regularExpression.ListPattern
import com.example.meditrack.regularExpression.MatchPattern.Companion.validate
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import com.example.meditrack.utility.ownDialogs.MonthYearPickerDialog
import com.example.meditrack.utility.UtilityFunction
import com.example.meditrack.utility.UtilityFunction.Companion.bitmapToUri
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import com.example.meditrack.homeActivity.medicine.addMedicine.AddMedicineFragment
import com.example.meditrack.homeActivity.reminder.notification.MedicineReminderDialog
import com.example.meditrack.utility.UtilityFunction.Companion.getCurrentDate
import com.example.meditrack.utility.UtilityFunction.Companion.getCurrentTime


class AddMedicineFragment : Fragment() {

    companion object {
        fun newInstance() = AddMedicineFragment()
    }

    private lateinit var viewModel: AddMedicineViewModel
    private lateinit var binding: FragmentAddMedicineBinding
    private val calendar = Calendar.getInstance()
    private val db = fBase.getFireStoreInstance()
    private val TAG = "AddMedicineFragment"
    /*private lateinit var homeActivity: HomeActivity*/
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var hiddenView: LinearLayout
    private lateinit var cardView: CardView
    private var medicineData: MedicineData?=null


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
        //val view = inflater.inflate(R.layout.fragment_add_medicine, container, false)
        binding = FragmentAddMedicineBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[AddMedicineViewModel::class.java]


        /*val autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.fragment_week_days_TextInputEditText)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, viewModel.weekDayItems)
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedWeekDayItem = viewModel.weekDayItems[position]
            binding.fragmentWeekDaysTextInputLayout.helperText=null
        }*/

        binding.apply {
            for (tag in viewModel.weekDayItems) {
                val chip = Chip(requireContext())
                chip.text = tag.name
                chip.isCheckable = true
                chip.isClickable = true
                chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                    if (isChecked) {
                        val selectedChip = Chip(requireContext())
                        selectedChip.text = tag.name
                        selectedChip.isCloseIconVisible = true
                        selectedChip.setOnCloseIconClickListener {
                            selectedChip.isChecked = false
                        }
                        viewModel.selectedWeekDayItem.add(tag)
                    }
                    else{
                        try{
                            viewModel.selectedWeekDayItem.remove(tag)
                        }
                        catch (ex:Exception)
                        {
                            Log.e("ADDMedicine","${ex.message}")
                        }
                    }
                }
                weekDaysChipGroup.addView(chip)
            }

            for (tag in viewModel.freqTags) {
                val chip = Chip(requireContext())
                chip.text = tag.name
                chip.isCheckable = true
                chip.isClickable = true
                chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                    if (isChecked) {
                        val selectedChip = Chip(requireContext())
                        selectedChip.text = tag.name
                        selectedChip.isCloseIconVisible = true
                        selectedChip.setOnCloseIconClickListener {
                            selectedChip.isChecked = false
                        }
                        viewModel.selectedfreqTags = tag
                        if(viewModel.selectedfreqTags == MedicineFrequency.WEEKLY)
                        {
                            weekDaysChipGroupLayout.visibility = View.VISIBLE
                            //binding.fragmentWeekDaysTextInputLayout.visibility=View.VISIBLE
                        }
                        else{
                            weekDaysChipGroupLayout.visibility = View.GONE
                            //binding.fragmentWeekDaysTextInputLayout.visibility=View.GONE
                        }
                    }
                    else{
                        viewModel.selectedfreqTags=null
                        weekDaysChipGroupLayout.visibility = View.GONE
                        //binding.fragmentWeekDaysTextInputLayout.visibility=View.GONE
                    }
                    //Toast.makeText(requireContext(),"${viewModel.selectedfreqTags}",Toast.LENGTH_SHORT).show()
                }
                freqChipGroup.addView(chip)
            }

            for (tag in viewModel.dinnerTags) {
                val chip = Chip(requireContext())
                chip.text = tag.description
                chip.isCheckable = true
                chip.isClickable = true
                chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                    if (isChecked) {
                        val selectedChip = Chip(requireContext())
                        selectedChip.text = tag.description
                        selectedChip.isCloseIconVisible = true
                        selectedChip.setOnCloseIconClickListener {
                            selectedChip.isChecked = false
                        }
                        viewModel.selectedMedicineTimeOfDayType1.add(tag)
                    }
                    else{
                        try{
                            viewModel.selectedMedicineTimeOfDayType1.remove(tag)
                        }
                        catch (ex:Exception)
                        {
                            Log.e("ADDMedicine","${ex.message}")
                        }
                    }
                }
                dinnerChipGroup.addView(chip)
            }

            for (tag in viewModel.breakFastTags) {
                val chip = Chip(requireContext())
                chip.text = tag.description
                chip.isCheckable = true
                chip.isClickable = true
                chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                    if (isChecked) {
                        val selectedChip = Chip(requireContext())
                        selectedChip.text = tag.description
                        selectedChip.isCloseIconVisible = true
                        selectedChip.setOnCloseIconClickListener {
                            selectedChip.isChecked = false
                        }
                        viewModel.selectedMedicineTimeOfDayType1.add(tag)
                    }
                    else{
                        try{
                            viewModel.selectedMedicineTimeOfDayType1.remove(tag)
                        }
                        catch (ex:Exception)
                        {
                            Log.e("ADDMedicine","${ex.message}")
                        }
                    }
                }
                breakFastChipGroup.addView(chip)
            }

            for (tag in viewModel.launchTags) {
                val chip = Chip(requireContext())
                chip.text = tag.description
                chip.isCheckable = true
                chip.isClickable = true
                chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                    if (isChecked) {
                        val selectedChip = Chip(requireContext())
                        selectedChip.text = tag.description
                        selectedChip.isCloseIconVisible = true
                        selectedChip.setOnCloseIconClickListener {
                            selectedChip.isChecked = false
                        }
                        viewModel.selectedMedicineTimeOfDayType1.add(tag)
                    }
                    else{
                        try{
                            viewModel.selectedMedicineTimeOfDayType1.remove(tag)
                        }
                        catch (ex:Exception)
                        {
                            Log.e("ADDMedicine","${ex.message}")
                        }
                    }
                }
                launchChipGroup.addView(chip)
            }

            for (tag in viewModel.medTypeTags) {
                val chip = Chip(requireContext())
                chip.text = tag.name
                chip.isCheckable = true
                chip.isClickable = true
                chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                    if (isChecked) {
                        val selectedChip = Chip(requireContext())
                        selectedChip.text = tag.name
                        selectedChip.isCloseIconVisible = true
                        selectedChip.setOnCloseIconClickListener {
                            selectedChip.isChecked = false
                        }
                        if(tag== MedicineType.Topical || tag == MedicineType.Drops)
                        {
                            binding.medicineTimeofDayType2ChipGroup.visibility=View.VISIBLE
                            binding.breakFastChipGroup.visibility=View.GONE
                            binding.launchChipGroup.visibility=View.GONE
                            binding.dinnerChipGroup.visibility=View.GONE
                        }
                        else{
                            binding.medicineTimeofDayType2ChipGroup.visibility=View.GONE
                            binding.breakFastChipGroup.visibility=View.VISIBLE
                            binding.launchChipGroup.visibility=View.VISIBLE
                            binding.dinnerChipGroup.visibility=View.VISIBLE
                        }
                        viewModel.selectedMedTypeTags = tag
                    }
                    else{
                        viewModel.selectedMedTypeTags=null
                    }
                    //Toast.makeText(requireContext(),"${viewModel.selectedMedTypeTags}",Toast.LENGTH_SHORT).show()
                }
                medTypeChipGroup.addView(chip)
            }

            for (tag in viewModel.medicineTimeOfDayType2) {
                val chip = Chip(requireContext())
                chip.text = tag.description
                chip.isCheckable = true
                chip.isClickable = true
                chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                    if (isChecked) {
                        val selectedChip = Chip(requireContext())
                        selectedChip.text = tag.description
                        selectedChip.isCloseIconVisible = true
                        selectedChip.setOnCloseIconClickListener {
                            selectedChip.isChecked = false
                        }

                        viewModel.selectedMedicineTimeOfDayType2.add(tag)
                    }
                    else{
                        try{
                            viewModel.selectedMedicineTimeOfDayType2.remove(tag)
                        }
                        catch (ex:Exception)
                        {
                            Log.e("ADDMedicine","${ex.message}")
                        }
                    }
                }
                medicineTimeofDayType2ChipGroup.addView(chip)
            }


            fragmentMedicineStartdateTextInputEditText.setOnClickListener {
                MonthYearPickerDialog().also {
                    it.setListener { _, year, month, _ ->
                        viewModel.mfgDate="${month}/${year}"
                        if(viewModel.expDate!=null)
                        {
                            if(UtilityFunction.validateMedicine(viewModel.mfgDate!!,viewModel.expDate!!))
                            {
                                fragmentMedicineStartdateTextInputEditText.setText(viewModel.mfgDate)
                                binding.fragmentMedicineStartdateTextInputLayout.helperText=null
                            }
                            else{
                                viewModel.mfgDate=null
                                fragmentMedicineStartdateTextInputEditText.setText("")
                                binding.fragmentMedicineStartdateTextInputLayout.helperText="Invalid"
                            }
                        }
                        else{
                            fragmentMedicineStartdateTextInputEditText.setText(viewModel.mfgDate)
                            binding.fragmentMedicineStartdateTextInputLayout.helperText=null
                        }
                    }
                    it.show(requireFragmentManager(), "MonthYearPickerDialog")
                }
                //mfgDatePicker()
            }
            fragmentMedicineExpirydateTextInputEditText.setOnClickListener {
                MonthYearPickerDialog().also {
                    it.setListener { _, year, month, _ ->
                        viewModel.expDate = "${month}/${year}"
                        if(viewModel.mfgDate!=null)
                        {
                            if(UtilityFunction.validateMedicine(viewModel.mfgDate!!,viewModel.expDate!!))
                            {
                                fragmentMedicineExpirydateTextInputEditText.setText(viewModel.expDate)
                                binding.fragmentMedicineExpirydateTextInputLayout.helperText=null
                            }
                            else{
                                viewModel.expDate=null
                                fragmentMedicineExpirydateTextInputEditText.setText("")
                                binding.fragmentMedicineExpirydateTextInputLayout.helperText="Invalid"
                            }
                        }
                        else{
                            fragmentMedicineExpirydateTextInputEditText.setText(viewModel.expDate)
                            binding.fragmentMedicineExpirydateTextInputLayout.helperText=null
                        }
                    }
                    it.show(requireFragmentManager(), "MonthYearPickerDialog")
                }
                /*expiryDatePickerDialog()*/
            }
        }




        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAddMedicineBinding.bind(view)
        progressDialog= CustomProgressDialog(requireActivity())

        cardView = binding.additionalView
        hiddenView = binding.hiddenView

        // Get the Bundle from arguments
        val bundle = arguments

        // Retrieve the bitmap from the Bundle
        viewModel.bimapMedImage = bundle?.getParcelable<Bitmap>("bitmap")

        // Retrieve the ArrayList from the Bundle
        viewModel.selectedTextArray = bundle?.getSerializable("arrayList") as ArrayList<Pair<Rect,String>>?

        if(viewModel.bimapMedImage!=null)
        {
            binding.medicineImage.setImageBitmap(viewModel.bimapMedImage)
        }
        try {
            viewModel.medName=""
            viewModel.selectedTextArray!!.forEach {
                viewModel.medName += it.second
            }
            viewModel.medName = viewModel.medName!!.trim()
            viewModel.medName = viewModel.medName!!.replace("\n"," ")
            binding.fragmentMedicineNameTextInputEditText.setText(viewModel.medName)
            binding.fragmentMedicineNameTextInputLayout.helperText=null
        }catch (e:java.lang.NullPointerException)
        {
            viewModel.medName=null
            e.printStackTrace()
        }



        binding.fixedLayout.setOnClickListener {
            showAditionalDetails()
        }
        binding.arrowButton.setOnClickListener {
            showAditionalDetails()
        }

        /*homeActivity.getToolbarMenuLayout().visibility = View.GONE*/

        binding.apply {
            medicineImage.setOnClickListener {
                findNavController().navigate(R.id.OCRFragment)
            }

            fragmentMedicineNameTextInputEditText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    viewModel.medName = s.toString()
                    Log.i(TAG,viewModel.medName.toString())
                    if(viewModel.medName.toString()=="")
                    {
                        viewModel.medName=null
                        fragmentMedicineNameTextInputLayout.helperText="Required"
                    }
                    /*else if(!viewModel.medName.toString().validate(ListPattern.getMedNameRegex()))
                    {
                        viewModel.medName=null
                        fragmentMedicineNameTextInputLayout.helperText="Invalid"
                    }*/
                    else {
                        // Input is valid, clear the error
                        fragmentMedicineNameTextInputLayout.helperText=null
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

                    try {
                        viewModel.dosage = s.toString().toDouble()
                        fragmentDosageInfoTextInputLayout.helperText=null
                    } catch (e: NumberFormatException) {
                        viewModel.dosage=null
                        fragmentDosageInfoTextInputLayout.helperText="Required"
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

            fragmentMedicineQuantityTextInputEditText.addTextChangedListener(object :TextWatcher{
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    try {
                        viewModel.medQuantity = s.toString().toInt()
                        fragmentMedicineQuantityTextInputLayout.helperText=null
                    } catch (e: NumberFormatException) {
                        viewModel.medQuantity=null
                        fragmentMedicineQuantityTextInputLayout.helperText="Required"
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

            fragmentDoctorNameTextInputEditText.addTextChangedListener(object :TextWatcher{
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    viewModel.doctorName = s.toString()
                    if(viewModel.doctorName=="")
                    {
                        viewModel.doctorName=null
                    }
                    else if(!viewModel.doctorName!!.validate(ListPattern.getDoctorNameRegex())){
                        viewModel.doctorName=null
                        fragmentDoctorNameTextInputLayout.helperText="Invalid Name"
                    }
                    else{
                        fragmentDoctorNameTextInputLayout.helperText=null
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

            fragmentDoctorContactTextInputEditText.addTextChangedListener(object:TextWatcher{
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    viewModel.doctorContact = s.toString()
                    if(viewModel.doctorContact=="")
                    {
                        viewModel.doctorContact=null
                    }
                    else if(!viewModel.doctorContact!!.validate(ListPattern.getDoctorContactRegex())){
                        viewModel.doctorContact=null
                        fragmentDoctorContactTextInputLayout.helperText="Invalid Name"
                    }
                    else{
                        fragmentDoctorContactTextInputLayout.helperText=null
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

            fragmentNotesOrCommentsTextInputEditText.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    viewModel.medNotes = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {}

            })

            fragmentSpecialInstructionTextInputEditText.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    viewModel.medInstruction = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            fragmentMedicineAddButton.setOnClickListener {
                MainScope().launch(Dispatchers.IO) {
                    if(validateInput())
                    {
                        if(viewModel.bimapMedImage==null){
                            Toast.makeText(requireContext(),"Please add Image of Drug or Medicine",Toast.LENGTH_SHORT).show()
                            return@launch
                        }
//                        Log.i("MedicineData","Medicine Name : ${viewModel.medName}")
//                        Log.i("MedicineData","Medicine Dosage : ${viewModel.dosage}")
//                        Log.i("MedicineData","Medicine Mfg. Date : ${viewModel.mfgDate}")
//                        Log.i("MedicineData","Medicine Exp. Date : ${viewModel.expDate}")
//                        Log.i("MedicineData", "Medicine Frequency : ${viewModel.selectedfreqTags!!.name}")

                        val medicineTime: Any?

                        if(viewModel.selectedMedicineTimeOfDayType1.isEmpty())
                        {
                            if(viewModel.selectedMedicineTimeOfDayType2.isEmpty())
                            {
                                Toast.makeText(requireContext(),"Please select medicine time",Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            medicineTime = viewModel.selectedMedicineTimeOfDayType2
                            viewModel.selectedMedicineTimeOfDayType2.forEach {
                                Log.i("MedicineData", "Medicine Time of Day Type 2 : ${it.description}")
                            }
                        }else{
                            medicineTime = viewModel.selectedMedicineTimeOfDayType1
                            viewModel.selectedMedicineTimeOfDayType1.forEach {
                                Log.i("MedicineData", "Medicine Time of Day Type 1 : ${it.description}")
                            }
                        }
                        viewModel.weekDayItems.forEach {
                            Log.i("MedicineData", "Medicine Week Day : ${it.name}")
                        }
                        Log.i("MedicineData","Medicine Doctor Name : ${viewModel.doctorName}")
                        Log.i("MedicineData","Medicine Doctor Contact : ${viewModel.doctorContact}")
                        Log.i("MedicineData","Medicine Instruction : ${viewModel.medInstruction}")
                        Log.i("MedicineData","Medicine Notes : ${viewModel.medNotes}")
                        Log.i("MedicineData","Medicine Quantity : ${viewModel.medQuantity}")

                        val imageUri = bitmapToUri(requireContext(),viewModel.bimapMedImage!!)
                        if(imageUri==null)
                        {
                            withContext(Dispatchers.Main)
                            {
                                Toast.makeText(requireContext(),"imageUri is NULL",Toast.LENGTH_SHORT).show()
                                return@withContext
                            }
                            return@launch
                        }


                        /*withContext(Dispatchers.Main)
                        {
                            Toast.makeText(requireContext(),"Done",Toast.LENGTH_SHORT).show()
                        }*/


                        withContext(Dispatchers.Main)
                        {
                            progressDialog.start("Loading...")
                        }
                        uploadImageToFirebaseStorage(imageUri, object : UploadCallback {
                            override fun onUploadSuccess(downloadUrl: String) {
                                try {
                                    medicineData = MedicineData(
                                        medImage = downloadUrl,
                                        medName = viewModel.medName!!,
                                        medicineType = viewModel.selectedMedTypeTags!!,
                                        dosage = viewModel.dosage!!,
                                        mfgDate = viewModel.mfgDate!!,
                                        expDate = viewModel.expDate!!,
                                        medFreq = viewModel.selectedfreqTags!!,
                                        weekDay = viewModel.selectedWeekDayItem,
                                        takeTime = medicineTime,
                                        instruction = viewModel.medInstruction.toString(),
                                        doctorName = viewModel.doctorName.toString(),
                                        doctorContact = viewModel.doctorContact.toString(),
                                        notes = viewModel.medNotes.toString(),
                                        totalQuantity = viewModel.medQuantity!!,
                                        mediDeleted = "No",
                                        getCurrentDate(),
                                        getCurrentTime()
                                    )
                                    Log.i(TAG, "onUploadSuccess: ${medicineData!!.medName}")
                                    Log.i(TAG, "onUploadSuccess: ${medicineData!!.medicineType}")
                                    Log.i(TAG, "onUploadSuccess: ${medicineData!!.medFreq}")
                                    Log.i(TAG, "onUploadSuccess: ${medicineData!!.weekDay}")
                                    Log.i(TAG, "onUploadSuccess: ${medicineData!!.takeTime}")
                                    Log.i(TAG, "onUploadSuccess: ${medicineData!!.notes}")
                                    Log.i(TAG, "onUploadSuccess: ${medicineData!!.totalQuantity}")
                                    val dialog = MedicineReminderDialog()
                                    db.collection("user_medicines").document(fBase.getUserId()).collection("medicine_data")
                                        .add(medicineData!!)
                                        .addOnSuccessListener { documentReference ->
                                            try {
                                                progressDialog.stop()
                                                Toast.makeText(requireContext(),"Medicine and Reminder Successfully Added",Toast.LENGTH_SHORT).show()
                                                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")

                                                // Medicine Taking Reminder
                                                for(item in medicineData!!.takeTime as ArrayList<MedicineTimeOfDayType1>)
                                                {
                                                    var hour = item.time.split(":")
                                                    Log.i(TAG, "onUploadSuccess: ${hour[0]}")
                                                    if(medicineData!!.medFreq == MedicineFrequency.DAILY) {
                                                        dialog.scheduleNotifications(medicineData!!.medName, mutableListOf(0, 1, 2, 3, 4, 5, 6),  hour[0].toInt(), 0)
                                                    }else{
                                                        dialog.scheduleNotifications(medicineData!!.medName, medicineData!!.weekDay as MutableList<Int>,  hour[0].toInt(), 0)
                                                    }
                                                }
                                                
                                            }
                                            catch (ex:Exception)
                                            {
                                                Log.e("addOnSuccessListener","${ex.message}")
                                            }

                                        }
                                        .addOnFailureListener { e ->
                                            progressDialog.stop()
                                            Toast.makeText(requireContext(),"addOnFailureListener: Error",Toast.LENGTH_SHORT).show()
                                            Log.w(TAG, "Error adding document", e)
                                        }
                                }
                                catch (ex:java.lang.Exception)
                                {
                                    Log.w("$TAG : uploadImageToFirebaseStorage", "Error uploadImageToFirebaseStorage", ex)
                                }
                            }

                            override fun onUploadFailure(exception: Exception) {
                                progressDialog.stop()
                                Log.w("$TAG : onUploadFailure", "Error adding document", exception)
                                Toast.makeText(requireContext(),"onUploadFailure: Error",Toast.LENGTH_SHORT).show()
                            }
                        })

                        /*db.collection("user_medicines").document(MediTrackUserReference.getUserId()).collection("medicine_data")
                            .add(medicineData)
                            .addOnSuccessListener { documentReference ->
                                progressDialog.stop()
                                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                            }
                            .addOnFailureListener { e ->
                                progressDialog.stop()
                                Log.w(TAG, "Error adding document", e)
                            }*/
                    }
                }
            }

            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.medicine_type_info_dialog_layout)

            val closeButton = dialog.findViewById<ImageButton>(R.id.closeButton)
            closeButton.setOnClickListener { dialog.dismiss() }

            val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)

            val medicineDataList = mutableListOf<Pair<String, String>>()
            val medicineTypes = MedicineType.values()
            for (type in medicineTypes) {
                val name = type.name.replace("_", " ")
                val description = type.getDescription(requireContext())
                medicineDataList.add(Pair(name, description))
            }

            val adapter = MedicineTypeItemAdapter(medicineDataList)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter

            // Add animation
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            medTypeInfo.setOnClickListener {
                dialog.show()
            }
        }
    }

    fun showAditionalDetails()
    {
        if(hiddenView.visibility == View.GONE){
            TransitionManager.beginDelayedTransition(cardView, AutoTransition())
            hiddenView.visibility = View.VISIBLE
            binding.arrowButton.setImageResource(R.drawable.round_keyboard_arrow_up_24)
        }
        else{
            TransitionManager.beginDelayedTransition(cardView, AutoTransition())
            hiddenView.visibility = View.GONE
            binding.arrowButton.setImageResource(R.drawable.round_keyboard_arrow_down_24)
        }
    }


    // Callback interface for upload result
    interface UploadCallback {
        fun onUploadSuccess(downloadUrl: String)
        fun onUploadFailure(exception: Exception)
    }

    // Function to upload an image to Firebase Storage and provide the result via callback
    fun uploadImageToFirebaseStorage(imageUri: Uri, callback: UploadCallback) {
        val storageRef = fBase.getStorageReference()
        val imagesRef = storageRef.child("images/${UUID.randomUUID()}")
        val uploadTask = imagesRef.putFile(imageUri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imagesRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                downloadUri?.let { callback.onUploadSuccess(it.toString()) }
            } else {
                // Handle failures
                task.exception?.let { callback.onUploadFailure(it) }
            }
        }
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

    fun validateInput():Boolean{
        return binding.fragmentMedicineNameTextInputLayout.helperText==null &&
                binding.fragmentMedicineStartdateTextInputLayout.helperText==null &&
                binding.fragmentMedicineExpirydateTextInputLayout.helperText==null &&
                binding.fragmentDosageInfoTextInputLayout.helperText==null &&
                binding.fragmentMedicineQuantityTextInputLayout.helperText==null &&
                viewModel.medName!=null &&
                viewModel.dosage!=null &&
                viewModel.medQuantity!=null &&
                viewModel.selectedMedTypeTags!=null &&
                (((viewModel.selectedMedTypeTags== MedicineType.Drops || viewModel.selectedMedTypeTags== MedicineType.Topical)
                        && viewModel.selectedMedicineTimeOfDayType2.isNotEmpty())
                        ||
                        ((viewModel.selectedMedTypeTags!= MedicineType.Drops && viewModel.selectedMedTypeTags!= MedicineType.Topical)
                                && viewModel.selectedMedicineTimeOfDayType1.isNotEmpty()))
                &&
                (viewModel.selectedfreqTags== MedicineFrequency.DAILY || (viewModel.selectedfreqTags== MedicineFrequency.WEEKLY && viewModel.selectedWeekDayItem.isNotEmpty()))
    }

}