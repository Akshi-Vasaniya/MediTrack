package com.example.meditrack.mainActivity.login

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.dataModel.EmailAvailabilityCallback
import com.example.meditrack.dataModel.dataClasses.UserData
import com.example.meditrack.databinding.FragmentLoginBinding
import com.example.meditrack.exception.HandleException
import com.example.meditrack.firebase.FBase
import com.example.meditrack.homeActivity.HomeActivity
import com.example.meditrack.permissionsHandle.PermissionUtils.Companion.isLocationEnabled
import com.example.meditrack.permissionsHandle.PermissionUtils.Companion.requestPermissions
import com.example.meditrack.permissionsHandle.PermissionUtils.Companion.toCheckLocationAccess
import com.example.meditrack.regularExpression.ListPattern
import com.example.meditrack.regularExpression.MatchPattern.Companion.validate
import com.example.meditrack.userSession.SessMan
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*


class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding
    private val tag="LoginFragment"
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var sessMan: SessMan

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view  = inflater.inflate(R.layout.fragment_login, container, false)
        binding = FragmentLoginBinding.bind(view)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        progressDialog= CustomProgressDialog(requireActivity())
        sessMan = SessMan(requireContext())
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
        if (!requireContext().toCheckLocationAccess()){
            requireContext().requestPermissions(mutableListOf(android.Manifest.permission.ACCESS_FINE_LOCATION)){
                if(it)
                {
                    if(!requireContext().isLocationEnabled()){
                        showLocationSettingsDialog(requireContext())
                    }
                }
                else{
                    showPermissionSettingsDialog(requireContext())
                }
            }
        }
        else{
            if(!requireContext().isLocationEnabled()){
                showLocationSettingsDialog(requireContext())
            }
        }

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

            fragmentForgotPasswordTextview.setOnClickListener {
                if(fragmentLoginEmailTextInputLayout.helperText==null && viewModel.inputEmail!=null){
                    viewModel.inputEmail = viewModel.inputEmail!!.trim()
                    progressDialog.start("Loading...")
                    UserData.isEmailAvailable(viewModel.inputEmail!!, object : EmailAvailabilityCallback{
                        override fun onResult(isAvailable: Boolean) {
                            if(isAvailable){
                                progressDialog.stop()
//                                val snackbar = Snackbar.make(view, "Email is exist", Snackbar.LENGTH_LONG)
//                                snackbar.show()
                                FBase.getFireBaseAuth().setLanguageCode("en")
                                FBase.getFireBaseAuth().sendPasswordResetEmail(viewModel.inputEmail!!).addOnCompleteListener {
                                    progressDialog.stop()
                                    val snackbar = Snackbar.make(view, "Reset Password email has been sent successfully", Snackbar.LENGTH_LONG)
                                    snackbar.show()
                                }.addOnFailureListener {
                                    progressDialog.stop()
                                    HandleException.firebaseCommonExceptions(requireContext(), it, tag)
                                }
                            }
                            else{
                                progressDialog.stop()
                                val snackbar = Snackbar.make(view, "Email not found! Please Register first", Snackbar.LENGTH_LONG)
                                snackbar.show()
                            }
                        }
                    })
                }
                else{
                    val snackbar = Snackbar.make(view, "Please enter the email first", Snackbar.LENGTH_LONG)
                    snackbar.show()
                }
            }

            fragmentLoginButton.setOnClickListener {
                if(fragmentLoginEmailTextInputLayout.helperText==null && fragmentLoginPasswordTextInputLayout.helperText==null && viewModel.inputEmail!=null && viewModel.inputPassword!=null)
                {
                    if (!requireContext().toCheckLocationAccess()){
                        requireContext().requestPermissions(mutableListOf(android.Manifest.permission.ACCESS_FINE_LOCATION)){
                            if(it)
                            {
                                if(!requireContext().isLocationEnabled()){
                                    showLocationSettingsDialog(requireContext())
                                    return@requestPermissions
                                }
                            }
                            else{
                                requireActivity().finishAffinity()
                            }
                        }
                        return@setOnClickListener
                    }
                    else{
                        if(!requireContext().isLocationEnabled()){
                            showLocationSettingsDialog(requireContext())
                            return@setOnClickListener
                        }
                    }
                    viewModel.inputEmail=viewModel.inputEmail!!.trim()
                    viewModel.inputPassword=viewModel.inputPassword!!.trim()
                    progressDialog.start("Loading...")
                    FBase.getFireBaseAuth().signInWithEmailAndPassword(viewModel.inputEmail!!,viewModel.inputPassword!!).addOnSuccessListener {
                        if(!FBase.getCurrentUser()!!.isEmailVerified)
                        {
                            progressDialog.stop()
                            Toast.makeText(requireContext(),"Please Verify your email", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                        /*viewModel.userData.observe(viewLifecycleOwner) { userData ->
                                MainScope().launch(Dispatchers.IO)
                                {
                                }
                            }*/
                        if(FBase.getCurrentUser()!=null)
                        {
                            /*viewModel.fetchUserData()*/
                            MainScope().launch(Dispatchers.IO) {
                                try{
                                    sessMan.createSession { sessionID ->
                                        progressDialog.stop()
                                        if(sessionID!=null){
                                            Intent(requireActivity(),HomeActivity::class.java).apply {
                                                startActivity(this)
                                            }
                                            requireActivity().finish()
                                        }
                                    }
                                }
                                catch (ex:Exception)
                                {
                                    progressDialog.stop()
                                    FirebaseAuth.getInstance().signOut()
                                }
                            }
                        }
                        else{
                            progressDialog.stop()
                            return@addOnSuccessListener
                        }
                    }.addOnFailureListener {
                        progressDialog.stop()
                        HandleException.firebaseCommonExceptions(requireContext(),it,tag)
                    }.addOnCanceledListener {
                        progressDialog.stop()
                    }

                }
            }
        }
    }

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            // Handle the result here if needed
            val resultCode = result.resultCode
            if (resultCode != Activity.RESULT_OK) {
                if(!requireContext().isLocationEnabled()){
                    showLocationSettingsDialog(requireContext())
                }
            }
        }

    private val permissionActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (!requireContext().toCheckLocationAccess()){
            showPermissionSettingsDialog(requireContext())
        }
        else{
            if(!requireContext().isLocationEnabled()){
                showLocationSettingsDialog(requireContext())
            }
        }
    }

    fun showPermissionSettingsDialog(context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setTitle("Location Permission")
        alertDialogBuilder.setMessage("Location Permission are required for this app.")

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

    fun showLocationSettingsDialog(context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setTitle("Enable Location")
        alertDialogBuilder.setMessage("Location services are required for this app. Please enable location.")

        alertDialogBuilder.setPositiveButton("Settings") { _, _ ->
            // Open device settings for location
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activityResultLauncher.launch(settingsIntent)
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