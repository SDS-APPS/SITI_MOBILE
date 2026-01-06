package com.siti.mobile.mvvm.preview.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.core.graphics.toColorInt
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.siti.mobile.Model.Room.RM_LiveStreamCategory
import com.siti.mobile.Utils.OnMiddleCatFirstFocused
import com.siti.mobile.databinding.CommonItemCategoryBinding
import com.siti.mobile.mvvm.preview.view.PreviewScreen

class CategoriesAdapter(
    private var list : List<RM_LiveStreamCategory>,
    private var selectedPosition : Int,
    private var onlyFocus : Boolean,
    private val onItemClickListener : View.OnClickListener,
    private val onMiddleCatFirstFocused: OnMiddleCatFirstFocused,
    private var enqueue: Boolean
) : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

    var focusedPosition : Int = 0
    var isFavFocused : Boolean = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = CommonItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bin(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateSelectedPosition(newPosition: Int) {
        enqueue = false
        onlyFocus = true
        focusedPosition = newPosition
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun updateData(newSelectedPosition: Int, recyclerView: RecyclerView) {
        notifyItemChanged(selectedPosition)
        selectedPosition = newSelectedPosition
        notifyItemChanged(selectedPosition)

        recyclerView.post {
            val holder = recyclerView.findViewHolderForAdapterPosition(selectedPosition)
            holder?.itemView?.requestFocus()
        }
    }


    inner class ViewHolder(private val binding: CommonItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {


        init {
            binding.containerItemCat.tag = this
            binding.containerItemCat.setOnClickListener(onItemClickListener)
        }

        @OptIn(UnstableApi::class)
        fun bin(position : Int) {
            val item = list[position]
            val isSelected = selectedPosition == position

            binding.tvChannelName.text = item.category_name
            binding.tvChannelName.setTextColor(
                if(isSelected) "#ec7b41".toColorInt()
                else Color.WHITE
            )

            binding.arrowAll.visibility =
                if(position == 0) View.VISIBLE
                else View.GONE

            if(isSelected && !onlyFocus && !PreviewScreen.oneChannelHasFocus) {
                binding.root.requestFocus()
            }else if(isSelected && onlyFocus){
                binding.root.requestFocus()
            }

            binding.containerItemCat.onFocusChangeListener = getOnFocusChangeListener(position = position)
        }


        @OptIn(UnstableApi::class)
        fun getOnFocusChangeListener(position: Int): View.OnFocusChangeListener {
            return View.OnFocusChangeListener { view, hasFocus ->
               if(position == 1) isFavFocused = hasFocus

                if(hasFocus){
                    PreviewScreen.isFirstCatFocused = position == 0
                    PreviewScreen.isLastCatFocused = position == list.lastIndex

                    if(selectedPosition != position && PreviewScreen.lastIsLeft) {
                        onMiddleCatFirstFocused.call()
                    }else{
                        focusedPosition = position
                    }


                }else {
                    PreviewScreen.isLastCatFocused = false
                    PreviewScreen.isFirstCatFocused = false
                }
            }
        }

    }

}