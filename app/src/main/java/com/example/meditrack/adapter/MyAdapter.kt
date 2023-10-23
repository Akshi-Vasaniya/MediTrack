package com.example.meditrack.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.dataModel.api.ApiData

class MyAdapter(private var itemsList: List<ApiData?>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemName : TextView = itemView.findViewById(R.id.itemName)
        val itemScore : TextView = itemView.findViewById(R.id.itemScore)
        val itemDesc : TextView = itemView.findViewById(R.id.itemDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.search_results_itemlayout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.itemName.text = itemsList[position]!!.Name.toUpperCase()
        holder.itemDesc.text = itemsList[position]!!.Document
        holder.itemScore.text = itemsList[position]!!.Score.toString()

    }

    override fun getItemCount(): Int {
        return itemsList.size
    }



}