package com.example.meditrack.mainActivity.register

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
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
import com.example.meditrack.databinding.FragmentRegistrationBinding
import com.example.meditrack.firebase.firebaseAuth
import com.example.meditrack.firebase.userReference
import com.example.meditrack.regularExpression.ListPattern
import com.example.meditrack.regularExpression.MatchPattern.Companion.validate
import com.example.meditrack.utility.utilityFunction
import com.example.meditrack.utility.utilityFunction.Companion.bitmapToBase64
import com.example.meditrack.utility.utilityFunction.Companion.uriToBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream


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
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegistrationBinding.bind(view)

        binding.alreadyAccount.setOnClickListener {
            findNavController().popBackStack()
            findNavController().navigate(R.id.loginFragment)
        }

        binding.apply {
            name.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val userInput = s.toString()
                    if (!userInput.validate(ListPattern.getNameRegex())) {
                        name.error = "Invalid name format"
                    } else {
                        // Input is valid, clear the error
                        name.error = null
                        inputName=name.text.toString()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            surname.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val userInput = s.toString()
                    if (!userInput.validate(ListPattern.getNameRegex())) {
                        surname.error = "Invalid surname format"
                    } else {
                        // Input is valid, clear the error
                        if(inputName!=null && inputName==surname.text.toString())
                        {
                            surname.error="Surname is same as Name"
                        }
                        else{
                            surname.error = null
                            inputSurname=surname.text.toString()
                        }

                    }
                }

                override fun afterTextChanged(s: Editable?) {}

            })

            email.addTextChangedListener(object :TextWatcher{
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

            password.addTextChangedListener(object :TextWatcher{
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
                        if(inputConfirmPassword!=null && password.text.toString()!=inputConfirmPassword){
                            confirmPassword.error="Not Match"
                        }
                        else{
                            password.error = null
                            inputPassword=password.text.toString()
                        }

                    }
                }
            })

            confirmPassword.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val userInput = s.toString()
                    if (!userInput.validate(ListPattern.getPasswordRegex())) {
                        confirmPassword.error = "Invalid confirm password format"
                    } else {
                        // Input is valid, clear the error
                        if(inputPassword==null || inputPassword!=confirmPassword.text.toString())
                        {
                            confirmPassword.error="Not Match"
                        }
                        else{
                            confirmPassword.error = null
                            inputConfirmPassword = confirmPassword.text.toString()
                        }

                    }
                }

                override fun afterTextChanged(s: Editable?) {}

            })

            registerBtn.setOnClickListener {

                if(name.error==null &&
                    surname.error==null &&
                    email.error==null &&
                    password.error==null &&
                    confirmPassword.error==null &&
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

                    firebaseAuth.getFireBaseAuth().createUserWithEmailAndPassword(inputEmail!!,inputPassword!!).addOnCompleteListener {
                        if(it.isSuccessful)
                        {
                            var imageString:String? = null
                            if(inputProfileImage!=null){
                                imageString = bitmapToBase64(inputProfileImage!!)
                            }
                            val User = User(inputName!!, inputSurname!!, inputEmail!!,imageString)
                            userReference.getUserReference().child(inputName!!).setValue(User).addOnSuccessListener {
                                firebaseAuth.getCurrentUser()?.sendEmailVerification()?.addOnSuccessListener {
                                    Toast.makeText(requireContext(),"Verify Your Email",Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.loginFragment)
                                }?.addOnFailureListener {
                                    Log.i(TAG,it.toString())
                                    Toast.makeText(requireContext(),it.toString(),Toast.LENGTH_SHORT).show()
                                }
                            }.addOnFailureListener{
                                Log.i(TAG,it.toString())
                                Toast.makeText(requireActivity(),it.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.addOnFailureListener {
                        Log.i(TAG,it.toString())
                        Toast.makeText(requireContext(),it.toString(),Toast.LENGTH_SHORT).show()
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
            } else {
                // Handle permission denial
                // You can show a message or take appropriate action
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
            // The user has selected an image. You can get the image URI from the data intent.
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                binding.profileImage.setPadding(0, 0, 0, 0)
                inputProfileImage = uriToBitmap(requireActivity(),selectedImageUri)
                inputProfileImage = utilityFunction.getCircularBitmap(inputProfileImage)
                binding.profileImage.setImageBitmap(inputProfileImage)
            }
        }
    }



}