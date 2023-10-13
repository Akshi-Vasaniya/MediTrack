package com.example.meditrack.mainActivity.register

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.dataModel.User
import com.example.meditrack.databinding.FragmentRegistrationBinding
import com.example.meditrack.exception.handleException
import com.example.meditrack.firebase.firebaseAuth
import com.example.meditrack.firebase.userReference
import com.example.meditrack.regularExpression.ListPattern
import com.example.meditrack.regularExpression.MatchPattern.Companion.validate
import com.example.meditrack.utility.progressDialog
import com.example.meditrack.utility.UtilityFunction
import com.example.meditrack.utility.UtilityFunction.Companion.bitmapToBase64
import com.example.meditrack.utility.UtilityFunction.Companion.uriToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RegistrationFragment : Fragment() {

    /*companion object {
        fun newInstance() = RegistrationFragment()
    }*/

    private lateinit var viewModel: RegistrationViewModel
    private lateinit var binding: FragmentRegistrationBinding
    private var inputProfileImage:Bitmap?=null
    private val TAG = "RegisterFragment"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]

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


        binding.fragmentRegistrationLoginAccountText.setOnClickListener {
            findNavController().popBackStack()
            findNavController().navigate(R.id.loginFragment)
        }

        binding.apply {

            fragmentRegistrationNameTextInputEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    viewModel.inputName = s.toString()
                    if(viewModel.inputName=="")
                    {
                        fragmentRegistrationNameTextInputLayout.helperText="Required"
                    }
                    else if (!viewModel.inputName!!.validate(ListPattern.getNameRegex())) {
                        fragmentRegistrationNameTextInputLayout.helperText = "Invalid name format"
                    } else {
                        // Input is valid, clear the error
                        fragmentRegistrationNameTextInputLayout.helperText = null
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            fragmentRegistrationSurnameTextInputEditText.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    viewModel.inputSurname = s.toString()
                    if(viewModel.inputSurname=="")
                    {
                        fragmentRegistrationSurnameTextInputLayout.helperText="Required"
                    }
                    else if (!viewModel.inputSurname!!.validate(ListPattern.getNameRegex())) {
                        fragmentRegistrationSurnameTextInputLayout.helperText = "Invalid surname format"
                    } else {
                        // Input is valid, clear the error
                        if(viewModel.inputName!=null && viewModel.inputName==fragmentRegistrationSurnameTextInputEditText.text.toString())
                        {
                            fragmentRegistrationSurnameTextInputLayout.helperText = "Surname is same as Name"
                        }
                        else{
                            fragmentRegistrationSurnameTextInputLayout.helperText = null
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
                    viewModel.inputEmail = s.toString()
                    if(viewModel.inputEmail=="")
                    {
                        fragmentRegistrationEmailTextInputLayout.helperText="Required"
                    }
                    else if (!viewModel.inputEmail!!.validate(ListPattern.getEmailRegex())) {
                        fragmentRegistrationEmailTextInputLayout.helperText = "Invalid email format"
                    } else {
                        // Input is valid, clear the error
                        fragmentRegistrationEmailTextInputLayout.helperText = null
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
                    viewModel.inputPassword = s.toString()
                    if(viewModel.inputPassword=="")
                    {
                        fragmentRegistrationPasswordTextInputLayout.helperText="Required"
                    }
                    else if (!viewModel.inputPassword!!.validate(ListPattern.getPasswordRegex())) {
                        fragmentRegistrationPasswordTextInputLayout.helperText = "Invalid password format"
                    } else {
                        // Input is valid, clear the error
                        if(viewModel.inputConfirmPassword!=null && fragmentRegistrationPasswordTextInputEditText.text.toString()!=viewModel.inputConfirmPassword){
                            fragmentRegistrationConfirmPasswordTextInputLayout.helperText="Not Match"
                        }
                        else{
                            fragmentRegistrationPasswordTextInputLayout.helperText = null
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
                    viewModel.inputConfirmPassword = s.toString()
                    if(viewModel.inputConfirmPassword=="")
                    {
                        fragmentRegistrationConfirmPasswordTextInputLayout.helperText="Required"
                    }
                    else if (!viewModel.inputConfirmPassword!!.validate(ListPattern.getPasswordRegex())) {
                        fragmentRegistrationConfirmPasswordTextInputLayout.helperText = "Invalid confirm password format"
                    } else {
                        // Input is valid, clear the error
                        if(viewModel.inputPassword==null || viewModel.inputPassword!=fragmentRegistrationConfirmPasswordTextInputEditText.text.toString())
                        {
                            fragmentRegistrationConfirmPasswordTextInputLayout.helperText="Not Match"
                        }
                        else{
                            fragmentRegistrationConfirmPasswordTextInputLayout.helperText = null
                        }

                    }
                }

                override fun afterTextChanged(s: Editable?) {}

            })

            fragmentRegistrationRegisterButton.setOnClickListener {
                val name = viewModel.inputName
                val surname = viewModel.inputSurname
                val email = viewModel.inputEmail
                val password = viewModel.inputPassword
                val confirmPassword = viewModel.inputConfirmPassword

                if(fragmentRegistrationNameTextInputLayout.helperText==null &&
                    fragmentRegistrationSurnameTextInputLayout.helperText==null &&
                    fragmentRegistrationEmailTextInputLayout.helperText==null &&
                    fragmentRegistrationPasswordTextInputLayout.helperText==null &&
                    fragmentRegistrationConfirmPasswordTextInputLayout.helperText==null &&
                    !name.isNullOrBlank() &&
                    !surname.isNullOrBlank() &&
                    !email.isNullOrBlank() &&
                    !password.isNullOrBlank() &&
                    !confirmPassword.isNullOrBlank()
                ){
                    viewModel.inputEmail = viewModel.inputEmail!!.trim()
                    viewModel.inputPassword = viewModel.inputPassword!!.trim()
                    viewModel.inputName = viewModel.inputName!!.trim().uppercase()
                    viewModel.inputSurname = viewModel.inputSurname!!.trim().uppercase()
                    progressDialog.getInstance(requireActivity()).start("Loading...")
                    firebaseAuth.getFireBaseAuth().createUserWithEmailAndPassword(viewModel.inputEmail!!,viewModel.inputPassword!!).addOnCompleteListener {
                        if(it.isSuccessful)
                        {
                            MainScope().launch(Dispatchers.IO) {
                                try{
                                    var imageString:String? = null
                                    if(inputProfileImage!=null){
                                        imageString = bitmapToBase64(inputProfileImage!!)
                                    }
                                    val user = User(viewModel.inputName!!, viewModel.inputSurname!!, viewModel.inputEmail!!,imageString)
                                    userReference.getUserReference().child(it.result?.user!!.uid).setValue(user).addOnSuccessListener {
                                        firebaseAuth.getCurrentUser()?.sendEmailVerification()?.addOnSuccessListener {
                                            progressDialog.getInstance(requireActivity()).stop()
                                            Toast.makeText(requireContext(),"Verify Your Email",Toast.LENGTH_SHORT).show()
                                            findNavController().navigate(R.id.loginFragment)
                                        }?.addOnFailureListener {exception->
                                            progressDialog.getInstance(requireActivity()).stop()
                                            Log.i(TAG,exception.toString())
                                            Toast.makeText(requireContext(),exception.toString(),Toast.LENGTH_SHORT).show()
                                        }
                                    }.addOnFailureListener{exception->
                                        progressDialog.getInstance(requireActivity()).stop()
                                        Log.i(TAG,exception.toString())
                                        Toast.makeText(requireActivity(),exception.toString(),Toast.LENGTH_SHORT).show()
                                    }
                                }
                                catch (ex:Exception)
                                {
                                    Log.e(TAG,"RegistrationFragment.kt -> click event fragmentRegistrationRegisterButton -> $ex")
                                    Toast.makeText(requireActivity(),"Error!",Toast.LENGTH_SHORT).show()
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        requestPermissions(
                            arrayOf(
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.READ_MEDIA_IMAGES
                            ),
                            101
                        )
                    }
                    else{
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
    }

    private fun checkPermission(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            val cameraPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            )
            val readMediaImagesPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_MEDIA_IMAGES
            )
            return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                    readMediaImagesPermission == PackageManager.PERMISSION_GRANTED
        }
        else{
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
                    try {
                        binding.profileImage.setPadding(0, 0, 0, 0)
                        inputProfileImage = uriToBitmap(requireActivity(),selectedImageUri)
                        inputProfileImage = UtilityFunction.getCircularBitmap(inputProfileImage)
                        withContext(Dispatchers.Main){
                            binding.profileImage.setImageBitmap(inputProfileImage)
                        }
                    }
                    catch (ex:Exception)
                    {
                        Log.e(TAG,"RegistrationFragment.kt -> function onActivityResult() -> $ex")
                        Toast.makeText(requireActivity(),"Error! to load Image",Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
    }



}