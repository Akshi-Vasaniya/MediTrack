package com.example.meditrack.homeActivity.about

import android.os.Binder
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.lifecycle.MutableLiveData
import com.example.meditrack.R
import com.example.meditrack.databinding.FragmentAboutUsBinding
import com.example.meditrack.databinding.FragmentAddMedicineBinding
import com.example.meditrack.firebase.fBase
import com.example.meditrack.homeActivity.medicine.addMedicine.AddMedicineViewModel
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AboutUsFragment : Fragment() {

    companion object {
        fun newInstance() = AboutUsFragment()
    }

    private lateinit var viewModel: AboutUsViewModel
    lateinit var binding:FragmentAboutUsBinding
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_about_us, container, false)
        binding = FragmentAboutUsBinding.bind(view)
        viewModel = ViewModelProvider(this)[AboutUsViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())

        binding.apply {

            MainScope().launch(Dispatchers.IO) {
                val firebaseStorageInstance = fBase.getFireStoreInstance()
                val aboutUsCollectionRef = firebaseStorageInstance.collection("about_us")
                val aboutProjectDetailsDocument = aboutUsCollectionRef.document("about_project_details")
                val collegeDetailsDocument = aboutUsCollectionRef.document("college_details")
                val teamMemberDetailsDocument = aboutUsCollectionRef.document("team_member_details")

                aboutProjectDetailsDocument.addSnapshotListener { value, error ->
                    if (error != null) {
                        Toast.makeText(requireContext(),"Error",Toast.LENGTH_SHORT).show()
                        Log.e("aboutProjectDetailsDocument.addSnapshotListener","${error.message}")
                        // Handle any errors
                        return@addSnapshotListener
                    }

                    if (value != null) {
                        viewModel.setAboutProjectDetails(value.get("data") as String)
                    } else {
                        Toast.makeText(requireContext(),"about_us collection is empty",Toast.LENGTH_SHORT).show()
                        // Handle the case when the collection is empty
                    }
                }

                collegeDetailsDocument.addSnapshotListener { value, error ->
                    if (error != null) {
                        Toast.makeText(requireContext(),"Error",Toast.LENGTH_SHORT).show()
                        Log.e("collegeDetailsDocument.addSnapshotListener","${error.message}")
                        // Handle any errors
                        return@addSnapshotListener
                    }

                    if (value != null) {
                        viewModel.setCollegeDetails(value.get("data") as String)
                    } else {
                        Toast.makeText(requireContext(),"about_us collection is empty",Toast.LENGTH_SHORT).show()
                        // Handle the case when the collection is empty
                    }
                }



                teamMemberDetailsDocument.addSnapshotListener { value, error ->
                    if (error != null) {
                        Toast.makeText(requireContext(),"Error",Toast.LENGTH_SHORT).show()
                        Log.e("teamMemberDetailsDocument.addSnapshotListener","${error.message}")
                        // Handle any errors
                        return@addSnapshotListener
                    }

                    if (value != null) {
                        viewModel.setTeamMemberDetailsData(value.get("data") as String)
                    } else {
                        Toast.makeText(requireContext(),"about_us collection is empty",Toast.LENGTH_SHORT).show()
                        // Handle the case when the collection is empty
                    }
                }

                withContext(Dispatchers.Main)
                {
                    viewModel._aboutProjectDetails.observe(viewLifecycleOwner){
                        projectDetailsTextView.text = HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    }
                    viewModel._collegeDetails.observe(viewLifecycleOwner){
                        collegeDetailsTextView.text = HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    }
                    viewModel._teamMemberDetails.observe(viewLifecycleOwner){
                        teamDetailsTextView.text = HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    }
                }





            }
            // Retrieve the HTML string from resources
            //val collegeDetailsHtml = resources.getString(R.string.college_details)
            //Log.i("collegeDetailsHtml","${collegeDetailsHtml}")
            //val teamDetailsHtml = resources.getString(R.string.team_member_details)

            // Set the text using HtmlCompat.fromHtml()
            //collegeDetailsTextView.text = HtmlCompat.fromHtml(collegeDetailsHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
            //teamDetailsTextView.text = HtmlCompat.fromHtml(teamDetailsHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        return binding.root
    }



}