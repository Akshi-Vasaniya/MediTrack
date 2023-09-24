package com.example.meditrack.mainActivity.login

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.dataModel.User
import com.example.meditrack.databinding.FragmentLoginBinding
import com.example.meditrack.exception.handleException
import com.example.meditrack.firebase.firebaseAuth
import com.example.meditrack.firebase.userReference
import com.example.meditrack.homeActivity.HomeActivity
import com.example.meditrack.regularExpression.ListPattern
import com.example.meditrack.regularExpression.MatchPattern.Companion.validate
import com.example.meditrack.utility.progressDialog
import com.example.meditrack.utility.utilityFunction
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

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding
    private var inputEmail:String?=null
    private var inputPassword:String?=null
    private val TAG="LoginFragment"

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
        binding.fragmentLoginEmailTextInputEditText.text!!.clear()
        binding.fragmentLoginPasswordTextInputEditText.text!!.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)


        binding.fragmentLoginRegisterAccountText.setOnClickListener {
            binding.fragmentLoginEmailTextInputEditText.text!!.clear()
            binding.fragmentLoginPasswordTextInputEditText.text!!.clear()
            findNavController().popBackStack()
            findNavController().navigate(R.id.registrationFragment)
        }

        binding.apply {

            fragmentLoginEmailTextInputEditText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val userInput = s.toString()
                    if(userInput=="")
                    {
                        fragmentLoginEmailTextInputLayout.helperText="Required"
                    }
                    else if (!userInput.validate(ListPattern.getEmailRegex())) {
                        fragmentLoginEmailTextInputLayout.helperText="Invalid email format"
                    } else {
                        // Input is valid, clear the error
                        fragmentLoginEmailTextInputLayout.helperText=null
                        inputEmail=fragmentLoginEmailTextInputEditText.text.toString()
                    }
                }
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun afterTextChanged(s: Editable?) {}
            })

            fragmentLoginPasswordTextInputEditText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val userInput = s.toString()
                    if(userInput=="")
                    {
                        fragmentLoginPasswordTextInputLayout.helperText="Required"
                    }
                    else if (!userInput.validate(ListPattern.getPasswordRegex())) {
                        fragmentLoginPasswordTextInputLayout.helperText = "Invalid password format"
                    } else {
                        // Input is valid, clear the error
                        fragmentLoginPasswordTextInputLayout.helperText = null
                        inputPassword=fragmentLoginPasswordTextInputEditText.text.toString()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}
            })

            fragmentLoginButton.setOnClickListener {
                if(fragmentLoginEmailTextInputLayout.helperText==null && fragmentLoginPasswordTextInputLayout.helperText==null && inputEmail!=null && inputPassword!=null)
                {
                    inputEmail=inputEmail!!.trim()
                    inputPassword=inputPassword!!.trim()
                    progressDialog.getInstance(requireActivity()).start("Loading...")
                    firebaseAuth.getFireBaseAuth().signInWithEmailAndPassword(inputEmail!!,inputPassword!!).addOnCompleteListener {
                        if(it.isSuccessful)
                        {
                            if(!firebaseAuth.getCurrentUser()!!.isEmailVerified)
                            {
                                progressDialog.getInstance(requireActivity()).stop()
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
                                            User.fetchUserData(requireContext(),dataSnapshot,TAG)
                                            withContext(Dispatchers.Main)
                                            {
                                                progressDialog.getInstance(requireActivity()).stop()
                                                Intent(requireActivity(), HomeActivity::class.java).apply {
                                                    startActivity(this)
                                                }
                                                requireActivity().finish()
                                            }
                                        }

                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // Handle errors here
                                        progressDialog.getInstance(requireActivity()).stop()
                                        handleException.firebaseDatabaseExceptions(requireContext(),databaseError,TAG)
                                    }
                                })

                            }

                        }
                    }.addOnFailureListener {
                        progressDialog.getInstance(requireActivity()).stop()
                        handleException.firebaseCommonExceptions(requireContext(),it,TAG)
                    }
                }
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        inputEmail=null
        inputPassword=null
        super.onViewStateRestored(savedInstanceState)
    }

}