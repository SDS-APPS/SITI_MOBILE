package com.siti.mobile.mvvm.preview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siti.mobile.Model.JoinData.JoinLiveStreams
import com.siti.mobile.Model.Room.RM_LiveStreamCategory
import com.siti.mobile.Model.advertisment.AdvertismentModel
import com.siti.mobile.mvvm.common.data.ParkingChannelModel
import com.siti.mobile.mvvm.preview.domain.PreviewDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PreviewScreenViewModel"

@HiltViewModel
class PreviewScreenViewModel @Inject constructor(
    private val domain : PreviewDomain
): ViewModel() {


    val channels = MutableLiveData<List<JoinLiveStreams>>()
    lateinit var channelsOriginal : List<JoinLiveStreams>
    val categories = MutableLiveData<List<RM_LiveStreamCategory>>()
    val parkingChannels = MutableLiveData<List<ParkingChannelModel>>()

    val currentChannel = MutableLiveData<JoinLiveStreams>()
    val currentChannelIndex = MutableLiveData<Int>()

    val currentCategory = MutableLiveData<RM_LiveStreamCategory>()
    val currentCategoryIndex = MutableLiveData<Int>()

    val lastPlayedUrl = MutableLiveData<String>()
    val lastPlayedUrlDrm = MutableLiveData<Int>()

    val advertisements = MutableLiveData<List<AdvertismentModel>>()

    companion object {
        var firstTime = true
    }

    init {
        viewModelScope.launch {
            val _currentCategoryIndex = domain.getCategoryIndex()
            val _categories = domain.getCategories()
            val _currentCategory = _categories[_currentCategoryIndex]
            val _channels = domain.getChannels(_currentCategory.category_id?.toInt())
            var _selectedChannel : JoinLiveStreams?

            if(firstTime) {
                _selectedChannel = domain.getLandingChannel()
                firstTime = false
            }else{
                _selectedChannel = domain.getSelectedChannel()
            }

            _selectedChannel?.let {
                currentChannel.postValue(it)
                currentChannelIndex.postValue(_channels.indexOfFirst { _selectedChannel.channel_id == it.channel_id })
            }


            parkingChannels.postValue(domain.getParkingChannels())
            channelsOriginal = domain.getChannels(null)
            currentCategoryIndex.postValue(_currentCategoryIndex)
            categories.postValue(_categories)
            channels.postValue(_channels)
            advertisements.postValue(domain.getAdvertisements())



        }
    }

    fun onCategoryClick(index : Int) {
        viewModelScope.launch {
            val categorySelected = categories.value!![index]
            domain.onCategoryClick(index, categorySelected)
            currentCategory.postValue(categorySelected)
            currentCategoryIndex.postValue(index)
            currentChannelIndex.postValue(-1)
            channels.postValue(domain.getChannels(categorySelected.category_id?.toInt()))
        }
    }

    fun onChannelClick(index: Int) {
       viewModelScope.launch {
           val channelSelected = channels.value!![index]
           domain.onChannelClick(channelSelected)
           currentChannel.postValue(channelSelected)
           currentChannelIndex.postValue(index)
       }
    }

    fun onChannelLongClickListener(index: Int) {
        viewModelScope.launch {
            val channelSelected = channels.value!![index]
            domain.addChannelToFavorite(channelSelected)
        }
    }

}