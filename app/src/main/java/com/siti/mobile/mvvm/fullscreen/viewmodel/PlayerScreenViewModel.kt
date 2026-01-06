package com.siti.mobile.mvvm.fullscreen.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siti.mobile.Model.JoinData.JoinLiveStreams
import com.siti.mobile.Model.RetroFit.ProgramsAllChannelsModel
import com.siti.mobile.Model.Room.RM_LiveStreamCategory
import com.siti.mobile.Model.advertisment.AdvertismentModel
import com.siti.mobile.mvvm.common.data.ParkingChannelModel
import com.siti.mobile.mvvm.common.data.programs.Program
import com.siti.mobile.mvvm.fullscreen.domain.PlayerDomain
import com.siti.mobile.mvvm.preview.domain.PreviewDomain
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerScreenViewModel @Inject constructor(
    private val domain : PlayerDomain,
    val previewDomain : PreviewDomain
) : ViewModel() {

    val currentChannel = MutableLiveData<JoinLiveStreams>()
    var landingChannel : JoinLiveStreams? = null
    val currentCategory = MutableLiveData<RM_LiveStreamCategory>()
    val parkingChannels = MutableLiveData<List<ParkingChannelModel>>()

    val actualEpgs = MutableLiveData<List<Program>>()
    val programs = MutableLiveData<List<ProgramsAllChannelsModel>>()
    val channels = MutableLiveData<List<JoinLiveStreams>>()
    val categories = MutableLiveData<List<RM_LiveStreamCategory>>()

    val currentChannelIndex = MutableLiveData<Int>()
    val currentCategoryIndex = MutableLiveData<Int>()

    lateinit var channelsOriginal : List<JoinLiveStreams>
    val advertisements = MutableLiveData<List<AdvertismentModel>>()

    init {
        viewModelScope.launch {
            val _currentCategoryIndex = previewDomain.getCategoryIndex()
            val _categories = previewDomain.getCategories()
            val _currentCategory = _categories[_currentCategoryIndex]
            val _channels = previewDomain.getChannels(_currentCategory.category_id?.toInt())
            val _selectedChannel = previewDomain.getSelectedChannel()


            landingChannel = previewDomain.getLandingChannel();
            actualEpgs.postValue(domain.getActualAndNextProgram(_selectedChannel.channel_id))
            channelsOriginal = previewDomain.getChannels(null)
            currentCategoryIndex.postValue(_currentCategoryIndex)
            currentCategory.postValue(_currentCategory)
            categories.postValue(_categories)
            channels.postValue(_channels)
            currentChannelIndex.postValue(_channels.indexOfFirst { _selectedChannel.channel_id == it.channel_id })
            currentChannel.postValue(landingChannel)
            programs.postValue(domain.getAllPrograms())
            advertisements.postValue(previewDomain.getAdvertisements())
            parkingChannels.postValue(previewDomain.getParkingChannels())
        }
    }


    fun onChannelPlay(channel : JoinLiveStreams) {
        viewModelScope.launch {
            domain.saveLastSeen(channel)
            actualEpgs.postValue(domain.getActualAndNextProgram(channel.channel_id))
            domain.startTimerIncreaseMinsChannels(channel)
            domain.eventChannelPlay(channel)
        }

    }


    fun onChannelChangeByNumberPressed(position : Int) {
        viewModelScope.launch {
            currentCategoryIndex.postValue(0)
            currentCategory.postValue(categories.value!!.first())
            currentChannelIndex.postValue(position)
            currentChannel.postValue(channelsOriginal[position])
            domain.saveCategoryIndex(0)
        }
    }

    fun changeCategoryToAll() {
        viewModelScope.launch {
            previewDomain.onCategoryClick(0, categories.value!!.first())
        }
    }

    fun onCategoryClick(index: Int) {
        viewModelScope.launch {
            val categorySelected = categories.value!![index]
            previewDomain.onCategoryClick(index, categorySelected)
            currentCategory.postValue(categorySelected)
            currentCategoryIndex.postValue(index)

            val allChannels = previewDomain.getAllChannels()

            val gson = Gson()
            val listType = object : TypeToken<List<Int>>() {}.type

            val selectedCatId = categorySelected.category_id.toInt()

            fun normalizeCategoryId(raw: String?): String {
                if (raw.isNullOrBlank()) return "[]"
                if (raw.trim().startsWith("[") && raw.trim().endsWith("]")) return raw
                return "[${raw.trim()}]"   // convert "1" → "[1]"
            }

            val newChannels = allChannels.filter { channel ->
                val json = normalizeCategoryId(channel.category_id)
                val catList = try {
                    gson.fromJson<List<Int>>(json, listType)
                } catch (e: Exception) {
                    emptyList()
                }
                catList.contains(selectedCatId)
            }

            val newChannelIndex = newChannels.indexOfFirst {
                it.channel_id == currentChannel.value!!.channel_id
            }
            currentChannelIndex.postValue(if (newChannelIndex >= 0) newChannelIndex else 0)

            channels.postValue(newChannels)
        }
    }

    fun onChannelClick(index: Int) {
        if(index < channels.value!!.size){
            viewModelScope.launch {
                val channelSelected = channels.value!![index]
                currentChannel.postValue(channelSelected)
                previewDomain.onChannelClick(channelSelected)
                currentChannelIndex.postValue(index)
            }
        }
    }

    fun onChannelClickNew(index: Int) {
        if(index < channelsOriginal.size){
            viewModelScope.launch {
                val channelSelected = channelsOriginal[index]
                currentChannel.postValue(channelSelected)
                previewDomain.onChannelClick(channelSelected)
                currentChannelIndex.postValue(index)
            }
        }
    }

    fun onChannelLongClickListener(index: Int) {
        viewModelScope.launch {
            val channelSelected = channelsOriginal[index]
            previewDomain.addChannelToFavorite(channelSelected)
        }
    }

    fun logOut(onFinished : () -> Unit) {
        viewModelScope.launch {
            domain.logOut(onFinished)
        }

    }

    fun onChannelClickEPG(index: Int) {
            viewModelScope.launch {
//                channels.postValue(channelsOriginal)
                val channelSelected = channelsOriginal[index]
                currentChannel.postValue(channelSelected)
                previewDomain.onChannelClick(channelSelected)
                previewDomain.onCategoryClick(0, categories.value!!.first())
                currentChannelIndex.postValue(index)
            }
    }
    fun setCurrentChannelIndex(index: Int) {
        currentChannelIndex.postValue(index)
    }
}