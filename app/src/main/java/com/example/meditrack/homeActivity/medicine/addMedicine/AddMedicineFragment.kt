package com.example.meditrack.homeActivity.medicine.addMedicine

import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.adapter.MedicineTypeItemAdapter
import com.example.meditrack.dataModel.api.ApiInstance
import com.example.meditrack.dataModel.dataClasses.MedicineData
import com.example.meditrack.dataModel.enumClasses.medicine.MedicineFrequency
import com.example.meditrack.dataModel.enumClasses.medicine.MedicineTimeOfDayType1
import com.example.meditrack.dataModel.enumClasses.medicine.MedicineType
import com.example.meditrack.databinding.FragmentAddMedicineBinding
import com.example.meditrack.firebase.FBase
import com.example.meditrack.homeActivity.reminder.notification.MedicineReminderDialog
import com.example.meditrack.regularExpression.ListPattern
import com.example.meditrack.regularExpression.MatchPattern.Companion.validate
import com.example.meditrack.utility.UtilsFunctions
import com.example.meditrack.utility.UtilsFunctions.Companion.getCurrentDate
import com.example.meditrack.utility.UtilsFunctions.Companion.getCurrentTime
import com.example.meditrack.utility.UtilsFunctions.Companion.showToast
import com.example.meditrack.utility.UtilsFunctions.Companion.stringNormalize
import com.example.meditrack.utility.UtilsFunctions.Companion.toBitmap
import com.example.meditrack.utility.UtilsFunctions.Companion.toUri
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import com.example.meditrack.utility.ownDialogs.MonthYearPickerDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.CollectionReference
import com.google.gson.JsonElement
import com.google.gson.JsonIOException
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class AddMedicineFragment : Fragment() {

    companion object {
        fun newInstance() = AddMedicineFragment()
    }

    private lateinit var viewModel: AddMedicineViewModel
    private lateinit var binding: FragmentAddMedicineBinding
    //private val calendar = Calendar.getInstance()
    private val tAG = "AddMedicineFragment"
    /*private lateinit var homeActivity: HomeActivity*/
    //private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var hiddenView: LinearLayout
    private lateinit var cardView: CardView
    private var medicineData: MedicineData?=null
    private lateinit var medicineDataDocRef:CollectionReference


    /*override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure that the parent activity is HomeActivity
        if (context is HomeActivity) {
            homeActivity = context
        } else {
            throw IllegalStateException("Parent activity must be HomeActivity")
        }
    }*/

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
        val view = inflater.inflate(R.layout.fragment_add_medicine, container, false)
        binding = FragmentAddMedicineBinding.bind(view)

//        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this)[AddMedicineViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        medicineDataDocRef = FBase.getUsersMedicineDataCollection()

        /*val autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.fragment_week_days_TextInputEditText)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, viewModel.weekDayItems)
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedWeekDayItem = viewModel.weekDayItems[position]
            binding.fragmentWeekDaysTextInputLayout.helperText=null
        }*/




        /*progressDialog.start("Loading......")
        MainScope().launch(Dispatchers.IO) {
            val response = ApiInstance.api.insertDocument("Cetrizine")
            response.enqueue(object : Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    progressDialog.stop()
                    val res = response.body()!!.asJsonObject
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setCancelable(true)
                    builder.setMessage(res.toString())
                    builder.setTitle("Response Success")
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.show()
                }

                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    progressDialog.stop()
                    Log.i("onFailure: ",t.message.toString())
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setCancelable(true)
                    builder.setMessage(t.message)
                    builder.setTitle("Error !")
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.show()

                }

            })
        }*/

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardView = binding.additionalView
        hiddenView = binding.hiddenView


        binding.apply {
            fillChipGroup()
            try{
                // Get the Bundle from arguments
                val bundle = arguments

                val mediName = bundle!!.getString("ocrMedNameString")
                val scanImageByteArray = bundle.getByteArray("scanImageByteArray")
                viewModel.selectedMedNameText = mediName
                viewModel.bimapMedImage = scanImageByteArray!!.toBitmap()
                if(viewModel.selectedMedNameText!=null && viewModel.selectedMedNameText != "")
                {
                    viewModel.medName = viewModel.selectedMedNameText
                    fragmentMedicineNameTextInputEditText.setText(viewModel.medName)
                    fragmentMedicineNameTextInputLayout.helperText=null

                }
                medicineImage.setImageBitmap(viewModel.bimapMedImage)

            }catch(e:Exception){
                viewModel.medName = null
                e.printStackTrace()
            }



            /*homeActivity.getToolbarMenuLayout().visibility = View.GONE*/


            loadTypeInfoDialog()


            fragmentMedicineStartdateTextInputEditText.setOnClickListener {
                MonthYearPickerDialog().also {
                    it.setListener { _, year, month, _ ->
                        viewModel.mfgDate="${month}/${year}"
                        if(viewModel.expDate!=null)
                        {
                            if(UtilsFunctions.validateMedicine(viewModel.mfgDate!!,viewModel.expDate!!))
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
                    it.show(childFragmentManager, "MonthYearPickerDialog")
                }
                //mfgDatePicker()
            }

            fragmentMedicineExpirydateTextInputEditText.setOnClickListener {
                MonthYearPickerDialog().also {
                    it.setListener { _, year, month, _ ->
                        viewModel.expDate = "${month}/${year}"
                        if(viewModel.mfgDate!=null)
                        {
                            if(UtilsFunctions.validateMedicine(viewModel.mfgDate!!,viewModel.expDate!!) && (viewModel.mfgDate!=viewModel.expDate))
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
                    it.show(childFragmentManager, "MonthYearPickerDialog")
                }
                /*expiryDatePickerDialog()*/
            }

            fixedLayout.setOnClickListener {
                showAdditionalDetails()
            }

            arrowButton.setOnClickListener {
                showAdditionalDetails()
            }

            medicineImage.setOnClickListener {
                findNavController().navigate(R.id.OCRFragment)
            }

            fragmentMedicineNameTextInputEditText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    viewModel.medName = s.toString()
                    Log.i(tAG,viewModel.medName.toString())
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

            btnCorrectSpell.setOnClickListener {
                if (viewModel.medName!="" && viewModel.medName!=null)
                {
                    progressDialog.start("Checking Spell...")
                    MainScope().launch(Dispatchers.IO) {
                        try{
                            val response = ApiInstance.api.spellCorrect(viewModel.medName)
                            response.enqueue(object : Callback<JsonElement> {
                                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                                    try {
                                        progressDialog.stop()
                                        val resJsonObject = response.body()!!.asJsonObject
                                        Log.i("JSOnDATA","$resJsonObject")
                                        if(resJsonObject.has("success"))
                                        {
                                            fragmentMedicineNameTextInputEditText.setText(resJsonObject.get("success").asString)
                                            viewModel.medName = resJsonObject.get("success").asString
                                        }
                                    }
                                    catch (e: JsonSyntaxException) {
                                        requireContext().showToast("JSON syntax error")
                                    } catch (e: JsonIOException) {
                                        requireContext().showToast("JSON I/O error")
                                    } catch (e: JsonParseException) {
                                        requireContext().showToast("JSON parsing error")
                                    }
                                    catch (e:NullPointerException){
                                        requireContext().showToast("server unavailable or respond null")
                                    }
                                    catch (e: Exception) {
                                        Log.e("JSOnDATA", "Error processing the JSON response", e)
                                        requireContext().showToast("An unexpected error occurred")
                                    }
                                }

                                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                                    progressDialog.stop()
                                    Log.i("onFailure: ",t.message.toString())
                                }

                            })
                        }
                        catch (e: IOException) {
                            if(isNetworkAvailable(requireContext()))
                            {
                                withContext(Dispatchers.Main)
                                {
                                    requireContext().showToast("server unavailable")
                                }

                            }
                            else{
                                withContext(Dispatchers.Main)
                                {
                                    requireContext().showToast("Network issue")
                                }

                            }

                        } catch (e: Exception) {
                            withContext(Dispatchers.Main)
                            {
                                requireContext().showToast("An unexpected error occurred")
                            }
                        }
                    }
                }

            }

            fragmentMedicineAddButton.setOnClickListener {
                //clearForm()
                MainScope().launch(Dispatchers.IO) {
                    if(validateInput())
                    {
                        if(viewModel.bimapMedImage==null){
                            Toast.makeText(requireContext(),"Please add Image of Drug or Medicine",Toast.LENGTH_SHORT).show()
                            return@launch
                        }
//                        Log.i("MedicineData","Medicine Name : ${viewModel.medName}")

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
                        }
                        else{
                            medicineTime = viewModel.selectedMedicineTimeOfDayType1
                            viewModel.selectedMedicineTimeOfDayType1.forEach {
                                Log.i("MedicineData", "Medicine Time of Day Type 1 : ${it.description}")
                            }
                        }
                        viewModel.weekDayItems.forEach {
                            Log.i("MedicineData", "Medicine Week Day : ${it.name}")
                        }

                        val imageUri = viewModel.bimapMedImage!!.toUri(requireContext())
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
                        val docName = stringNormalize(viewModel.medName!!)
                        try{
                            val insertDocumentFlaskPython = object : Callback<JsonElement>{
                                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                                    progressDialog.stop()
                                    requireContext().showToast("Your Medicine added successfully")
                                    /*try {
                                        progressDialog.stop()
                                        val res = response.body()!!.asJsonObject
                                        var toastMessage = ""
                                        if(res.has("success") || res.has("info"))
                                        {
                                            toastMessage = "Success"
                                        }
                                        else if(res.has("error"))
                                        {
                                            toastMessage = res.get("error").toString()
                                        }
                                        clearForm()
                                        Toast.makeText(requireContext(),toastMessage,Toast.LENGTH_SHORT).show()
                                        findNavController().navigate(R.id.homeFragment)
                                    }
                                    catch (ex:Exception){
                                        requireContext().showToast("Your Medicine added successfully, (No worry) Error at server side")
                                    }*/
                                }

                                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                                    progressDialog.stop()
                                    requireContext().showToast("Your Medicine added successfully")
                                    Log.i("onFailure: ",t.message.toString())
                                    //Toast.makeText(requireContext(),"Request Error",Toast.LENGTH_SHORT).show()
                                }

                            }
                            val insertMedicineDataFirebaseCallback = object : InsertMedicineDataFirebaseCallback{
                                override fun onSuccess() {
                                    MainScope().launch(Dispatchers.IO) {
                                        try {
                                            val dialog = MedicineReminderDialog(requireActivity())
                                            withContext(Dispatchers.Main)
                                            {
                                                //progressDialog.stop()
                                                //Toast.makeText(requireContext(),"Medicine and Reminder Successfully Added",Toast.LENGTH_SHORT).show()
                                                Log.d(tAG, "DocumentSnapshot added with ID: $docName")

                                                //progressDialog.start("Server Increase Medicine Dataset...")
                                            }
                                            launch {
                                                val response = ApiInstance.api.insertDocument(medicineData!!.medName)
                                                response.enqueue(insertDocumentFlaskPython)

                                            }
                                            val takeTimeList: ArrayList<MedicineTimeOfDayType1>? = medicineData?.takeTime as? ArrayList<MedicineTimeOfDayType1>
                                            // Medicine Taking Reminder
                                            if(takeTimeList!=null)
                                            {
                                                for(item in takeTimeList)
                                                {
                                                    val hour = item.time.split(":")
                                                    Log.i(tAG, "onUploadSuccess: ${hour[0]}")
                                                    if(medicineData!!.medFreq == MedicineFrequency.DAILY) {
                                                        dialog.scheduleNotifications(medicineData!!.medName, mutableListOf(0, 1, 2, 3, 4, 5, 6),  hour[0].toInt(), 0)
                                                    }else{
                                                        dialog.scheduleNotifications(medicineData!!.medName, medicineData!!.weekDay as MutableList<Int>,  hour[0].toInt(), 0)
                                                    }
                                                }

                                            }
                                        }
                                        catch (ex:Exception)
                                        {
//                                            requireContext().showToast("Unexpected Error")
                                            Log.e("addOnSuccessListener","$ex")
                                        }

                                    }

                                }

                                override fun onFailure(exception: Exception) {
                                    progressDialog.stop()
                                    Toast.makeText(requireContext(),"addOnFailureListener: Error",Toast.LENGTH_SHORT).show()
                                    Log.w(tAG, "Error adding document", exception)
                                }

                            }
                            val imageUploadCallback = object : UploadCallback{
                                override fun onUploadSuccess(downloadUrl: String) {
                                    MainScope().launch(Dispatchers.IO) {
                                        try {
                                            medicineData = MedicineData(
                                                medImage = downloadUrl,
                                                medName = viewModel.medName!!.trim(),
                                                medicineType = viewModel.selectedMedTypeTags!!,
                                                dosage = viewModel.dosage!!,
                                                mfgDate = viewModel.mfgDate!!,
                                                expDate = viewModel.expDate!!,
                                                medFreq = viewModel.selectedFreqTags!!,
                                                weekDay = viewModel.selectedWeekDayItem,
                                                takeTime = medicineTime,
                                                instruction = viewModel.medInstruction.toString().trim(),
                                                doctorName = viewModel.doctorName.toString().trim(),
                                                doctorContact = viewModel.doctorContact.toString().trim(),
                                                notes = viewModel.medNotes.toString().trim(),
                                                totalQuantity = viewModel.medQuantity!!,
                                                mediDeleted = "No",
                                                getCurrentDate(),
                                                getCurrentTime()
                                            )


                                            medicineDataDocRef.document(docName).set(medicineData!!)
                                                .addOnSuccessListener {
                                                    insertMedicineDataFirebaseCallback.onSuccess()
                                                }
                                                .addOnFailureListener { e ->
                                                    insertMedicineDataFirebaseCallback.onFailure(e)
                                                }
                                        }
                                        catch (ex:java.lang.Exception)
                                        {
                                            Log.w("$tAG : uploadImageToFirebaseStorage", "Error uploadImageToFirebaseStorage", ex)
                                        }
                                    }

                                }

                                override fun onUploadFailure(exception: Exception) {
                                    progressDialog.stop()
                                    Log.w("$tAG : onUploadFailure", "Error adding document", exception)
                                    Toast.makeText(requireContext(),"onUploadFailure: Error",Toast.LENGTH_SHORT).show()
                                }

                            }
                            val medicineExistsFirebaseCallback = object : MedicineExistsFirebaseCallback{
                                override fun onSuccess(status: Boolean) {
                                    if(status)
                                    {
                                        uploadImageToFirebaseStorage(imageUri, imageUploadCallback)
                                    }
                                    else{
                                        Toast.makeText(requireContext(),"Medicine Already Exists",Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onFailure(exception: Exception) {}
                            }


                            medicineDataDocRef.document(docName).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        progressDialog.stop()
                                        // Document with the same custom name already exists, skip insertion
                                        // Handle this case if needed
                                        medicineExistsFirebaseCallback.onSuccess(false)

                                    } else {
                                        medicineExistsFirebaseCallback.onSuccess(true)

                                    }
                                }
                                .addOnFailureListener { e ->
                                    medicineExistsFirebaseCallback.onFailure(e)
                                }
                        }
                        catch (ex:Exception)
                        {
                            requireContext().showToast("Unexpected Error")
                        }



                        /*db.collection("users_data").document(MediTrackUserReference.getUserId()).collection("medicine_data")
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
        }
    }

    interface MedicineExistsFirebaseCallback{
        fun onSuccess(status:Boolean)
        fun onFailure(exception: Exception)
    }

    interface  InsertMedicineDataFirebaseCallback{
        fun onSuccess()
        fun onFailure(exception: Exception)
    }

    // Callback interface for upload result
    interface UploadCallback {
        fun onUploadSuccess(downloadUrl: String)
        fun onUploadFailure(exception: Exception)
    }

    private fun loadTypeInfoDialog()
    {
        binding.apply {
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

    private fun showAdditionalDetails()
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


    private fun fillChipGroup(){
        binding.apply {
            for (tag in viewModel.weekDayItems) {
                val chip = Chip(requireContext())
                chip.text = tag.name
                chip.isCheckable = true
                chip.isClickable = true
                chip.isChecked = viewModel.selectedWeekDayItem.contains(tag)
                chip.setOnCheckedChangeListener { _, isChecked ->
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
                chip.isChecked = tag == viewModel.selectedFreqTags
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        val selectedChip = Chip(requireContext())
                        selectedChip.text = tag.name
                        selectedChip.isCloseIconVisible = true
                        selectedChip.setOnCloseIconClickListener {
                            selectedChip.isChecked = false
                        }
                        viewModel.selectedFreqTags = tag
                        if(viewModel.selectedFreqTags == MedicineFrequency.WEEKLY)
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
                        viewModel.selectedFreqTags=null
                        weekDaysChipGroupLayout.visibility = View.GONE
                        //binding.fragmentWeekDaysTextInputLayout.visibility=View.GONE
                    }
                    //Toast.makeText(requireContext(),"${viewModel.selectedfreqTags}",Toast.LENGTH_SHORT).show()
                }
                freqChipGroup.addView(chip)
            }

            if(viewModel.selectedFreqTags == MedicineFrequency.WEEKLY)
            {
                weekDaysChipGroupLayout.visibility = View.VISIBLE
                //binding.fragmentWeekDaysTextInputLayout.visibility=View.VISIBLE
            }
            else{
                weekDaysChipGroupLayout.visibility = View.GONE
                //binding.fragmentWeekDaysTextInputLayout.visibility=View.GONE
            }

            for (tag in viewModel.dinnerTags) {
                val chip = Chip(requireContext())
                chip.text = tag.description
                chip.isCheckable = true
                chip.isClickable = true
                chip.isChecked = viewModel.selectedMedicineTimeOfDayType1.contains(tag)
                chip.setOnCheckedChangeListener { _, isChecked ->
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
                chip.isChecked = viewModel.selectedMedicineTimeOfDayType1.contains(tag)
                chip.setOnCheckedChangeListener { _, isChecked ->
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
                chip.isChecked = viewModel.selectedMedicineTimeOfDayType1.contains(tag)
                chip.setOnCheckedChangeListener { _, isChecked ->
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
                chip.isChecked = viewModel.selectedMedTypeTags == tag
                chip.setOnCheckedChangeListener { _, isChecked ->
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
                chip.isChecked = viewModel.selectedMedicineTimeOfDayType2.contains(tag)
                chip.setOnCheckedChangeListener { _, isChecked ->
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
        }
    }

    fun clearForm()
    {
        binding.apply {
            viewModel.medName = null
            viewModel.medImage = null
            viewModel.bimapMedImage = null
            viewModel.medInstruction = null
            viewModel.medNotes = null
            viewModel.dosage = null
            viewModel.medQuantity = null
            viewModel.mfgDate = null
            viewModel.expDate = null
            viewModel.doctorName = null
            viewModel.doctorContact = null
            viewModel.selectedFreqTags = null
            viewModel.selectedMedicineTimeOfDayType1 = ArrayList()
            viewModel.selectedMedicineTimeOfDayType2 = ArrayList()
            viewModel.selectedMedTypeTags = null
            viewModel.selectedWeekDayItem = ArrayList()

            MainScope().launch {
                fragmentMedicineNameTextInputEditText.setText("")
                fragmentDosageInfoTextInputEditText.setText("")
                fragmentMedicineStartdateTextInputEditText.setText("")
                fragmentMedicineExpirydateTextInputEditText.setText("")
                fragmentMedicineQuantityTextInputEditText.setText("")
                fragmentDoctorContactTextInputEditText.setText("")
                fragmentDoctorNameTextInputEditText.setText("")
                fragmentSpecialInstructionTextInputEditText.setText("")
                fragmentNotesOrCommentsTextInputEditText.setText("")
                //val drawableResourceId = resources.getIdentifier("round_medication_24", "drawable", requireContext().packageName)
                val drawableResourceId = R.drawable.round_medication_24
                medicineImage.setImageResource(drawableResourceId)

                /*for (i in 0 until medTypeChipGroup.childCount) {
                    val childView = medTypeChipGroup.getChildAt(i)

                    // Check if the child view is a Chip
                    if (childView is Chip) {
                        // Uncheck the chip
                        if(childView.isChecked)
                        {
                            childView.isChecked = false
                        }
                    }
                }

                for (i in 0 until freqChipGroup.childCount) {
                    val childView = freqChipGroup.getChildAt(i)

                    // Check if the child view is a Chip
                    if (childView is Chip) {
                        // Uncheck the chip
                        if(childView.isChecked)
                        {
                            childView.isChecked = false

                        }
                    }
                }

                for (i in 0 until weekDaysChipGroup.childCount) {
                    val childView = weekDaysChipGroup.getChildAt(i)

                    // Check if the child view is a Chip
                    if (childView is Chip) {
                        // Uncheck the chip
                        if(childView.isChecked)
                        {
                            childView.isChecked = false
                        }
                    }
                }


                for (i in 0 until breakFastChipGroup.childCount) {
                    val childView = breakFastChipGroup.getChildAt(i)

                    // Check if the child view is a Chip
                    if (childView is Chip) {
                        // Uncheck the chip
                        if(childView.isChecked)
                        {
                            childView.isChecked = false
                        }
                    }
                }

                for (i in 0 until launchChipGroup.childCount) {
                    val childView = launchChipGroup.getChildAt(i)

                    // Check if the child view is a Chip
                    if (childView is Chip) {
                        // Uncheck the chip
                        if(childView.isChecked)
                        {
                            childView.isChecked = false
                        }
                    }
                }

                for (i in 0 until dinnerChipGroup.childCount) {
                    val childView = dinnerChipGroup.getChildAt(i)

                    // Check if the child view is a Chip
                    if (childView is Chip) {
                        // Uncheck the chip
                        if(childView.isChecked)
                        {
                            childView.isChecked = false
                        }
                    }
                }


                for (i in 0 until medicineTimeofDayType2ChipGroup.childCount) {
                    val childView = medicineTimeofDayType2ChipGroup.getChildAt(i)

                    // Check if the child view is a Chip
                    if (childView is Chip) {
                        // Uncheck the chip
                        if(childView.isChecked)
                        {
                            childView.isChecked = false
                        }
                    }
                }*/

                // Launch coroutines for each ChipGroup in parallel
                val jobMedType = async { uncheckChipsInGroup(medTypeChipGroup) }
                val jobFreq = async { uncheckChipsInGroup(freqChipGroup) }
                val jobWeekDays = async { uncheckChipsInGroup(weekDaysChipGroup) }
                val jobBreakfast = async { uncheckChipsInGroup(breakFastChipGroup) }
                val jobLunch = async { uncheckChipsInGroup(launchChipGroup) }
                val jobDinner = async { uncheckChipsInGroup(dinnerChipGroup) }
                val jobMedicineTimeOfDay = async { uncheckChipsInGroup(medicineTimeofDayType2ChipGroup) }

                // Wait for all coroutines to finish
                jobMedType.await()
                jobFreq.await()
                jobWeekDays.await()
                jobBreakfast.await()
                jobLunch.await()
                jobDinner.await()
                jobMedicineTimeOfDay.await()

            }

        }
    }

    suspend fun uncheckChipsInGroup(chipGroup: ChipGroup) = coroutineScope {
        val jobs = (0 until chipGroup.childCount).map { i ->
            async(Dispatchers.Default) {
                val childView = chipGroup.getChildAt(i)
                if (childView is Chip && childView.isChecked) {
                    withContext(Dispatchers.Main)
                    {
                        childView.isChecked = false
                    }

                }
            }
        }
        jobs.awaitAll()
    }


    // Function to upload an image to Firebase Storage and provide the result via callback
    fun uploadImageToFirebaseStorage(imageUri: Uri, callback: UploadCallback) {
        val storageRef = FBase.getStorageReference()
        val imagesRef = storageRef.child("user_medicine_images/${FBase.getUserId()}/${stringNormalize(viewModel.medName!!)}")
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

    private fun validateInput():Boolean{
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
                (viewModel.selectedFreqTags== MedicineFrequency.DAILY || (viewModel.selectedFreqTags== MedicineFrequency.WEEKLY && viewModel.selectedWeekDayItem.isNotEmpty()))
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnectedOrConnecting == true
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