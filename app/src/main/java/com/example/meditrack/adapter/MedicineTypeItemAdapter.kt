package com.example.meditrack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R

class MedicineTypeItemAdapter(private val dataList: List<Pair<String, String>>) : RecyclerView.Adapter<MedicineTypeItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.medicine_type_info_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val medicineTypeTextView: TextView = itemView.findViewById(R.id.medicineTypeTextView)
        private val medicineDescriptionTextView: TextView = itemView.findViewById(R.id.medicineDescriptionTextView)

        fun bind(data: Pair<String, String>) {
            medicineTypeTextView.text = data.first
            medicineDescriptionTextView.text = data.second
        }
    }
}
