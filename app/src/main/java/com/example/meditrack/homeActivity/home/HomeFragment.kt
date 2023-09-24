package com.example.meditrack.homeActivity.home

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.caverock.androidsvg.SVG
import com.example.meditrack.R
import com.example.meditrack.dataModel.User
import com.example.meditrack.databinding.FragmentHomeBinding
import com.example.meditrack.databinding.MenuLayoutBinding
import com.example.meditrack.mainActivity.MainActivity
import com.example.meditrack.utility.utilityFunction.Companion.getCircularBitmap
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var slideInAnimation: ValueAnimator
    private lateinit var slideOutAnimation: ValueAnimator
    private lateinit var binding:FragmentHomeBinding
    private var userData:User? = null
    private val TAG = "HomeFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        binding.apply {
            MainScope().launch(Dispatchers.IO) {
                val addMedicineSVG = SVG.getFromResource(resources, R.raw.add_medicine)
                val addPrescriptionSVG = SVG.getFromResource(resources, R.raw.add_prescription)
                val checkStockMedicineSVG = SVG.getFromResource(resources, R.raw.check_stock_medicine)
                val medicineSuggestionSVG = SVG.getFromResource(resources, R.raw.medicine_sugestion)

                val sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
                val userData = User(
                    sharedPreferences.getString("name", ""),
                    sharedPreferences.getString("surname", ""),
                    sharedPreferences.getString("email","")
                )
                if(sharedPreferences.getBoolean("hasImage", false))
                {
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
                withContext(Dispatchers.Main){
                    menuLayout.usernameTxt.text=requireActivity().getString(R.string.full_name,userData.name,userData.surname)
                    addMedicineImageView.setSVG(addMedicineSVG)
                    addPrescriptionImageView.setSVG(addPrescriptionSVG)
                    checkStockMedicinesImageView.setSVG(checkStockMedicineSVG)
                    medicineSuggestionsImageView.setSVG(medicineSuggestionSVG)
                }
            }

            menuLayout.slidingMenu.setOnClickListener {  }

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
            }
        }

    }

    private fun hideMenu() {
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

    }

}