package com.example.meditrack.homeActivity.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.caverock.androidsvg.SVG
import com.example.meditrack.R
import com.example.meditrack.dataModel.api.MyAsyncTask
import com.example.meditrack.databinding.FragmentHomeBinding
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    /*private lateinit var slideInAnimation: ValueAnimator
    private lateinit var slideOutAnimation: ValueAnimator*/
    private lateinit var binding:FragmentHomeBinding
    //private lateinit var homeActivity: HomeActivity
    private val homeTag = "HomeFragment"
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*if (context is HomeActivity) {
            homeActivity = context as HomeActivity
        } else {
            throw IllegalStateException("Parent activity must be HomeActivity")
        }*/
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //homeActivity.getToolbarMenuLayout().visibility = View.VISIBLE

       /* val navigationView = activity?.findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView?.getHeaderView(0)
        val userName = headerView?.findViewById<TextView>(R.id.user_name_menu_header)
        val userEmail = headerView?.findViewById<TextView>(R.id.user_email_menu_header)
        val userImage = headerView?.findViewById<ImageView>(R.id.user_image_menu_header)*/

        /*val url = "https://leavemanagementuvpce.000webhostapp.com/test.php"

        try{
            progressDialog.start("Connecting...")
            MainScope().launch(Dispatchers.IO) {
                val flaskBaseUrlCallback: (String) -> Unit = { result ->
                    progressDialog.stop()
                    // Process the result or update UI
                    val jsonResult = JSONObject(result)
                    flaskURL = jsonResult.getString("success")
                }
                MyAsyncTask(flaskBaseUrlCallback).execute(url)
            }

        }
        catch (ex:Exception)
        {
            Log.i("HomeFragment",ex.message.toString())
        }*/



        binding.apply {
            suggestionMedicineCard.setOnClickListener {
                findNavController().navigate(R.id.searchFragment)
            }
            addMedicineCard.setOnClickListener {
                findNavController().navigate(R.id.addMedicineFragment)
            }
            /*addPrescriptionCard.setOnClickListener {
                findNavController().navigate(R.id.OCRFragment)
            }*/
            scanPrescriptionCard.setOnClickListener {
                findNavController().navigate(R.id.scanPrescriptionFragment)
            }
            checkMedicineStockCard.setOnClickListener {
                findNavController().navigate(R.id.medicineStockFragment)
            }
        }

        /*isUserEmailAvailable("anhowdontcare@gmail.com", object : EmailAvailabilityCallback {
            override fun onResult(isAvailable: Boolean) {
                // Use the isAvailable value here
                if (isAvailable) {
                    Log.i("isUsernameAvailable","True")
                } else {
                    Log.i("isUsernameAvailable","False")
                }
            }
        })*/


        /*viewModel._userData.observe(viewLifecycleOwner){
            MainScope().launch(Dispatchers.IO) {
                try {
                    withContext(Dispatchers.Main)
                    {
                        userName!!.text = requireActivity().getString(R.string.full_name,it.name,it.surname)
                        userEmail!!.text = it.email

                        //binding.menuLayout.usernameTxt.text=requireActivity().getString(R.string.full_name,it.name,it.surname)
                        //binding.menuLayout.profileImage.setImageBitmap(null)
                    }

                    if(!it?.profileImage.isNullOrBlank())
                    {
                        val bitmap = UtilityFunction.decodeBase64ToBitmap(it?.profileImage!!)

                        //bitmap = getCircularBitmap(bitmap)
                        withContext(Dispatchers.Main)
                        {
                            //val parentView = binding.menuLayout.profileImage.parent as View
                            //binding.menuLayout.profileImage.layoutParams = ViewGroup.LayoutParams(
                                //ViewGroup.LayoutParams.MATCH_PARENT,
                                //ViewGroup.LayoutParams.MATCH_PARENT
                            //)
                            //parentView.requestLayout()
                            //binding.menuLayout.profileImage.setImageBitmap(bitmap)
                            userImage!!.setImageBitmap(bitmap)
                            userImage.setOnClickListener {
                                findNavController().navigate(R.id.userProfileFragment)
                            }
                        }
                    }
                }
                catch (ex:Exception)
                {
                    Log.e(homeTag,"$ex")
                }

            }
        }*/

        binding.apply {
            /*MainScope().launch(Dispatchers.IO) {
                val userQuery = MediTrackUserReference.getUserDataQuery()
                userQuery.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach {
                            viewModel.setUserData(User(
                                it.child("name").value.toString(),
                                it.child("surname").value.toString(),
                                it.child("email").value.toString(),
                                it.child("profileImage").value.toString()))
                        }

                        //Log.i("Name Updated: ", "${viewModel._userData.value!!.name}")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }*/
            MainScope().launch(Dispatchers.IO) {
                val addMedicineSVG = SVG.getFromResource(resources, R.raw.add_medicine)
                val addPrescriptionSVG = SVG.getFromResource(resources, R.raw.add_prescription)
                val checkStockMedicineSVG = SVG.getFromResource(resources, R.raw.check_stock_medicine)
                val medicineSuggestionSVG = SVG.getFromResource(resources, R.raw.medicine_sugestion)

                /*val sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
                val userData = User(
                    sharedPreferences.getString("name", ""),
                    sharedPreferences.getString("surname", ""),
                    sharedPreferences.getString("email","")
                )
                if(sharedPreferences.getBoolean("hasImage", false))
                {
                    try {
                        val cacheDirectory = requireActivity().cacheDir
                        val imageFileName = "profileImg.jpg"
                        val imageFile = File(cacheDirectory, imageFileName)
                        if (imageFile.exists()) {
                            var bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                            bitmap = getCircularBitmap(bitmap)
                            withContext(Dispatchers.Main){
                                val parentView = menuLayout.profileImage.parent as View
                                menuLayout.profileImage.layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                parentView.requestLayout()
                                menuLayout.profileImage.setImageBitmap(bitmap)
                            }
                        } else {
                            // The image file doesn't exist, handle the case accordingly
                        }
                    }
                    catch (ex:Exception)
                    {
                        Log.e(TAG,"HomeFragment.kt -> $ex")
                        Toast.makeText(requireActivity(),"Error! to load Image",Toast.LENGTH_LONG).show()
                    }

                }*/
                withContext(Dispatchers.Main){
                    /*menuLayout.usernameTxt.text=requireActivity().getString(R.string.full_name,userData.name,userData.surname)*/
                    addMedicineImageView.setSVG(addMedicineSVG)
//                    addPrescriptionImageView.setSVG(addPrescriptionSVG)
                    checkStockMedicinesImageView.setSVG(checkStockMedicineSVG)
                    medicineSuggestionsImageView.setSVG(medicineSuggestionSVG)
                }
            }

            /*menuLayout.slidingMenu.setOnClickListener {  }

            menuLayout.btnLogout.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                Intent(requireActivity(),MainActivity::class.java).apply {
                    startActivity(this)
                }
                requireActivity().finish()
            }
            rootLayout.setOnClickListener {
                if (binding.menuLayout.slidingMenu.visibility == View.VISIBLE) {
                    hideMenu()
                }
            }

            slideInAnimation = ValueAnimator.ofFloat(-1f, 0f)
            slideInAnimation.duration = 300

            slideOutAnimation = ValueAnimator.ofFloat(0f, -1f)
            slideOutAnimation.duration = 300

            slideInAnimation.addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Float
                menuLayout.slidingMenu.translationX = value * menuLayout.slidingMenu.width
            }

            val fragmentHomeToolbarMenuLayout = requireActivity().findViewById<ConstraintLayout>(R.id.fragment_home_toolbar_menu_layout)

            fragmentHomeToolbarMenuLayout.setOnClickListener {
                if (menuLayout.slidingMenu.visibility == View.INVISIBLE) {
                    menuLayout.slidingMenu.visibility = View.VISIBLE
                    menuLayout.slidingMenu.translationX = -menuLayout.slidingMenu.width.toFloat()
                    slideInAnimation.start()
                } else {
                    hideMenu()
                }
            }*/
        }

    }

    /*private fun hideMenu() {
        binding.apply {
            if (menuLayout.slidingMenu.visibility == View.VISIBLE) {
                slideOutAnimation.addUpdateListener { valueAnimator ->
                    val value = valueAnimator.animatedValue as Float
                    menuLayout.slidingMenu.translationX = value * menuLayout.slidingMenu.width
                    if (value == -1f) {
                        menuLayout.slidingMenu.visibility = View.INVISIBLE
                    }
                }
                slideOutAnimation.start()
            }
        }
    }*/

    // Callback interface to return the result
    /*interface EmailAvailabilityCallback {
        fun onResult(isAvailable: Boolean)
    }*/

    /*fun isUserEmailAvailable(email: String, callback: EmailAvailabilityCallback) {
        val usersRef = MediTrackUserReference.getUserReference()

        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val isAvailable = dataSnapshot.childrenCount == 0L
                callback.onResult(!isAvailable)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onResult(true)
            }
        })
    }*/
}