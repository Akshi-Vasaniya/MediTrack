package com.example.meditrack.homeActivity.medicine.medicineStock

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meditrack.R
import com.example.meditrack.adapter.MedicineStockItemAdapter
import com.example.meditrack.dataModel.ItemsViewModel
import com.example.meditrack.databinding.FragmentMedicineStockBinding
import com.example.meditrack.firebase.FBase
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MedicineStockFragment : Fragment() {
    private lateinit var medicineAdapter: MedicineStockItemAdapter
    private val medicineList = ArrayList<ItemsViewModel>()
    private lateinit var dialog: View
    private lateinit var dropDown: Spinner
    private val tAG = "MedicineStockfragment"
    private lateinit var customAdapter: ArrayAdapter<String>
    private var selectedFilter: MedicineFilter = MedicineFilter.INVENTORY


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
        val view = inflater.inflate(R.layout.fragment_medicine_stock, container, false)
        binding = FragmentMedicineStockBinding.bind(view)
        viewModel = ViewModelProvider(this)[MedicineStockViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        medicineList.clear()
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
        dropDown = view.findViewById(R.id.dropdownSpinner)
        binding.apply {
            medicineListView.layoutManager = LinearLayoutManager(requireContext())
            // On edit or delete
            updateRecyclerView(medicineList)
            // Set the dropdown layout style
            customAdapter.setDropDownViewResource(R.layout.custom_spinner_item)
            // Attach the custom adapter to the Spinner
            dropdownSpinner.adapter = customAdapter
            addMedicineInRecycleView(MedicineFilter.INVENTORY)

            dropdownSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    when (position) {
                        0 -> selectedFilter = MedicineFilter.INVENTORY
                        1 -> selectedFilter = MedicineFilter.EXPIRED
                        2 -> selectedFilter = MedicineFilter.DELETED
                    }
                    addMedicineInRecycleView(selectedFilter)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }
        return view
    }

    private fun addMedicineInRecycleView(filter: MedicineFilter){
        Log.i(tAG, "addMedicineInRecycleView: id ${FBase.getUserId()}")
        val db = FirebaseFirestore.getInstance()
        val medicineDataRef = db.collection("user_medicines").document(FBase.getUserId()).collection("medicine_data")
        medicineList.clear()
        medicineAdapter.notifyDataSetChanged()
        medicineDataRef.get()
            .addOnSuccessListener { querySnapshot ->
                when(filter){
                    MedicineFilter.INVENTORY ->{
                        medicineList.clear()
                        medicineAdapter.notifyDataSetChanged()
                        for (document in querySnapshot) {
                            if((document.get("mediDeleted").toString() == "No" || document.get("mediDeleted").toString() == "NO") && !isExpired(document.get("expDate").toString())){
                                medicineList.add(ItemsViewModel(document.id,document.get("medName").toString(), document.get("expDate").toString()))
                                updateRecyclerView(medicineList)
                                medicineAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    MedicineFilter.EXPIRED ->{
                        for (document in querySnapshot) {
                            if(isExpired(document.get("expDate").toString()) && (document.get("mediDeleted").toString() == "No" || document.get("mediDeleted").toString() == "NO")){
                                medicineList.add(ItemsViewModel(document.id,document.get("medName").toString(), document.get("expDate").toString()))
                                updateRecyclerView(medicineList)
                                medicineAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    MedicineFilter.DELETED ->{
                        for (document in querySnapshot) {
                            if((document.get("mediDeleted").toString() == "Yes") || (document.get("mediDeleted").toString() == "YES")){
                                medicineList.add(ItemsViewModel(document.id,document.get("medName").toString(), document.get("expDate").toString()))
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
            db.collection("user_medicines").document(FBase.getUserId()).collection("medicine_data")

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
            db.collection("user_medicines").document(FBase.getUserId()).collection("medicine_data")

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
                addMedicineInRecycleView(MedicineFilter.INVENTORY)
            }
        } else{
            Toast.makeText(requireContext(), "Fill up the all details", Toast.LENGTH_SHORT).show()
        }
    }


}

enum class MedicineFilter{
    INVENTORY, EXPIRED, DELETED
}