package com.example.meditrack.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.dataModel.ItemsViewModel

class MedicineStockItemAdapter(private val context: Context, private val mList: List<ItemsViewModel>) : RecyclerView.Adapter<MedicineStockItemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.medicine_list_layout, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]

        // sets the name of medicine to the textview from our itemHolder class
        holder.medicineName.text = ItemsViewModel.medicine_name

        // sets the expiry date to the textview from our itemHolder class
        holder.expiryDate.text = ItemsViewModel.expiry_date

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val medicineName: TextView = itemView.findViewById(R.id.medicine_name)
        val expiryDate: TextView = itemView.findViewById(R.id.expiry_date)
    }
}
