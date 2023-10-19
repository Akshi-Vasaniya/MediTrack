package com.example.meditrack.homeActivity.medicine.medicineStock

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.dataModel.ItemsViewModel
import com.example.meditrack.databinding.FragmentAddMedicineBinding
import com.example.meditrack.databinding.FragmentMedicineStockBinding

class MedicineStockFragment : Fragment() {

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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.medicineListView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val data = ArrayList<ItemsViewModel>()
        for (i in 1..20) {
            data.add(ItemsViewModel("Paracetamol", "02/10/2023"))
        }

        val adapter = CustomAdapter(data)
        recyclerView.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MedicineStockViewModel::class.java)
        // TODO: Use the ViewModel
    }

}