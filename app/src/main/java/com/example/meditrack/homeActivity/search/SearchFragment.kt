package com.example.meditrack.homeActivity.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.meditrack.R
import com.example.meditrack.adapter.SearchItemAdapter
import com.example.meditrack.dataModel.api.ApiInstance
import com.example.meditrack.dataModel.dataClasses.SearchItemData
import com.example.meditrack.databinding.FragmentSearchBinding
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SearchFragment : Fragment() {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private lateinit var viewModel: SearchViewModel
    private lateinit var binding:FragmentSearchBinding
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        binding=FragmentSearchBinding.bind(view)
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        progressDialog= CustomProgressDialog(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            outputText.text = ""
            fragmentSearchButton.setOnClickListener {
                val searchText = fragmentSearchTextTextInputEditText.text.toString()
                MainScope().launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main){
                        progressDialog.start("Searching...")
                    }

                    val response = ApiInstance.api.listDocument(searchText)
                    response!!.enqueue(object : Callback<List<SearchItemData?>?> {
                        override fun onResponse(
                            call: Call<List<SearchItemData?>?>,
                            response: Response<List<SearchItemData?>?>
                        ) {
                            try {
                                Log.i("Search", "onResponse: ${response.body()!!}")
                                val res = response.body()!!
                                binding.rvCombineImage.adapter = SearchItemAdapter(res)
                                progressDialog.stop()
                            }
                            catch (ex:Exception)
                            {
                                progressDialog.stop()
                            }
                        }

                        override fun onFailure(call: Call<List<SearchItemData?>?>, t: Throwable) {

                            Log.i("Search", "onResponse: ${t.message}")
                            progressDialog.stop()
                        }
                    })
                }
            }

        }
    }

}