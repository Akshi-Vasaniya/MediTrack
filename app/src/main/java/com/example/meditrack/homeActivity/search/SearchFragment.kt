package com.example.meditrack.homeActivity.search

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.adapter.SearchItemAdapter
import com.example.meditrack.dataModel.api.ApiInstance
import com.example.meditrack.dataModel.dataClasses.SearchItemData
import com.example.meditrack.databinding.FragmentSearchBinding
import com.example.meditrack.utility.UtilsFunctions.Companion.isMediTrackServerLive
import com.example.meditrack.utility.UtilsFunctions.Companion.showToast
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class SearchFragment : Fragment() {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private lateinit var viewModel: SearchViewModel
    private lateinit var binding:FragmentSearchBinding
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
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        binding=FragmentSearchBinding.bind(view)

//        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        progressDialog= CustomProgressDialog(requireActivity())
        return binding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        viewModel.setValueSearchItemsList(viewModel._dataList.value!!)
        super.onConfigurationChanged(newConfig)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isMediTrackServerLive{
            if(!it)
            {
                requireContext().showServerNotAvailableDialog()
            }
        }

        binding.apply {
            viewModel._dataList.observe(viewLifecycleOwner) { newDataList ->
                // Update the RecyclerView with the new data
                if(newDataList!=null)
                {
                    binding.rvCombineImage.adapter = SearchItemAdapter(newDataList)
                }

            }

            fragmentSearchButton.setOnClickListener {
                val searchText = fragmentSearchTextTextInputEditText.text.toString()
                MainScope().launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main){
                        progressDialog.start("Searching...")
                    }

                    try {
                        val response = ApiInstance.api.listDocument(searchText)
                        response!!.enqueue(object : Callback<List<SearchItemData?>?> {
                            override fun onResponse(
                                call: Call<List<SearchItemData?>?>,
                                response: Response<List<SearchItemData?>?>
                            ) {
                                try {
                                    Log.i("Search", "onResponse: ${response.body()!!}")
                                    val res = response.body()!!
                                    viewModel.setValueSearchItemsList(res)
                                    progressDialog.stop()
                                }
                                catch (ex:Exception)
                                {
                                    progressDialog.stop()
                                    requireContext().showToast("An unexpected error occurred")
                                }
                            }

                            override fun onFailure(call: Call<List<SearchItemData?>?>, t: Throwable) {
                                Log.i("Search", "onResponse: ${t.message}")
                                progressDialog.stop()
                            }
                        })
                    }
                    catch (e: IOException) {
                        if(isNetworkAvailable(requireContext()))
                        {
                            requireContext().showToast("server unavailable")
                        }
                        else{
                            requireContext().showToast("Network issue")
                        }

                    } catch (e: Exception) {
                        requireContext().showToast("An unexpected error occurred")
                    }

                }
            }

        }
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


    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnectedOrConnecting == true
    }

}