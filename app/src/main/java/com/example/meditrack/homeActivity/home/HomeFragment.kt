package com.example.meditrack.homeActivity.home

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.caverock.androidsvg.SVG
import com.example.meditrack.R
import com.example.meditrack.dataModel.User
import com.example.meditrack.databinding.ActivityHomeBinding
import com.example.meditrack.databinding.FragmentHomeBinding
import com.example.meditrack.databinding.MenuLayoutBinding
import com.example.meditrack.firebase.firebaseAuth
import com.example.meditrack.firebase.userReference
import com.example.meditrack.homeActivity.HomeActivity
import com.example.meditrack.mainActivity.MainActivity
import com.example.meditrack.utility.utilityFunction
import com.example.meditrack.utility.utilityFunction.Companion.decodeBase64ToBitmap
import com.example.meditrack.utility.utilityFunction.Companion.getCircularBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File


class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var slidingMenu: View
    private lateinit var menuLayout: MenuLayoutBinding
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

        MainScope().launch(Dispatchers.IO) {
            val sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
            val username = sharedPreferences.getString("name", "")
            if(sharedPreferences.getBoolean("hasImage", false))
            {
                val cacheDirectory = requireActivity().cacheDir
                val imageFileName = "profileImg.jpg"
                val imageFile = File(cacheDirectory, imageFileName)
                if (imageFile.exists()) {
                    var bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    bitmap = getCircularBitmap(bitmap)

                    withContext(Dispatchers.Main){
                        val parentView = binding.menuLayout.profileImage.parent as View
                        binding.menuLayout.profileImage.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        parentView.requestLayout()
                        binding.menuLayout.profileImage.setImageBitmap(bitmap)
                    }
                } else {
                    // The image file doesn't exist, handle the case accordingly
                }
            }
            withContext(Dispatchers.Main){
                binding.menuLayout.usernameTxt.text=username
            }
        }


        val addMedicineSVG = SVG.getFromResource(resources, R.raw.add_medicine)
        val addPrescriptionSVG = SVG.getFromResource(resources, R.raw.add_prescription)
        val checkStockMedicineSVG = SVG.getFromResource(resources, R.raw.check_stock_medicine)
        val medicineSugestionSVG = SVG.getFromResource(resources, R.raw.medicine_sugestion)

        binding.addMedicineImageView.setSVG(addMedicineSVG)
        binding.addPrescriptionImageView.setSVG(addPrescriptionSVG)
        binding.checkStockMedicinesImageView.setSVG(checkStockMedicineSVG)
        binding.medicineSuggestionsImageView.setSVG(medicineSugestionSVG)

        slidingMenu = binding.menuLayout.slidingMenu

        slidingMenu.setOnClickListener {  }

        menuLayout = binding.menuLayout

        menuLayout.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Intent(requireActivity(),MainActivity::class.java).apply {
                startActivity(this)
            }
            requireActivity().finish()
        }
        binding.rootLayout.setOnClickListener {
            if (slidingMenu.visibility == View.VISIBLE) {
                hideMenu()
            }
        }

        slideInAnimation = ValueAnimator.ofFloat(-1f, 0f)
        slideInAnimation.duration = 300

        slideOutAnimation = ValueAnimator.ofFloat(0f, -1f)
        slideOutAnimation.duration = 300

        slideInAnimation.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float
            slidingMenu.translationX = value * slidingMenu.width
        }

        val profileImageTop = requireActivity().findViewById<ConstraintLayout>(R.id.profile_image_top)



        profileImageTop.setOnClickListener {
            if (slidingMenu.visibility == View.INVISIBLE) {
                slidingMenu.visibility = View.VISIBLE
                slidingMenu.translationX = -slidingMenu.width.toFloat()
                slideInAnimation.start()
            } else {
                hideMenu()
            }
        }
    }

    private fun hideMenu() {
        if (slidingMenu.visibility == View.VISIBLE) {
            slideOutAnimation.addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Float
                slidingMenu.translationX = value * slidingMenu.width
                if (value == -1f) {
                    slidingMenu.visibility = View.INVISIBLE
                }
            }
            slideOutAnimation.start()
        }
    }

}