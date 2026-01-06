package com.siti.mobile.mvvm.fullscreen.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.siti.mobile.Model.Room.RM_LiveStreamCategory
import com.siti.mobile.databinding.ItemCategoryCategoriesViewBinding
import com.siti.mobile.mvvm.fullscreen.view.PlayerScreen

class CategoriesAdapter(
    private val categories : List<RM_LiveStreamCategory>,
    private var selectedPosition : Int,
    private val onItemClickListener : View.OnClickListener,
    private val onCategoryFocused : (Int) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemCategoryCategoriesViewBinding.inflate(
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
        return categories.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun updateData(newSelectedPosition : Int) {
        notifyItemChanged(selectedPosition)
        selectedPosition = newSelectedPosition
        notifyItemChanged(selectedPosition)
    }


    inner class ViewHolder(private val binding : ItemCategoryCategoriesViewBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.tag = this
            binding.root.setOnClickListener(onItemClickListener)
        }

        fun bind(position : Int){
            val category = categories[position]
            binding.tvCategoryName.text = category.category_name.uppercase()


            if(selectedPosition == position){
                binding.root.requestFocus()
                binding.root.isSelected = true
            }else{
                binding.root.isSelected = false
            }

            binding.root.onFocusChangeListener = object : View.OnFocusChangeListener {
                @OptIn(UnstableApi::class)
                override fun onFocusChange(v: View?, hasFocus: Boolean) {
                    v?.isSelected = hasFocus

                    if(hasFocus) {
                        onCategoryFocused(position)
                        binding.tvCategoryName.setTextColor(Color.BLACK)
                        PlayerScreen.isLastCatFocused = position == categories.lastIndex
                        PlayerScreen.isFirstCatFocused = position == 0
                    }else{
                        binding.tvCategoryName.setTextColor(Color.WHITE)
                        PlayerScreen.isLastCatFocused = false
                        PlayerScreen.isFirstCatFocused = false
                    }
                }

            }
        }

    }
}