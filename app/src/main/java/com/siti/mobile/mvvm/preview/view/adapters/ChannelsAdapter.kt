package com.siti.mobile.mvvm.preview.view.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.siti.mobile.Model.JoinData.JoinLiveStreams
import com.siti.mobile.R
import com.siti.mobile.Utils.AdapterChannels
import com.siti.mobile.Utils.Helper
import com.siti.mobile.Utils.RGBColors
import com.siti.mobile.Utils.changeLocalToGlobalIfRequired
import com.siti.mobile.databinding.RvLiveChannelBinding
import com.siti.mobile.mvvm.preview.view.PreviewScreen
import kotlin.String

const val TAG = "ChannelsAdapter"

class ChannelsAdapter(
    private val context : Context,
    private var list : List<JoinLiveStreams>,
    private var categoryIdWhenCalled : String,
    private var selectedChannelId : String,
    private val isMulticast : Boolean,
    private val onItemClickListener : View.OnClickListener,
    private val onItemLongClickListener : View.OnLongClickListener
) : RecyclerView.Adapter<ChannelsAdapter.ViewHolder>(), AdapterChannels {

    var firstTimeToSetFocus = true
    var focusedIndex : Int = 0
    var focusCalled = false
    var isCategoryChanged = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = RvLiveChannelBinding.inflate(
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
        return list.size
    }

    override fun getIndexOfNewChannel(channelNumber: Int): Int {
        return list.indexOfFirst { channelNumber == it.channel_no }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun updateData(newList: List<JoinLiveStreams>, newSelectedChannelId: String, newCategoryId : String, recyclerView: RecyclerView) {
        list = newList
        selectedChannelId = newSelectedChannelId
        if(newCategoryId != categoryIdWhenCalled) {
            isCategoryChanged = true
            categoryIdWhenCalled = newCategoryId
        }
        focusCalled = false
        notifyDataSetChanged()
    }


    inner class ViewHolder(private val binding : RvLiveChannelBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.tag = this
            binding.root.setOnClickListener(onItemClickListener)
            binding.root.setOnLongClickListener(onItemLongClickListener)
        }

        @OptIn(UnstableApi::class)
        fun bind(position : Int){
            val currentItem = list[position]
            val drmSign = if(currentItem.drm_enabled == 1 && isMulticast) "" else ""
            val isSelected = selectedChannelId == currentItem.channel_id

            binding.tvChannel.text = Helper.getCamelCase(currentItem.channel_name)
            binding.ivCatchupIcon.visibility = if(currentItem.catch_up == 1) View.VISIBLE else View.GONE
            binding.drmChannel.text = drmSign
            binding.ivLiveFav.visibility = if(currentItem.isFavorite != null && currentItem.isFavorite.equals("true")) View.VISIBLE else View.GONE

            if(currentItem.channel_no > 0){
                binding.tvChannelCount.text = currentItem.channel_no.toString()
            }else{
                binding.tvChannelCount.visibility = View.GONE
            }

            val imagePath = changeLocalToGlobalIfRequired(currentItem.logo)
            if(imagePath.isNotEmpty()){
                Glide.with(context).load(imagePath).error(R.drawable.icon_launcher).into(binding.ivChannelIcon)
            }

            binding.root.onFocusChangeListener = getOnFocusChangeListener(position, isSelected)

            if(isCategoryChanged && !focusCalled) {
                binding.root.requestFocus()
                isCategoryChanged = false
                focusCalled = true
            } else if(isSelected && !focusCalled){
                binding.root.requestFocus()
                binding.root.isSelected = true
                focusCalled = true
            }

            if(isSelected){
                binding.tvChannel.setTextColor("#ec7b41".toColorInt())
                binding.tvChannelCount.setTextColor("#ec7b41".toColorInt())

                val interBold = ResourcesCompat.getFont(context, R.font.inter_bold)
                binding.tvChannel.typeface = interBold
                binding.tvChannelCount.typeface = interBold

                if(PreviewScreen.firstTimeAdaptFocus || firstTimeToSetFocus) {
                    binding.root.requestFocus()
                    PreviewScreen.firstTimeAdaptFocus = false
                    firstTimeToSetFocus = false
                    PreviewScreen.settingChannelAdapter = false
                }
                if(PreviewScreen.usingNumberToChangeChannel) {
                    binding.root.performClick()
                    PreviewScreen.usingNumberToChangeChannel = false
                }

            }else{
                binding.tvChannel.setTextColor(Color.WHITE)
                binding.tvChannelCount.setTextColor(Color.WHITE)

                binding.tvChannel.setTypeface(null, Typeface.NORMAL)
                binding.tvChannelCount.setTypeface(null, Typeface.NORMAL)
            }

            if(position == list.lastIndex) {
                isCategoryChanged = false
            }
        }


        @OptIn(UnstableApi::class)
        fun getOnFocusChangeListener(position : Int, isSelected : Boolean) : View.OnFocusChangeListener {
            return View.OnFocusChangeListener { view, hasFocus ->
                PreviewScreen.oneChannelHasFocus = hasFocus

                if(hasFocus && !isCategoryChanged){
                    PreviewScreen.isFirstChannelFocused = position == 0
                    PreviewScreen.isLastChannelFocused = position == list.lastIndex
                    PreviewScreen.isLastCatFocused = false
                    PreviewScreen.isFirstCatFocused = false

                    if(!isSelected){
                        binding.tvChannel.setTextColor(RGBColors.hightLighColor)
                        binding.tvChannelCount.setTextColor(RGBColors.hightLighColor)

                        binding.tvChannel.setTypeface(null, Typeface.BOLD)
                        binding.tvChannelCount.setTypeface(null, Typeface.BOLD)
                    }
                    focusedIndex = position
                    binding.root.isSelected = true
                }else if(!isSelected) {
                    PreviewScreen.isLastChannelFocused = false
                    PreviewScreen.isFirstChannelFocused = false

                    binding.tvChannel.setTextColor(Color.WHITE)
                    binding.tvChannelCount.setTextColor(Color.WHITE)

                    binding.tvChannel.setTypeface(null, Typeface.NORMAL)
                    binding.tvChannelCount.setTypeface(null, Typeface.NORMAL)

                    binding.root.isSelected = false
                }else{
                    PreviewScreen.isLastChannelFocused = false
                    PreviewScreen.isFirstChannelFocused = false
                    PreviewScreen.isLastCatFocused = false
                    binding.root.isSelected = false
                }
            }
        }

    }

}