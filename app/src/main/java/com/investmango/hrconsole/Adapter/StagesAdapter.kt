package com.investmango.hrconsole.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.investmango.hrconsole.R
import com.investmango.hrconsole.model.Stage

class StagesAdapter(val list: List<Stage>, val context: Context) :
    RecyclerView.Adapter<StagesHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StagesHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.view_story, parent, false);
        return StagesHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: StagesHolder, position: Int) {
        val imgs = intArrayOf(R.drawable.hour_glass, R.drawable.tick)

        if (list.get(position).stageName != null)
            holder.stageDescription.text = list.get(position).stageName

        if (list.get(position).status != "COMPLETED") {
            holder.image.setImageDrawable(ContextCompat.getDrawable(context, imgs[1]))
        } else holder.image.setImageDrawable(ContextCompat.getDrawable(context, imgs[0]))


    }
}

class StagesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val stageDescription = itemView.findViewById<TextView>(R.id.storyDescription)
    val image = itemView.findViewById<ImageView>(R.id.image)
}