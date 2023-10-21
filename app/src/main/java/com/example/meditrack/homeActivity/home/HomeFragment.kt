package com.example.meditrack.homeActivity.home

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.caverock.androidsvg.SVG
import com.example.meditrack.R
import com.example.meditrack.dataModel.User
import com.example.meditrack.databinding.FragmentHomeBinding
import com.example.meditrack.firebase.MediTrackUserReference
import com.example.meditrack.homeActivity.HomeActivity
import com.example.meditrack.mainActivity.MainActivity
import com.example.meditrack.utility.UtilityFunction
import com.example.meditrack.utility.UtilityFunction.Companion.getCircularBitmap
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    /*private lateinit var slideInAnimation: ValueAnimator
    private lateinit var slideOutAnimation: ValueAnimator*/
    private lateinit var binding:FragmentHomeBinding
//    private lateinit var homeActivity: HomeActivity
    private val tag = "HomeFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        if (context is HomeActivity) {
//            homeActivity = context as HomeActivity
//        } else {
//            throw IllegalStateException("Parent activity must be HomeActivity")
//        }
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]


        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

//        homeActivity.getToolbarMenuLayout().visibility = View.VISIBLE

        val navigationView = activity?.findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView?.getHeaderView(0)
        val userName = headerView?.findViewById<TextView>(R.id.user_name_menu_header)
        val userEmail = headerView?.findViewById<TextView>(R.id.user_email_menu_header)
        val userImage = headerView?.findViewById<ImageView>(R.id.user_image_menu_header)


        binding.apply {
            suggestionMedicineCard.setOnClickListener {
                findNavController().navigate(R.id.searchFragment)
            }
            addMedicineCard.setOnClickListener {
                findNavController().navigate(R.id.addMedicineFragment)
            }
//            addPrescriptionCard.setOnClickListener {
//                findNavController().navigate(R.id.OCRFragment)
//            }
            checkMedicineStockCard.setOnClickListener {
                findNavController().navigate(R.id.medicineStockFragment)
            }
        }

        viewModel._userData.observe(viewLifecycleOwner){
            MainScope().launch(Dispatchers.IO) {
                try {
                    withContext(Dispatchers.Main)
                    {
                        userName!!.text = requireActivity().getString(R.string.full_name,it.name,it.surname)
                        userEmail!!.text = it.email

                        /*binding.menuLayout.usernameTxt.text=requireActivity().getString(R.string.full_name,it.name,it.surname)
                        binding.menuLayout.profileImage.setImageBitmap(null)*/
                    }

                    if(!it?.profileImage.isNullOrBlank())
                    {
                        val bitmap = UtilityFunction.decodeBase64ToBitmap(it?.profileImage!!)

                        /*bitmap = getCircularBitmap(bitmap)*/
                        withContext(Dispatchers.Main)
                        {
                            /*val parentView = binding.menuLayout.profileImage.parent as View
                            binding.menuLayout.profileImage.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            parentView.requestLayout()
                            binding.menuLayout.profileImage.setImageBitmap(bitmap)*/
                            userImage!!.setImageBitmap(bitmap)
                        }
                    }
                }
                catch (ex:Exception)
                {
                    Log.e(tag,"$ex")
                }

            }
        }

        binding.apply {
            MainScope().launch(Dispatchers.IO) {
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

//                        Log.i("Name Updated: ", "${viewModel._userData.value!!.name}")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
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

}