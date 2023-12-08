package com.example.meditrack.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.dataModel.ItemsViewModel
import java.util.Calendar

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

        // Time and Date
        holder.medicineAddedTime.text = itemsViewModel.medicine_add_time
        holder.medicineAddedDate.text = itemsViewModel.medicine_add_date

        // Expiry
        val remaningDay = itemsViewModel.expiry_date
        Log.i("TAG", "onBindViewHolder: ${remaningDay[0]}")
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        var month = remaningDay[0]+ ""+remaningDay[1]
        var year = remaningDay[3]+""+remaningDay[4]+""+remaningDay[5]+""+remaningDay[6]

        if(month.toInt() == currentMonth &&  year.toInt() == currentYear){
            holder.expiryTime.text = "Expired In this Month"
        } else if(year.toInt() > currentYear){
            var totalYear: Int = year.toInt() - currentYear
            var totalMonth =  (12 - currentMonth)
            Log.i("TAG", "onBindViewHolder: $totalMonth")
            holder.expiryTime.text = totalYear.toString()+" Year "+totalMonth.toString() + " Month"
        } else if(year.toInt() == currentYear){
            if(currentMonth < month.toInt()){
                holder.expiryTime.text = (12 - month.toInt()).toString() + " Month"
            }
        }



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
        val medicineAddedDate: TextView = itemView.findViewById(R.id.added_date)
        val medicineAddedTime: TextView = itemView.findViewById(R.id.added_time)
        val expiryTime: TextView = itemView.findViewById(R.id.expiry_remain_time)

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
