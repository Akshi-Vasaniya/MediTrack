package com.example.meditrack.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.dataModel.ItemsViewModel
import java.util.Calendar

class MedicineStockItemAdapter(private val mList: List<ItemsViewModel>) : RecyclerView.Adapter<MedicineStockItemAdapter.ViewHolder>() {

    // Set to store selected items
    private val selectedItems = mutableSetOf<Int>()
    private var isItemSelection = false

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

        if(isItemSelection)
        {
            holder.selectedCheckBox.visibility = View.VISIBLE
            holder.deleteIcon.isClickable=false
            holder.editIcon.isClickable=false
        }
        else{
            holder.selectedCheckBox.visibility = View.GONE
            holder.deleteIcon.isClickable=true
            holder.editIcon.isClickable=true
        }
        holder.selectedCheckBox.isClickable=false
        holder.selectedCheckBox.isChecked = itemsViewModel.isSelected

        // Handle item long press for selection
        holder.itemView.setOnLongClickListener {
            isItemSelection = true
            toggleSelection(position)
            notifyDataSetChanged()
            true
        }


        // Handle item click for additional actions if needed
        holder.itemView.setOnClickListener {
            if (isItemSelection) {
                toggleSelection(position)
                notifyDataSetChanged()
            } else {
                // Handle item click as needed
            }
        }

        // Expiry
        val remaningDay = itemsViewModel.expiry_date
        Log.i("TAG", "onBindViewHolder: ${remaningDay[0]}")
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        var month: String = ""
        var year: String = ""
        if(remaningDay[1].toString() == "/"){
            month = remaningDay[0].toString()
            year = remaningDay[2]+""+remaningDay[3]+""+remaningDay[4]+""+remaningDay[5]
        } else{
            month = remaningDay[0]+ ""+remaningDay[1]
            year = remaningDay[3]+""+remaningDay[4]+""+remaningDay[5]+""+remaningDay[6]
        }



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


    // Holds the views for adding it to image and text
    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val medicineName: TextView = itemView.findViewById(R.id.medicine_name)
        val expiryDate: TextView = itemView.findViewById(R.id.expiry_date)
        val deleteIcon: ImageView = itemView.findViewById(R.id.delete_btn)
        val editIcon: ImageView = itemView.findViewById(R.id.edit_btn)
        val medicineAddedDate: TextView = itemView.findViewById(R.id.added_date)
        val medicineAddedTime: TextView = itemView.findViewById(R.id.added_time)
        val expiryTime: TextView = itemView.findViewById(R.id.expiry_remain_time)
        val selectedCheckBox:CheckBox = itemView.findViewById(R.id.selectItem)

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

    // Toggle selection for the item at the given position
    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            mList[position].isSelected=false
            selectedItems.remove(position)
        } else {
            mList[position].isSelected=true
            selectedItems.add(position)
        }

        // Check if no items are selected to end the selection mode
        if (selectedItems.isEmpty()) {
            isItemSelection = false
        }
    }

    // Select all items
    fun selectAll() {
        isItemSelection=true
        selectedItems.clear()
        for (i in 0 until itemCount)
        {
            mList[i].isSelected=true
            selectedItems.add(i)
        }
        notifyDataSetChanged()
    }

    // Unselect all items
    fun unselectAll() {
        isItemSelection=false
        selectedItems.clear()
        for (i in 0 until itemCount)
        {
            mList[i].isSelected=false
        }
        notifyDataSetChanged()
    }

    // Inverse select items
    fun inverseSelect() {
        if(isItemSelection)
        {
            val totalItems = itemCount
            val currentSelectedItems = selectedItems.toList()
            selectedItems.clear()

            for (position in 0 until totalItems) {
                if (!currentSelectedItems.contains(position)) {
                    selectedItems.add(position)
                    mList[position].isSelected=true
                }
                else{
                    mList[position].isSelected=false
                }
            }
            if (selectedItems.isEmpty()) {
                isItemSelection = false
            }
            notifyDataSetChanged()
        }
    }

    // Get the count of selected items
    fun getSelectedItemCount(): Int {
        return selectedItems.size
    }

    // Get the list of selected items
    fun getSelectedItems(): Set<Int> {
        return selectedItems
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
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
