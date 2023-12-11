package com.example.meditrack.homeActivity.medicine.medicineStock

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meditrack.R
import com.example.meditrack.adapter.MedicineStockItemAdapter
import com.example.meditrack.dataModel.ItemsViewModel
import com.example.meditrack.databinding.FragmentMedicineStockBinding
import com.example.meditrack.firebase.FBase
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MedicineStockFragment : Fragment() {
    private lateinit var medicineAdapter: MedicineStockItemAdapter
    private val medicineList = ArrayList<ItemsViewModel>()
    private lateinit var dialog: View
    private lateinit var medFilterDialog: View
    private lateinit var dropDown: Spinner
    private val tAG = "MedicineStockfragment"
    private lateinit var customAdapter: ArrayAdapter<String>
//    private var selectedFilter: MedicineFilter = MedicineFilter.INVENTORY
    private var currentSortType: SortType = SortType.NONE


    companion object {
        fun newInstance() = MedicineStockFragment()
    }

    private lateinit var viewModel: MedicineStockViewModel
    private lateinit var binding: FragmentMedicineStockBinding
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_medicine_stock, container, false)
        binding = FragmentMedicineStockBinding.bind(view)

        viewModel = ViewModelProvider(this)[MedicineStockViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        medicineList.clear()

        binding.innerFilterAndSortLayout.setOnClickListener {
            openFilterAndSortDialog()
        }

        // Drop Down
        val items = arrayOf("Inventory", "Expired", "Deleted")
        customAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.custom_spinner_item,
            items
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val getView = super.getView(position, convertView, parent)
                val textView = getView.findViewById<TextView>(R.id.customSpinnerItemTextView)
                textView.text = getItem(position)
                return getView
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val dropDownView = super.getDropDownView(position, convertView, parent)
                val textView = dropDownView.findViewById<TextView>(R.id.customSpinnerItemTextView)
                textView.text = getItem(position)
                return dropDownView
            }

        }
//        dropDown = view.findViewById(R.id.dropdownSpinner)
        binding.apply {
            medicineListView.layoutManager = LinearLayoutManager(requireContext())
            // On edit or delete
            updateRecyclerView(medicineList)
            // Set the dropdown layout style
            customAdapter.setDropDownViewResource(R.layout.custom_spinner_item)
            // Attach the custom adapter to the Spinner
//            dropdownSpinner.adapter = customAdapter
            addMedicineInRecycleView("INVENTORY")
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.medicine_stock_options_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_select_all -> {
                medicineAdapter.selectAll()
                //Toast.makeText(requireContext(), "Clicked Select all", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_inverse_select -> {
                medicineAdapter.inverseSelect()
                //Toast.makeText(requireContext(), "Clicked Inverse", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_unselect_all -> {
                medicineAdapter.unselectAll()
                //Toast.makeText(requireContext(), "Clicked Unselect all", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_delete_selection -> {
                Toast.makeText(requireContext(), "Clicked Delete selection", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun openFilterAndSortDialog(){
        val inflater = requireActivity().layoutInflater
        medFilterDialog = inflater.inflate(R.layout.medicine_stock_filter_layout, null)

        val filterDialog = Dialog(requireContext())
        filterDialog.setContentView(medFilterDialog)

        val width = resources.displayMetrics.widthPixels * 1.0 // Adjust the percentage as needed
        val params = filterDialog.window?.attributes
        params?.width = width.toInt()
        filterDialog.window?.attributes = params

//        val sortByChipGroup: ChipGroup = medFilterDialog.findViewById(R.id.sortByChipGroup)
//        val applyFiltersChipGroup: ChipGroup = medFilterDialog.findViewById(R.id.applyFiltersChipGroup)

        val sortByChipGroup: ChipGroup = filterDialog.findViewById(R.id.sortByChipGroup)
        val applyFiltersChipGroup: ChipGroup = filterDialog.findViewById(R.id.applyFiltersChipGroup)

        val sortChipValues = listOf("Alphabetical", "Expiry Date", "Time to expire")
        for (sortByChipValue in sortChipValues) {
            val sortChip = Chip(requireContext())
            sortChip.text = sortByChipValue
            sortChip.isCheckable = true
            sortChip.isClickable = true

            sortByChipGroup.addView(sortChip)
        }

        val applyFiltersChipValues = listOf("INVENTORY", "EXPIRED", "DELETED")
        for (applyFilterChipValue in applyFiltersChipValues) {
            val applyFilterChip = Chip(requireContext())
            applyFilterChip.text = applyFilterChipValue
            applyFilterChip.isCheckable = true
            applyFilterChip.isClickable = true

            applyFiltersChipGroup.addView(applyFilterChip)
        }

        val applyBtn: Button = medFilterDialog.findViewById(R.id.btnApply)
        applyBtn.setOnClickListener {
            val selectedSortByChips = sortByChipGroup.children.filter {
                (it as Chip).isChecked
            }.map {
                (it as Chip).text.toString()
            }.toList()

            val selectedApplyFiltersChips = applyFiltersChipGroup.children.filter {
                (it as Chip).isChecked
            }.map {
                (it as Chip).text.toString()
            }.toList()

            val allSelectedChips = selectedSortByChips + selectedApplyFiltersChips

            if(allSelectedChips.isEmpty()){
                val snackbar = Snackbar.make(requireView(),"No chips are selected",Snackbar.LENGTH_LONG)
                snackbar.show()
            } else{
                val selectedChipsString = allSelectedChips.joinToString(", ")
//                val snackbar = Snackbar.make(requireView(),"Selected Chips: $selectedChipsString",Snackbar.LENGTH_LONG)
//                snackbar.show()

                // Sort
                if(selectedSortByChips.isNotEmpty()){
                    if(selectedSortByChips.contains("Alphabetical")) {
                        currentSortType = SortType.ALPHABETICAL
                    } else {
                        currentSortType = SortType.NONE
                    }
                }

                if(selectedApplyFiltersChips.isNotEmpty()){
                    medicineList.clear()
                    updateRecyclerView(medicineList)
                    if(selectedApplyFiltersChips.size == 1){
                        addMedicineInRecycleView(selectedApplyFiltersChips[0])
                    } else{
                        for (i in selectedApplyFiltersChips){
                            addMedicineInRecycleView(i)
                        }
                    }
                }
            }
            filterDialog.dismiss()
        }


        // Reset Button Code
        val resetBtn: Button = medFilterDialog.findViewById(R.id.btnReset)
        val initialBackground = ContextCompat.getDrawable(requireContext(), R.drawable.custom_filter_reset_button_background)
        resetBtn.background = initialBackground
        resetBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.coffeetype))
        resetBtn.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Change the text color when the button is pressed
                    resetBtn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Restore the initial text color when the button is released or touch is canceled
                    resetBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.coffeetype))
                }
            }
            // Return false to allow onClickListener to handle the click event
            false
        }
        // Set a dummy onFocusChangeListener to make the button focusable
        resetBtn.onFocusChangeListener = View.OnFocusChangeListener { _, _ -> }

        // Set the background back to initial state when focus is lost
        resetBtn.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                resetBtn.backgroundTintList = null
                resetBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.coffeetype))
            }
        }
        resetBtn.setOnClickListener {
            Log.i(tAG, "openFilterAndSortDialog: resetBtn")
            sortByChipGroup.clearCheck()
            applyFiltersChipGroup.clearCheck()
        }

        val closeButton = filterDialog.findViewById<ImageButton>(R.id.filterCloseButton)
        closeButton.setOnClickListener { filterDialog.dismiss() }
        filterDialog.show()
    }

    private fun addMedicineInRecycleView(filter: String){
        Log.i(tAG, "addMedicineInRecycleView: id ${FBase.getUserId()}")
        val db = FirebaseFirestore.getInstance()
        val medicineDataRef = db.collection("users_data").document(FBase.getUserId()).collection("medicine_data")
//        medicineList.clear()
        medicineAdapter.notifyDataSetChanged()
        medicineDataRef.get()
            .addOnSuccessListener { querySnapshot ->
                when(filter){
                    "INVENTORY" ->{
//                        medicineList.clear()
                        medicineAdapter.notifyDataSetChanged()
                        for (document in querySnapshot) {
                            if((document.get("mediDeleted").toString() == "No" || document.get("mediDeleted").toString() == "NO") && !isExpired(document.get("expDate").toString())){
                                medicineList.add(ItemsViewModel(
                                    document.id,document.get("medName").toString(),
                                    document.get("expDate").toString(),
                                    document.get("createdDate").toString(),
                                    document.get("createdTime").toString(),
                                false)
                                )
                                if(currentSortType == SortType.ALPHABETICAL){
                                    medicineList.sortBy { it.medicine_name }
                                }
                                updateRecyclerView(medicineList)
                                medicineAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    "EXPIRED" ->{
                        for (document in querySnapshot) {
                            if(isExpired(document.get("expDate").toString()) && (document.get("mediDeleted").toString() == "No" || document.get("mediDeleted").toString() == "NO")){
                                medicineList.add(ItemsViewModel(
                                    document.id,document.get("medName").toString(),
                                    document.get("expDate").toString(),
                                    document.get("createdDate").toString(),
                                    document.get("createdTime").toString(),
                                false)
                                )
                                if(currentSortType == SortType.ALPHABETICAL){
                                    medicineList.sortBy { it.medicine_name }
                                }
                                updateRecyclerView(medicineList)
                                medicineAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    "DELETED" ->{
                        for (document in querySnapshot) {
                            if((document.get("mediDeleted").toString() == "Yes") || (document.get("mediDeleted").toString() == "YES")){
                                medicineList.add(ItemsViewModel(
                                    document.id,document.get("medName").toString(),
                                    document.get("expDate").toString(),
                                    document.get("createdDate").toString(),
                                    document.get("createdTime").toString(),
                                false)
                                )
                                if(currentSortType == SortType.ALPHABETICAL){
                                    medicineList.sortBy { it.medicine_name }
                                }
                                updateRecyclerView(medicineList)
                                medicineAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors that occur
                Log.e("Firebase", "Error getting medicine data: $e")
            }
    }

    private fun updateRecyclerView(dataList: List<ItemsViewModel>) {
        Log.d("TAG", "updateRecyclerView: $dataList")
        medicineAdapter = MedicineStockItemAdapter(dataList)
        binding.medicineListView.adapter = medicineAdapter

        medicineAdapter.setOnItemClickListener(object : MedicineStockItemAdapter.OnItemClickListener {
            override fun onItemClick(medicineId: String) {
                handleDeleteButtonClick(medicineId)
            }

            override fun onEditClick(medicineId: String) {
                openEditDialog(medicineId)
                Log.i("TAG", "onEditClick: 1")
            }
        })
    }

    private fun isExpired(expiryDate: String): Boolean {
        val parts  = expiryDate.split("/")
        if(parts.size == 2){
            val expiryMonth = parts[0].toInt()
            val expiryYear = parts[1].toInt()

            // Get the current date
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is zero-based

            // Check if the current year is greater than the expiry year
            // OR if it's the same year but the current month is greater than the expiry month
            return (calendar.get(Calendar.YEAR) > expiryYear) || (calendar.get(Calendar.YEAR) == expiryYear && currentMonth > expiryMonth)
        }
        return false
    }


    private fun handleDeleteButtonClick(medicineId: String) {
        val db = FirebaseFirestore.getInstance()
        val medicineDataRef =
            db.collection("users_data").document(FBase.getUserId()).collection("medicine_data")

        medicineDataRef.document(medicineId).update("mediDeleted", "Yes")
            .addOnSuccessListener {
                // Find and remove the deleted medicine from the local data sourc
                val deletedMedicine = medicineList.find { it.medicine_id == medicineId }
                if (deletedMedicine != null) {
                    medicineList.remove(deletedMedicine)
                    medicineAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error updating mediDeleted: $e")
            }
    }

    private fun openEditDialog(medicineId: String) {
        val inflater = requireActivity().layoutInflater
        dialog = inflater.inflate(R.layout.medicine_details_modification_layout, null)

        val mediName: TextInputEditText = dialog.findViewById(R.id.addMedi_name_modified_txtInpEditTxt)
        val mediDosage: TextInputEditText = dialog.findViewById(R.id.dosage_modified_txtInputEditText)
        val mediQuantity: TextInputEditText = dialog.findViewById(R.id.medicine_quantity_modified_TextInputEditText)

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        builder.setView(dialog)
            .setTitle("Medicine Details Modification")
            .setPositiveButton("Add") { dialog, which ->
                editMedicinDetail(
                    medicineId,
                    mediName.text.toString(),
                    mediDosage.text.toString(),
                    mediQuantity.text.toString()
                )
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                Toast.makeText(requireContext(), "Cancel", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        builder.show()
    }

    private fun editMedicinDetail(medicineId: String, medicineName: String, medicineDosage: String, medicineQuantity: String){
        val db = FirebaseFirestore.getInstance()
        val medicineDataRef =
            db.collection("users_data").document(FBase.getUserId()).collection("medicine_data")

        if(medicineName.isNotEmpty() && medicineDosage.isNotEmpty() && medicineQuantity.isNotEmpty()){
            medicineDataRef.document(medicineId).update("medName", medicineName)
            medicineDataRef.document(medicineId).update("dosage", medicineDosage)
            medicineDataRef.document(medicineId).update("totalQuantity", medicineQuantity)
            Toast.makeText(requireContext(), "Modification Successfully done", Toast.LENGTH_SHORT).show()
            val deletedMedicine = medicineList.find { it.medicine_id == medicineId }
            if (deletedMedicine != null) {
                medicineList.remove(deletedMedicine)
                medicineAdapter.notifyDataSetChanged()
                medicineList.clear()
                addMedicineInRecycleView("INVENTORY")
            }
        } else{
            Toast.makeText(requireContext(), "Fill up the all details", Toast.LENGTH_SHORT).show()
        }
    }
}

enum class SortType {
    ALPHABETICAL,
    NONE
}


enum class MedicineFilter{
    INVENTORY, EXPIRED, DELETED
}