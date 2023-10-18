package com.example.meditrack

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.SearchManager
import android.app.appsearch.AppSearchManager.SearchContext
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.example.meditrack.databinding.FragmentHomeBinding
import com.example.meditrack.databinding.MenuLayoutBinding

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var slidingMenu: View
    private lateinit var imageView: ImageView
    private lateinit var bottomShapeImage: ImageView
    private lateinit var slideInAnimation: ValueAnimator
    private lateinit var slideOutAnimation: ValueAnimator
    private lateinit var rootLayout: FrameLayout

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

        slidingMenu = view.findViewById(R.id.sliding_menu)
        imageView = view.findViewById(R.id.profile_image_top)
        rootLayout = view.findViewById(R.id.root_layout)

        rootLayout.setOnClickListener {
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

        slidingMenu.setOnClickListener { }

        imageView.setOnClickListener {
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