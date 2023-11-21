package com.example.meditrack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.dataModel.dataClasses.SessData2
import com.example.meditrack.dataModel.enumClasses.others.SessStatus

class RecyclerViewAdapter(private val mItemList: List<SessData2?>, private val signOutClickListener:SignOutClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    interface SignOutClickListener{
        fun onClick(position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.devices_items_layouts, parent, false)
            ItemViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is ItemViewHolder) {
            populateItemRows(viewHolder, position)
        } else if (viewHolder is LoadingViewHolder) {
            showLoadingView(viewHolder, position)
        }
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (mItemList[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    private inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDeviceName: TextView = itemView.findViewById(R.id.txtDeviceName)
        var txtLocation: TextView = itemView.findViewById(R.id.txtLocation)
        var txtLoginTimeStamp: TextView = itemView.findViewById(R.id.txtLoginTimeStamp)
        var txtDeviceVersion: TextView = itemView.findViewById(R.id.txtDeviceVersion)
        var btnSignOut: TextView = itemView.findViewById(R.id.btnSignOut)
    }

    private inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }

    private fun showLoadingView(viewHolder: LoadingViewHolder, position: Int) {
        // ProgressBar would be displayed
    }

    private fun populateItemRows(viewHolder: ItemViewHolder, position: Int) {
        val item = mItemList[position]
        viewHolder.txtDeviceName.text = item?.deviceName
        viewHolder.txtLoginTimeStamp.text = item?.loginTimestamp
        if(item?.status==SessStatus.LOGGED_OUT){
            viewHolder.btnSignOut.setTextAppearance(R.style.DevicesFragmentSignOut)
            viewHolder.btnSignOut.isEnabled=false
        }
        else{
            viewHolder.btnSignOut.setTextAppearance(R.style.LoginHead)
            viewHolder.btnSignOut.isEnabled=true
        }
        viewHolder.txtDeviceVersion.text = "${item?.osVersion} (${item?.apiLevel})"
        val locationList = mutableListOf<String>()
        if(item?.country!=null && item.country!="" && item.country!="null"){
            locationList.add(0,item.country)
        }
        if(item?.state!=null && item.state!="" && item.state!="null"){
            locationList.add(0,item.state)
        }
        if(item?.city!=null && item.city!="" && item.city!="null"){
            locationList.add(0,item.city)
        }
        if(item?.area!=null && item.area!="" && item.area!="null"){
            locationList.add(0,item.area)
        }
        viewHolder.txtLocation.text = locationList.joinToString(", ")
        viewHolder.btnSignOut.setOnClickListener {
            signOutClickListener.onClick(position)
        }
    }
}