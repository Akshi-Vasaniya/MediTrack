package com.example.meditrack.mainActivity.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.databinding.FragmentSplashBinding
import com.example.meditrack.firebase.FBase
import com.example.meditrack.homeActivity.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class SplashFragment : Fragment() {

    /*companion object {
        fun newInstance() = SplashFragment()
    }*/

    private lateinit var viewModel: SplashViewModel
    private lateinit var binding: FragmentSplashBinding
    private lateinit var firebaseAuth: FirebaseAuth
    //private val tAG="SplashFragment"

    /*override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        binding = FragmentSplashBinding.bind(view)
        viewModel = ViewModelProvider(this)[SplashViewModel::class.java]

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        firebaseAuth=FBase.getFireBaseAuth()

        binding.mediIcon.setSVG(viewModel.getAppSVG(resources))

        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        binding.mediIcon.startAnimation(fadeIn)
        binding.welcomeMsg.startAnimation(fadeIn)

        // Create an animation listener
        val animationListener = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // Code to be executed when the animation starts
            }

            override fun onAnimationEnd(animation: Animation?) {
                // Code to be executed when the animation ends
                // Place your code here that you want to execute after the animations
                // e.g., call a function or perform any specific operation
                // Your code here

                if(firebaseAuth.currentUser!=null)
                {
                    MainScope().launch {
                        viewModel.delayAndNavigate(500L)
                        Intent(requireActivity(),HomeActivity::class.java).apply {
                            startActivity(this)
                        }
                        requireActivity().finish()
                    }
                }
                else{
                    MainScope().launch(Dispatchers.Main)
                    {
                        viewModel.delayAndNavigate(500L)
                        //val sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
                        //val editor = sharedPreferences.edit()
                        //editor.clear()
                        //editor.apply()
                        findNavController().popBackStack()
                        findNavController().navigate(R.id.loginFragment)
                    }
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {
                // Code to be executed when the animation repeats
            }
        }

        // Set the animation listener for both animations
        fadeIn.setAnimationListener(animationListener)

        super.onViewCreated(view, savedInstanceState)
    }


}