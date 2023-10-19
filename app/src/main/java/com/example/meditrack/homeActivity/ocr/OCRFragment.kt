package com.example.meditrack.homeActivity.ocr

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.example.meditrack.R
import com.example.meditrack.databinding.FragmentOCRBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class OCRFragment : Fragment() {

    companion object {
        fun newInstance() = OCRFragment()
    }

    private lateinit var viewModel: OCRViewModel
    private lateinit var binding:FragmentOCRBinding
    private lateinit var recognizer: TextRecognizer
    private val TAG = "Testing"
    private val SAVED_TEXT_TAG = "SavedText"

    //private val SAVED_IMAGE_BITMAP = "SavedImage"
    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    lateinit var camera: Camera
    var savedBitmap: Bitmap? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_o_c_r, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOCRBinding.bind(view)
        if (savedInstanceState != null) {
            val savedText = savedInstanceState.getString(SAVED_TEXT_TAG)
            binding.apply {
                if (isTextValid(savedText)) {
                    textInImageLayout.visibility = View.VISIBLE
                    textInImage.text = savedInstanceState.getString(SAVED_TEXT_TAG)
                }
                if (savedBitmap != null) {
                    previewImage.visibility = View.VISIBLE
                    previewImage.setImageBitmap(savedBitmap)
                }
                //previewImage.setImageBitmap(savedInstanceState.getParcelable(SAVED_IMAGE_BITMAP))
            }
        }
        init()
        binding.ocrCamera.setOnClickListener {
            if(!allPermissionsGranted()){
                requestPermissions()
            } else {
                binding.viewFinder.visibility=View.VISIBLE
                binding.previewImage.visibility = View.GONE
                binding.textInImageLayout.visibility = View.GONE
                savedBitmap = null
            }
        }
        binding.ocrGallery.setOnClickListener {
            binding.textInImageLayout.visibility = View.GONE
            getContent.launch("image/*")
        }
    }

    private fun init() {

        cameraExecutor = Executors.newSingleThreadExecutor()

        recognizer = TextRecognition.getClient()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        binding.apply {
            extractTextButton.setOnClickListener {
                when {
                    previewImage.visibility == View.VISIBLE -> {
                        savedBitmap = previewImage.drawable.toBitmap()
                        runTextRecognition(savedBitmap!!)
                    }
                    viewFinder.bitmap != null -> {
                        previewImage.visibility = View.VISIBLE
                        savedBitmap = viewFinder.bitmap
                        previewImage.setImageBitmap(viewFinder.bitmap!!)
                        runTextRecognition(savedBitmap!!)
                    }
                    else -> {
                        showToast(getString(R.string.camera_error_default_msg))
                    }
                }
            }



            copyToClipboard.setOnClickListener {
                val textToCopy = textInImage.text
                if (isTextValid(textToCopy.toString())) {
                    copyToClipboard(textToCopy)
                } else {
                    showToast(getString(R.string.no_text_found))
                }
            }

            share.setOnClickListener {
                val textToCopy = textInImage.text.toString()
                if (isTextValid(textToCopy)) {
                    shareText(textToCopy)
                } else {
                    showToast(getString(R.string.no_text_found))
                }
            }

            close.setOnClickListener {
                textInImageLayout.visibility = View.GONE
            }

        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                showToast(
                    getString(R.string.permission_denied_msg)
                )
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
                    binding.viewFinder.visibility = View.GONE
                    binding.previewImage.apply {
                        visibility = View.VISIBLE
                        setImageURI(result.uri)
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.i("OCR Crop Image","$requestCode")
                }
            }
            catch (ex:Exception)
            {
                showToast("Error")
                Log.i("OCR","${ex.message}")
            }

        }
    }

    private fun runTextRecognition(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        recognizer
            .process(inputImage)
            .addOnSuccessListener { text ->
                binding.textInImageLayout.visibility = View.VISIBLE
                processTextRecognitionResult(text)
            }.addOnFailureListener { e ->
                e.printStackTrace()
                showToast(e.localizedMessage ?: getString(R.string.error_default_msg))
            }
    }

    private fun processTextRecognitionResult(result: Text) {
        var finalText = ""
        for (block in result.textBlocks) {
            for (line in block.lines) {
                finalText += line.text + " \n"
            }
            finalText += "\n"
        }

        Log.d(TAG, finalText)
        Log.d(TAG, result.text)

        binding.textInImage.text = if (finalText.isNotEmpty()) {
            finalText
        } else {
            getString(R.string.no_text_found)
        }

        Linkify.addLinks(binding.textInImage, Linkify.ALL)

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )


                binding.apply {

                    camera.apply {
                        torchImage.setBackgroundColor(resources.getColor(R.color.purple_200))
                        if (cameraInfo.hasFlashUnit()) {
                            torchButton.setOnClickListener {
                                cameraControl.enableTorch(cameraInfo.torchState.value == TorchState.OFF)
                            }
                        } else {
                            torchButton.setOnClickListener {
                                showToast(getString(R.string.torch_not_available_msg))
                            }
                        }

                        cameraInfo.torchState.observe(requireActivity()) { torchState ->
                            if (torchState == TorchState.OFF) {
                                torchImage.setImageResource(R.drawable.ic_flashlight_on)
                            } else {
                                torchImage.setImageResource(R.drawable.ic_flashlight_off)
                            }
                        }

                    }

                }
            } catch (exc: Exception) {
                showToast(getString(R.string.error_default_msg))
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireActivity()))

    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    private fun isTextValid(text: String?): Boolean {
        if (text == null)
            return false

        return text.isNotEmpty() and !text.equals(getString(R.string.no_text_found))
    }

    private fun shareText(text: String) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_text_title)))
    }

    private fun copyToClipboard(text: CharSequence) {
        val clipboard =
            ContextCompat.getSystemService(requireActivity(), ClipboardManager::class.java)
        val clip = ClipData.newPlainText("label", text)
        clipboard?.setPrimaryClip(clip)
        showToast(getString(R.string.clipboard_text))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.isShutdown
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
        val textInImage = (binding.textInImage.text).toString()
        if (isTextValid(textInImage)) {
            outState.putString(SAVED_TEXT_TAG, textInImage)
        }
        /*if (binding.previewImage.visibility == View.VISIBLE) {
            outState.putParcelable(SAVED_IMAGE_BITMAP, binding.previewImage.drawable.toBitmap())
        }*/
    }

}