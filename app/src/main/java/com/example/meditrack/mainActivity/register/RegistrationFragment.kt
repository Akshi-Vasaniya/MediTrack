package com.example.meditrack.mainActivity.register

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.dataModel.User
import com.example.meditrack.dataModel.User.Companion.isUsernameAvailable
import com.example.meditrack.databinding.FragmentRegistrationBinding
import com.example.meditrack.exception.handleException
import com.example.meditrack.firebase.firebaseAuth
import com.example.meditrack.firebase.userReference
import com.example.meditrack.regularExpression.ListPattern
import com.example.meditrack.regularExpression.MatchPattern.Companion.validate
import com.example.meditrack.utility.progressDialog
import com.example.meditrack.utility.utilityFunction
import com.example.meditrack.utility.utilityFunction.Companion.bitmapToBase64
import com.example.meditrack.utility.utilityFunction.Companion.uriToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RegistrationFragment : Fragment() {

    companion object {
        fun newInstance() = RegistrationFragment()
    }

    private lateinit var viewModel: RegistrationViewModel
    private lateinit var binding: FragmentRegistrationBinding
    private var inputName:String?=null
    private var inputSurname:String?=null
    private var inputEmail:String?=null
    private var inputPassword:String?=null
    private var inputConfirmPassword:String?=null
    private var inputProfileImage:Bitmap?=null
    private val TAG = "RegisterFragment"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
        binding.fragmentRegistrationEmailTextInputEditText.text?.clear()
        binding.fragmentRegistrationNameTextInputEditText.text?.clear()
        binding.fragmentRegistrationPasswordTextInputEditText.text?.clear()
        binding.fragmentRegistrationSurnameTextInputEditText.text?.clear()
        binding.fragmentRegistrationConfirmPasswordTextInputEditText.text?.clear()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegistrationBinding.bind(view)

        binding.fragmentRegistrationLoginAccountText.setOnClickListener {
            findNavController().popBackStack()
            findNavController().navigate(R.id.loginFragment)
        }

        binding.apply {
            fragmentRegistrationNameTextInputEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val userInput = s.toString()
                    if(userInput=="")
                    {
                        fragmentRegistrationNameTextInputLayout.helperText="Required"
                    }
                    else if (!userInput.validate(ListPattern.getNameRegex())) {
                        fragmentRegistrationNameTextInputLayout.helperText = "Invalid name format"
                    } else {
                        // Input is valid, clear the error
                        fragmentRegistrationNameTextInputLayout.helperText = null
                        inputName=fragmentRegistrationNameTextInputEditText.text.toString()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            fragmentRegistrationSurnameTextInputEditText.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val userInput = s.toString()
                    if(userInput=="")
                    {
                        fragmentRegistrationSurnameTextInputLayout.helperText="Required"
                    }
                    else if (!userInput.validate(ListPattern.getNameRegex())) {
                        fragmentRegistrationSurnameTextInputLayout.helperText = "Invalid surname format"
                    } else {
                        // Input is valid, clear the error
                        if(inputName!=null && inputName==fragmentRegistrationSurnameTextInputEditText.text.toString())
                        {
                            fragmentRegistrationSurnameTextInputLayout.helperText = "Surname is same as Name"
                        }
                        else{
                            fragmentRegistrationSurnameTextInputLayout.helperText = null
                            inputSurname=fragmentRegistrationSurnameTextInputEditText.text.toString()
                        }

                    }
                }

                override fun afterTextChanged(s: Editable?) {}

            })

            fragmentRegistrationEmailTextInputEditText.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun afterTextChanged(s: Editable?) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val userInput = s.toString()
                    if(userInput=="")
                    {
                        fragmentRegistrationEmailTextInputLayout.helperText="Required"
                    }
                    else if (!userInput.validate(ListPattern.getEmailRegex())) {
                        fragmentRegistrationEmailTextInputLayout.helperText = "Invalid email format"
                    } else {
                        // Input is valid, clear the error
                        fragmentRegistrationEmailTextInputLayout.helperText = null
                        inputEmail=fragmentRegistrationEmailTextInputEditText.text.toString()
                    }
                }
            })

            fragmentRegistrationPasswordTextInputEditText.addTextChangedListener(object :TextWatcher{
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val userInput = s.toString()
                    if(userInput=="")
                    {
                        fragmentRegistrationPasswordTextInputLayout.helperText="Required"
                    }
                    else if (!userInput.validate(ListPattern.getPasswordRegex())) {
                        fragmentRegistrationPasswordTextInputLayout.helperText = "Invalid password format"
                    } else {
                        // Input is valid, clear the error
                        if(inputConfirmPassword!=null && fragmentRegistrationPasswordTextInputEditText.text.toString()!=inputConfirmPassword){
                            fragmentRegistrationConfirmPasswordTextInputLayout.helperText="Not Match"
                        }
                        else{
                            fragmentRegistrationPasswordTextInputLayout.helperText = null
                            inputPassword=fragmentRegistrationPasswordTextInputEditText.text.toString()
                        }

                    }
                }
            })

            fragmentRegistrationConfirmPasswordTextInputEditText.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val userInput = s.toString()
                    if(userInput=="")
                    {
                        fragmentRegistrationConfirmPasswordTextInputLayout.helperText="Required"
                    }
                    else if (!userInput.validate(ListPattern.getPasswordRegex())) {
                        fragmentRegistrationConfirmPasswordTextInputLayout.helperText = "Invalid confirm password format"
                    } else {
                        // Input is valid, clear the error
                        if(inputPassword==null || inputPassword!=fragmentRegistrationConfirmPasswordTextInputEditText.text.toString())
                        {
                            fragmentRegistrationConfirmPasswordTextInputLayout.helperText="Not Match"
                        }
                        else{
                            fragmentRegistrationConfirmPasswordTextInputLayout.helperText = null
                            inputConfirmPassword = fragmentRegistrationConfirmPasswordTextInputEditText.text.toString()
                        }

                    }
                }

                override fun afterTextChanged(s: Editable?) {}

            })

            fragmentRegistrationRegisterButton.setOnClickListener {

                if(fragmentRegistrationNameTextInputLayout.helperText==null &&
                    fragmentRegistrationSurnameTextInputLayout.helperText==null &&
                    fragmentRegistrationEmailTextInputLayout.helperText==null &&
                    fragmentRegistrationPasswordTextInputLayout.helperText==null &&
                    fragmentRegistrationConfirmPasswordTextInputLayout.helperText==null &&
                    inputName!=null &&
                    inputSurname!=null &&
                    inputEmail!=null &&
                    inputPassword!=null &&
                    inputConfirmPassword!=null
                ){
                    inputEmail= inputEmail!!.trim()
                    inputPassword= inputPassword!!.trim()
                    inputName= inputName!!.trim().uppercase()
                    inputSurname= inputSurname!!.trim().uppercase()
                    progressDialog.getInstance(requireActivity()).start("Loading...")
                    firebaseAuth.getFireBaseAuth().createUserWithEmailAndPassword(inputEmail!!,inputPassword!!).addOnCompleteListener {
                        if(it.isSuccessful)
                        {
                            MainScope().launch(Dispatchers.IO) {
                                var imageString:String? = null
                                if(inputProfileImage!=null){
                                    imageString = bitmapToBase64(inputProfileImage!!)
                                }
                                val User = User(inputName!!, inputSurname!!, inputEmail!!,imageString)
                                userReference.getUserReference().child(it.result?.user!!.uid).setValue(User).addOnSuccessListener {
                                    firebaseAuth.getCurrentUser()?.sendEmailVerification()?.addOnSuccessListener {
                                        progressDialog.getInstance(requireActivity()).stop()
                                        Toast.makeText(requireContext(),"Verify Your Email",Toast.LENGTH_SHORT).show()
                                        findNavController().navigate(R.id.loginFragment)
                                    }?.addOnFailureListener {
                                        progressDialog.getInstance(requireActivity()).stop()
                                        Log.i(TAG,it.toString())
                                        Toast.makeText(requireContext(),it.toString(),Toast.LENGTH_SHORT).show()
                                    }
                                }.addOnFailureListener{
                                    progressDialog.getInstance(requireActivity()).stop()
                                    Log.i(TAG,it.toString())
                                    Toast.makeText(requireActivity(),it.toString(),Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }.addOnFailureListener {
                        progressDialog.getInstance(requireActivity()).stop()
                        handleException.firebaseCommonExceptions(requireContext(),it,TAG)
                    }


                }

            }

            profileImage.setOnClickListener {
                if (checkPermission()) {
                    openImagePicker()
                } else {
                    requestPermissions(
                        arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        101
                    )
                }
            }
        }
    }


    private fun checkPermission(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        )
        val storagePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                storagePermission == PackageManager.PERMISSION_GRANTED
    }




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                openImagePicker()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 102)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 102 && resultCode == RESULT_OK) {
            MainScope().launch(Dispatchers.IO) {
                // The user has selected an image. You can get the image URI from the data intent.
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    binding.profileImage.setPadding(0, 0, 0, 0)
                    inputProfileImage = uriToBitmap(requireActivity(),selectedImageUri)
                    inputProfileImage = utilityFunction.getCircularBitmap(inputProfileImage)
                    withContext(Dispatchers.Main){
                        binding.profileImage.setImageBitmap(inputProfileImage)
                    }

                }
            }

        }
    }



}