package com.siti.mobile.mvvm.fullscreen.view

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.StatFs
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.edit
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.siti.mobile.FrequencyGenerator
import com.siti.mobile.Model.JoinData.JoinLiveStreams
import com.siti.mobile.Model.RetroFit.ProgramsAllChannelsModel
import com.siti.mobile.Model.Room.RM_LiveStreamCategory
import com.siti.mobile.Model.advertisment.AdvertismentModel
import com.siti.mobile.Player.AsyncTaskStartMulticast.Companion.g_ChannelStopped
import com.siti.mobile.Player.CatchupHelper
import com.siti.mobile.Player.CurrentPlayerScreen
import com.siti.mobile.Player.PlayInterface
import com.siti.mobile.Player.PlayerFrozenFrame
import com.siti.mobile.Player.PlayerFuncI
import com.siti.mobile.Player.PlayerManager
import com.siti.mobile.Player.PlayerManager.Companion.containerChannelNotAvailable
import com.siti.mobile.Player.PlayerManager.Companion.currentContainerPlayer
import com.siti.mobile.Player.PlayerManager.Companion.currentPlayerScreen
import com.siti.mobile.Player.PlayerManager.Companion.currentProgressBar
import com.siti.mobile.Player.PlayerManager.Companion.currentTvHoldSec
import com.siti.mobile.Player.PlayerManager.Companion.exoPlayer
import com.siti.mobile.Player.PlayerType
import com.siti.mobile.Player.StreamType
import com.siti.mobile.R
import com.siti.mobile.Utils.ChannelSelectedCallback
import com.siti.mobile.Utils.Helper
import com.siti.mobile.Utils.HelperCategoryFocus
import com.siti.mobile.Utils.KEY_BOOTUP_ACTIVITY
import com.siti.mobile.Utils.KEY_EXP_DATE
import com.siti.mobile.Utils.KEY_USER_ID
import com.siti.mobile.Utils.LatencyHelper
import com.siti.mobile.Utils.NumberPressedUtil
import com.siti.mobile.Utils.ServerChecker
import com.siti.mobile.Utils.SocketHelper
import com.siti.mobile.Utils.SocketSingleton
import com.siti.mobile.Utils.VALUE_FULL_SCREEN_ACTIVITY
import com.siti.mobile.Utils.changeLocalToGlobalIfRequired
import com.siti.mobile.databinding.ActivityVideoPlayerBinding
import com.siti.mobile.mvvm.common.data.programs.Program
import com.siti.mobile.mvvm.config.helpers.ConfigurationHelper
import com.siti.mobile.mvvm.fullscreen.view.adapters.CategoriesAdapter
import com.siti.mobile.mvvm.fullscreen.view.adapters.ChannelSuggestionAdapter
import com.siti.mobile.mvvm.fullscreen.view.adapters.ChannelsAdapter
import com.siti.mobile.mvvm.fullscreen.view.adapters.EPGChannelAdapter
import com.siti.mobile.mvvm.fullscreen.view.adapters.ViewedChannelsAdapter
import com.siti.mobile.mvvm.fullscreen.viewmodel.PlayerScreenViewModel
import com.siti.mobile.mvvm.login.view.LoginActivity
import com.siti.mobile.mvvm.preview.data.CATEGORY_ID_FAVORITE
import com.siti.mobile.mvvm.preview.view.PreviewScreen
import com.siti.mobile.mvvm.util.helpers.CustomStringHelper.Companion.getHour
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.system.exitProcess

private const val DELAY_INFO_LIVE = 7500L
private const val TAG = "PlayerScreen"

@UnstableApi
@AndroidEntryPoint
class PlayerScreen : SocketHelper(), SurfaceHolder.Callback, PlayerFrozenFrame {

    private lateinit var binding: ActivityVideoPlayerBinding
    private val previewScreenViewModel: PlayerScreenViewModel by viewModels()

    @Inject
    lateinit var playerManager: PlayerManager

    @Inject
    lateinit var mPreferences: SharedPreferences

    @Inject
    lateinit var configurationHelper: ConfigurationHelper

    private lateinit var containerPlayer: LinearLayout
    private lateinit var tvHoldOk3Sec: TextView

    private var goingToLiveTV = false

    private lateinit var player: ExoPlayer
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var surfaceHolder: SurfaceHolder

    var playerCreated = false

    private lateinit var numberPressedUtil: NumberPressedUtil


    // START KEY DOWN VARIABLES
    private var lastIsDown = false
    private var lastKeyDown = 0L
    private var returned = false
    private var pulseAKey = false
    private var lastKeyDownCenter = 0L
    private var isOkButtonHold3Secs = false
    var lastIsOk = false
    var isDown = false
    // END KEY DOWN VARIABLES

    // START ADAPTERS
    private var categoriesAdapter: CategoriesAdapter? = null
    private var channelsAdapter: ChannelsAdapter? = null

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    // Handlers
    var handlerGoLive: Handler = Handler()
    var runnableGoLive: Runnable = Runnable { isOkButtonHold3Secs = true }

    var handlerContainerInfoLive: Handler = Handler()
    var runnableInfoLive: Runnable = object : Runnable {
        override fun run() {
            if (!pulseAKey) {
                Log.w("VISIBILITY", "TO GONE RUNNABLE")
                println("print called -->1")
                binding.containerChannelInfo.visibility = View.INVISIBLE
            } else {
                pulseAKey = false
            }
        }
    }

    var handlerChannelChangeUp: Handler? = null
    var runnableChannelChangeUp: Runnable? = null
    var firstTimeChanged = true
    var finalExpiryDate: Int = 0


    var currentLastParkingChannelIndex = 0

    private lateinit var gestureDetector: GestureDetector

    companion object {
        var isLastParkingChannelPlayed = false
        var lastFrameTimeInMillis = 0L
        var lastUpdateBuffering = 0L
        var isLastCatFocused = false
        var isFirstCatFocused = false
        var actualUrl = ""
        var dateFullScreen = ""
        var isActive = false
        var isLastEPGFocused = false
        var ON_LOOPER = false
        var currentPlayer: PlayerType = PlayerType.EXOPLAYER
        var isCatchup = false
        var goingFromFullScreen = false

        const val SOURCE_ALL = 1
        const val SOURCE_GENRE = 2
        const val SOURCE_FAV = 3
        const val SOURCE_RECENT = 4

        var lastNavigatedSource = SOURCE_ALL
    }

    private lateinit var serverChecker: ServerChecker

    @Inject
    lateinit var latencyHelper: LatencyHelper

    private val standByHours: Int = 120
    private val channelTimeoutHandler = Handler(Looper.getMainLooper())
    private var channelTimeoutRunnable: Runnable? = null
    private val CHANNEL_TIMEOUT_MS = standByHours * 60 * 1000L // 2 hour

    private var standbyCountDown: CountDownTimer? = null
    private val STANDBY_COUNTDOWN_MS = 2 * 60 * 1000L // 2 mins

    private var skipNextEpgUpdate = false

    private var channelTimer: CountDownTimer? = null
    private var isAllChannelScreenOpen = false
    private var recentPlayIndex = 1
    private var currentSearchResults: List<JoinLiveStreams> = emptyList()
    private var blockAutoPlay = false
    private var isFavouriteNavigation = false
    private var favList: List<JoinLiveStreams> = emptyList()
    private var favIndex = 0
    private var showAlphabet = false
    private var lastPlayedCategoryIndex = 0
    private val PREF_TUTORIAL = "pref_tutorial"
    private val KEY_TUTORIAL_SHOWN = "tutorial_shown"

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
//        handleTutorial()
        binding.containerTutorial.visibility = View.VISIBLE
        supportActionBar?.hide()
        binding.player.useController = false
        setContentView(binding.root)
        initCustomControllerViews()
        initSurfaceComponents()
        initDependencies()
        initRecyclerViews()
        registerNetworkCallback()
        setControllerClickListeners()
        setInfoValues()
        onClick()
        isAllChannelScreenOpen = true

        val expDate = mPreferences.getString(KEY_EXP_DATE, "")

        finalExpiryDate = Helper.daysRemaining(expDate)

        println("print expiry date -->$finalExpiryDate")


        if (finalExpiryDate <= 3) {
//            binding.includeChannelInfo.qrFL.visibility = View.GONE
            binding.includeChannelInfo.expiryLL.visibility = View.VISIBLE
            binding.includeChannelInfo.tvExpiry.text =
                "Your package expires in " + Helper.daysRemaining(expDate) + " days. To recharge please Scan the QR CODE"
        } else {
//            binding.includeChannelInfo.qrFL.visibility = View.GONE
            binding.includeChannelInfo.expiryLL.visibility = View.GONE
        }

//        binding.includeChannelInfo.tvExpiryDate.text = "Expires in ${Helper.daysRemaining(expDate)} days."


        val isFirstTimeBootUp = mPreferences.getString(
            KEY_BOOTUP_ACTIVITY, VALUE_FULL_SCREEN_ACTIVITY
        ) == VALUE_FULL_SCREEN_ACTIVITY

        if (isFirstTimeBootUp) {
            firstTimeChanged = false
        }

        previewScreenViewModel.currentChannel.observe(this) { channel ->

            // 👇 Load last viewed channel from SharedPreferences
            fun loadLastViewedChannel(): JoinLiveStreams? {
                val gson = Gson()
                val json = mPreferences.getString("recent_channels", "[]")
                val type = object : TypeToken<MutableList<JoinLiveStreams>>() {}.type
                val list: MutableList<JoinLiveStreams> = gson.fromJson(json, type)
                return list.firstOrNull()   // index 0 => last viewed
            }

            val lastViewed = loadLastViewedChannel()

            // 👇 UPDATED safeChannel logic (your original + lastViewed)
            val safeChannel =
                channel ?: previewScreenViewModel.parkingChannels.value?.firstOrNull()?.channel
                ?: previewScreenViewModel.landingChannel ?: lastViewed
                ?: previewScreenViewModel.channelsOriginal.firstOrNull()


            if (safeChannel == null) {
                Toast.makeText(this, "No channels available to play", Toast.LENGTH_SHORT).show()
                return@observe
            }

            // If channel was null but we found a fallback -> set it
            if (channel == null) {
                previewScreenViewModel.currentChannel.value = safeChannel
            }

            binding.containerChannelNotAvailable.visibility = View.GONE
            setInfoLiveValues(safeChannel, emptyList())
            PlayerManager.lastPlayedUrl = safeChannel.source ?: ""
            actualUrl = safeChannel.source ?: ""
            binding.player.useController = safeChannel.catch_up == 1
            isCatchup = safeChannel.catch_up == 1
            callSockets(safeChannel.channel_id)

            if (!safeChannel.source.isNullOrEmpty()) {
                if (binding.ivSubscriptionExpired.isVisible || binding.containerChannelNotAvailable.isVisible) {
                    binding.player.setShutterBackgroundColor(Color.BLACK)
                }
                binding.ivSubscriptionExpired.visibility = View.GONE
                binding.containerChannelNotAvailableLayout.textSubscribed.text =
                    getString(R.string.channel_temporarily_unavailable)

                if (!firstTimeChanged && !PreviewScreen.goingFromLiveTV) {
                    play(safeChannel)
                }
            } else {
                binding.ivSubscriptionExpired.visibility = View.VISIBLE
            }

            PreviewScreen.goingFromLiveTV = false
            firstTimeChanged = false
        }

        previewScreenViewModel.categories.observe(this) {
            setCategoryAdapter(it, previewScreenViewModel.currentCategoryIndex.value ?: 0)
        }

        previewScreenViewModel.advertisements.observe(this) { ads ->
            if (ads.isNotEmpty()) {
                loadAdInto(1, ads, binding.lastViewChannelDialog.lastChannelIV)
                loadAdInto(1, ads, binding.includeContainerChannels.allChannelIV)
                loadAdInto(2, ads, binding.includeChannelInfo.ivAd)
                loadAdInto(3, ads, binding.includeControllerPlayer.controllerIV)
            }
        }

        previewScreenViewModel.currentCategoryIndex.observe(this) {
            if (previewScreenViewModel.categories.value != null) {
                if (!isAllChannelScreenOpen) {
                    binding.includeContainerChannels.tvCategoryName.text =
                        previewScreenViewModel.categories.value!![it].category_name
                }

            }
            setCategoryAdapter(previewScreenViewModel.categories.value ?: emptyList(), it)
        }

        previewScreenViewModel.channels.observe(this) {
            setChannelsAdapter(it, previewScreenViewModel.currentChannelIndex.value ?: 0)
        }

        previewScreenViewModel.currentChannelIndex.observe(this) {
            setChannelsAdapter(previewScreenViewModel.channels.value ?: emptyList(), it)
        }

        previewScreenViewModel.programs.observe(this) {
            setEPGAdapter(it)
        }

        previewScreenViewModel.actualEpgs.observe(this) {
            if (skipNextEpgUpdate) {
                skipNextEpgUpdate = false
                return@observe
            }
            if (previewScreenViewModel.currentChannel.value != null) {
                setInfoLiveValues(previewScreenViewModel.currentChannel.value!!, it)
            }
        }

        previewScreenViewModel.parkingChannels.observe(this) { parkingChannels ->
            if (!isLastParkingChannelPlayed) {

                if (parkingChannels.isNotEmpty()) {

                    CoroutineScope(Dispatchers.Main).launch {

                        for (parking in parkingChannels) {
                            playPreview(parking.channel)
                            delay(parking.milliSecs)
                        }

                        delay(1000)
                        isLastParkingChannelPlayed = true
                        playLastViewedAfterLanding()
                    }

                } else {
                    isLastParkingChannelPlayed = true
                    previewScreenViewModel.landingChannel?.let {
                        playPreview(it)
                        return@observe
                    }
                    playLastViewedAfterLanding()
                }
            }
        }

        binding.includeControllerPlayer.tvLogout.setOnClickListener {
            binding.includeControllerPlayer.containerSettings.visibility = View.INVISIBLE
            showLogoutDialog()
        }

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {

            private val SWIPE_THRESHOLD = 120

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            // ✅ SINGLE TAP
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (previewScreenViewModel.currentChannel.value != null) {
                    setInfoLiveValues(previewScreenViewModel.currentChannel.value!!, emptyList())
                }

                return true
            }

            // ✅ DOUBLE TAP
            override fun onDoubleTap(e: MotionEvent): Boolean {
                binding.containerKeyboard.visibility = View.VISIBLE
                showAlphabet = false
                setupCustomKeyboard()
                return true
            }


            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {

                if (e1 == null) return false

                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                // HORIZONTAL SWIPE
                if (kotlin.math.abs(diffX) > kotlin.math.abs(diffY)) {

                    if (kotlin.math.abs(diffX) > SWIPE_THRESHOLD) {

                        if (diffX > 0) {
                            // ➡️ LEFT SWIPE
                            playLastViewedChannel()
                        } else {
                            // ⬅️ RIGHT SWIPE
//                            binding.newControlPlayer.visibility = View.VISIBLE
                            handleRightNavigation()
                        }
                        return true
                    }

                } else {
                    // VERTICAL SWIPE
                    if (kotlin.math.abs(diffY) > SWIPE_THRESHOLD) {

                        if (diffY < 0) {
                            // ⬆️ UP
                            channelChangeByKeyCode(KeyEvent.KEYCODE_DPAD_UP)
                        } else {
                            // ⬇️ DOWN
                            channelChangeByKeyCode(KeyEvent.KEYCODE_DPAD_DOWN)
                        }
                        return true
                    }
                }
                return false
            }

        })

        binding.player.setOnTouchListener { _, event ->

            if (binding.newControlPlayer.isVisible || binding.favouriteChannel.isVisible || binding.lastViewChannel.isVisible || binding.epgView.isVisible || binding.containerInfoDialog.isVisible || binding.containerInfoUser.isVisible || binding.containerSupport.isVisible || binding.containerDialogLogout.isVisible || binding.containerKeyboard.isVisible) {
                return@setOnTouchListener false
            }

            gestureDetector.onTouchEvent(event)
            true
        }

    }

    private fun handleRightNavigation() {

        if (previewScreenViewModel.currentChannel.value?.catch_up == 1) return
        if (binding.containerCategories.isVisible) return
        if (!binding.containerChannels.isGone) return
        if (binding.containerKeyboard.isVisible) return
        if (binding.containerDialogExit.isVisible) return
        if (binding.containerDialogLogout.isVisible) return

        isFavouriteNavigation = false
        pulseAKey = true
        newControllerHide()
        binding.containerChannelInfo.visibility = View.INVISIBLE

        when (lastNavigatedSource) {

            SOURCE_GENRE -> {
                binding.containerChannels.visibility = View.VISIBLE
                val categoryIndex = lastPlayedCategoryIndex

                binding.includeContainerChannels.tvCategoryName.text =
                    previewScreenViewModel.categories.value
                        ?.get(categoryIndex)?.category_name ?: "Genre"

                isAllChannelScreenOpen = false
                previewScreenViewModel.onCategoryClick(categoryIndex)

                val currentChannel = previewScreenViewModel.currentChannel.value
                val genreChannels = previewScreenViewModel.channels.value

                if (genreChannels != null && currentChannel != null) {
                    val selectedIndex = genreChannels.indexOfFirst {
                        it.channel_id == currentChannel.channel_id
                    }.coerceAtLeast(0)

                    val channelRv = binding.includeContainerChannels.rvChannels
                    (channelRv.layoutManager as? LinearLayoutManager)
                        ?.scrollToPositionWithOffset(selectedIndex, 0)

                    channelRv.post {
                        channelRv.requestFocus()
                        channelRv.findViewHolderForAdapterPosition(selectedIndex)
                            ?.itemView?.requestFocus()
                    }
                }
            }

            SOURCE_FAV -> showFavouriteChannelDialog()
            SOURCE_RECENT -> showLastViewChannelDialog()

            else -> {
                binding.containerChannels.visibility = View.VISIBLE
                binding.includeContainerChannels.tvCategoryName.text = "All Channel"
                isAllChannelScreenOpen = true

                val currentChannel = previewScreenViewModel.currentChannel.value

                CoroutineScope(Dispatchers.Main).launch {
                    val allChannels = withContext(Dispatchers.IO) {
                        previewScreenViewModel.previewDomain.getAllChannels()
                    }
                    val favChannels = withContext(Dispatchers.IO) {
                        previewScreenViewModel.previewDomain.getChannels(CATEGORY_ID_FAVORITE)
                    }

                    val favIds = favChannels.map { it.channel_id }.toSet()
                    allChannels.forEach {
                        it.isFavorite = if (favIds.contains(it.channel_id)) "true" else "false"
                    }

                    val selectedIndex = allChannels.indexOfFirst {
                        it.channel_id == currentChannel?.channel_id
                    }.coerceAtLeast(0)

                    previewScreenViewModel.channels.postValue(allChannels)
                    previewScreenViewModel.currentChannelIndex.postValue(selectedIndex)
                }
            }
        }
    }


    private fun handleTutorial() {
        val prefs = getSharedPreferences(PREF_TUTORIAL, Context.MODE_PRIVATE)
        val isShown = prefs.getBoolean(KEY_TUTORIAL_SHOWN, false)

        if (!isShown) {
            binding.containerTutorial.visibility = View.VISIBLE
        } else {
            binding.containerTutorial.visibility = View.GONE
        }
    }

    private fun playLastViewedAfterLanding() {

        // RESET so observer allows autoplay
        firstTimeChanged = true

        val gson = Gson()
        val json = mPreferences.getString("recent_channels", "[]")
        val type = object : TypeToken<MutableList<JoinLiveStreams>>() {}.type
        val list: MutableList<JoinLiveStreams> = gson.fromJson(json, type)

        if (list.isNotEmpty()) {
            val lastViewed = list[0]

            // directly play
            previewScreenViewModel.currentChannel.value = lastViewed
            play(lastViewed)
        }
    }


    private fun loadAdInto(position: Int, ads: List<AdvertismentModel>, imageView: ImageView) {
        ads.find { it.position == position }?.let { ad ->
            Glide.with(imageView).load(changeLocalToGlobalIfRequired(ad.url)).into(imageView)
        }
    }

    fun setEPGAdapter(list: List<ProgramsAllChannelsModel>) {
        binding.epgViewContainer.loadingEpgs.visibility = View.GONE
        val adapter = EPGChannelAdapter(list) { channelId ->
            val index = previewScreenViewModel.channelsOriginal.indexOfFirst {
                it.channel_id == channelId
            }
            previewScreenViewModel.onChannelClickEPG(index)
            binding.epgView.visibility = View.GONE
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.epgViewContainer.rvEpgInformation.layoutManager = layoutManager
        binding.epgViewContainer.rvEpgInformation.adapter = adapter
    }

    fun initRecyclerViews() {
        val catLayoutManager = GridLayoutManager(this, 4)
        catLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.includeContainerCategories.rvCategories.layoutManager = catLayoutManager

        val channelsLayoutManager = LinearLayoutManager(this)
        channelsLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.includeContainerChannels.rvChannels.layoutManager = channelsLayoutManager
    }

    fun initDependencies() {
        numberPressedUtil = NumberPressedUtil(this)
        serverChecker = ServerChecker(this, binding.root, binding.ivNetworkError)
    }

    fun startCheckLatency() {
//        val ip = mPreferences.getString(KEY_SERVER_IP, SERVER_GLOBAL_IP_EMPTY)
//        val ipToCheck =
//            if (ip!!.contains(SERVER_GLOBAL_IP_EMPTY)) SERVER_GLOBAL_IP_EMPTY else SERVER_LOCAL_IP_EMPTY
//        latencyHelper.startChecking(ipToCheck, 443, binding.containerInfoLiveLayout.tvLatency)
    }

    fun callSockets(channel: String?) {
        super.socketConnection(
            this,
            SocketSingleton.getInstance(this, mPreferences),
            binding.guideline4,
            binding.guideline3,
            binding.textView2,
            findViewById<ImageView?>(R.id.fingerprintForensic),
            channel,
            binding.fingerprintFakeLaker,
            null,
            binding.includeChannelInfo.tvCurrentHour,
            null
        )
    }

    private fun initCustomControllerViews() {
        containerPlayer = findViewById<LinearLayout>(R.id.containerPlayer)
        tvHoldOk3Sec = findViewById<TextView>(R.id.tvOk3SecForLive)
    }

    private fun playPreview(channel: JoinLiveStreams) {
        if (channel.source == null) {
            return
        }
        playerManager.playChannel(
            false,
            binding.player,
            binding.progressBar,
            channel.source,
            channel.drm_enabled,
            object : PlayerFuncI {
                override fun createPlayerCallback() {
                    createPlayerAndPlay(channel)
                }

                override fun releasePlayerCallback() {
//                    releasePlayer("PlayerFuncI Callback")
                }
            },
            previewScreenViewModel.currentChannel.value?.catch_up == 1,
            CurrentPlayerScreen.PLAYER_FRAGMENT,
            channel.streamToken
        )
    }

    private fun releasePlayer(from: String) {
        if (!goingToLiveTV) {
            g_ChannelStopped = true
            playerManager.releaseExoPlayer("[PlayerScreen] $from")
        }
        playerManager.releaseMediaPlayer()
    }

    private fun createMediaPlayer() {
        playerManager.createMediaPlayer(binding.player, surfaceHolder = surfaceHolder, null)
    }

    private fun initSurfaceComponents() {
        val surfaceViewMediaPlayer = binding.videoLayout
        val holder = surfaceViewMediaPlayer.holder
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        surfaceHolder = holder
        holder.addCallback(this)
    }

    var parkingChannelJob: Job? = null

    private fun createPlayerAndPlay(currentChannel: JoinLiveStreams?) {
        currentPlayerScreen = CurrentPlayerScreen.PLAYER_FRAGMENT
        if (playerManager.getPlayerByTypeCast(StreamType.UNICAST) == PlayerType.MEDIAPLAYER && previewScreenViewModel.currentChannel.value?.catch_up != 1) {
            binding.containerSurfaceView.visibility = View.VISIBLE
            //  Toast.makeText(this, "Playing with MediaPlayer", Toast.LENGTH_SHORT).show();
            createMediaPlayer()
            currentChannel?.let {
                play(currentChannel)
            }
        } else {
            //Toast.makeText(this, "Playing with ExoPlayer", Toast.LENGTH_SHORT).show();
            binding.containerSurfaceView.visibility = View.GONE
            if (exoPlayer == null) {
//                val badSignalViews = BadSignalViews(
//                    binding.containerNotSignal,
//                    binding.tvCounterBadSignal
//                )
                playerManager.createExoPlayer(
                    configurationHelper.getDefaultLoadControl(true),
                    this,
                    binding.player,
                    null,
                    null,
                    onFrameStuck = {
                        Toast.makeText(this, "Frame Stuck", Toast.LENGTH_SHORT).show()
                    },
                    onPlayerReady = {
                        parkingChannelJob?.cancel()
                        parkingChannelJob = CoroutineScope(Dispatchers.IO).launch {
                            if (!isLastParkingChannelPlayed) {
                                val parkingChannels =
                                    previewScreenViewModel.parkingChannels.value ?: emptyList()
                                delay(parkingChannels[currentLastParkingChannelIndex].milliSecs)
                                currentLastParkingChannelIndex++
                                if (currentLastParkingChannelIndex < parkingChannels.size) {
                                    withContext(Dispatchers.Main) {
                                        val channel =
                                            parkingChannels[currentLastParkingChannelIndex].channel
                                        playPreview(channel)
//                                        Toast.makeText(
//                                            this@PlayerScreen,
//                                            "Playing Parking Channel [$currentLastParkingChannelIndex]",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
                                    }
                                } else {
                                    isLastParkingChannelPlayed = true
                                    previewScreenViewModel.landingChannel?.let { landingChannel ->
                                        withContext(Dispatchers.Main) {
                                            playPreview(landingChannel)
//                                            Toast.makeText(this@PlayerScreen, "Playing Landing Channel", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                            }
                        }

                    })
                binding.player.setPlayer(exoPlayer)
                currentChannel?.let {
                    play(currentChannel)
                }
            } else {
                if (binding.player.player == null) {
                    currentTvHoldSec = tvHoldOk3Sec
//                    playerManager.currentHdIcon = ivInfoLiveHDChannel
//                    playerManager.currentCathupIcon = ivInfoLiveCatchup
                    playerManager.updateReadyAgain()
                    playerManager.refreshRatio(binding.player, exoPlayer!!)
                    binding.player.setPlayer(exoPlayer)
//                    if (playerManager.isFrameBlackEnabled()) {
//                        binding.player.setShutterBackgroundColor(Color.BLACK)
//                    }
                }
            }
        }
        currentContainerPlayer = containerPlayer
        currentProgressBar = binding.progressBar
        containerChannelNotAvailable = binding.containerChannelNotAvailable
        PlayerManager.ivSubscriptionExpired = binding.ivSubscriptionExpired
        player = exoPlayer!!
        this.mediaPlayer = PlayerManager.mediaPlayer

        playerManager.setPlayerFrozenCallback(this)
        playerManager.setPlayInterface(object : PlayInterface {
            override fun onPlay(url: String, drm: Int, token: String?) {
                play(previewScreenViewModel.currentChannel.value!!)
            }

        })
    }

    private fun play(channel: JoinLiveStreams) {
        lastPlayedCategoryIndex = previewScreenViewModel.currentCategoryIndex.value ?: 0
        saveViewedChannel(channel)
        previewScreenViewModel.onChannelPlay(channel)
        playPreview(channel)

        startChannelTimeout()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
//        if(previewScreenViewModel.currentChannel.value != null){
//            createPlayerAndPlay(previewScreenViewModel.currentChannel.value !!, true)
//        }
        surfaceHolder.setKeepScreenOn(true)
    }

    private fun setControllerClickListeners() {
        val bindingController = binding.includeControllerPlayer
        bindingController.tvGenre.setOnClickListener {
            binding.containerCategories.visibility = View.VISIBLE
            hideController()
        }
        bindingController.tvAllChannel.setOnClickListener {
            hideController()
            binding.containerChannels.visibility = View.VISIBLE
            previewScreenViewModel.onCategoryClick(0)
        }
        bindingController.btnChannelEpg.setOnClickListener {
            showOrHideEPG()
            hideController()
        }
        bindingController.btnChannelSettings.setOnClickListener {
//            if(bindingController.containerChannelSettings.isVisible) {
//                bindingController.containerChannelSettings.visibility = View.INVISIBLE
//            }else{
//            bindingController.containerChannelSettings.visibility = View.VISIBLE
//            }
            bindingController.containerChannelSettings.visibility = View.VISIBLE
            bindingController.containerUserInfo.visibility = View.INVISIBLE
            bindingController.containerSettings.visibility = View.INVISIBLE
            binding.epgView.visibility = View.GONE
        }
        bindingController.btnSettings.setOnClickListener {
//            if(bindingController.containerSettings.isVisible) {
//                bindingController.containerSettings.visibility = View.INVISIBLE
//            }else{
//                bindingController.containerSettings.visibility = View.VISIBLE
//            }

            bindingController.containerSettings.visibility = View.VISIBLE
            bindingController.containerChannelSettings.visibility = View.GONE
            bindingController.containerUserInfo.visibility = View.GONE
            binding.epgView.visibility = View.GONE
        }
        bindingController.tvSystemInfo.setOnClickListener {
            binding.containerInfoDialog.visibility = View.VISIBLE
            hideController()
        }

        bindingController.tvMoreSettings.setOnClickListener {
            binding.containerMoreSettings.visibility = View.VISIBLE
            hideController()
        }

        bindingController.btnMyAccount.setOnClickListener {
//            if(bindingController.containerUserInfo.isVisible) {
//                bindingController.containerUserInfo.visibility = View.INVISIBLE
//            }else{
//                bindingController.containerUserInfo.visibility = View.VISIBLE
//            }

            bindingController.containerUserInfo.visibility = View.VISIBLE
            bindingController.containerChannelSettings.visibility = View.INVISIBLE
            bindingController.containerSettings.visibility = View.INVISIBLE
            binding.epgView.visibility = View.GONE
        }
        binding.includeControllerPlayer.tvUserInfo.setOnClickListener {
            binding.containerInfoUser.visibility = View.VISIBLE
            hideController()
        }
        bindingController.tvFavourite.setOnClickListener {
            showFavouriteChannelDialog()
            hideController()
        }

        binding.includeNewControllerPlayer.allChannelBtn.setOnClickListener {
            newControllerHide()
            binding.containerChannels.visibility = View.VISIBLE
            binding.includeContainerChannels.tvCategoryName.text = "All Channel"
            isAllChannelScreenOpen = true

            val currentChannel = previewScreenViewModel.currentChannel.value

            CoroutineScope(Dispatchers.Main).launch {
                val allChannels = withContext(Dispatchers.IO) {
                    previewScreenViewModel.previewDomain.getAllChannels()
                }

                val favChannels = withContext(Dispatchers.IO) {
                    previewScreenViewModel.previewDomain.getChannels(CATEGORY_ID_FAVORITE)
                }

                val favIds = favChannels.map { it.channel_id }.toSet()

                allChannels.forEach { ch ->
                    ch.isFavorite = if (favIds.contains(ch.channel_id)) "true" else "false"
                }

                val selectedIndex = allChannels.indexOfFirst {
                    it.channel_id == currentChannel?.channel_id
                }.coerceAtLeast(0)

                previewScreenViewModel.channels.postValue(allChannels)
                previewScreenViewModel.currentChannelIndex.postValue(selectedIndex)
            }
        }

        binding.includeNewControllerPlayer.genreBtn.setOnClickListener {
            newControllerHide()
            binding.containerCategories.visibility = View.VISIBLE
            isAllChannelScreenOpen = false

            val lastCategoryIndex = previewScreenViewModel.currentCategoryIndex.value ?: 0
            val categoryRv = binding.includeContainerCategories.rvCategories

            (categoryRv.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                lastCategoryIndex, 0
            )
            categoryRv.post {
                categoryRv.requestFocus()
                val selectedView =
                    categoryRv.findViewHolderForAdapterPosition(lastCategoryIndex)?.itemView
                selectedView?.requestFocus()
            }
        }

        binding.includeNewControllerPlayer.favBtn.setOnClickListener {
            newControllerHide()
            showFavouriteChannelDialog()
        }

        binding.includeNewControllerPlayer.recentBtn.setOnClickListener {
            newControllerHide()
            showLastViewChannelDialog()
        }

        binding.includeNewControllerPlayer.epgBtn.setOnClickListener {
            newControllerHide()
            showOrHideEPG()
        }

        binding.includeNewControllerPlayer.systemInfoBtn.setOnClickListener {
            newControllerHide()
            binding.containerInfoDialog.visibility = View.VISIBLE
        }


        binding.includeNewControllerPlayer.userInfoBtn.setOnClickListener {
            newControllerHide()
            binding.containerInfoUser.visibility = View.VISIBLE
        }

        binding.includeNewControllerPlayer.supportBtn.setOnClickListener {
            newControllerHide()
            binding.containerSupport.visibility = View.VISIBLE
        }

        binding.includeNewControllerPlayer.logoutBtn.setOnClickListener {
            newControllerHide()
            showLogoutDialog()
        }
    }

    private fun newControllerHide() {
        binding.newControlPlayer.visibility = View.GONE
    }

    private fun hideController() {
        binding.containerController.visibility = View.GONE
        binding.includeControllerPlayer.containerChannelSettings.visibility = View.INVISIBLE
        binding.includeControllerPlayer.containerUserInfo.visibility = View.INVISIBLE
        binding.includeControllerPlayer.containerSettings.visibility = View.INVISIBLE
    }

    override fun surfaceChanged(
        holder: SurfaceHolder, format: Int, width: Int, height: Int
    ) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        playerManager.releaseMediaPlayer()
    }

    override fun onFrameFrozen() {
//        TODO("Not yet implemented")
    }

    override fun onFrameUnfrozen() {
//        TODO("Not yet implemented")
    }

    private fun setInfoLiveValues(
        currentChannel: JoinLiveStreams, programData: List<Program>
    ) {
        binding.player.hideController()
        binding.includeChannelInfo.tvChannelName.text = currentChannel.channel_name
        binding.includeChannelInfo.tvChannelNo.text = currentChannel.channel_no.toString()
//        binding.includeChannelInfo.channelInfoBottom.MacId.text =
//            mPreferences.getString("mac", "null")?.uppercase()
        Glide.with(this).load(changeLocalToGlobalIfRequired(currentChannel.logo))
            .into(binding.includeChannelInfo.ivChannelLogo)

        val tvInfoLiveCurrentProgramHour = binding.includeChannelInfo.tvCurrentProgramHour
        val tvInfoLiveCurrentProgramName = binding.includeChannelInfo.tvCurrentProgram

        val tvInfoLiveNextProgramHour = binding.includeChannelInfo.tvNextProgramHour
        val tvInfoLiveNextProgramName = binding.includeChannelInfo.tvNextProgram

        val tvChannelPrice = binding.includeChannelInfo.tvChannelPrice

        if (currentChannel.price < 1) {
            tvChannelPrice.text = "FREE"
        } else {
            tvChannelPrice.text = "${currentChannel.price}"
        }


//        val tvInfoLiveNext2ProgramHour = binding.containerInfoLiveLayout.tvInfoLiveHourNext2Program
//        val tvInfoLiveNext2ProgramName = binding.containerInfoLiveLayout.tvInfoLiveNameNext2Program

        if (programData.size > 0) {
            val currentProgram = programData[0]
            tvInfoLiveCurrentProgramHour.text = "${getHour(currentProgram.startAt)} - " + getHour(
                currentProgram.endAt
            )
            tvInfoLiveCurrentProgramName.text = currentProgram.title
        } else {
            tvInfoLiveCurrentProgramHour.text = getString(R.string.no_information_available_hour)
            tvInfoLiveCurrentProgramName.text = getString(R.string.no_information_available)
        }

        if (programData.size > 1) {
            val nextProgram = programData[1]
            tvInfoLiveNextProgramHour.text = getHour(nextProgram.startAt) + " - " + getHour(
                nextProgram.endAt
            )
            tvInfoLiveNextProgramName.text = nextProgram.title
        } else {
            tvInfoLiveNextProgramHour.text = getString(R.string.no_information_available_hour)
            tvInfoLiveNextProgramName.text = getString(R.string.no_information_available)
        }

//        if (programData.size > 2) {
//            val next2Program = programData[2]
//            tvInfoLiveNext2ProgramHour.text = getHour(next2Program.startAt) + " - " + getHour(
//                next2Program.endAt
//            )
//            tvInfoLiveNext2ProgramName.text = next2Program.title
//        } else {
//            tvInfoLiveNext2ProgramHour.text = getString(R.string.no_information_available_hour)
//            tvInfoLiveNext2ProgramName.text = getString(R.string.no_information_available)
//        }
        showChannelInfo()
    }


    private var lastKeyPressTime = 0L


    @SuppressLint("SetTextI18n")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9) {

            when (keyCode) {
                KeyEvent.KEYCODE_0 -> "0"
                KeyEvent.KEYCODE_1 -> "1"
                KeyEvent.KEYCODE_2 -> "2"
                KeyEvent.KEYCODE_3 -> "3"
                KeyEvent.KEYCODE_4 -> "4"
                KeyEvent.KEYCODE_5 -> "5"
                KeyEvent.KEYCODE_6 -> "6"
                KeyEvent.KEYCODE_7 -> "7"
                KeyEvent.KEYCODE_8 -> "8"
                KeyEvent.KEYCODE_9 -> "9"
                else -> ""
            }

            numberPressed(keyCode)

            return true
        }

        if (isFavouriteNavigation) {

            val size = favList.size

            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                favIndex = (favIndex + 1) % size           // ⭐ WRAP
                playFavChannel(favIndex)
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                favIndex = if (favIndex - 1 < 0) size - 1  // ⭐ WRAP
                else favIndex - 1
                playFavChannel(favIndex)
                return true
            }
        } else {
            println(" --> else called")
        }

        if (!isLastParkingChannelPlayed) return true
//        if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER &&  binding.containerChannelInfo.isVisible){
//            binding.containerChannelInfo.visibility = View.INVISIBLE
//            return true;
//        }
        if (binding.containerDialogExit.isVisible) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (binding.includeDialogExit.tvNo.hasFocus()) {
                    binding.includeDialogExit.tvYes.requestFocus()
                } else {
                    binding.includeDialogExit.tvNo.requestFocus()
                }
            }
            if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER) {
                return true
            }
        }

        if (binding.containerDialogLogout.isVisible) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (binding.includeLogoutExit.tvNo.hasFocus()) {
                    binding.includeLogoutExit.tvYes.requestFocus()
                } else {
                    binding.includeLogoutExit.tvNo.requestFocus()
                }
            }
            if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER) {
                return true
            }
        }

        if (binding.containerDialogStandBy.isVisible) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                hideStandbyDialog()
                goOnBack = false
                resetChannelTimeout()
                return true
            }
            hideStandbyDialog()
            resetChannelTimeout()
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            if (binding.containerChannels.isVisible) {
                binding.containerChannels.visibility = View.GONE
                binding.newControlPlayer.visibility = View.GONE
                return true
            }

            if (binding.favouriteChannel.isVisible) {
                binding.favouriteChannel.visibility = View.GONE
//                binding.newControlPlayer.visibility = View.VISIBLE
//                binding.includeNewControllerPlayer.favBtn.requestFocus()
                return true
            }

            if (binding.lastViewChannel.isVisible) {
                binding.lastViewChannel.visibility = View.GONE
//                binding.newControlPlayer.visibility = View.VISIBLE
//                binding.includeNewControllerPlayer.recentBtn.requestFocus()
                return true
            }

            if (binding.containerKeyboard.isVisible) {
                goOnBack = false
                stopChannelChangeTimer()
                binding.containerKeyboard.visibility = View.GONE
                clearText()
                return true
            }

            if (binding.newControlPlayer.isVisible) {
                goOnBack = false
                binding.newControlPlayer.visibility = View.GONE
                return true
            }

            return super.onKeyDown(keyCode, event)
        }

        if (binding.containerChannelInfo.isVisible) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (!binding.containerKeyboard.isVisible) {
                    //                    showLastViewChannelDialog()
                    playLastViewedChannel()
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                println("visible --> 1")
                binding.containerChannelInfo.visibility = View.INVISIBLE
//                showController()
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                pulseAKey = true
                try {
                    if (binding.containerKeyboard.isGone) {
                        channelChangeByKeyCode(keyCode)
                    }
                } catch (e: Exception) {
                    println("Channel change failed: ${e.message}")
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                println("called center")
                binding.containerKeyboard.visibility = View.VISIBLE
                showAlphabet = false
                setupCustomKeyboard()
                return true
            }
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (!binding.newControlPlayer.isVisible && !binding.containerChannels.isVisible && !binding.containerCategories.isVisible && !binding.favouriteChannel.isVisible && !binding.lastViewChannel.isVisible && !binding.epgView.isVisible && !binding.containerInfoDialog.isVisible && !binding.containerInfoUser.isVisible && !binding.containerSupport.isVisible && !binding.containerDialogLogout.isVisible) {
                binding.containerKeyboard.visibility = View.VISIBLE
                showAlphabet = false
                setupCustomKeyboard()
            }
            return true
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (!binding.newControlPlayer.isVisible && !binding.containerKeyboard.isVisible && !binding.containerChannels.isVisible && !binding.containerCategories.isVisible && !binding.favouriteChannel.isVisible && !binding.lastViewChannel.isVisible && !binding.epgView.isVisible && !binding.containerInfoDialog.isVisible && !binding.containerInfoUser.isVisible && !binding.containerSupport.isVisible) {
                playLastViewedChannel()
            }
        }

        if (binding.lastViewChannel.isVisible) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                binding.lastViewChannel.visibility = View.GONE
                binding.newControlPlayer.visibility = View.VISIBLE
                binding.includeNewControllerPlayer.recentBtn.requestFocus()
                return true
            }
            return super.onKeyDown(keyCode, event)
        }

        if (binding.containerChannels.isVisible && !isAllChannelScreenOpen) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                binding.containerChannels.visibility = View.GONE
                binding.containerCategories.visibility = View.VISIBLE

                val lastCategoryIndex = previewScreenViewModel.currentCategoryIndex.value ?: 0
                val categoryRv = binding.includeContainerCategories.rvCategories

                (categoryRv.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                    lastCategoryIndex, 0
                )
                categoryRv.requestFocus()
                categoryRv.post {
                    val selectedView =
                        categoryRv.findViewHolderForAdapterPosition(lastCategoryIndex)?.itemView
                    selectedView?.requestFocus()
                }
                return true
            }
            return super.onKeyDown(keyCode, event)
        }

        if (binding.favouriteChannel.isVisible) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                binding.favouriteChannel.visibility = View.GONE
                isFavouriteNavigation = false
                binding.newControlPlayer.visibility = View.VISIBLE
                binding.includeNewControllerPlayer.favBtn.requestFocus()
                return true
            }
            return super.onKeyDown(keyCode, event)
        }



        if (binding.newControlPlayer.isVisible) {

            if (binding.includeNewControllerPlayer.gridLayout.isVisible) {
                binding.includeNewControllerPlayer.bottomContainer.menuTitle.text = "View Channels"
            } else {
                binding.includeNewControllerPlayer.bottomContainer.menuTitle.text = "Previous Menu"
            }

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                newControllerHide()
                return false
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                return false
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                return false
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                return false
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                return false
            }
            return super.onKeyDown(keyCode, event)
        }

        if (binding.containerChannels.isVisible) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                binding.containerChannels.visibility = View.GONE
                return false
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                binding.newControlPlayer.visibility = View.VISIBLE
                binding.containerChannels.visibility = View.GONE
                return false
            }
        }

        if (binding.fingerprintFakeLaker.isVisible) {
            Log.w(TAG, "FP FORCED, NOT KEY WORKING")
            return true
        }
        val currentTime = System.currentTimeMillis()
        if ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP)) {
            // Si no han pasado 300 ms desde la última pulsación, ignorar
            if (currentTime - lastKeyPressTime < 50) {
                return true
            }
            lastKeyPressTime = currentTime
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && previewScreenViewModel.currentChannel.value?.catch_up != 1 && !binding.containerCategories.isVisible && binding.containerChannels.isGone && binding.containerKeyboard.isGone && binding.containerDialogExit.isGone && binding.containerDialogLogout.isGone) {

            isFavouriteNavigation = false

            pulseAKey = true
            newControllerHide()
            binding.containerChannelInfo.visibility = View.INVISIBLE

            when (lastNavigatedSource) {
                SOURCE_GENRE -> {
                    binding.containerChannels.visibility = View.VISIBLE
                    val categoryIndex = lastPlayedCategoryIndex
                    binding.includeContainerChannels.tvCategoryName.text =
                        previewScreenViewModel.categories.value?.get(categoryIndex)?.category_name
                            ?: "Genre"
                    isAllChannelScreenOpen = false

                    previewScreenViewModel.onCategoryClick(categoryIndex)
                    val currentChannel = previewScreenViewModel.currentChannel.value
                    val genreChannels = previewScreenViewModel.channels.value

                    if (genreChannels != null && currentChannel != null) {
                        val selectedIndex = genreChannels.indexOfFirst {
                            it.channel_id == currentChannel.channel_id
                        }.coerceAtLeast(0)
                        val channelRv = binding.includeContainerChannels.rvChannels
                        (channelRv.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                            selectedIndex,
                            0
                        )
                        channelRv.post {
                            channelRv.requestFocus()
                            channelRv.findViewHolderForAdapterPosition(selectedIndex)?.itemView?.requestFocus()
                        }
                    }
                }

                SOURCE_FAV -> {
                    showFavouriteChannelDialog()
                }

                SOURCE_RECENT -> {
                    showLastViewChannelDialog()
                }

                else -> {
                    binding.containerChannels.visibility = View.VISIBLE
                    binding.includeContainerChannels.tvCategoryName.text = "All Channel"
                    isAllChannelScreenOpen = true

                    val currentChannel = previewScreenViewModel.currentChannel.value

                    CoroutineScope(Dispatchers.Main).launch {
                        val allChannels = withContext(Dispatchers.IO) {
                            previewScreenViewModel.previewDomain.getAllChannels()
                        }
                        val favChannels = withContext(Dispatchers.IO) {
                            previewScreenViewModel.previewDomain.getChannels(CATEGORY_ID_FAVORITE)
                        }
                        val favIds = favChannels.map { it.channel_id }.toSet()
                        allChannels.forEach { ch ->
                            ch.isFavorite = if (favIds.contains(ch.channel_id)) "true" else "false"
                        }

                        val selectedIndex = allChannels.indexOfFirst {
                            it.channel_id == currentChannel?.channel_id
                        }.coerceAtLeast(0)

                        previewScreenViewModel.channels.postValue(allChannels)
                        previewScreenViewModel.currentChannelIndex.postValue(selectedIndex)
                    }
                }
            }
            return false
        }
        if (binding.containerController.isVisible) {
            pulseAKey = true
            val controlled = controlFocus(keyCode)
            if (controlled) {
                return true
            }
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && event!!.repeatCount > 0) return true
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event!!.repeatCount > 0) {
//            CatchupHelper.btnOkPressed = true
//            if (player.getPlayWhenReady() == false) {
//                player.setPlayWhenReady(true)
//                binding.containerPauseFR.visibility = View.GONE
//                binding.containerPlayFR.setVisibility(View.VISIBLE)
//                Handler().postDelayed(
//                    Runnable { binding.containerPlayFR.visibility = View.GONE },
//                    3000
//                )
//            }
//            returned = true
//            return true
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            returned = false
        }
        if (binding.fingerprintFakeLaker.isVisible) return true
//        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && binding.containerController.isVisible && previewScreenViewModel.currentChannel.value?.catch_up != 1 && !binding.containerCategories.isVisible && !binding.containerChannels.isVisible) {
//            binding.containerController.setVisibility(View.GONE)
//            pulseAKey = true
//            showOrHideEPG()
//            return false
//        } else

//        } else
//            if (keyCode == KeyEvent.KEYCODE_F4 || keyCode == KeyEvent.KEYCODE_D) {
//            pulseAKey = true
//            showOrHideCatchup(previewScreenViewModel.currentChannel.value?.catch_up == 1)
//            return false
//        }
        if ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) && lastKeyDown != 0L && lastKeyDown + HelperCategoryFocus.delayChannel > System.currentTimeMillis()) {
            return true
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            lastKeyDown = System.currentTimeMillis()
        }
        lastIsDown = keyCode == KeyEvent.KEYCODE_DPAD_DOWN
        CatchupHelper.btnOkPressed = keyCode == KeyEvent.KEYCODE_DPAD_CENTER

        //     CatchupHelper.actualKeyCode = event.getKeyCode();
        if (keyCode >= 7 && keyCode <= 16) {
            numberPressed(keyCode)
        }
//        if (categoriesAdapter.isFavFocused && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
//            setCategoryAdapter(LiveTvPreview.categoryData, 0, false)
//        }

//        if ((keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.ACTION_UP) && isFirstCatFocused) return true

        if ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.ACTION_DOWN) && (isLastCatFocused || PreviewScreen.isLastChannelFocused || isLastEPGFocused)) return true
//        if ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) && binding.categoryView.visibility != View.VISIBLE && binding.epgView.visibility != View.VISIBLE && binding.catchupView.visibility != View.VISIBLE) {
//            try {//|| binding.player.getIfMediaplayerIsPlaying()
//                if ((player.isPlaying()) || (mediaPlayer != null && mediaPlayer!!.isPlaying) || player.playWhenReady ) {
//                    Log.w(TAG, "Changing channelby Key Code normal")
//                    channelChangeByKeyCode(keyCode)
//                    return true
//                } else {
//                    //  playerView.showController();
//                }
//            } catch (e: IllegalStateException) {
//                channelChangeByKeyCode(keyCode)
//                return true
//            }

        if ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) && binding.containerCategories.visibility != View.VISIBLE && binding.containerChannels.visibility != View.VISIBLE && binding.epgView.visibility != View.VISIBLE && binding.catchupView.visibility != View.VISIBLE && binding.containerInfoDialog.visibility != View.VISIBLE && binding.containerMoreSettings.visibility != View.VISIBLE && binding.containerSupport.visibility != View.VISIBLE && binding.containerInfoUser.visibility != View.VISIBLE && binding.containerKeyboard.visibility != View.VISIBLE) {
            try {
                // Allow channel change even if player is null or not playing
                channelChangeByKeyCode(keyCode)
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Channel change failed: ${e.message}")
                return true
            }


        } else {
            // playerView.showController();
        }
        if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)) {
            lastKeyDownCenter = System.currentTimeMillis()
            isOkButtonHold3Secs = false
            handlerGoLive.removeCallbacksAndMessages(null)
            //  onOkButtonPressed();
//            showChannelInfo()
            //}/
            //  lastIsOk = true;
            return super.onKeyDown(keyCode, event)
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (player != null) {
                player.playWhenReady = false
            }
            showPauseFR()
        } else {
            lastIsOk = false
        }



        return super.onKeyDown(keyCode, event)
    }

    private fun startChannelProgress() {
        if (blockAutoPlay) return
        val progressBar = binding.includeKeyboard.channelProgressBar
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0

        channelTimer?.cancel()

        channelTimer = object : CountDownTimer(5000, 50) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((5000 - millisUntilFinished) * 100 / 5000).toInt()
                progressBar.progress = progress
            }

            override fun onFinish() {
                if (blockAutoPlay) return
                progressBar.progress = 100
                progressBar.visibility = View.GONE

                // 👉 Auto-play first matching channel
                if (currentSearchResults.isNotEmpty()) {
                    val firstChannel = currentSearchResults[0]

                    previewScreenViewModel.channels.postValue(previewScreenViewModel.channelsOriginal)
                    previewScreenViewModel.changeCategoryToAll()   // <<< ADD THIS
                    previewScreenViewModel.setCurrentChannelIndex(0)

                    val index = previewScreenViewModel.channelsOriginal.indexOfFirst {
                        it.channel_id == firstChannel.channel_id
                    }

                    if (index != -1) {
                        skipNextEpgUpdate = true
                        previewScreenViewModel.setCurrentChannelIndex(index)
                        previewScreenViewModel.currentChannel.postValue(firstChannel)
                        play(firstChannel)
                        setInfoLiveValues(firstChannel, emptyList())
                    }

                    binding.containerKeyboard.visibility = View.GONE
                    clearText()
                }
            }
        }.start()
    }

    private fun getKeys(): List<List<String>> {
        return if (!showAlphabet) {
            listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("⌫", "0", "abc"),
            )
        } else {
            listOf(
                listOf("1", "2", "3", "a", "b", "c", "d", "e", "f", "g"),
                listOf("4", "5", "6", "h", "i", "j", "k", "l", "m", "n"),
                listOf("7", "8", "9", "o", "p", "q", "r", "s", "t", "u"),
                listOf(" ", "0", "<-", "v", "w", "x", "y", "z", "⌫", "OK")
            )
        }
    }

    private fun setupCustomKeyboard() {
        val grid = binding.includeKeyboard.keyboardGrid
        val edtChannel = binding.includeKeyboard.edtChannel
        val rvSuggestions = binding.includeKeyboard.rvChannelSuggestions

        val keys = getKeys()

        grid.visibility = View.VISIBLE

        grid.removeAllViews()
        grid.columnCount = if (!showAlphabet) 3 else 10
        val allButtons = mutableListOf<Button>()

        rvSuggestions.layoutManager = LinearLayoutManager(this)
        rvSuggestions.visibility = View.GONE

        keys.flatten().forEach { key ->
            if (key.isEmpty()) return@forEach

            val btn = Button(this).apply {
                text = key
                textSize = 12f
                setTextColor(Color.WHITE)

                val defaultShape = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.TRANSPARENT)
                }
                background = defaultShape

                if (key == " ") {
                    isFocusable = false
                    isClickable = false
                    isEnabled = false
                    alpha = 0f
                } else {
                    isFocusable = true
                    isFocusableInTouchMode = false

                    setOnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) {
                            val focusShape = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setColor(Color.WHITE)
                            }
                            background = focusShape
                            setTextColor(Color.BLACK)
                        } else {
                            background = defaultShape
                            setTextColor(Color.WHITE)
                        }
                    }

                    setOnClickListener {
                        when (key) {
                            "abc" -> {
                                showAlphabet = !showAlphabet
                                setupCustomKeyboard()
                            }

                            "<-" -> {
                                showAlphabet = false
                                setupCustomKeyboard()
                            }

                            "⌫" -> {
                                if (edtChannel.text.isNotEmpty()) {
                                    edtChannel.text.delete(
                                        edtChannel.text.length - 1, edtChannel.text.length
                                    )

                                    updateChannelName(edtChannel.text.toString())

                                    if (edtChannel.text.isNotEmpty()) {
                                        blockAutoPlay = false
                                        startChannelProgress()
                                    } else {
                                        stopChannelChangeTimer()
                                    }
                                }
                            }

                            "OK" -> {
                                handleChannelSearch(edtChannel.text.toString())
                            }

                            else -> {
                                edtChannel.append(key)
                                updateChannelName(edtChannel.text.toString())

                                blockAutoPlay = false
                                startChannelProgress()
                            }
                        }
                    }


                    setOnKeyListener { v, keyCode, event ->
                        if (event.action == KeyEvent.ACTION_DOWN) {

                            val index = allButtons.indexOf(v)
                            val totalCols = grid.columnCount
                            val totalRows = keys.size

                            val currentRow = index / totalCols
                            val currentCol = index % totalCols

                            if (rvSuggestions.isVisible) {

                                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && currentRow == totalRows - 1) {
                                    rvSuggestions.requestFocus()
                                    stopChannelChangeTimer()
                                    return@setOnKeyListener true
                                }

                                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && currentRow == 0) {
                                    rvSuggestions.requestFocus()
                                    stopChannelChangeTimer()
                                    return@setOnKeyListener true
                                }
                            }

                            fun wrap(row: Int, col: Int): Int {
                                var r = row
                                var c = col

                                if (r < 0) r = totalRows - 1
                                if (r >= totalRows) r = 0

                                if (c < 0) c = totalCols - 1
                                if (c >= totalCols) c = 0

                                return r * totalCols + c
                            }

                            val targetIndex = when (keyCode) {
                                KeyEvent.KEYCODE_DPAD_LEFT -> wrap(currentRow, currentCol - 1)
                                KeyEvent.KEYCODE_DPAD_RIGHT -> wrap(currentRow, currentCol + 1)
                                KeyEvent.KEYCODE_DPAD_UP -> wrap(currentRow - 1, currentCol)
                                KeyEvent.KEYCODE_DPAD_DOWN -> wrap(currentRow + 1, currentCol)
                                else -> -1
                            }

                            if (targetIndex in allButtons.indices) {
                                allButtons[targetIndex].requestFocus()
                                return@setOnKeyListener true
                            }
                        }
                        false
                    }
                }
            }

            val params = GridLayout.LayoutParams().apply {
                width = 110
                height = 110
                setMargins(8, 8, 8, 8)
            }
            btn.layoutParams = params
            grid.addView(btn)
            allButtons.add(btn)
        }

        allButtons.firstOrNull()?.requestFocus()
    }

    private fun updateChannelName(input: String) {
        val rvSuggestions = binding.includeKeyboard.rvChannelSuggestions
        val gridKeyboard = binding.includeKeyboard.keyboardGrid

        if (input.isEmpty()) {
            rvSuggestions.visibility = View.GONE
            gridKeyboard.visibility = View.VISIBLE
            return
        }

        // Always search from full channel list
        val allChannels = previewScreenViewModel.channelsOriginal

        val matchingChannels = allChannels.filter { channel ->
            val numberMatch = channel.channel_no?.toString()?.startsWith(input) == true
            val nameMatch = channel.channel_name?.startsWith(input, ignoreCase = true) == true
            numberMatch || nameMatch
        }

        // 🔥 Save results for 5-sec auto-play
        currentSearchResults = matchingChannels

        if (matchingChannels.isNotEmpty()) {
            rvSuggestions.visibility = View.VISIBLE

            val adapter = ChannelSuggestionAdapter(matchingChannels) { selectedChannel ->
                try {
                    channelTimer?.cancel()
                    channelTimer = null

                    binding.includeKeyboard.channelProgressBar.apply {
                        visibility = View.GONE
                        progress = 0
                    }
                    val index = previewScreenViewModel.channelsOriginal.indexOfFirst {
                        it.channel_id == selectedChannel.channel_id
                    }

                    if (index != -1) {
                        skipNextEpgUpdate = true
                        play(selectedChannel)
                        setInfoLiveValues(selectedChannel, emptyList())
                        previewScreenViewModel.setCurrentChannelIndex(index)
                        previewScreenViewModel.currentChannel.postValue(selectedChannel)
                    }

                    rvSuggestions.visibility = View.GONE
                    gridKeyboard.visibility = View.VISIBLE
                    binding.includeKeyboard.edtChannel.setText("")
                    binding.containerKeyboard.visibility = View.GONE

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error playing channel", Toast.LENGTH_SHORT).show()
                }
            }

            rvSuggestions.adapter = adapter

        } else {
            rvSuggestions.visibility = View.VISIBLE

            val noMatchChannel = JoinLiveStreams().apply {
                channel_name = "No Match Found"
            }

            val adapter = ChannelSuggestionAdapter(listOf(noMatchChannel)) { /* No action */ }
            rvSuggestions.adapter = adapter
        }
    }

    private fun stopChannelChangeTimer() {
        channelTimer?.cancel()
        channelTimer = null

        binding.includeKeyboard.channelProgressBar.apply {
            visibility = View.GONE
            progress = 0
        }

        blockAutoPlay = true
    }

    private fun handleChannelSearch(channelInput: String) {
        if (channelInput.isEmpty()) return

        stopChannelChangeTimer()

        val channels = previewScreenViewModel.channelsOriginal

        val normalizedInput = channelInput.replace(" ", "").lowercase()

        val selectedChannel = channels.find {
            val normalizedName = it.channel_name.replace(" ", "").lowercase()
            it.channel_no.toString()
                .equals(normalizedInput, ignoreCase = true) || normalizedName == normalizedInput
        }

        if (selectedChannel != null) {
            val index = previewScreenViewModel.channels.value?.indexOfFirst {
                it.channel_id == selectedChannel.channel_id
            } ?: -1

            if (index != -1) {
                previewScreenViewModel.setCurrentChannelIndex(index)
                previewScreenViewModel.currentChannel.postValue(selectedChannel)
            }

            skipNextEpgUpdate = true
            play(selectedChannel)

            binding.containerKeyboard.visibility = View.GONE
            clearText()

            setInfoLiveValues(selectedChannel, emptyList())
        } else {
            val isNumeric = channelInput.all { it.isDigit() }
            val message = if (isNumeric) {
                clearText()
                "Invalid channel number"
            } else {
                clearText()
                "Invalid channel name"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearText() {
        binding.includeKeyboard.edtChannel.text.clear()
    }

    private fun showLastViewChannelDialog() {
        binding.containerChannelInfo.visibility = View.INVISIBLE
        binding.lastViewChannel.visibility = View.VISIBLE

        val gson = Gson()
        CoroutineScope(Dispatchers.Main).launch {
            val favouriteChannels = withContext(Dispatchers.IO) {
                previewScreenViewModel.previewDomain.getChannels(CATEGORY_ID_FAVORITE)
            }
            val favoriteChannelIds = favouriteChannels.map { it.channel_id }.toSet()
            val json = mPreferences.getString("recent_channels", "[]")
            val type = object : TypeToken<MutableList<JoinLiveStreams>>() {}.type
            val viewedChannels: MutableList<JoinLiveStreams> = gson.fromJson(json, type)

            println("~~~ Recent channels count = ${viewedChannels.size}")
            viewedChannels.forEachIndexed { index, channel ->
                println("[$index] ${channel.channel_name}  |  ID=${channel.channel_id}  |  Fav=${channel.isFavorite}")
            }

            var listWasModified = false
            viewedChannels.forEach { channel ->
                val isTrulyFavorite = favoriteChannelIds.contains(channel.channel_id)
                val isFavoriteString = if (isTrulyFavorite) "true" else "false"
                if (channel.isFavorite != isFavoriteString) {
                    channel.isFavorite = isFavoriteString
                    listWasModified = true
                }
            }
            if (listWasModified) {
                mPreferences.edit().putString("recent_channels", gson.toJson(viewedChannels))
                    .apply()
            }
            lateinit var adapter: ViewedChannelsAdapter

            adapter = ViewedChannelsAdapter(viewedChannels, onItemClick = { channel ->
                val index = previewScreenViewModel.channelsOriginal.indexOfFirst {
                    it.channel_id == channel.channel_id
                }
                if (index != -1) {
                    previewScreenViewModel.onChannelClick(index)
                    lastNavigatedSource = SOURCE_RECENT
                }
                binding.lastViewChannel.visibility = View.GONE
            }, onItemLongClick = { channel ->
                val index = previewScreenViewModel.channelsOriginal.indexOfFirst {
                    it.channel_id == channel.channel_id
                }
                if (index != -1) {
                    previewScreenViewModel.onChannelLongClickListener(index)
                }
                val indexInViewedList =
                    viewedChannels.indexOfFirst { it.channel_id == channel.channel_id }
                if (indexInViewedList != -1) {
                    val currentChannel = viewedChannels[indexInViewedList]
                    currentChannel.isFavorite =
                        if (currentChannel.isFavorite == "true") "false" else "true"
                    mPreferences.edit().putString("recent_channels", gson.toJson(viewedChannels))
                        .apply()
                    adapter.notifyItemChanged(indexInViewedList)
                }
            })
            binding.lastViewChannelDialog.lastChannelRV.apply {
                layoutManager = LinearLayoutManager(this@PlayerScreen)
                this.adapter = adapter
            }
        }
    }

    private fun showFavouriteChannelDialog() {
        binding.containerChannelInfo.visibility = View.INVISIBLE
        binding.favouriteChannel.visibility = View.VISIBLE

        lateinit var adapter: ChannelsAdapter

        CoroutineScope(Dispatchers.Main).launch {
            val favouriteChannels = withContext(Dispatchers.IO) {
                previewScreenViewModel.previewDomain.getChannels(CATEGORY_ID_FAVORITE)
            }

            if (favouriteChannels.isNotEmpty()) {

                val currentChannel = previewScreenViewModel.currentChannel.value
                val selectedPos = favouriteChannels.indexOfFirst {
                    it.channel_id == currentChannel?.channel_id
                }.coerceAtLeast(0)

                binding.favouriteChannelDialog.rvFavouriteChannels.visibility = View.VISIBLE
                binding.favouriteChannelDialog.noFavData.visibility = View.GONE

                adapter = ChannelsAdapter(
                    channels = favouriteChannels.toMutableList(),
                    selectedPosition = selectedPos,
                    onChannelItemClickListener = { view ->
                        val holder = view.tag as ChannelsAdapter.ViewHolder
                        val channel = favouriteChannels[holder.bindingAdapterPosition]

                        println("~~~ FAV CLICKED: ${channel.channel_name}")
                        lastNavigatedSource = SOURCE_FAV
                        favList = favouriteChannels
                        favIndex = holder.bindingAdapterPosition
                        isFavouriteNavigation = true

                        val allChannels = previewScreenViewModel.channelsOriginal
                        previewScreenViewModel.channels.postValue(allChannels)
                        val globalIndex = allChannels.indexOfFirst {
                            it.channel_id == channel.channel_id
                        }

                        if (globalIndex != -1) {
                            previewScreenViewModel.currentChannelIndex.postValue(globalIndex)
                            previewScreenViewModel.currentChannel.postValue(channel)

                            play(channel)
                            setInfoLiveValues(channel, emptyList())
                        }

                        binding.favouriteChannel.visibility = View.GONE
                    },
                    onChannelItemLongClickListener = { v ->
                        val holder = v.tag as ChannelsAdapter.ViewHolder
                        val channel = favouriteChannels[holder.bindingAdapterPosition]

                        val index = previewScreenViewModel.channelsOriginal.indexOfFirst {
                            it.channel_id == channel.channel_id
                        }
                        if (index != -1) {
                            previewScreenViewModel.onChannelLongClickListener(index)
                        }

                        adapter.channels.removeAt(holder.bindingAdapterPosition)
                        adapter.notifyItemRemoved(holder.bindingAdapterPosition)

                        if (adapter.channels.isEmpty()) {
                            binding.favouriteChannelDialog.rvFavouriteChannels.visibility =
                                View.INVISIBLE
                            binding.favouriteChannelDialog.noFavData.visibility = View.VISIBLE
                        }

                        true
                    },
                    isFavoriteCat = false
                )

                binding.favouriteChannelDialog.rvFavouriteChannels.apply {
                    layoutManager = LinearLayoutManager(this@PlayerScreen)
                    this.adapter = adapter
                }

            } else {
                binding.favouriteChannelDialog.rvFavouriteChannels.visibility = View.INVISIBLE
                binding.favouriteChannelDialog.noFavData.visibility = View.VISIBLE
            }
        }
    }

    private fun playLastViewedChannel() {
        try {
            val gson = Gson()
            val json = mPreferences.getString("recent_channels", "[]")
            val type = object : TypeToken<MutableList<JoinLiveStreams>>() {}.type
            val viewedChannels: MutableList<JoinLiveStreams> = gson.fromJson(json, type)
            if (viewedChannels.size < 2) {
                Toast.makeText(this, "No previous channel found", Toast.LENGTH_SHORT).show()
                return
            }
            val previousChannel = viewedChannels[1]

            println("~~~ Playing PREVIOUS: ${previousChannel.channel_name}")
            val allChannels = previewScreenViewModel.channelsOriginal
            previewScreenViewModel.channels.postValue(allChannels)
            val index = allChannels.indexOfFirst {
                it.channel_id == previousChannel.channel_id
            }

            if (index != -1) {
                previewScreenViewModel.onChannelClick(index)
            } else {
                Toast.makeText(this, "Previous channel not found", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error playing previous channel", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveViewedChannel(channel: JoinLiveStreams) {
        val gson = Gson()
        val json = mPreferences.getString("recent_channels", "[]")
        val type = object : TypeToken<MutableList<JoinLiveStreams>>() {}.type
        val list: MutableList<JoinLiveStreams> = gson.fromJson(json, type)

        list.removeAll { it.channel_id == channel.channel_id }
        list.add(0, channel)

        if (list.size > 20) {
            list.subList(20, list.size).clear()
        }

        mPreferences.edit().putString("recent_channels", gson.toJson(list)).apply()
    }

    private fun playFavChannel(pos: Int) {
        val ch = favList[pos]

        val index = previewScreenViewModel.channelsOriginal.indexOfFirst {
            it.channel_id == ch.channel_id
        }

        if (index != -1) {
            previewScreenViewModel.onChannelClick(index)
        }
    }

    private fun onClick() {

        binding.includeNewControllerPlayer.closeBtn.setOnClickListener {
            binding.newControlPlayer.visibility = View.GONE
        }

        binding.includeContainerChannels.closeBtn.setOnClickListener {
            binding.containerChannels.visibility = View.GONE
            binding.newControlPlayer.visibility = View.VISIBLE
        }

        binding.includeContainerCategories.closeBtn.setOnClickListener {
            binding.containerCategories.visibility = View.GONE
        }

        binding.favouriteChannelDialog.closeBtn.setOnClickListener {
            binding.favouriteChannel.visibility = View.GONE
            binding.newControlPlayer.visibility = View.VISIBLE
        }

        binding.lastViewChannelDialog.closeBtn.setOnClickListener {
            binding.lastViewChannel.visibility = View.GONE
            binding.newControlPlayer.visibility = View.VISIBLE
        }

        binding.epgViewContainer.closeDialog.setOnClickListener {
            binding.epgView.visibility = View.GONE
        }

        binding.includeInfoDialog.closeDialog.setOnClickListener {
            binding.containerInfoDialog.visibility = View.GONE
        }

        binding.includeInfoUser.closeDialog.setOnClickListener {
            binding.containerInfoUser.visibility = View.GONE
        }

        binding.includeSupportDialog.closeDialog.setOnClickListener {
            binding.containerSupport.visibility = View.GONE
        }

        binding.tvSkip.setOnClickListener {

            binding.containerTutorial.visibility = View.GONE

//            val prefs = getSharedPreferences(PREF_TUTORIAL, Context.MODE_PRIVATE)
//            prefs.edit()
//                .putBoolean(KEY_TUTORIAL_SHOWN, true)
//                .apply()
        }
    }

    private fun setInfoValues() {

        val densityDpi = getResources().displayMetrics.densityDpi
        binding.includeInfoDialog.tvDensity.text = "$densityDpi dpi"

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val currentDate = sdf.format(Date())

        binding.includeInfoDialog.releaseDate.text = currentDate
        println("current date -->" + currentDate)

        try {
            val pm = packageManager
            val pInfo = pm.getPackageInfo(packageName, 0)

            val versionCode = pInfo.versionCode // deprecated desde API 28
            pInfo.versionName

            binding.includeInfoDialog.tvProfileAppVersion.text = "$versionCode"

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val username = mPreferences.getString("name", "null")
        val exp_date = mPreferences.getString("expDate", "null")

        binding.includeInfoUser.tvProfileUsername.text = username?.uppercase()
        binding.includeInfoUser.tvProfileValidity.text = Helper.timeStamp(exp_date).uppercase()
        binding.includeInfoUser.tvProfileUniqueid.text =
            mPreferences.getString("mac", "null")?.uppercase()
        binding.includeInfoDialog.tvProfileAndroidVersion.text = Build.VERSION.RELEASE
        binding.includeInfoUser.tvProfileUserId.text = "${mPreferences.getInt(KEY_USER_ID, 0)}"


        val actManager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        binding.includeInfoDialog.tvProfileRam.text = "2 GB"

        binding.includeInfoDialog.btnProfileOk.setOnClickListener {
            binding.containerInfoDialog.visibility = View.GONE
        }


        val stat = StatFs(Environment.getDataDirectory().path)
        stat.availableBytes
        binding.includeInfoDialog.tvProfileStorage.text = "8 GB"
    }

    private fun setCategoryAdapter(
        categories: List<RM_LiveStreamCategory>, selectedPosition: Int
    ) {
        if (categories.isNotEmpty()) {
            if (categoriesAdapter == null) {
                categoriesAdapter = CategoriesAdapter(
                    categories = categories,
                    selectedPosition = selectedPosition,
                    onCategoryItemClickListener,
                    onCategoryFocused = {
                        binding.includeContainerCategories.rvCategories.smoothScrollToPosition(it)
                    })
                binding.includeContainerCategories.rvCategories.adapter = categoriesAdapter
            } else {
                categoriesAdapter!!.updateData(selectedPosition)
            }
        }
    }

    private fun setChannelsAdapter(channels: List<JoinLiveStreams>, selectedPosition: Int) {
        if (channelsAdapter == null) {
            channelsAdapter = ChannelsAdapter(
                channels = channels.toMutableList(),
                selectedPosition = selectedPosition,
                onChannelItemClickListener = onChannelItemClickListener,
                onChannelItemLongClickListener = onItemLongClickListener,
                isFavoriteCat = previewScreenViewModel.currentCategory.value?.category_id == "$CATEGORY_ID_FAVORITE"
            )
            binding.includeContainerChannels.rvChannels.adapter = channelsAdapter
        } else {
            channelsAdapter!!.updateData(channels, selectedPosition)
        }

        binding.includeContainerChannels.rvChannels.scrollToPosition(selectedPosition)
    }

    private val onCategoryItemClickListener = View.OnClickListener { v ->
        val rvBinding = v?.tag as CategoriesAdapter.ViewHolder
        previewScreenViewModel.onCategoryClick(rvBinding.bindingAdapterPosition)
        binding.containerCategories.visibility = View.GONE
        binding.containerChannels.visibility = View.VISIBLE
    }

    private val onItemLongClickListener = View.OnLongClickListener { v ->
        val binding = v?.tag as ChannelsAdapter.ViewHolder
        val indexInCurrentList = binding.bindingAdapterPosition

        val channel = previewScreenViewModel.channels.value?.getOrNull(indexInCurrentList)

        if (channel != null) {
            val globalIndex =
                previewScreenViewModel.channelsOriginal.indexOfFirst { it.channel_id == channel.channel_id }

            if (globalIndex != -1) {
                previewScreenViewModel.onChannelLongClickListener(globalIndex)
            }
        }
        true
    }

    private val onChannelItemClickListener = View.OnClickListener { v ->
        binding.containerChannels.visibility = View.GONE
        val binding = v?.tag as ChannelsAdapter.ViewHolder
        previewScreenViewModel.onChannelClick(binding.bindingAdapterPosition)

        if (isAllChannelScreenOpen) {
            lastNavigatedSource = SOURCE_ALL
        } else {
            lastNavigatedSource = SOURCE_GENRE
        }
    }

    private val onChannelItemLongClickListener = object : View.OnLongClickListener {
        override fun onLongClick(v: View?): Boolean {
            return true
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun registerNetworkCallback() {
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val request =
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    binding.ivNetworkError.visibility = View.GONE
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                    binding.ivNetworkError.visibility = View.VISIBLE
                }
            }

            override fun onUnavailable() {
                runOnUiThread {
                    binding.ivNetworkError.visibility = View.VISIBLE
                }
            }
        }

        connectivityManager.registerNetworkCallback(request, networkCallback)
    }


    private fun showOrHideEPG() {
        if (binding.epgView.isVisible) {
            binding.epgView.visibility = View.GONE
            binding.newControlPlayer.visibility = View.VISIBLE
            binding.includeNewControllerPlayer.epgBtn.requestFocus()
        } else {
            binding.containerController.visibility = View.GONE
            binding.catchupView.visibility = View.GONE
            binding.catchupView.visibility = View.GONE
            binding.epgView.visibility = View.VISIBLE
            if (previewScreenViewModel.programs.value == null) {
                binding.epgViewContainer.loadingEpgs.visibility = View.VISIBLE
            }

        }
    }

    private fun showOrHideCategoryView() {
        if (binding.containerCategories.isVisible) {
            binding.containerCategories.visibility = View.GONE
        } else {
            binding.containerController.visibility = View.GONE
            binding.epgView.visibility = View.GONE
            binding.catchupView.visibility = View.GONE
            binding.containerCategories.visibility = View.VISIBLE
        }
    }

    private fun showOrHideCatchup(isCatchup: Boolean) {
        if (binding.catchupView.isVisible) {
            binding.catchupView.visibility = View.GONE
        } else if (!binding.containerChannels.isVisible) {
            binding.containerController.visibility = View.GONE
            binding.epgView.visibility = View.GONE
            binding.containerCategories.visibility = View.GONE
            binding.catchupView.visibility = View.GONE
        }
    }

    private fun numberPressed(keyCode: Int) {
        if (keyCode == 7 && numberPressedUtil.numberCode == "") {
            return
        }
        val handler = Handler()
        val runnableShow = Runnable {
            if (!binding.player.isControllerFullyVisible) binding.player.performClick()
        }
        handler.postDelayed(runnableShow, 4700)
        if (numberPressedUtil.numberCode.length >= 3 && binding.tvNumberPressed.scaleX == 0f) {
            numberPressedUtil.numberCode = ""
        }
        numberPressedUtil.onNumberPressed(
            keyCode,
            previewScreenViewModel.channelsOriginal,
            previewScreenViewModel.channelsOriginal,
            ChannelsAdapter(
                channels = previewScreenViewModel.channelsOriginal.toMutableList(),
                selectedPosition = previewScreenViewModel.currentChannelIndex.value ?: 0,
                onChannelItemClickListener = onChannelItemClickListener,
                onChannelItemLongClickListener = onItemLongClickListener,
                isFavoriteCat = previewScreenViewModel.currentCategory.value?.category_id == "$CATEGORY_ID_FAVORITE"
            ),
            binding.tvNumberPressed,
            binding.tvChannelNameSelected,
            object : ChannelSelectedCallback {
                override fun onChannelSelected(
                    position: Int, foundedInOriginalChannelData: Boolean
                ) {
                    if (position >= 0) {
                        previewScreenViewModel.onChannelChangeByNumberPressed(position)
                    }
                }

            })
    }

    private fun showPauseFR() {
        binding.containerPlayFR.visibility = View.GONE
        binding.containerPauseFR.visibility = View.VISIBLE
        Handler().postDelayed(Runnable { binding.containerPauseFR.visibility = View.GONE }, 3000)
    }

    private fun channelChangeByKeyCode(keycode: Int) {

        val currentChannel = previewScreenViewModel.currentChannel.value ?: return

        val isAllMode = isAllChannelScreenOpen

        if (isAllMode) {
            println("calling --> 001")
            val allList = previewScreenViewModel.channelsOriginal
            val index = allList.indexOfFirst { it.channel_id == currentChannel.channel_id }
            if (index == -1) return

            when (keycode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    val next = (index + 1) % allList.size   // wrap
                    previewScreenViewModel.onChannelClickNew(next)
                }

                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    val prev = if (index - 1 < 0) allList.size - 1 else index - 1
                    previewScreenViewModel.onChannelClickNew(prev)
                }
            }

        } else {
            println("calling --> 002")
            val list = previewScreenViewModel.channels.value ?: return
            val index = list.indexOfFirst { it.channel_id == currentChannel.channel_id }
            if (index == -1) return

            when (keycode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    val next = (index + 1) % list.size
                    changeChannel(next)
                }

                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    val prev = if (index - 1 < 0) list.size - 1 else index - 1
                    changeChannel(prev)
                }
            }
        }
    }

    private fun changeChannel(index: Int) {
        previewScreenViewModel.onChannelClick(index)
    }

    private fun controlFocus(keyCode: Int): Boolean {
        var controlled = false

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            controlled = true
            if (binding.includeControllerPlayer.tvGenre.hasFocus()) {
                binding.includeControllerPlayer.tvFavourite.requestFocus()
            } else if (binding.includeControllerPlayer.tvAllChannel.hasFocus()) {
                binding.includeControllerPlayer.tvGenre.requestFocus()
            } else if (binding.includeControllerPlayer.tvFavourite.hasFocus()) {
                binding.includeControllerPlayer.btnChannelSettings.requestFocus()
            } else if (binding.includeControllerPlayer.tvLogout.hasFocus()) {
                binding.includeControllerPlayer.btnSettings.requestFocus()
            } else if (binding.includeControllerPlayer.tvSystemInfo.hasFocus()) {
                binding.includeControllerPlayer.tvMoreSettings.requestFocus()
            } else if (binding.includeControllerPlayer.tvMoreSettings.hasFocus()) {
                binding.includeControllerPlayer.tvLogout.requestFocus()
            } else if (binding.includeControllerPlayer.tvUserInfo.hasFocus()) {
                binding.includeControllerPlayer.btnMyAccount.requestFocus()
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            controlled = true
            if (binding.includeControllerPlayer.tvFavourite.hasFocus()) {
                binding.includeControllerPlayer.tvGenre.requestFocus()
            } else if (binding.includeControllerPlayer.btnChannelSettings.hasFocus() && binding.includeControllerPlayer.containerChannelSettings.isVisible) {
                binding.includeControllerPlayer.tvFavourite.requestFocus()
            } else if (binding.includeControllerPlayer.tvLogout.hasFocus()) {
                binding.includeControllerPlayer.tvMoreSettings.requestFocus()
            } else if (binding.includeControllerPlayer.tvMoreSettings.hasFocus()) {
                binding.includeControllerPlayer.tvSystemInfo.requestFocus()
            } else if (binding.includeControllerPlayer.btnSettings.hasFocus() && binding.includeControllerPlayer.containerSettings.isVisible) {
                binding.includeControllerPlayer.tvLogout.requestFocus()
            } else if (binding.includeControllerPlayer.btnMyAccount.hasFocus() && binding.includeControllerPlayer.containerUserInfo.isVisible) {
                binding.includeControllerPlayer.tvUserInfo.requestFocus()
            } else if (binding.includeControllerPlayer.tvGenre.hasFocus()) {
                binding.includeControllerPlayer.tvAllChannel.requestFocus()
            }
        }

        return controlled
    }


    private fun showNumberPressed(
        tvNumberPress: TextView,
        tvNumberChannelSelected: TextView,
        channelNo: Int,
        channelName: String?
    ) {
        tvNumberPress.text = "" + channelNo
        tvNumberChannelSelected.text = channelName
        if (handlerChannelChangeUp != null && runnableChannelChangeUp != null) {
            handlerChannelChangeUp!!.removeCallbacks(runnableChannelChangeUp!!)
        }
        handlerChannelChangeUp = Handler()
        runnableChannelChangeUp = Runnable {
            hideNumberPressed(
                tvNumberPress, tvNumberChannelSelected
            )
        }


        handlerChannelChangeUp!!.postDelayed(runnableChannelChangeUp!!, 5000)
        if (tvNumberPress.visibility != View.VISIBLE) {
            tvNumberPress.visibility = View.VISIBLE
            tvNumberChannelSelected.visibility = View.VISIBLE
            val anim = AnimationUtils.loadAnimation(this, R.anim.scale_in_number_press)
            tvNumberPress.startAnimation(anim)
            tvNumberChannelSelected.startAnimation(anim)
            anim.fillAfter = true
        }
    }

    private fun hideNumberPressed(tvNumberPress: TextView, tvChannelName: TextView) {
        val anim = AnimationUtils.loadAnimation(this, R.anim.scale_out_number_press)
        tvNumberPress.startAnimation(anim)
        tvChannelName.startAnimation(anim)
        anim.fillAfter = true
        Handler().postDelayed(Runnable {
            tvNumberPress.visibility = View.GONE
            tvChannelName.visibility = View.GONE
        }, 1000)
    }

    var goOnBack = false

    override fun onBackPressed() {
        goOnBack = true


        //        if(containerInfoLive.getVisibility() == View.VISIBLE){
//            containerInfoLive.setVisibility(View.GONE);
//            goOnBack = false;
//        }
        if (binding.containerController.isVisible) {
            binding.includeControllerPlayer.containerChannelSettings.visibility = View.INVISIBLE
            binding.includeControllerPlayer.containerUserInfo.visibility = View.INVISIBLE
            binding.includeControllerPlayer.containerSettings.visibility = View.INVISIBLE
            binding.containerController.visibility = View.GONE
            goOnBack = false
        }
        if (binding.containerInfoUser.isVisible) {
            binding.containerInfoUser.visibility = View.GONE
            binding.newControlPlayer.visibility = View.VISIBLE
            binding.includeNewControllerPlayer.userInfoBtn.requestFocus()
            goOnBack = false
        }
        if (binding.containerSupport.isVisible) {
            binding.containerSupport.visibility = View.GONE
            binding.newControlPlayer.visibility = View.VISIBLE
            binding.includeNewControllerPlayer.supportBtn.requestFocus()
            goOnBack = false
        }
        if (binding.containerChannels.isVisible) {
            binding.containerChannels.visibility = View.GONE
            binding.newControlPlayer.visibility = View.VISIBLE

            if (isAllChannelScreenOpen) {
                println("~~~ back from All Channel")
                binding.includeNewControllerPlayer.allChannelBtn.requestFocus()
                isAllChannelScreenOpen = false
            } else {
                println("~~~ back from Genre Channel")
                binding.newControlPlayer.visibility = View.GONE
                binding.containerCategories.visibility = View.VISIBLE
                binding.includeNewControllerPlayer.genreBtn.requestFocus()
            }
            goOnBack = false
        } else if (binding.containerCategories.isVisible) {
            binding.containerCategories.visibility = View.GONE
            binding.newControlPlayer.visibility = View.VISIBLE
            binding.includeNewControllerPlayer.genreBtn.requestFocus()
            goOnBack = false
        }
        if (binding.epgView.isVisible) {
            binding.epgView.visibility = View.GONE
            binding.newControlPlayer.visibility = View.VISIBLE
            binding.includeNewControllerPlayer.epgBtn.requestFocus()
            goOnBack = false
        }
        if (binding.catchupView.isVisible) {
            binding.catchupView.visibility = View.GONE
            goOnBack = false
        }
        if (binding.containerInfoDialog.isVisible) {
            binding.containerInfoDialog.visibility = View.GONE
            binding.newControlPlayer.visibility = View.VISIBLE
            binding.includeNewControllerPlayer.systemInfoBtn.requestFocus()
            goOnBack = false
        }
        if (binding.containerMoreSettings.isVisible) {
            binding.containerMoreSettings.visibility = View.GONE
            goOnBack = false
        }

        if (goOnBack) {
            showExitDialog()
        }
    }

    private fun showExitDialog() {
        binding.containerDialogExit.visibility = View.VISIBLE

        binding.includeDialogExit.tvYes.setOnClickListener {
            finishAffinity()
            System.exit(0)
        }

        binding.includeDialogExit.tvNo.setOnClickListener {
            binding.containerDialogExit.visibility = View.GONE
        }

        binding.includeDialogExit.tvNo.requestFocus()
    }

    private fun showLogoutDialog() {
        binding.containerDialogLogout.visibility = View.VISIBLE
        binding.includeLogoutExit.tvYes.setOnClickListener {
            previewScreenViewModel.logOut {
                CoroutineScope(Dispatchers.Main).launch {
                    mPreferences.edit { remove("recent_channels") }
                    val intent = Intent(applicationContext, LoginActivity::class.java).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    startActivity(intent)
                    finish()
                }
            }
        }

        binding.includeLogoutExit.tvNo.setOnClickListener {
            binding.containerDialogLogout.visibility = View.GONE
            binding.newControlPlayer.visibility = View.VISIBLE
            binding.includeNewControllerPlayer.logoutBtn.requestFocus()
//            hideController()
        }

        binding.includeLogoutExit.tvNo.requestFocus()
    }

    private val handlerHideChannelInfo = Handler(Looper.getMainLooper())

    private val runnableHideChannelInfo = Runnable {
        binding.containerChannelInfo.visibility = View.INVISIBLE
        Log.w("VISIBILITY", "Auto hide after 5s triggered")
    }

    private fun showChannelInfo() {
        Log.w(
            "VISIBILITY", """
        containerInfoDialog: ${binding.containerInfoDialog.isVisible}
        containerController: ${binding.containerController.isVisible}
        containerCategories: ${binding.containerCategories.isVisible}
        containerChannels: ${binding.containerChannels.isVisible}
        isLastParkingChannelPlayed: $isLastParkingChannelPlayed
    """.trimIndent()
        )

        if (!binding.containerInfoDialog.isVisible && !binding.containerController.isVisible && !binding.containerCategories.isVisible && !binding.containerChannels.isVisible) {
            if (isLastParkingChannelPlayed) {
                handlerHideChannelInfo.removeCallbacks(runnableHideChannelInfo)
                if (!binding.lastViewChannel.isVisible) {
                    binding.containerChannelInfo.visibility = View.VISIBLE
                    Log.w("VISIBILITY", "Setting containerChannelInfo VISIBLE")
                } else {
                    binding.containerChannelInfo.visibility = View.GONE
                    Log.w("VISIBILITY", "Hiding because lastViewChannel visible")
                }
                handlerHideChannelInfo.postDelayed(runnableHideChannelInfo, 5000)
                handlerContainerInfoLive.removeCallbacksAndMessages(null)
                handlerContainerInfoLive.postDelayed(runnableInfoLive, DELAY_INFO_LIVE)
            }
            binding.containerChannelInfo.requestLayout()
        }
    }


//    private fun showController() {
//        if (!binding.containerChannelInfo.isVisible && !binding.containerCategories.isVisible && !binding.containerChannels.isVisible && !binding.containerController.isVisible) {
//            binding.containerController.visibility = View.VISIBLE
//            binding.containerController.postDelayed({
//                binding.includeControllerPlayer.btnChannelSettings.requestFocus()
//            }, 100)
//            binding.includeControllerPlayer.tvGenre.requestFocus()
//        }
//    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Log.w(TAG, "onKeyUp Called")
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

            if (binding.newControlPlayer.isVisible && binding.containerKeyboard.isVisible && binding.containerChannels.isVisible && binding.containerCategories.isVisible && binding.favouriteChannel.isVisible && binding.lastViewChannel.isVisible && binding.epgView.isVisible && binding.containerInfoDialog.isVisible && binding.containerInfoUser.isVisible && binding.containerSupport.isVisible) {
                binding.newControlPlayer.visibility = View.GONE
            }
        }
        val repeatCount = event?.repeatCount ?: 0
        if (repeatCount > 1 && (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP)) {
            return true
        }
        val isContinuos =
            lastKeyDownCenter != 0L && System.currentTimeMillis() > lastKeyDownCenter + 500
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (isCatchup && isContinuos) {
//                showChannelInfo()
            } else if (isContinuos && !binding.containerKeyboard.isVisible) {
                showOrHideCatchup(previewScreenViewModel.currentChannel.value?.catch_up == 1)
            } else {
//                showChannelInfo()
            }
        }
//        if (!returned) {
//            if (keyCode === KeyEvent.KEYCODE_DPAD_LEFT || keyCode === KeyEvent.KEYCODE_DPAD_RIGHT) {
//                if (actualChannel.getCatch_up() === 1) {
//                    onPressForward(keyCode)
//                }
//            }
//            if (keyCode === KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
//                onPressForward(KeyEvent.KEYCODE_DPAD_RIGHT)
//            } else if (keyCode === KeyEvent.KEYCODE_MEDIA_REWIND) {
//                onPressForward(KeyEvent.KEYCODE_DPAD_LEFT)
//            }
//        } else {
//            returned = false
//        }

        return super.onKeyUp(keyCode, event)
    }

    public override fun onStop() {
        super.onStop()
        binding.progressBar.visibility = View.GONE
        if (Util.SDK_INT > 23) {
            currentPlayerScreen = CurrentPlayerScreen.NONE
            serverChecker.stopChecking()
            isActive = false
            latencyHelper.stopChecking()
//            playerManager.setPlayInterface(null)
//            binding.player.setPlayer(null)
//            numberPressedUtil.cancelRunnable()
            releasePlayer("onStop")
//            ON_LOOPER = false
//            finish()
        } else if (Util.SDK_INT > 23) {
            FrequencyGenerator.called = System.currentTimeMillis()
        }
    }

    override fun onResume() {
        super.onResume()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        isActive = true
        containerChannelNotAvailable = binding.containerChannelNotAvailable
        serverChecker.startChecking()

        // use currentChannel instead of null
        previewScreenViewModel.currentChannel.value?.let {
            createPlayerAndPlay(it)
        }

        startCheckLatency()
    }

    public override fun onPause() {
        super.onPause()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding.progressBar.visibility = View.GONE
        if (Util.SDK_INT <= 23) {
            currentPlayerScreen = CurrentPlayerScreen.NONE
            serverChecker.stopChecking()
            isActive = false
            latencyHelper.stopChecking()
//            playerManager.setPlayInterface(null)
//            binding.player.setPlayer(null)
//            numberPressedUtil.cancelRunnable()
            releasePlayer("onPause")
//            ON_LOOPER = false
//            finish()
        } else if (Util.SDK_INT <= 23) {
            FrequencyGenerator.called = System.currentTimeMillis()
        }
    }

    private fun startChannelTimeout() {
        channelTimeoutRunnable?.let { channelTimeoutHandler.removeCallbacks(it) }
        channelTimeoutRunnable = Runnable {
            showStandbyDialog()
        }
        channelTimeoutHandler.postDelayed(channelTimeoutRunnable!!, CHANNEL_TIMEOUT_MS)
    }

    private fun resetChannelTimeout() {
        startChannelTimeout() // restart the timer
    }

    private fun showStandbyDialog() {
        binding.containerDialogStandBy.visibility = View.VISIBLE

        standbyCountDown?.cancel()
        standbyCountDown = object : CountDownTimer(STANDBY_COUNTDOWN_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                val totalSeconds = millisUntilFinished / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60

                val formattedTime = String.format("%02d:%02d", minutes, seconds)
                println("print formated timer -->" + formattedTime)

                binding.includeDialogStandBy.tvStandByTimer.text = formattedTime
            }

            override fun onFinish() {
                finishAffinity()
                exitProcess(0)
            }
        }.start()
    }

    private fun hideStandbyDialog() {
        binding.containerDialogStandBy.visibility = View.GONE
        standbyCountDown?.cancel()
    }
}