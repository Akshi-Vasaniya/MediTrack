package com.example.meditrack.homeActivity.userprofile.updateprofileimage

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.meditrack.R
import com.example.meditrack.databinding.FragmentUpdateProfileImageBinding
import com.example.meditrack.firebase.FBase
import com.example.meditrack.utility.UtilsFunctions.Companion.toBitmap
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateProfileImageFragment : Fragment() {

    companion object {
        fun newInstance() = UpdateProfileImageFragment()
    }

    private lateinit var viewModel: UpdateProfileImageViewModel
    private lateinit var binding: FragmentUpdateProfileImageBinding
    private var profileImageUri:Uri? = null
    private var imageDeleted:Boolean = false
    private lateinit var progressDialog: CustomProgressDialog
    private var changesMade = false

//    override fun onPrepareOptionsMenu(menu: Menu) {
//        for(i in 0 until menu.size()){
//            menu.getItem(i).isVisible = false
//        }
//
//        super.onPrepareOptionsMenu(menu)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_update_profile_image, container, false)
        binding = FragmentUpdateProfileImageBinding.bind(view)

//        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this)[UpdateProfileImageViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDiscardChangesDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            val receivedBundle = arguments
            if (receivedBundle != null) {
                val data = receivedBundle.getString("profileImageUrl")

                if(data!="null" && data!!.isNotBlank() && data.isNotEmpty()){
                    Glide.with(requireActivity())
                        .load(data)
                        .into(imageViewProfile)
                }
                else{
                    profileImageUri=null
                    imageDeleted=true
                }

                // Do something with the data
            }

            btnSelectImage.setOnClickListener {
                if (checkPermission()) {
                    getContent.launch("image/*")
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

            btnRemoveImage.setOnClickListener {
                if (!imageDeleted)
                {
                    //val drawableResourceId = resources.getIdentifier("profilepic", "drawable", requireContext().packageName)
                    //val drawableResourceId = R.drawable.profilepic
                    //imageViewProfile.setImageResource(drawableResourceId)
                    //profileImageUri = null
                    //imageDeleted = true
                    showConfirmationDialog()
                }

                //profileImageUri = getImageUriFromImageView(binding.imageViewProfile)

            }

            btnSave.setOnClickListener {
                if (profileImageUri!=null)
                {
                    progressDialog.start("Updating....")

                    MainScope().launch(Dispatchers.IO) {
                        updateUserProfileImage(profileImageUri!!) { isSuccess, message ->
                            if (isSuccess) {
                                // Handle success
                                progressDialog.stop()
                                findNavController().popBackStack()
                                //Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            } else {
                                // Handle failure
                                progressDialog.stop()
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                }else{
                    if (imageDeleted)
                    {
                        progressDialog.start("Updating....")
                        MainScope().launch(Dispatchers.IO) {
                            deleteImageAndData(FBase.getUserId(), object : DeletionCallback{
                                override fun onSuccess(message: String) {

                                    progressDialog.stop()
                                    findNavController().popBackStack()
                                }

                                override fun onFailure(errorMessage: String) {
                                    progressDialog.stop()
                                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                                }

                            })
                        }
                    }
                    else{
                        findNavController().popBackStack()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            try {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    MainScope().launch(Dispatchers.IO) {
                        // The user has selected an image. You can get the image URI from the data intent.
                        val selectedImageUri: Uri? = result.uri
                        if (selectedImageUri != null) {
                            try {
                                profileImageUri = selectedImageUri
                                val bimapImage = selectedImageUri.toBitmap(requireContext())
                                withContext(Dispatchers.Main){
                                    binding.imageViewProfile.setImageBitmap(bimapImage)
                                    changesMade = true
                                    imageDeleted = false
                                }
                            }
                            catch (ex:Exception)
                            {
                                Toast.makeText(requireActivity(),"Error! to load Image", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.i("UpdateProfileImage Crop Image","$requestCode")
                }
            }
            catch (ex:Exception)
            {
                Log.i("UpdateProfileImage","${ex.message}")
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
                getContent.launch("image/*")
            }
        }
    }

    interface DeletionCallback {
        fun onSuccess(message: String)
        fun onFailure(errorMessage: String)
    }
    private fun deleteImageAndData(userID: String, deletionCallback: DeletionCallback) {
        val storageRef = FBase.getStorageReference()
        val imageRef = storageRef.child("userProfileImage/$userID")

        // Delete the image from the Firebase Storage
        imageRef.delete().addOnSuccessListener {
            // If image deletion is successful, delete the corresponding data from the Realtime Database
            val databaseRef = FBase.getUserReference().child(userID)
            databaseRef.child("profileImage").removeValue().addOnSuccessListener {
                deletionCallback.onSuccess("Image and corresponding data deleted successfully.")
            }.addOnFailureListener {
                deletionCallback.onFailure("Failed to delete corresponding data from the database: $it")
            }
        }.addOnFailureListener {
            deletionCallback.onFailure("Failed to delete the image from the storage: $it")
        }
    }

    private fun updateUserProfileImage(imageUri: Uri, callback: (Boolean, String) -> Unit) {
        val storageReference = FBase.getStorageReference()
        val databaseReference = FBase.getUserReference().child(FBase.getUserId())

        // Upload the image to Firebase Storage
        val userProfileImageRef = storageReference.child("userProfileImage/${FBase.getUserId()}")
        val uploadTask = userProfileImageRef.putFile(imageUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            userProfileImageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                // Update the profile image URL in the Realtime Database
                databaseReference.child("profileImage").setValue(downloadUri.toString())
                    .addOnSuccessListener {
                        // Profile image URL updated successfully
                        callback(true, "Profile Updated")
                    }
                    .addOnFailureListener { e ->
                        // Error updating the profile image URL
                        callback(false, "Error occur while updating profile image")
                    }
            } else {
                // Handle failures
                callback(false, "Error occur while uploading profile image")
            }
        }
    }


    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to delete your profile picture?")

        builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
            // Handle deletion logic here
            deleteProfilePic()
        }

        builder.setNegativeButton("No") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun deleteProfilePic() {
        //val drawableResourceId = resources.getIdentifier("profilepic", "drawable", requireContext().packageName)
        val drawableResourceId = R.drawable.profilepic
        binding.imageViewProfile.setImageResource(drawableResourceId)
        changesMade=true
        profileImageUri = null
        imageDeleted = true
    }

    private fun showDiscardChangesDialog() {
        if (changesMade) {
            val builder = AlertDialog.Builder(requireContext())

            builder.setTitle("Discard Changes")
            builder.setMessage("Do you want to discard changes to your profile picture?")

            builder.setPositiveButton("Discard") { _: DialogInterface, _: Int ->
                // Handle discard logic here
                findNavController().popBackStack()
            }

            builder.setNegativeButton("Save") { _: DialogInterface, _: Int ->
                // Handle save logic here
                binding.btnSave.performClick()
            }

            builder.setNeutralButton("Cancel") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        } else {
            // If no changes are made, allow normal back press behavior
            findNavController().popBackStack()
        }
    }
}