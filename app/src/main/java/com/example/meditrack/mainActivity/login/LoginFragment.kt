package com.example.meditrack.mainActivity.login

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.dataModel.User
import com.example.meditrack.databinding.FragmentLoginBinding
import com.example.meditrack.firebase.firebaseAuth
import com.example.meditrack.firebase.userReference
import com.example.meditrack.homeActivity.HomeActivity
import com.example.meditrack.regularExpression.ListPattern
import com.example.meditrack.regularExpression.MatchPattern.Companion.validate
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


class LoginFragment : Fragment() {

    companion object {
    }

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding
    private var inputEmail:String?=null
    private var inputPassword:String?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        binding.alreadyAccount.setOnClickListener {
            binding.email.text!!.clear()
            binding.password.text!!.clear()
            binding.email.error=null
            binding.password.error=null
            findNavController().popBackStack()
            findNavController().navigate(R.id.registrationFragment)
        }

        binding.apply {

            email.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun afterTextChanged(s: Editable?) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val userInput = s.toString()
                    if (!userInput.validate(ListPattern.getEmailRegex())) {
                        email.error = "Invalid email format"

                    } else {
                        // Input is valid, clear the error
                        email.error = null
                        inputEmail=email.text.toString()
                    }
                }
            })

            password.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val userInput = s.toString()
                    if (!userInput.validate(ListPattern.getPasswordRegex())) {
                        password.error = "Invalid password format"
                    } else {
                        // Input is valid, clear the error
                        password.error = null
                        inputPassword=password.text.toString()
                    }
                }
            })

            login.setOnClickListener {
                if(email.error==null && password.error==null && inputEmail!=null && inputPassword!=null)
                {
                    inputEmail=inputEmail!!.trim()
                    inputPassword=inputPassword!!.trim()

                    firebaseAuth.getFireBaseAuth().signInWithEmailAndPassword(inputEmail!!,inputPassword!!).addOnCompleteListener {
                        if(it.isSuccessful)
                        {
                            if(!firebaseAuth.getCurrentUser()!!.isEmailVerified)
                            {
                                Toast.makeText(requireContext(),"Please Verify your email", Toast.LENGTH_SHORT).show()
                            }
                            else{
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
                                                Intent(requireActivity(), HomeActivity::class.java).apply {
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

                        }
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(),it.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        binding.email.error=null
        binding.password.error=null
        inputEmail=null
        inputPassword=null
        super.onViewStateRestored(savedInstanceState)
    }

}