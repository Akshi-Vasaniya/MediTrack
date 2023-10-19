package com.example.meditrack.homeActivity.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.meditrack.R
import com.example.meditrack.dataModel.api.ApiData
import com.example.meditrack.dataModel.api.ApiInstance
import com.example.meditrack.databinding.FragmentSearchBinding
import com.example.meditrack.utility.CustomProgressDialog
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
    private lateinit var progressDialog:CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentSearchBinding.bind(view)
        progressDialog=CustomProgressDialog(requireActivity())
        binding.outputText.text = ""
        binding.apply {
            fragmentSearchButton.setOnClickListener {
                val searchText = fragmentSearchTextTextInputEditText.text.toString()
                MainScope().launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main){
                        progressDialog.start("Searching...")
                    }

                    val response = ApiInstance.api.listDocument(searchText)
                    response!!.enqueue(object : Callback<List<ApiData?>?> {
                        override fun onResponse(
                            call: Call<List<ApiData?>?>,
                            response: Response<List<ApiData?>?>
                        ) {
                            Log.i("Search", "onResponse: ${response.body()!!}")
                            var res = ""
                            for (item in response.body()!!)
                            {
                                res += item!!.Document+"\n\n"
                            }
                            progressDialog.stop()

                            binding.outputText.text = res
                        }

                        override fun onFailure(call: Call<List<ApiData?>?>, t: Throwable) {

                            Log.i("Search", "onResponse: ${t.message}")
                            progressDialog.stop()
                        }
                    })
                }
            }

        }
    }

}