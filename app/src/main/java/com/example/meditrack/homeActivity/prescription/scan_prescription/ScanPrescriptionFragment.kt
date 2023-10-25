package com.example.meditrack.homeActivity.prescription.scan_prescription

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.databinding.FragmentScanPrescriptionBinding

class ScanPrescriptionFragment : Fragment() {

    private lateinit var scanPrescriptionBinding: FragmentScanPrescriptionBinding

    companion object {
        fun newInstance() = ScanPrescriptionFragment()
    }

    private lateinit var viewModel: ScanPrescriptionFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan_prescription, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanPrescriptionBinding = FragmentScanPrescriptionBinding.bind(view)


        scanPrescriptionBinding.ScanPrescriptionBtn.setOnClickListener {
            findNavController().navigate(R.id.OCRFragment)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ScanPrescriptionFragmentViewModel::class.java)
        // TODO: Use the ViewModel
    }

}