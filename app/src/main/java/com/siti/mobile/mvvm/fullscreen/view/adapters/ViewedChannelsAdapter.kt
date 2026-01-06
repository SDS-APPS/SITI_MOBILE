package com.siti.mobile.mvvm.fullscreen.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.siti.mobile.Model.JoinData.JoinLiveStreams
import com.siti.mobile.databinding.ItemChannelChannelsViewContainerBinding

class ViewedChannelsAdapter(
    private val channels: List<JoinLiveStreams>,
    private val onItemClick: (JoinLiveStreams) -> Unit,
    private val onItemLongClick: (JoinLiveStreams) -> Unit
) : RecyclerView.Adapter<ViewedChannelsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemChannelChannelsViewContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: JoinLiveStreams) {
            binding.tvChannelName.text = channel.channel_name
            binding.tvChannelNumber.text = channel.channel_no.toString()

            if(channel.isFavorite == "true"){
                binding.ivFavorite.visibility = View.VISIBLE
            }else{
                binding.ivFavorite.visibility = View.INVISIBLE
            }

            binding.tvPrice.text = if (channel.price < 1) "FREE" else channel.price.toString()

            binding.root.setOnClickListener { onItemClick(channel) }

            binding.root.setOnLongClickListener { view ->
                onItemLongClick(channel)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChannelChannelsViewContainerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(channels[position])
    }

    override fun getItemCount() = channels.size
}
