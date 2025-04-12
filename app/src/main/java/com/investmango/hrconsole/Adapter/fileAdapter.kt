package com.investmango.hrconsole.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.investmango.hrconsole.R
import com.investmango.hrconsole.manager.activity.ManagerActivity
import com.investmango.hrconsole.manager.activity.ProjectsFragment
import com.investmango.hrconsole.model.SubTaskItem

class fileAdapter(val context: Context,val list: List<String?>) : RecyclerView.Adapter<FileHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        val view =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.file_view, parent, false);
        return FileHolder(view)
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: FileHolder, position: Int) {
        val count=position+1
        holder.textName.text = "File "+ count.toString()
        holder.textName.setOnClickListener {
              openfile(list.get(position)!!)
        }
    }
    fun openfile(url: String) {
        val urlIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        context.startActivity(urlIntent)
    }
}

class FileHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textName = itemView.findViewById<TextView>(R.id.textName)
}