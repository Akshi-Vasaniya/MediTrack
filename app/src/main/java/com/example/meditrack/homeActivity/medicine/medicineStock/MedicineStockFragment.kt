package com.example.meditrack.homeActivity.medicine.medicineStock

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

        binding.apply {
            medicineListView.layoutManager = LinearLayoutManager(requireContext())
//            val data = ArrayList<ItemsViewModel>()
//            for (i in 1..20) {
//                data.add(ItemsViewModel("Paracetamol", "02/10/2023"))
//            }

        }

        val db = FirebaseFirestore.getInstance()
        val medicineDataRef = db.collection("user_medicines").document(fBase.getUserId()).collection("medicine_data")
        medicineList.clear()
        medicineDataRef.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    medicineList.add(ItemsViewModel(document.get("medName").toString(), document.get("expDate").toString()))
//                    Log.i("TAG", "onCreateView: ${medicineList.size}")
//                    Log.i("TAG", "onCreateView: ${ItemsViewModel(document.get("medName").toString(), document.get("expDate").toString())}")
                    updateRecyclerView(medicineList)
                    medicineAdapter.notifyDataSetChanged()
                }

                Log.i("TAG", "onCreateView: ${querySnapshot.isEmpty}")
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
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val adapter = MedicineStockItemAdapter(data)
//        recyclerView.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MedicineStockViewModel::class.java)
        // TODO: Use the ViewModel
    }

}