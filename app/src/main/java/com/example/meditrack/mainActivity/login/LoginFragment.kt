package com.example.meditrack.mainActivity.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.databinding.FragmentLoginBinding
import com.example.meditrack.exception.handleException
import com.example.meditrack.firebase.firebaseAuth
import com.example.meditrack.homeActivity.HomeActivity
import com.example.meditrack.regularExpression.ListPattern
import com.example.meditrack.regularExpression.MatchPattern.Companion.validate
import com.example.meditrack.utility.progressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding
    private val TAG="LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
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


        binding.fragmentLoginRegisterAccountText.setOnClickListener {
            binding.fragmentLoginEmailTextInputEditText.text!!.clear()
            binding.fragmentLoginPasswordTextInputEditText.text!!.clear()
            findNavController().popBackStack()
            findNavController().navigate(R.id.registrationFragment)
        }

        binding.apply {

            fragmentLoginEmailTextInputEditText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    viewModel.inputEmail = s.toString()
                    if(viewModel.inputEmail=="")
                    {
                        fragmentLoginEmailTextInputLayout.helperText="Required"
                    }
                    else if (!viewModel.inputEmail!!.validate(ListPattern.getEmailRegex())) {
                        fragmentLoginEmailTextInputLayout.helperText="Invalid email format"
                    } else {
                        // Input is valid, clear the error
                        fragmentLoginEmailTextInputLayout.helperText=null
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
                    viewModel.inputPassword = s.toString()
                    if(viewModel.inputPassword=="")
                    {
                        fragmentLoginPasswordTextInputLayout.helperText="Required"
                    }
                    else if (!viewModel.inputPassword!!.validate(ListPattern.getPasswordRegex())) {
                        fragmentLoginPasswordTextInputLayout.helperText = "Invalid password format"
                    } else {
                        // Input is valid, clear the error
                        fragmentLoginPasswordTextInputLayout.helperText = null
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
                if(fragmentLoginEmailTextInputLayout.helperText==null && fragmentLoginPasswordTextInputLayout.helperText==null && viewModel.inputEmail!=null && viewModel.inputPassword!=null)
                {
                    viewModel.inputEmail=viewModel.inputEmail!!.trim()
                    viewModel.inputPassword=viewModel.inputPassword!!.trim()
                    progressDialog.getInstance(requireActivity()).start("Loading...")
                    firebaseAuth.getFireBaseAuth().signInWithEmailAndPassword(viewModel.inputEmail!!,viewModel.inputPassword!!).addOnCompleteListener {
                        if(it.isSuccessful)
                        {
                            if(!firebaseAuth.getCurrentUser()!!.isEmailVerified)
                            {
                                progressDialog.getInstance(requireActivity()).stop()
                                Toast.makeText(requireContext(),"Please Verify your email", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                /*viewModel.userData.observe(viewLifecycleOwner) { userData ->
                                    MainScope().launch(Dispatchers.IO)
                                    {
                                    }
                                }*/
                                if(firebaseAuth.getCurrentUser()!=null)
                                {
                                    /*viewModel.fetchUserData()*/
                                    progressDialog.getInstance(requireActivity()).stop()
                                    Intent(requireActivity(),HomeActivity::class.java).apply {
                                        startActivity(this)
                                    }
                                    requireActivity().finish()
                                }
                                else{
                                    MainScope().launch(Dispatchers.Main)
                                    {
                                        val sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
                                        val editor = sharedPreferences.edit()
                                        editor.clear()
                                        editor.apply()
                                        findNavController().popBackStack()
                                        findNavController().navigate(R.id.loginFragment)
                                    }
                                }

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

}