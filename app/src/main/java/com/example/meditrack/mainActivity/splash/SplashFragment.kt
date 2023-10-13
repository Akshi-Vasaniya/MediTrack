package com.example.meditrack.mainActivity.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.meditrack.R
import com.example.meditrack.databinding.FragmentSplashBinding
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
    private val tag="SplashFragment"

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_splash, container, false)

        viewModel = ViewModelProvider(this)[SplashViewModel::class.java]

        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding = FragmentSplashBinding.bind(view)

        firebaseAuth=FirebaseAuth.getInstance()

        binding.mediIcon.setSVG(viewModel.getAppSVG(resources))

        if(firebaseAuth.currentUser!=null)
        {
            MainScope().launch {
                viewModel.delayAndNavigate(2000L)
                Intent(requireActivity(),HomeActivity::class.java).apply {
                    startActivity(this)
                }
                requireActivity().finish()
            }
        }
        else{
            MainScope().launch(Dispatchers.Main)
            {
                viewModel.delayAndNavigate(3000L)
                val sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()
                findNavController().popBackStack()
                findNavController().navigate(R.id.loginFragment)
            }
        }


        super.onViewCreated(view, savedInstanceState)
    }


}