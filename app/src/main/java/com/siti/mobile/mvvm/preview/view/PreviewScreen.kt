package com.siti.mobile.mvvm.preview.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.siti.mobile.Model.JoinData.JoinLiveStreams
import com.siti.mobile.Model.Room.RM_LiveStreamCategory
import com.siti.mobile.Player.BadSignalViews
import com.siti.mobile.Player.CurrentPlayerScreen
import com.siti.mobile.Player.PlayInterface
import com.siti.mobile.Player.PlayerFrozenFrame
import com.siti.mobile.Player.PlayerFuncI
import com.siti.mobile.Player.PlayerManager
import com.siti.mobile.Player.PlayerManager.Companion.containerChannelNotAvailable
import com.siti.mobile.Player.PlayerManager.Companion.currentPlayerScreen
import com.siti.mobile.Player.PlayerManager.Companion.currentProgressBar
import com.siti.mobile.Player.PlayerManager.Companion.exoPlayer
import com.siti.mobile.R
import com.siti.mobile.Utils.AdvertismentScreen
import com.siti.mobile.Utils.ChannelSelectedCallback
import com.siti.mobile.Utils.CurrentTimeContainer.date
import com.siti.mobile.Utils.CurrentTimeContainer.hour
import com.siti.mobile.Utils.NumberPressedUtil
import com.siti.mobile.Utils.OnMiddleCatFirstFocused
import com.siti.mobile.Utils.ServerChecker
import com.siti.mobile.Utils.SocketHelper
import com.siti.mobile.Utils.SocketSingleton
import com.siti.mobile.Utils.getAdvertismentByPosition
import com.siti.mobile.Utils.startLoopAdvertisment
import com.siti.mobile.databinding.ActivityLivetvNewBinding
import com.siti.mobile.mvvm.common.SliderAdAdapter
import com.siti.mobile.mvvm.common.view.dialogs.ExitAppDialog
import com.siti.mobile.mvvm.common.view.dialogs.onClickOk
import com.siti.mobile.mvvm.config.helpers.ConfigurationHelper
import com.siti.mobile.mvvm.fullscreen.view.PlayerScreen
import com.siti.mobile.mvvm.preview.view.adapters.CategoriesAdapter
import com.siti.mobile.mvvm.preview.view.adapters.ChannelsAdapter
import com.siti.mobile.mvvm.preview.viewmodel.PreviewScreenViewModel
import com.siti.mobile.mvvm.settings.view.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "PreviewScreen"

@UnstableApi
@AndroidEntryPoint
class PreviewScreen : SocketHelper(), PlayerFrozenFrame {

    private lateinit var binding : ActivityLivetvNewBinding

    private val previewScreenViewModel : PreviewScreenViewModel by viewModels()

    private var categoriesAdapter: CategoriesAdapter? = null
    private var channelsAdapter: ChannelsAdapter? = null

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    @Inject
    lateinit var playerManager : PlayerManager

    @Inject
    lateinit var mPreferences : SharedPreferences

    @Inject
    lateinit var configurationHelper: ConfigurationHelper

    private lateinit var playerView : PlayerView
    private lateinit var progressBar : ProgressBar

    private lateinit var numberPressedUtil: NumberPressedUtil

    private var goingToFullScreen = false

    companion object {
        var oneChannelHasFocus = false
        var isFirstChannelFocused = false
        var isFirstCatFocused = false
        var isLastCatFocused = false
        var isLastChannelFocused = false
        var lastIsLeft = false
        var firstTimeAdaptFocus = false
        var settingChannelAdapter = false
        var usingNumberToChangeChannel = false
        var lcoValid = true
        var firstBootUp = false
        var isActive = false
        var lastChannelId = ""
        var goingFromLiveTV = false
    }

    private lateinit var serverChecker: ServerChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityLivetvNewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        serverChecker = ServerChecker(this, binding.root, binding.content.containerPLayerPreview.ivNetworkError)

        playerView = binding.content.containerPLayerPreview.playerViewHome
        progressBar = binding.content.containerPLayerPreview.progressBar

        binding.contentCatsRv.baseBackgroundD.baseBackground.btnSettings.setOnClickListener {
//            releasePlayer()
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }

        binding.contentCatsRv.baseBackgroundD.baseBackground.btnSettings.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if(!hasFocus) {
                    binding.contentCatsRv.baseBackgroundD.baseBackground.btnSettings.isFocusable = false
                }
            }

        }

        initDependencies()
        setRecyclerViews()
        registerNetworkCallback()

        previewScreenViewModel.channels.observe(this) {  channels ->
            setChannelsAdapter(channels, previewScreenViewModel.currentChannelIndex.value ?: 0, previewScreenViewModel.currentChannel.value?.channel_id ?: "" ,previewScreenViewModel.currentCategory.value?.category_id ?: "", )
        }
        previewScreenViewModel.categories.observe(this) { categories ->
            setCategoriesAdapter(categories, previewScreenViewModel.currentCategoryIndex.value ?: 0)
        }
        previewScreenViewModel.currentChannelIndex.observe(this) { currentChannelIndex ->
            Log.w(TAG, "CHANNELS: ${previewScreenViewModel.channels.value}")
            if(previewScreenViewModel.channels.value != null && previewScreenViewModel.channels.value!!.isNotEmpty()){
                setChannelsAdapter(previewScreenViewModel.channels.value!!, currentChannelIndex, previewScreenViewModel.currentChannel.value?.channel_id ?: "" ,previewScreenViewModel.currentCategory.value?.category_id ?: "")
            }

        }
        previewScreenViewModel.currentCategoryIndex.observe(this) { currentCategoryIndex ->
            if(previewScreenViewModel.categories.value !=null && previewScreenViewModel.categories.value!!.isNotEmpty()){
                setCategoriesAdapter(previewScreenViewModel.categories.value!!, currentCategoryIndex)
            }
        }
        previewScreenViewModel.currentChannel.observe(this) {
//            if(previewScreenViewModel.channels.value != null && previewScreenViewModel.channels.value!!.isNotEmpty()){
//                setChannelsAdapter(previewScreenViewModel.channels.value!!, previewScreenViewModel.currentChannelIndex.value ?: 0, false)
//            }
//            binding.content.containerPLayerPreview.playerViewHome.setShutterBackgroundColor(Color.BLACK)
            binding.content.containerPLayerPreview.containerChannelNotAvailable.visibility = View.GONE
            PlayerManager.lastPlayedUrl = it.source ?: ""
            lastChannelId = it.channel_id
//            releasePlayer()
            if(it.source != null && it.source.isNotEmpty()){
//                if(binding.content.containerPLayerPreview.ivSubscriptionExpired.isVisible ||  binding.content.containerPLayerPreview.containerChannelNotAvailable.isVisible){
//                    binding.content.containerPLayerPreview.playerViewHome.setShutterBackgroundColor(Color.BLACK)
//                }
                binding.content.containerPLayerPreview.layoutChannelNotAvailable.textSubscribed.text = getString(R.string.channel_temporarily_unavailable)
                if(!PlayerScreen.goingFromFullScreen){
                    play(it.source, it.drm_enabled, it.streamToken, "ViewModel Observer: ")
                }else{
                    PlayerScreen.goingFromFullScreen = false
                }
                binding.content.containerPLayerPreview.ivSubscriptionExpired.visibility = View.GONE
            }else{
                Toast.makeText(this, R.string.channel_not_subscribed, Toast.LENGTH_SHORT).show()
                binding.content.containerPLayerPreview.ivSubscriptionExpired.visibility = View.VISIBLE
            }
            callSockets(it.channel_id)
        }

        previewScreenViewModel.advertisements.observe(this) {
            val advertismentModelFiltered = getAdvertismentByPosition(2, it)
            val sliderAdAdapter = SliderAdAdapter(this)
            sliderAdAdapter.renewItems(advertismentModelFiltered)
            val sliderAdview = binding.content.contentAdvertisements.imageSlider
            sliderAdview.setSliderAdapter(sliderAdAdapter)
            startLoopAdvertisment(sliderAdview, advertismentModelFiltered, AdvertismentScreen.LIVETV)
            sliderAdview.sliderPager.setFocusable(false)
        }

        playerManager.setPlayerFrozenCallback(this)
        playerManager.setPlayInterface(object  : PlayInterface {
            override fun onPlay(url: String, drm: Int, token : String?) {
                play(url, drm, token, "PlayInterface")
            }

        })
    }

    override fun onBackPressed() {
        val exitAppDialog = ExitAppDialog(object : onClickOk {
            public override fun invoke() {
                finishAffinity()
                System.exit(0)
            }
        })
        exitAppDialog.show(supportFragmentManager, "exitDialogTag")
    }

    private fun initDependencies(){
        numberPressedUtil = NumberPressedUtil(this)
    }

    override fun onResume() {
        super.onResume()
        binding.contentCatsRv.baseBackgroundD.baseBackground.btnSettings.visibility = View.VISIBLE
        goingToFullScreen = false
        containerChannelNotAvailable = binding.content.containerPLayerPreview.containerChannelNotAvailable
        serverChecker.startChecking()
        createPlayer()
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            serverChecker.stopChecking()
            binding.contentCatsRv.baseBackgroundD.baseBackground.btnSettings.visibility = View.GONE
            //     setCurrentAdvertismentScreen(AdvertismentScreen.NONE);
            playerView.setPlayer(null)
            if (!goingToFullScreen) {
                playerManager.setPlayInterface(null)
                firstBootUp = true
                playerManager.releaseExoPlayer("[PreviewScreen] onPause")
            }
            isActive = false
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            serverChecker.stopChecking()
            binding.contentCatsRv.baseBackgroundD.baseBackground.btnSettings.visibility = View.GONE
            //     setCurrentAdvertismentScreen(AdvertismentScreen.NONE);
            playerView.setPlayer(null)
            if (!goingToFullScreen) {
                playerManager.releaseExoPlayer("[PreviewScreen] onStop")
                firstBootUp = true
                playerManager.setPlayInterface(null)
            }
            isActive = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun registerNetworkCallback() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    binding.content.containerPLayerPreview.ivNetworkError.visibility = View.GONE
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                    binding.content.containerPLayerPreview.ivNetworkError.visibility = View.VISIBLE
                }
            }

            override fun onUnavailable() {
                runOnUiThread {
                    binding.content.containerPLayerPreview.ivNetworkError.visibility = View.VISIBLE
                }
            }
        }

        connectivityManager.registerNetworkCallback(request, networkCallback)
    }


    private fun setRecyclerViews(){
        val layoutManagerCategory = LinearLayoutManager(this)
        layoutManagerCategory.setOrientation(LinearLayoutManager.VERTICAL)
        binding.contentCatsRv.containerCats.rvCatsDefault.setLayoutManager(layoutManagerCategory)
        binding.contentCatsRv.containerCats.rvCatsDefault.itemAnimator = null


        //Add Channels recyclerview
        val layoutManager = LinearLayoutManager(this)
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL)
        val dividerItemDecoration = DividerItemDecoration(
            applicationContext,
            layoutManager.orientation
        )
        binding.content.rvChannelsLiveTv.setItemAnimator(null)
        binding.content.rvChannelsLiveTv.setLayoutManager(layoutManager)
        dividerItemDecoration.setDrawable(
            AppCompatResources.getDrawable(
                this,
                R.drawable.live_divider_line
            )!!
        )
        binding.content.rvChannelsLiveTv.addItemDecoration(dividerItemDecoration)
    }

    private fun setChannelsAdapter(channels : List<JoinLiveStreams>, currentIndexSelected : Int, selectedChannelId : String ,categoryIdWhenCalled : String) {

            if(channelsAdapter == null){
                channelsAdapter = ChannelsAdapter(
                    context = this,
                    list = channels,
                    categoryIdWhenCalled = categoryIdWhenCalled,
                    isMulticast = false,
                    onItemClickListener = onChannelItemClickListener,
                    onItemLongClickListener = onChannelItemLongClickListener,
                    selectedChannelId = selectedChannelId
                )
                binding.content.rvChannelsLiveTv.adapter = channelsAdapter
            }else{
                channelsAdapter!!.updateData(channels, selectedChannelId, categoryIdWhenCalled, recyclerView = binding.content.rvChannelsLiveTv)
            }

            binding.content.rvChannelsLiveTv.post {
                binding.content.rvChannelsLiveTv.scrollToPosition(currentIndexSelected)
            }

    }

    private fun setCategoriesAdapter(categories : List<RM_LiveStreamCategory>, currentIndexSelected : Int) {

            if(categoriesAdapter == null){
                categoriesAdapter = CategoriesAdapter(
                    list = categories,
                    selectedPosition = currentIndexSelected,
                    onlyFocus = false,
                    onItemClickListener = onCategoryItemClickListener,
                    onMiddleCatFirstFocused = onMiddleCatFirstFocused,
                    enqueue = false
                )
                binding.contentCatsRv.containerCats.rvCatsDefault.adapter = categoriesAdapter
            }else{
                categoriesAdapter!!.updateData(currentIndexSelected, binding.contentCatsRv.containerCats.rvCatsDefault)
            }


           binding.contentCatsRv.containerCats.rvCatsDefault.post {
               binding.contentCatsRv.containerCats.rvCatsDefault.scrollToPosition(currentIndexSelected)
           }

    }

    private val onCategoryItemClickListener = View.OnClickListener {
        val viewHolder = it.tag as RecyclerView.ViewHolder
        val position = viewHolder.bindingAdapterPosition

        if(position == 0 && previewScreenViewModel.currentCategoryIndex.value!! == 0){
            binding.content.rvChannelsLiveTv.scrollToPosition(0)
        }
        previewScreenViewModel.onCategoryClick(position)
    }

    private val onMiddleCatFirstFocused = OnMiddleCatFirstFocused {
        try{
            val categoryRowIndex = previewScreenViewModel.currentCategoryIndex.value ?: 0
            categoriesAdapter?.updateSelectedPosition(categoryRowIndex)
            categoriesAdapter?.notifyDataSetChanged()
            binding.contentCatsRv.containerCats.rvCatsDefault.smoothScrollToPosition(categoryRowIndex)
        } catch (e : Exception) {
            e.printStackTrace()
        }

    }

    private val onChannelItemClickListener = View.OnClickListener {
        val viewHolder = it.tag as RecyclerView.ViewHolder
        val position = viewHolder.bindingAdapterPosition
        if(position == previewScreenViewModel.currentChannelIndex.value){
            goingToFullScreen = true
//            releasePlayer()
            goingFromLiveTV = true
            startActivity(Intent(this, PlayerScreen::class.java))
            finish()
        }else{
            previewScreenViewModel.onChannelClick(position)
        }
    }

    private val onChannelItemLongClickListener = View.OnLongClickListener { view ->
        val binding = view?.tag as com.siti.mobile.mvvm.fullscreen.view.adapters.ChannelsAdapter.ViewHolder
        previewScreenViewModel.onChannelLongClickListener(binding.position)
        true
    }

    private fun play(url: String?, drm: Int, token : String?, from : String) {
        Log.w(TAG, "CALLIING PLAY FORM PREVIEW SCCREEN -- $from")
        if (url == null) {
            return
        }

        playerManager.playChannel(true, playerView, progressBar, url, drm, object : PlayerFuncI {
            override fun createPlayerCallback() {
                Log.w(TAG, "Creating player")
                createPlayer()
            }

            override fun releasePlayerCallback() {
//                Log.w(TAG, "Releasing player")
//                releasePlayer()
//                playerManager.releaseMediaPlayer()
            }
        }, false, CurrentPlayerScreen.LIVE_TV_PREVIEW, token)
    }


    private fun createPlayer() {
        currentPlayerScreen = CurrentPlayerScreen.LIVE_TV_PREVIEW
        if (exoPlayer == null) {
            val badSignalViews = BadSignalViews(
                binding.content.containerPLayerPreview.containerNotSignal,
                binding.content.containerPLayerPreview.tvCounterBadSignal
            )
            playerManager.createExoPlayer(
                configurationHelper.getDefaultLoadControl(true),
                this,
                binding.content.containerPLayerPreview.playerViewHome,
                badSignalViews,
                null,
                onFrameStuck = {
                    Toast.makeText(this, "Frame Stuck", Toast.LENGTH_SHORT).show()
                },
                onPlayerReady = {

                }
            )
            play(previewScreenViewModel.lastPlayedUrl.value, previewScreenViewModel.lastPlayedUrlDrm.value ?: 0 ,previewScreenViewModel.currentChannel.value?.streamToken ?: "", "Create Pllayer")

        } else {
            Log.w(TAG, "SETTING PREVIEW PLAYER")
//            if (playerView.player == null) {
                playerView.setPlayer(exoPlayer)
                playerManager.refreshRatio(playerView, exoPlayer!!)
                //                playerView.setEnabledShutterView(playerManager.isFrameBlackEnabled());
//            }
        }
        currentProgressBar = progressBar
        containerChannelNotAvailable = binding.content.containerPLayerPreview.containerChannelNotAvailable
        PlayerManager.ivSubscriptionExpired = binding.content.containerPLayerPreview.ivSubscriptionExpired
    }

    private fun releasePlayer() {
        Log.w(TAG, "Releasing player method")
        playerManager.releaseExoPlayer("[PreviewScreen] releasePlayer")
    }

    override fun onFrameFrozen() {
//        channelContainerLogoFullScreen.setVisibility(View.VISIBLE)
//        Glide.with(this).load(LiveTvPreview.actualChannel.getLogo()).into(channelLogoFullScreen)
    }

    override fun onFrameUnfrozen() {
//        TODO("Not yet implemented")
    }

    fun callSockets(channel: String?) {
        val tvCurrentDate = binding.contentCatsRv.baseBackgroundD.baseBackground.tvCurrentDate
        val tvCurrentHour = binding.contentCatsRv.baseBackgroundD.baseBackground.tvCurrentHour
        tvCurrentDate.text = date
        tvCurrentHour.text = hour

        val fpContainer = binding.contentFpLayout

        super.socketConnection(
            this,
            SocketSingleton.getInstance(this, mPreferences),
            fpContainer.fingerprintGuidelineX,
            fpContainer.fingerprintGuidelineY,
            fpContainer.fingerprintTextView,
            fpContainer.fingerprintForensic,
            channel,
            fpContainer.fingerprintFakeLaker,
            tvCurrentDate,
            tvCurrentHour,
            null
        )
    }

    private fun manageNumberPressed(keyCode : Int){
        if (numberPressedUtil.numberCode.length >= 3 && binding.tvNumberPressed.scaleX == 0f) {
            numberPressedUtil.numberCode = ""
        }
        numberPressedUtil.onNumberPressed(
            keyCode,
            previewScreenViewModel.channels.value,
            previewScreenViewModel.channelsOriginal,
            channelsAdapter,
            binding.tvNumberPressed,
            binding.tvNameChannelSelected,
            object : ChannelSelectedCallback {
                override fun onChannelSelected(
                    position: Int,
                    foundedInOriginalChannelData: Boolean
                ) {
                    usingNumberToChangeChannel = true
                    if (foundedInOriginalChannelData) {
                        previewScreenViewModel.onCategoryClick(0)
                    }
                    previewScreenViewModel.onChannelClick(position)
                }
            })
    }

    private var lastUpKeyTime: Long = 0

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        lastIsLeft = keyCode == KeyEvent.KEYCODE_DPAD_LEFT

        if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && oneChannelHasFocus && !isFirstChannelFocused) {
            return true
        }else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && isFirstChannelFocused) {
            binding.contentCatsRv.baseBackgroundD.baseBackground.btnSettings.isFocusable = true
            binding.contentCatsRv.baseBackgroundD.baseBackground.btnSettings.requestFocus()
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpKeyTime < 100) {
                return true
            }
            lastUpKeyTime = currentTime
        }

        if ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.ACTION_DOWN) &&
            (isLastCatFocused || isLastChannelFocused)
        ) return true

        if ((keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.ACTION_UP) &&
            (isFirstChannelFocused || isFirstCatFocused)
        ) {
            return true
        }

        if (keyCode in 7..16) {
            Log.w(TAG, "Keycode: $keyCode")
            if (keyCode == 7 && numberPressedUtil.numberCode == "") {
                return true
            }
            try {
                manageNumberPressed(keyCode)
            } catch (e: UninitializedPropertyAccessException) {
                e.printStackTrace()
            }
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

}