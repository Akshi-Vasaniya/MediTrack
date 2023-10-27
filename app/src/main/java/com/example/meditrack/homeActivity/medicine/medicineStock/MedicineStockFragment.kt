package com.example.meditrack.homeActivity.medicine.medicineStock

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meditrack.R
import com.example.meditrack.adapter.MedicineStockItemAdapter
import com.example.meditrack.dataModel.ItemsViewModel
import com.example.meditrack.databinding.FragmentMedicineStockBinding
import com.example.meditrack.firebase.fBase
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class MedicineStockFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var medicineAdapter: MedicineStockItemAdapter
    private val medicineList = ArrayList<ItemsViewModel>()
    private lateinit var dialog: View


    companion object {
        fun newInstance() = MedicineStockFragment()
    }

    private lateinit var viewModel: MedicineStockViewModel
    private lateinit var binding: FragmentMedicineStockBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medicine_stock, container, false)
        binding = FragmentMedicineStockBinding.bind(view)

        medicineList.clear()
        binding.apply {
            medicineListView.layoutManager = LinearLayoutManager(requireContext())
            updateRecyclerView(medicineList)
        }

        addMedicineInRecycleView()

        return view
    }

    private fun addMedicineInRecycleView(){
        val db = FirebaseFirestore.getInstance()
        val medicineDataRef = db.collection("user_medicines").document(fBase.getUserId()).collection("medicine_data")

        medicineDataRef.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    Log.i("TAG", "onCreateView: mediDeleted ${document.get("mediDeleted")}")
                    if(document.get("mediDeleted").toString() != "Yes"){
                        medicineList.add(ItemsViewModel(document.id,document.get("medName").toString(), document.get("expDate").toString()))
                        updateRecyclerView(medicineList)
                        medicineAdapter.notifyDataSetChanged()
                    }
                }

            }
            .addOnFailureListener { e ->
                // Handle any errors that occur
                Log.e("Firebase", "Error getting medicine data: $e")
            }
    }

    fun updateRecyclerView(dataList: List<ItemsViewModel>) {
        Log.d("TAG", "updateRecyclerView: $dataList")
        medicineAdapter = MedicineStockItemAdapter(requireContext(), dataList)
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

    private fun handleDeleteButtonClick(medicineId: String) {
        val db = FirebaseFirestore.getInstance()
        val medicineDataRef =
            db.collection("user_medicines").document(fBase.getUserId()).collection("medicine_data")

        medicineDataRef.document(medicineId).update("mediDeleted", "Yes")
            .addOnSuccessListener {
                // Find and remove the deleted medicine from the local data source
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
            db.collection("user_medicines").document(fBase.getUserId()).collection("medicine_data")

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
                addMedicineInRecycleView()
            }
        } else{
            Toast.makeText(requireContext(), "Fill up the all details", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MedicineStockViewModel::class.java)
        // TODO: Use the ViewModel
    }

}