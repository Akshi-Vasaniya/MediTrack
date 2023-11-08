package com.example.meditrack.homeActivity.userprofile.updateprofileimage

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.meditrack.R
import com.example.meditrack.dataModel.dataClasses.UserData
import com.example.meditrack.databinding.FragmentUpdateProfileImageBinding
import com.example.meditrack.databinding.FragmentUserProfileBinding
import com.example.meditrack.firebase.fBase
import com.example.meditrack.homeActivity.HomeActivityViewModel
import com.example.meditrack.utility.UtilityFunction
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class UpdateProfileImageFragment : Fragment() {

    companion object {
        fun newInstance() = UpdateProfileImageFragment()
    }

    private lateinit var viewModel: UpdateProfileImageViewModel
    private lateinit var binding: FragmentUpdateProfileImageBinding
    private var profileImageUri:Uri? = null
    private var imageDeleted:Boolean = false
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_update_profile_image, container, false)
        binding = FragmentUpdateProfileImageBinding.bind(view)
        progressDialog = CustomProgressDialog(requireContext())
        val receivedBundle = arguments
        if (receivedBundle != null) {
            val data = receivedBundle.getString("profileImageUrl")

            if(data!="null" && data!!.isNotBlank() && data.isNotEmpty()){
                Glide.with(requireActivity())
                    .load(data)
                    .into(binding.imageViewProfile)
            }
            else{
                profileImageUri=null
                imageDeleted=false
            }

            // Do something with the data
        }
        binding.btnSelectImage.setOnClickListener {
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



        binding.btnRemoveImage.setOnClickListener {
            if (!imageDeleted)
            {
                val drawableResourceId = resources.getIdentifier("profilepic", "drawable", requireContext().packageName)
                binding.imageViewProfile.setImageResource(drawableResourceId)
                profileImageUri = null
                imageDeleted = true
            }

            //profileImageUri = getImageUriFromImageView(binding.imageViewProfile)

        }

        binding.btnSave.setOnClickListener {
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
                        deleteImageAndData(fBase.getUserId(), object : DeletionCallback{
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
        return view
    }

    interface DeletionCallback {
        fun onSuccess(message: String)
        fun onFailure(errorMessage: String)
    }

    private fun getHomeActivityViewModel(): HomeActivityViewModel? {
        activity?.let {
            return ViewModelProvider(it)[HomeActivityViewModel::class.java]
        }
        return null
    }

    // Function to get the Uri from ImageView
    fun getImageUriFromImageView(imageView: ImageView): Uri? {
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        var imageUri: Uri? = null
        try {
            val file = File(requireContext().externalCacheDir, "${UUID.randomUUID()}.jpg")
            val fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
            imageUri = Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return imageUri
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
                                val bimapImage = UtilityFunction.uriToBitmap(requireActivity(), selectedImageUri)
                                withContext(Dispatchers.Main){
                                    binding.imageViewProfile.setImageBitmap(bimapImage)
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

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 102)
    }

    fun deleteImageAndData(userID: String, deletionCallback: DeletionCallback) {
        val storageRef = fBase.getStorageReference()
        val imageRef = storageRef.child("userProfileImage/$userID")

        // Delete the image from the Firebase Storage
        imageRef.delete().addOnSuccessListener {
            // If image deletion is successful, delete the corresponding data from the Realtime Database
            val databaseRef = fBase.getUserReference().child(userID)
            databaseRef.child("profileImage").removeValue().addOnSuccessListener {
                deletionCallback.onSuccess("Image and corresponding data deleted successfully.")
            }.addOnFailureListener {
                deletionCallback.onFailure("Failed to delete corresponding data from the database: $it")
            }
        }.addOnFailureListener {
            deletionCallback.onFailure("Failed to delete the image from the storage: $it")
        }
    }

    fun updateUserProfileImage(imageUri: Uri, callback: (Boolean, String) -> Unit) {
        val storageReference = fBase.getStorageReference()
        val databaseReference = fBase.getUserReference().child(fBase.getUserId())

        // Upload the image to Firebase Storage
        val userProfileImageRef = storageReference.child("userProfileImage/${fBase.getUserId()}")
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


}