package com.investmango.hrconsole.Adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.investmango.hrconsole.R
import com.investmango.hrconsole.manager.activity.ManagerActivity
import com.investmango.hrconsole.manager.activity.ProjectsFragment
import com.investmango.hrconsole.manager.activity.SubTaskFragment
import com.investmango.hrconsole.model.UsersItem

class showTaskAdap(val context: ProjectsFragment, val list: List<UsersItem?>) :
    RecyclerView.Adapter<ShowTakHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowTakHolder {
        val view =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.task_member, parent, false);
        return ShowTakHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ShowTakHolder, position: Int) {
        holder.name.text = list.get(position)?.assignToName
        Glide.with(context).load(list.get(position)?.assignToProfile)
            .into(holder.profile)

        holder.go_arrow.setOnClickListener {
            context.goToSubTask(list.get(position)?.assignToId!!)
        }
    }

}

class ShowTakHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val name = itemView.findViewById<TextView>(R.id.nameOfMember)
    val profile = itemView.findViewById<ImageView>(R.id.profilephoto)
    val go_arrow = itemView.findViewById<ImageView>(R.id.go_arrow)
}