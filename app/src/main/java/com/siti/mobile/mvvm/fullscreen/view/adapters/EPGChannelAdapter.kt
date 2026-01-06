package com.siti.mobile.mvvm.fullscreen.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.siti.mobile.Model.RetroFit.ProgramsAllChannelsModel
import com.siti.mobile.Utils.changeLocalToGlobalIfRequired
import com.siti.mobile.databinding.EpgFullScreenPlayerItemBinding
import com.siti.mobile.mvvm.fullscreen.view.PlayerScreen

class EPGChannelAdapter(
    val list: List<ProgramsAllChannelsModel>,
    val onItemClickListener: (String) -> Unit
) : RecyclerView.Adapter<EPGChannelAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = EpgFullScreenPlayerItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) = holder.bind(position)

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int  = position

    override fun getItemId(position: Int): Long = position.toLong()


    inner class ViewHolder(val binding: EpgFullScreenPlayerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

            init {
                binding.root.tag = this
            }


        fun bind(position : Int) {
            val current = list[position]

            if(current.programs.isNotEmpty()) {
                binding.tvCurrentProgramEpgFullScreen.text = current.programs.first().title
                Glide.with(binding.root).load(changeLocalToGlobalIfRequired(current.programs.first().logo)).into(binding.ivEpgFullScreen)
            }

            if(current.programs.size > 1){
                binding.tvNextProgramEpgFullScreen.text = current.programs[1].title
            }

            binding.container.onFocusChangeListener = object : View.OnFocusChangeListener {
                @OptIn(UnstableApi::class)
                override fun onFocusChange(v: View?, hasFocus: Boolean) {
                    if(position == list.lastIndex) {
                        PlayerScreen.isLastEPGFocused = hasFocus
                    }
                }

            }

            binding.container.setOnClickListener {
                onItemClickListener(current.channel_id)
            }
            if(position == 0) {
                binding.container.requestFocus()
            }
        }
    }

}