package com.example.meditrack.homeActivity.about

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.example.meditrack.R

class AboutUsFragment : Fragment() {

    companion object {
        fun newInstance() = AboutUsFragment()
    }

    private lateinit var viewModel: AboutUsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about_us, container, false)
        val collegeDetailsTextView: TextView = view.findViewById(R.id.collegeDetailsTextView)
        val teamDetailsTextView: TextView = view.findViewById(R.id.teamDetailsTextView)

        // Retrieve the HTML string from resources
        val collegeDetailsHtml = resources.getString(R.string.college_details)
        val teamDetailsHtml = resources.getString(R.string.team_member_details)

        // Set the text using HtmlCompat.fromHtml()
        collegeDetailsTextView.text = HtmlCompat.fromHtml(collegeDetailsHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        teamDetailsTextView.text = HtmlCompat.fromHtml(teamDetailsHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)

        return view
    }



}