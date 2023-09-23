package com.example.meditrack.mainActivity.splash

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.caverock.androidsvg.SVG
import com.example.meditrack.R
import com.example.meditrack.dataModel.User
import com.example.meditrack.databinding.FragmentSplashBinding
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
            viewModel.delayAndNavigate(3000L)
            if(firebaseAuth.currentUser!=null)
            {
                val query = userReference.getUserDataQuery()
                // Now, you can read data from the user's specific node in the database
                query.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // This method is called when data is retrieved successfully
                        // dataSnapshot contains the data for the user
                        MainScope().launch(Dispatchers.IO) {
                            val userData = User.mapDataSnapshotToUser(dataSnapshot)

                            val sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("name", userData.name)
                            editor.putString("surname", userData.surname)
                            editor.putString("email", userData.email)


                            if(userData.profileImage !=null)
                            {
                                editor.putBoolean("hasImage",true)
                                val bitmap = utilityFunction.decodeBase64ToBitmap(userData.profileImage.toString())

                                val cacheDirectory = requireActivity().cacheDir

                                val imageFileName = "profileImg.jpg"
                                val imageFile = File(cacheDirectory, imageFileName)
                                try {
                                    val outputStream = FileOutputStream(imageFile)
                                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                                    outputStream.flush()
                                    outputStream.close()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                                /*MainScope().launch(Dispatchers.Main) {
                                    val activity = requireActivity() as HomeActivity
                                    val parentView = activity.myToolbarImage.parent as View
                                    activity.myToolbarImage.layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    parentView.requestLayout()
                                    activity.myToolbarImage.setImageBitmap(bitmap)
                                }*/
                            }
                            else{
                                editor.putBoolean("hasImage",false)
                            }
                            editor.apply()
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
                    }
                })

            }
            else{
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