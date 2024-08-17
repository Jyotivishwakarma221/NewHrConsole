package com.investmango.hrconsole.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.investmango.hrconsole.R
import com.investmango.hrconsole.model.Stage

class StagesAdapter(val list: List<Stage>) : RecyclerView.Adapter<StagesHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StagesHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.view_story, parent, false);
        return StagesHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: StagesHolder, position: Int) {
        if (list.get(position).stageName!=null)
        holder.stageDescription.text =list.get(position).stageName
    }
}

class StagesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val stageDescription = itemView.findViewById<TextView>(R.id.storyDescription)
}