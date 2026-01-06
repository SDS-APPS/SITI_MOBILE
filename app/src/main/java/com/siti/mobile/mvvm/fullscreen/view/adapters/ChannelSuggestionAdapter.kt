package com.siti.mobile.mvvm.fullscreen.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.siti.mobile.Model.JoinData.JoinLiveStreams
import com.siti.mobile.R

class ChannelSuggestionAdapter(
    private val channels: List<JoinLiveStreams>,
    private val onItemClick: (JoinLiveStreams) -> Unit
) : RecyclerView.Adapter<ChannelSuggestionAdapter.ChannelViewHolder>() {

    inner class ChannelViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.tvChannelName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel_suggestion, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        holder.txtName.text = "${channel.channel_no} - ${channel.channel_name}"

        // 👉 Highlight on focus
        holder.itemView.isFocusable = true
        holder.itemView.isFocusableInTouchMode = false

        holder.itemView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.setBackgroundColor(Color.parseColor("#33FFFFFF"))
                holder.txtName.setTextColor(Color.BLACK)
            } else {
                v.setBackgroundColor(Color.TRANSPARENT)
                holder.txtName.setTextColor(Color.WHITE)
            }
        }

        holder.itemView.setOnClickListener {
            if (channel.channel_name != "No Match Found") {
                onItemClick(channel)
            }
        }
    }

    override fun getItemCount(): Int = channels.size
}

