package com.investmango.hrconsole.EmployeeAction

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.investmango.hrconsole.R
import com.investmango.hrconsole.model.TotalEmpResponseItem

class DepartmentAdap(val fragment: CreateNewFragment, val list: List<String?>) :
    RecyclerView.Adapter<DepartmentAdap.DepartmentItem>() {
    private var selectedPosition = -1
    private var previousPosition = -1

    inner class DepartmentItem(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(com.investmango.hrconsole.R.id.departmentName)
        val department_layout: RelativeLayout =
            itemView.findViewById(com.investmango.hrconsole.R.id.department_layout)

        init {
            department_layout.setOnClickListener {
                Log.e("MyAdapter", "ViewHolder clicked")
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                Log.e("MyAdapter", "Selected position updated to $selectedPosition")
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }


        }

        fun notifyyyy() {
            notifyItemChanged(selectedPosition)
            notifyItemChanged(previousPosition)
            Log.e("MyAdapter", "Selected position updated to $selectedPosition")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentItem {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.investmango.hrconsole.R.layout.department_recycler, parent, false)
        return DepartmentItem(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(
        holder: DepartmentItem,
        @SuppressLint("RecyclerView") position: Int,
    ) {
        if (selectedPosition == position){
            holder.department_layout.setBackgroundResource(R.drawable.blue_bg)
        }else{
            holder.department_layout.setBackgroundResource(R.drawable.dark_grey_bg)

        }


        holder.name.setText(list.get(position))
        holder.department_layout.setOnClickListener {
            if (selectedPosition != position) {
                previousPosition = selectedPosition
                selectedPosition = position
                Log.e("recyclerTag", ": " + selectedPosition + " " + previousPosition)
                fragment.departments = list.get(position).toString()
                Log.e("recyclerTag", ": " +fragment.departments)
                holder.department_layout.setBackgroundResource(R.drawable.blue_bg)
                holder.notifyyyy()
            } else {
                holder.department_layout.setBackgroundResource(R.drawable.dark_grey_bg)

            }
        }

    }


}