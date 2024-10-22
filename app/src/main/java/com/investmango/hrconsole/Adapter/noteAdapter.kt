package com.investmango.hrconsole.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.investmango.hrconsole.R

class noteApater(val list: List<String?>) : RecyclerView.Adapter<NoteHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val view =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_view, parent, false);
        return NoteHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        holder.notee.text = list.get(position)
    }
}

class NoteHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val notee = itemView.findViewById<TextView>(R.id.notee)
}