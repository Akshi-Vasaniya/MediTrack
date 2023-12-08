package com.example.meditrack.homeActivity.ocr

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Menu
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.databinding.FragmentOCRBinding
import com.example.meditrack.permissionsHandle.PermissionUtils.Companion.requestPermissions
import com.example.meditrack.permissionsHandle.PermissionUtils.Companion.toCheckCameraAccess
import com.example.meditrack.utility.UtilsFunctions.Companion.showToast
import com.example.meditrack.utility.UtilsFunctions.Companion.toByteArray
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class OCRFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback,
    OverlayView.OverlaySelectionListener {

    private val ORIENTATIONS = SparseIntArray()

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 0)
        ORIENTATIONS.append(Surface.ROTATION_90, 90)
        ORIENTATIONS.append(Surface.ROTATION_180, 180)
        ORIENTATIONS.append(Surface.ROTATION_270, 270)
    }

    companion object {
        fun newInstance() = OCRFragment()
    }

    private lateinit var viewModel: OCRViewModel
    private lateinit var binding: FragmentOCRBinding
    private lateinit var recognizer: TextRecognizer
    private val tAG = "ju"
    private val savedTestTag = "SavedText"
    private var currentRotation: Float = 0f


    //private val SAVED_IMAGE_BITMAP = "SavedImage"
    private val requestCodePermissions = 10
    private val requiredPermissions = arrayOf(Manifest.permission.CAMERA)

    private lateinit var camera: Camera
    private var savedBitmap: Bitmap? = null

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var overlayView: OverlayView
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var imageAnalysis: ImageAnalysis

    override fun onPrepareOptionsMenu(menu: Menu) {
        for(i in 0 until menu.size()){
            menu.getItem(i).isVisible = false
        }

        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_o_c_r, container, false)
        viewModel = ViewModelProvider(this)[OCRViewModel::class.java]
        binding = FragmentOCRBinding.bind(view)

        setHasOptionsMenu(true)

        progressDialog = CustomProgressDialog(requireContext())

        overlayView = OverlayView(requireContext())
        overlayView.overlaySelectionListener = this
        val frameLayout: ViewGroup =
            view!!.findViewById(R.id.camera_fragment_layout) // Replace with your layout id
        frameLayout.addView(overlayView)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            val savedText = savedInstanceState.getString(savedTestTag)
            binding.apply {
                if (isTextValid(savedText)) {
                    textInImageLayout.visibility = View.VISIBLE
                    textInImage.text = savedInstanceState.getString(savedTestTag)
                }
                if (savedBitmap != null) {
                    previewImage.visibility = View.VISIBLE
                    previewImage.setImageBitmap(savedBitmap)
                }
                //previewImage.setImageBitmap(savedInstanceState.getParcelable(SAVED_IMAGE_BITMAP))
            }
        }
        binding.apply {
            torchImage.setOnClickListener {
                Log.i(tAG, "torchButton.setOnClickListener: true")
                try {
                    camera.apply {
                        //torchImage.setBackgroundColor(resources.getColor(R.color.purple_200))
                        Log.i(tAG, "camera.apply: true")
                        if (cameraInfo.hasFlashUnit()) {
                            Log.i(tAG, "cameraInfo.hasFlashUnit(): ${cameraInfo.hasFlashUnit()}")
                            cameraControl.enableTorch(cameraInfo.torchState.value == TorchState.OFF)
                        } else {
                            requireContext().showToast(getString(R.string.torch_not_available_msg))
                        }

                        cameraInfo.torchState.observe(requireActivity()) { torchState ->
                            if (torchState == TorchState.OFF) {
                                torchImage.setImageResource(R.drawable.ic_flashlight_on)
                            } else {
                                torchImage.setImageResource(R.drawable.ic_flashlight_off)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }


        }

        init()

        binding.ocrCamera.setOnClickListener {
            overlayView.clearOverlay()
            binding.viewFinder.visibility = View.VISIBLE
            binding.previewImage.visibility = View.GONE
            binding.textInImageLayout.visibility = View.GONE
            savedBitmap = null
        }
        binding.ocrGallery.setOnClickListener {
            overlayView.clearOverlay()
            binding.textInImageLayout.visibility = View.GONE
            getContent.launch("image/*")
        }

        binding.saveMedName.setOnClickListener {
            if (viewModel.listSelectedMedName.isEmpty()) {
                Toast.makeText(requireContext(), "Please Select Medicine Name", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Create a Bundle
                val bundle = Bundle()
                MainScope().launch(Dispatchers.Main) {
                    progressDialog.start("Saving...")
                    var scanImageByteArray: ByteArray? = null
                    var medName: String? = null
                    val job1 = launch { scanImageByteArray = savedBitmap!!.toByteArray() }
                    val job2 = launch { medName = bindSelectedMedName() }

                    job1.join()
                    job2.join()

                    // Add the bitmap to the Bundle
                    bundle.putByteArray("scanImageByteArray", scanImageByteArray)

                    // Add the ArrayList to the Bundle
                    bundle.putString("ocrMedNameString", medName)
                    progressDialog.stop()
                    if(scanImageByteArray==null)
                    {
                        return@launch
                    }
                    findNavController().popBackStack(R.id.addMedicineFragment, true)
                    findNavController().navigate(R.id.addMedicineFragment, bundle)

                }

            }
        }
    }

    private fun bindSelectedMedName():String{
        var medName = ""
        viewModel.listSelectedMedName.forEach {
            medName += it.second
        }
        medName = medName.trim()
        medName = medName.replace("\n", " ")
        return medName
    }

    private fun init() {

        cameraExecutor = Executors.newSingleThreadExecutor()

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // Request camera permissions
        if (requireContext().toCheckCameraAccess()) {
            startCamera()
        } else {
            requireContext().requestPermissions(mutableListOf(Manifest.permission.CAMERA)){
                if(it)
                {
                    startCamera()
                }
                else{
                    showPermissionSettingsDialog(requireContext())
                }
            }
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
                        requireContext().showToast(getString(R.string.camera_error_default_msg))
                    }
                }
            }



            copyToClipboard.setOnClickListener {
                val textToCopy = textInImage.text
                if (isTextValid(textToCopy.toString())) {
                    copyToClipboard(textToCopy)
                } else {
                    requireContext().showToast(getString(R.string.no_text_found))
                }
            }

            /*share.setOnClickListener {
                val textToCopy = textInImage.text.toString()
                if (isTextValid(textToCopy)) {
                    shareText(textToCopy)
                } else {
                    requireContext().showToast(getString(R.string.no_text_found))
                }
            }*/

            close.setOnClickListener {
                textInImageLayout.visibility = View.GONE
            }

        }

    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            try {
                if (uri != null) {
                    CropImage.activity(uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAllowRotation(true)
                        .setMultiTouchEnabled(true)
                        .start(requireActivity(), this)
                }
            } catch (ex: Exception) {
                Log.i("OCR Select Image", "${ex.message}")
            }
        }


    @Deprecated("Deprecated in Java")
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
                    Log.i("OCR Crop Image", "$requestCode")
                }
            } catch (ex: Exception) {
                requireContext().showToast("Error")
                Log.i("OCR", "${ex.message}")
            }

        }
    }


    private fun runTextRecognition(bitmap: Bitmap) {
        val chunkSize = 2
        val inputImage = InputImage.fromBitmap(bitmap,0)

        recognizer
            .process(inputImage)
            .addOnSuccessListener { text ->
                viewModel.listSelectedMedName = ArrayList()
                val rectList: ArrayList<Pair<RectF, String>> = ArrayList()
                val job1 = MainScope().launch(Dispatchers.IO) {

                    for (textBlock in text.textBlocks) {
                        //Log.d(tAG, "TextBlock text is: " + textBlock.text)
                        //Log.d(tAG, "TextBlock boundingbox is: " + textBlock.boundingBox)
                        //Log.d( tAG, "TextBlock cornerpoint is: " + Arrays.toString(textBlock.cornerPoints) )

                        // Split the list into chunks
                        val chunks = textBlock.lines.chunked(chunkSize)
                        val jobs = ArrayList<Job>()
                        for (chunk in chunks) {
                            val job = launch {
                                for (line in chunk) {
                                    val box = RectF(line.boundingBox ?: continue)
                                    val txt = line.text
                                    rectList.add(Pair(box, txt))
                                }
                            }
                            jobs.add(job)
                        }
                        // Wait for all jobs to complete
                        jobs.forEach { it.join() }

                        /*for (line in textBlock.lines) {
                            val box = async { RectF(line.boundingBox) }
                            val txt = async { line.text }
                            rectList.add(Pair(box.await(), txt.await()))
                            Log.d(tAG, "Line text is: ${txt.await()}")
                            Log.d(tAG, "Line boundingbox is: " + line.boundingBox)
                            Log.d(tAG, "Line cornerpoint is: " + Arrays.toString(line.cornerPoints))
                            for (element in line.elements) {
                                Log.d(tAG, "Element text is: " + element.text)
                                Log.d(tAG, "Element boundingbox is: " + element.boundingBox)
                                Log.d(
                                    tAG,
                                    "Element cornerpoint is: " + Arrays.toString(element.cornerPoints)
                                )
                                Log.d(tAG, "Element language is: " + element.recognizedLanguage)
                            }
                        }*/
                    }
                }
                runBlocking {
                    job1.join()
                    overlayView.setBoundingRects(rectList)
                }
                /*var filterOutput = UtilityFunction.filterFirstSecondThirdLargestRect(rectList)
                var filterOutput = UtilityFunction.findNearlySimilarRectangles(rectList,0.9)

                filterOutput?.let { (firstList, secondList, thirdList) ->

                    //overlayView.setBoundingRects((ArrayList(firstList)+ArrayList(secondList)+ArrayList(thirdList)) as ArrayList<Pair<Rect, String>>)
                    //binding.textInImageLayout.visibility = View.VISIBLE
                    //processTextRecognitionResult(text)

                    // Accessing the first list of pairs
                    println("First List:")
                    firstList.forEach { (rect, string) ->
                        println("Rect: $rect, String: $string")
                    }

                    // Accessing the second list of pairs
                    println("Second List:")
                    secondList.forEach { (rect, string) ->
                        println("Rect: $rect, String: $string")
                    }

                    // Accessing the third list of pairs
                    println("Third List:")
                    thirdList.forEach { (rect, string) ->
                        println("Rect: $rect, String: $string")
                    }
                }
                overlayView.setBoundingRects(rectList)
                binding.textInImageLayout.visibility = View.VISIBLE
                processTextRecognitionResult(text)*/

            }.addOnFailureListener { e ->
                e.printStackTrace()
                requireContext().showToast(e.localizedMessage ?: getString(R.string.error_default_msg))
            }
    }


    /*private fun processTextRecognitionResult(result: Text) {
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

        Linkify.addLinks(
            binding.textInImage,
            Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS
        )

    }*/

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


                /*binding.apply {

                    camera.apply {
                        //torchImage.setBackgroundColor(resources.getColor(R.color.purple_200))
                        Log.i(tAG,"camera.apply: true")
                        if (cameraInfo.hasFlashUnit()) {
                            Log.i(tAG,"cameraInfo.hasFlashUnit(): ${cameraInfo.hasFlashUnit()}")

                            torchButton.setOnClickListener {
                                Log.i(tAG,"torchButton.setOnClickListener: true")
                                cameraControl.enableTorch(cameraInfo.torchState.value == TorchState.OFF)
                            }
                        } else {
                            torchButton.setOnClickListener {
                                Log.i(tAG,"torchButton.setOnClickListener: true")
                                requireContext().showToast(getString(R.string.torch_not_available_msg))
                            }
                        }

                        cameraInfo.torchState.observe(requireActivity()) { torchState ->
                            if (torchState == TorchState.OFF) {
                                torchButton.setImageResource(R.drawable.ic_flashlight_on)
                            } else {
                                torchButton.setImageResource(R.drawable.ic_flashlight_off)
                            }
                        }

                    }

                }*/


            } catch (exc: Exception) {
                requireContext().showToast(getString(R.string.error_default_msg))
                Log.e(tAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireActivity()))

    }

    /*private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(), requiredPermissions, requestCodePermissions
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {


        if (requestCode == requestCodePermissions) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                requireContext().showToast(
                    getString(R.string.permission_denied_msg)
                )
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }*/

    private fun isTextValid(text: String?): Boolean {
        if (text == null)
            return false

        return text.isNotEmpty() and (text != getString(R.string.no_text_found))
    }

    /*private fun shareText(text: String) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_text_title)))
    }*/

    private fun copyToClipboard(text: CharSequence) {
        val clipboard =
            ContextCompat.getSystemService(requireActivity(), ClipboardManager::class.java)
        val clip = ClipData.newPlainText("label", text)
        clipboard?.setPrimaryClip(clip)
        requireContext().showToast(getString(R.string.clipboard_text))
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
            outState.putString(savedTestTag, textInImage)
        }
        /*if (binding.previewImage.visibility == View.VISIBLE) {
            outState.putParcelable(SAVED_IMAGE_BITMAP, binding.previewImage.drawable.toBitmap())
        }*/
    }

    override fun onRectSelected(rect: ArrayList<Pair<RectF, String>>) {
        viewModel.listSelectedMedName = rect
        Log.i("OCR Fragment", rect.toString())
    }

    private val permissionActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (!requireContext().toCheckCameraAccess()){
            showPermissionSettingsDialog(requireContext())
        }
        else{
            startCamera()
        }
    }
    fun showPermissionSettingsDialog(context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setTitle("Camera Permission")
        alertDialogBuilder.setMessage("Camera Permission are required for this app.")

        alertDialogBuilder.setPositiveButton("Settings") { _, _ ->
            // Open device settings for location
            val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName))
            permissionActivityResultLauncher.launch(settingsIntent)
            //context.startActivity(settingsIntent)
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            requireActivity().finishAffinity()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

}
