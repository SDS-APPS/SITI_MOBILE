package com.siti.mobile.mvvm.fullscreen.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.siti.mobile.Model.JoinData.JoinLiveStreams
import com.siti.mobile.Utils.AdapterChannels
import com.siti.mobile.Utils.Helper
import com.siti.mobile.Utils.NumberPressedUtil
import com.siti.mobile.databinding.ItemChannelChannelsViewContainerBinding

class ChannelsAdapter(
    var channels : MutableList<JoinLiveStreams>,
    private var selectedPosition : Int,
    private val onChannelItemClickListener : View.OnClickListener,
    private val onChannelItemLongClickListener : View.OnLongClickListener,
    private val isFavoriteCat : Boolean
) : RecyclerView.Adapter<ChannelsAdapter.ViewHolder>(), AdapterChannels{
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemChannelChannelsViewContainerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
       holder.bind(position)
    }

    override fun getItemCount(): Int {
        return channels.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getIndexOfNewChannel(channelNumber: Int): Int {
        return channels.indexOfFirst { channelNumber == it.channel_no }
    }

    fun updateData(newList: List<JoinLiveStreams>, newSelectedPosition: Int) {
        channels.clear()
        channels.addAll(newList)
        selectedPosition = newSelectedPosition
        notifyDataSetChanged()
    }


    inner  class ViewHolder(private val binding : ItemChannelChannelsViewContainerBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.tag = this
        }

        fun bind(position : Int) {
            val currentChannel = channels[position]
            val isSelected = position == selectedPosition

            binding.tvChannelNumber.text = currentChannel.channel_no.toString().uppercase()
            val drmSign = if (currentChannel.drm_enabled == 1) " $ " else ""
            binding.tvChannelName.text = Helper.getCamelCase(currentChannel.channel_name + drmSign).uppercase()

            if(!isFavoriteCat) {
                binding.containerPrice.visibility = View.VISIBLE
                if(currentChannel.price < 1){
                    binding.tvPrice.text = "FREE"
                }else{
                    binding.tvPrice.text = "${currentChannel.price}"
                }
            }else{
//                binding.containerPrice.visibility = View.GONE
            }

            if(currentChannel.isFavorite == "true"){
                binding.ivFavorite.visibility = View.VISIBLE
            }else{
                binding.ivFavorite.visibility = View.INVISIBLE
            }


            binding.root.onFocusChangeListener  = View.OnFocusChangeListener { v, hasFocus ->
                if(!isSelected) {
                    if (hasFocus) {
                        binding.tvChannelNumber.setTextColor(Color.BLACK)
                        binding.tvChannelName.setTextColor(Color.BLACK)
                    } else {
                        binding.tvChannelNumber.setTextColor(Color.WHITE)
                        binding.tvChannelName.setTextColor(Color.WHITE)
                    }
                }

            }



            if (position == selectedPosition && binding.root.isFocusable && NumberPressedUtil.CHANGING_BY_NUMBER) {
                NumberPressedUtil.CHANGING_BY_NUMBER = false
                binding.root.requestFocus()
                binding.root.callOnClick()
            } else if (isSelected) {
                binding.tvChannelNumber.setTextColor("#ec7b41".toColorInt())
                binding.tvChannelName.setTextColor("#ec7b41".toColorInt())
                binding.root.requestFocus()
                binding.root.post { itemView.requestFocus() }
            }else {
                binding.tvChannelNumber.setTextColor(Color.WHITE)
                binding.tvChannelName.setTextColor(Color.WHITE)
            }

            if(currentChannel.source.isNullOrEmpty() && !isSelected) {
                binding.root.setFocusable(false)
                binding.root.setAlpha(0.5f)
            }else{
                binding.root.setOnClickListener(onChannelItemClickListener)
                binding.root.setOnLongClickListener { view ->
                    currentChannel.isFavorite =
                        if (currentChannel.isFavorite == "true") "false" else "true"
                    binding.ivFavorite.visibility =
                        if (currentChannel.isFavorite == "true") View.VISIBLE else View.INVISIBLE
                    onChannelItemLongClickListener.onLongClick(view)
                    true
                }
            }



        }

    }

}