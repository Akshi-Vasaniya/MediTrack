package com.example.meditrack.homeActivity.prescription.scan_prescription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.databinding.FragmentScanPrescriptionBinding
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog

class ScanPrescriptionFragment : Fragment() {


    companion object {
        fun newInstance() = ScanPrescriptionFragment()
    }

    private lateinit var viewModel: ScanPrescriptionFragmentViewModel
    private lateinit var binding: FragmentScanPrescriptionBinding
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_scan_prescription, container, false)
        binding = FragmentScanPrescriptionBinding.bind(view)
        viewModel = ViewModelProvider(this)[ScanPrescriptionFragmentViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ScanPrescriptionBtn.setOnClickListener {
            findNavController().navigate(R.id.OCRFragment)
        }
    }

}