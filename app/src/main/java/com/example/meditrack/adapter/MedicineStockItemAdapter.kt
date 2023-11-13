package com.example.meditrack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.dataModel.ItemsViewModel

class MedicineStockItemAdapter(private val mList: List<ItemsViewModel>) : RecyclerView.Adapter<MedicineStockItemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.medicine_list_layout, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemsViewModel = mList[position]

        // sets the name of medicine to the textview from our itemHolder class
        holder.medicineName.text = itemsViewModel.medicine_name

        // sets the expiry date to the textview from our itemHolder class
        holder.expiryDate.text = itemsViewModel.expiry_date

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val medicineName: TextView = itemView.findViewById(R.id.medicine_name)
        val expiryDate: TextView = itemView.findViewById(R.id.expiry_date)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.delete_btn)
        private val editIcon: ImageView = itemView.findViewById(R.id.edit_btn)

        init {
            deleteIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = mList[position]
                    itemClickListener?.onItemClick(item.medicine_id)
                }
            }

            editIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = mList[position]
                    itemClickListener?.onEditClick(item.medicine_id)
                }
            }

        }
    }

    // Delete Medicine
    // Interface to handle item click events
    interface OnItemClickListener {
        fun onItemClick(medicineId: String)
        fun onEditClick(medicineId: String)
    }

    // Click listener for items
    private var itemClickListener: OnItemClickListener? = null

    // Function to set the click listener from outside the adapter
    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

}
