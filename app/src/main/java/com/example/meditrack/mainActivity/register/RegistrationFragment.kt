package com.example.meditrack.mainActivity.register

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.dataModel.dataClasses.UserData
import com.example.meditrack.databinding.FragmentRegistrationBinding
import com.example.meditrack.exception.HandleException
import com.example.meditrack.firebase.FBase
import com.example.meditrack.homeActivity.medicine.addMedicine.AddMedicineFragment
import com.example.meditrack.permissionsHandle.PermissionUtils.Companion.requestPermissions
import com.example.meditrack.permissionsHandle.PermissionUtils.Companion.toCheckCameraAccess
import com.example.meditrack.permissionsHandle.PermissionUtils.Companion.toCheckReadMediaImagesAccess
import com.example.meditrack.permissionsHandle.PermissionUtils.Companion.toCheckReadStorageAccess
import com.example.meditrack.permissionsHandle.PermissionUtils.Companion.toCheckWriteStorageAccess
import com.example.meditrack.regularExpression.ListPattern
import com.example.meditrack.regularExpression.MatchPattern.Companion.validate
import com.example.meditrack.utility.UtilsFunctions.Companion.toBitmap
import com.example.meditrack.utility.UtilsFunctions.Companion.toCircularBitmap
import com.example.meditrack.utility.UtilsFunctions.Companion.toUri
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
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
    private lateinit var progressDialog: CustomProgressDialog
    private val tag = "RegisterFragment"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_registration, container, false)
        viewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]
        binding = FragmentRegistrationBinding.bind(view)
        progressDialog= CustomProgressDialog(requireActivity())
        return binding.root
    }

    /*override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }*/


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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

                if(validateUserInput()){
                    viewModel.inputEmail = viewModel.inputEmail!!.trim()
                    viewModel.inputPassword = viewModel.inputPassword!!.trim()
                    viewModel.inputName = viewModel.inputName!!.trim().uppercase()
                    viewModel.inputSurname = viewModel.inputSurname!!.trim().uppercase()
                    progressDialog.start("Loading...")
                    FBase.getFireBaseAuth().createUserWithEmailAndPassword(viewModel.inputEmail!!,viewModel.inputPassword!!).addOnCompleteListener {
                        if(it.isSuccessful)
                        {
                            MainScope().launch(Dispatchers.IO) {
                                try{
                                    //var imageString:String? = null
                                    if(inputProfileImage!=null){
                                        val imageUri = inputProfileImage!!.toUri(requireContext())
                                        uploadImageToFirebaseStorage(it.result?.user!!.uid,imageUri!!, object :
                                            AddMedicineFragment.UploadCallback {
                                            override fun onUploadSuccess(downloadUrl: String) {
                                                try {
                                                    val user = UserData(viewModel.inputName!!, viewModel.inputSurname!!, viewModel.inputEmail!!,downloadUrl)
                                                    FBase.getUserReference().child(it.result?.user!!.uid).setValue(user).addOnSuccessListener {
                                                        FBase.getCurrentUser()?.sendEmailVerification()?.addOnSuccessListener {
                                                            progressDialog.stop()
                                                            Toast.makeText(requireContext(),"Verify Your Email",Toast.LENGTH_SHORT).show()
                                                            findNavController().navigate(R.id.loginFragment)
                                                        }?.addOnFailureListener {exception->
                                                            progressDialog.stop()
                                                            Log.i(tag,exception.toString())
                                                            Toast.makeText(requireContext(),exception.toString(),Toast.LENGTH_SHORT).show()
                                                        }
                                                    }.addOnFailureListener{exception->
                                                        progressDialog.stop()
                                                        Log.i(tag,exception.toString())
                                                        Toast.makeText(requireActivity(),exception.toString(),Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                catch (ex:java.lang.Exception)
                                                {
                                                    progressDialog.stop()
                                                }
                                            }

                                            override fun onUploadFailure(exception: Exception) {
                                                progressDialog.stop()
                                                Toast.makeText(requireContext(),"onUploadFailure: Error",Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                        //imageString = bitmapToBase64(inputProfileImage!!)
                                    }

                                }
                                catch (ex:Exception)
                                {
                                    Log.e(tag,"RegistrationFragment.kt -> click event fragmentRegistrationRegisterButton -> $ex")
                                    Toast.makeText(requireActivity(),"Error!",Toast.LENGTH_SHORT).show()
                                }

                            }
                        }
                    }.addOnFailureListener {
                        progressDialog.stop()
                        HandleException.firebaseCommonExceptions(requireContext(),it,tag)
                    }
                }

            }

            profileImage.setOnClickListener {
                if (requireContext().toCheckCameraAccess() && requireContext().toCheckReadStorageAccess() && requireContext().toCheckReadMediaImagesAccess()) {
                    //openImagePicker()
                    getContent.launch("image/*")
                } else {
                    requireContext().requestPermissions(mutableListOf(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_MEDIA_IMAGES)){
                        if (it) {
                            getContent.launch("image/*")
                        }
                        else{
                            showPermissionsSettingsDialog(requireContext())
                        }
                    }
                }
            }
        }
    }
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        try {
            if(uri!=null)
            {
                CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAllowRotation(true)
                    .setMultiTouchEnabled(true)
                    .setAspectRatio(1,1)
                    .start(requireActivity(),this)
            }
        }
        catch (ex:Exception)
        {
            Log.i("OCR Select Image","${ex.message}")
        }
    }

    private fun uploadImageToFirebaseStorage(uid:String, imageUri: Uri, callback: AddMedicineFragment.UploadCallback) {
        val storageRef = FBase.getStorageReference()
        val imagesRef = storageRef.child("userProfileImage/${uid}")
        val uploadTask = imagesRef.putFile(imageUri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imagesRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                downloadUri?.let { callback.onUploadSuccess(it.toString()) }
            } else {
                // Handle failures
                task.exception?.let { callback.onUploadFailure(it) }
            }
        }
    }

    private fun validateUserInput():Boolean
    {
        return (binding.fragmentRegistrationNameTextInputLayout.helperText==null &&
                binding.fragmentRegistrationSurnameTextInputLayout.helperText==null &&
                binding.fragmentRegistrationEmailTextInputLayout.helperText==null &&
                binding.fragmentRegistrationPasswordTextInputLayout.helperText==null &&
                binding.fragmentRegistrationConfirmPasswordTextInputLayout.helperText==null &&
                !viewModel.inputName.isNullOrBlank() &&
                !viewModel.inputSurname.isNullOrBlank() &&
                !viewModel.inputEmail.isNullOrBlank() &&
                !viewModel.inputPassword.isNullOrBlank() &&
                !viewModel.inputConfirmPassword.isNullOrBlank())
    }


    fun showPermissionsSettingsDialog(context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setTitle("Required Permission")
        alertDialogBuilder.setMessage("Camera and other permissions are required for this app.")

        alertDialogBuilder.setPositiveButton("Settings") { dialog, _ ->
            // Open device settings for location
            val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:" + requireActivity().packageName))
            //settingsIntent.flags=Intent.FLAG_ACTIVITY_NEW_TASK
            activityResultLauncher.launch(settingsIntent)
            dialog.dismiss()
            //context.startActivity(settingsIntent)
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            requireActivity().finishAffinity()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (requireContext().toCheckCameraAccess() && requireContext().toCheckReadStorageAccess() && requireContext().toCheckReadMediaImagesAccess()) {
            getContent.launch("image/*")
        }
        else{
            showPermissionsSettingsDialog(requireContext())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            try {
                val result = CropImage.getActivityResult(data)
                if (resultCode == RESULT_OK) {
                    MainScope().launch(Dispatchers.IO) {
                        // The user has selected an image. You can get the image URI from the data intent.
                        val selectedImageUri: Uri? = result.uri
                        if (selectedImageUri != null) {
                            try {
                                binding.profileImage.setPadding(0, 0, 0, 0)
                                inputProfileImage = selectedImageUri.toBitmap(requireContext())
                                inputProfileImage = inputProfileImage.toCircularBitmap()
                                withContext(Dispatchers.Main){
                                    binding.profileImage.setImageBitmap(inputProfileImage)
                                }
                            }
                            catch (ex:Exception)
                            {
                                Log.e(tag,"RegistrationFragment.kt -> function onActivityResult() -> $ex")
                                Toast.makeText(requireActivity(),"Error! to load Image",Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.i("Registration Crop Image","$requestCode")
                }
            }
            catch (ex:Exception)
            {
                Log.i("Registration","${ex.message}")
            }

        }
    }

//    private fun openImagePicker() {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        startActivityForResult(intent, 102)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == 102 && resultCode == RESULT_OK) {
//            MainScope().launch(Dispatchers.IO) {
//                // The user has selected an image. You can get the image URI from the data intent.
//                val selectedImageUri: Uri? = data?.data
//                if (selectedImageUri != null) {
//                    try {
//                        binding.profileImage.setPadding(0, 0, 0, 0)
//                        inputProfileImage = uriToBitmap(requireActivity(),selectedImageUri)
//                        inputProfileImage = UtilityFunction.getCircularBitmap(inputProfileImage)
//                        withContext(Dispatchers.Main){
//                            binding.profileImage.setImageBitmap(inputProfileImage)
//                        }
//                    }
//                    catch (ex:Exception)
//                    {
//                        Log.e(tag,"RegistrationFragment.kt -> function onActivityResult() -> $ex")
//                        Toast.makeText(requireActivity(),"Error! to load Image",Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//
//        }
//    }

}