package com.example.meditrack.homeActivity.medicine.medicineStock

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meditrack.R
import com.example.meditrack.adapter.MedicineStockItemAdapter
import com.example.meditrack.dataModel.ItemsViewModel
import com.example.meditrack.dataModel.dataClasses.MedicineData
import com.example.meditrack.databinding.FragmentMedicineStockBinding
import com.example.meditrack.firebase.fBase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class MedicineStockFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var medicineAdapter: MedicineStockItemAdapter
    private val medicineList = ArrayList<ItemsViewModel>()

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
        val db = FirebaseFirestore.getInstance()
        val medicineDataRef = db.collection("user_medicines").document(fBase.getUserId()).collection("medicine_data")

        medicineDataRef.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    Log.i("TAG", "onCreateView: mediDeleted ${document.get("mediDeleted")}")
                    if(document.get("mediDeleted").toString() != "Yes"){
//                        Log.i("TAG", "onCreateView: ${document.id}")
                        medicineList.add(ItemsViewModel(document.id,document.get("medName").toString(), document.get("expDate").toString()))
                        updateRecyclerView(medicineList)
                        medicineAdapter.notifyDataSetChanged()
                    }
                }

//                Log.i("TAG", "onCreateView: ${querySnapshot.isEmpty}")
                // After fetching the data, update your RecyclerView
            }
            .addOnFailureListener { e ->
                // Handle any errors that occur
                Log.e("Firebase", "Error getting medicine data: $e")
            }

        return view
    }

    fun updateRecyclerView(dataList: List<ItemsViewModel>) {
        Log.d("TAG", "updateRecyclerView: $dataList")
        medicineAdapter = MedicineStockItemAdapter(requireContext(), dataList)
        binding.medicineListView.adapter = medicineAdapter

        medicineAdapter.setOnItemClickListener(object : MedicineStockItemAdapter.OnItemClickListener {
            override fun onItemClick(medicineId: String) {
                handleDeleteButtonClick(medicineId)
            }
        })
    }

    private fun handleDeleteButtonClick(medicineId: String) {
        val db = FirebaseFirestore.getInstance()
        val medicineDataRef =
            db.collection("user_medicines").document(fBase.getUserId()).collection("medicine_data")

        // Update the 'mediDeleted' field to "Yes" in Firestore for the selected medicine
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MedicineStockViewModel::class.java)
        // TODO: Use the ViewModel
    }

}