package com.example.meditrack.mainActivity.splash

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.caverock.androidsvg.SVG
import com.example.meditrack.R
import com.example.meditrack.dataModel.User
import com.example.meditrack.databinding.FragmentSplashBinding
import com.example.meditrack.exception.handleException
import com.example.meditrack.firebase.userReference
import com.example.meditrack.homeActivity.HomeActivity
import com.example.meditrack.utility.utilityFunction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class SplashFragment : Fragment() {

    companion object {
        fun newInstance() = SplashFragment()
    }

    private lateinit var viewModel: SplashViewModel
    private lateinit var binding: FragmentSplashBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val TAG="SplashFragment"

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_splash, container, false)

        viewModel = ViewModelProvider(this).get(SplashViewModel::class.java)

        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding = FragmentSplashBinding.bind(view)

        firebaseAuth=FirebaseAuth.getInstance()

        val svg = SVG.getFromResource(resources, R.raw.meditracklogo)

        binding.mediIcon.setSVG(svg)

        viewLifecycleOwner.lifecycleScope.launch {

            if(firebaseAuth.currentUser!=null)
            {
                val query = userReference.getUserDataQuery()
                // Now, you can read data from the user's specific node in the database
                query.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // This method is called when data is retrieved successfully
                        // dataSnapshot contains the data for the user
                        MainScope().launch(Dispatchers.IO) {
                            User.fetchUserData(requireContext(),dataSnapshot,TAG)
                            withContext(Dispatchers.Main)
                            {
                                Intent(requireActivity(),HomeActivity::class.java).apply {
                                    startActivity(this)
                                }
                                requireActivity().finish()
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors here
                        handleException.firebaseDatabaseExceptions(requireContext(),databaseError,TAG)
                    }
                })

            }
            else{
                viewModel.delayAndNavigate(3000L)
                val sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()
                findNavController().popBackStack()
                findNavController().navigate(R.id.loginFragment)
            }

        }
        super.onViewCreated(view, savedInstanceState)
    }

}