package com.example.meditrack.homeActivity.prescription.scan_prescription

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.dataModel.api.ApiInstance
import com.example.meditrack.dataModel.dataClasses.SearchItemData
import com.example.meditrack.databinding.FragmentScanPrescriptionBinding
import com.example.meditrack.utility.UtilsFunctions
import com.example.meditrack.utility.UtilsFunctions.Companion.showToast
import com.example.meditrack.utility.UtilsFunctions.Companion.toBase64
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ScanPrescriptionFragment : Fragment() {


    companion object {
        fun newInstance() = ScanPrescriptionFragment()
    }

    private lateinit var viewModel: ScanPrescriptionFragmentViewModel
    private lateinit var binding: FragmentScanPrescriptionBinding
    private lateinit var progressDialog: CustomProgressDialog

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
        val view = inflater.inflate(R.layout.fragment_scan_prescription, container, false)
        binding = FragmentScanPrescriptionBinding.bind(view)

//        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this)[ScanPrescriptionFragmentViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        UtilsFunctions.isMediTrackServerLive {
            if (!it) {
                requireContext().showServerNotAvailableDialog()
            }
        }

        binding.ScanPrescriptionBtn.setOnClickListener {
            findNavController().navigate(R.id.OCRFragment)
        }

        binding.ScanPrescriptionSearchBtn.setOnClickListener {
            val search: String? = binding.scanSearchDataEditText.text.toString()
            MainScope().launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    progressDialog.start("Searching...")
                }
                try {
                    val response = ApiInstance.api.labelDataUsingNER(search?.toBase64())
                    response.enqueue(object : Callback<JsonElement?> {
                        override fun onResponse(
                            call: Call<JsonElement?>,
                            response: Response<JsonElement?>
                        ) {
                            try {
                                Log.i("Search", "onResponse: ${response.body()!!}")
                                val res = response.body()!!
                                binding.ScanPrescriptionReceivedData.text = res.toString()
                                progressDialog.stop()
                            } catch (ex: Exception) {
                                progressDialog.stop()
                                requireContext().showToast("An unexpected error occurred")
                            }
                        }

                        override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                            Log.i("Search", "onResponse: ${t.message}")
                            progressDialog.stop()
                        }
                    })
                } catch (e: IOException) {
                    if (isNetworkAvailable(requireContext())) {
                        requireContext().showToast("server unavailable")
                    } else {
                        requireContext().showToast("Network issue")
                    }

                } catch (e: Exception) {
                    requireContext().showToast("An unexpected error occurred")
                }

            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnectedOrConnecting == true
    }

    fun Context.showServerNotAvailableDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Server Not Available")
        builder.setMessage("The server is not set up or running. Please try again later.")
        builder.setCancelable(false)
        builder.setPositiveButton("OK") { dialog, _ ->
            findNavController().popBackStack()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

}