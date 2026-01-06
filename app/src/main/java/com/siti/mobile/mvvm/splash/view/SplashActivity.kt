package com.siti.mobile.mvvm.splash.view

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.dcastalia.localappupdate.DownloadApk
import com.siti.mobile.BootUpReceiver
import com.siti.mobile.BuildConfig
import com.siti.mobile.Interface.ApiInterface
import com.siti.mobile.Model.RetroFit.Login
import com.siti.mobile.Model.app_update.AppUpdateResponse
import com.siti.mobile.Model.ktor.StreamUser
import com.siti.mobile.Player.PlayerLiveContainer
import com.siti.mobile.R
import com.siti.mobile.Utils.APP_NAME_COLLECTION
import com.siti.mobile.Utils.APP_UPDATE_DOCUMENT
import com.siti.mobile.Utils.COUNT_SERVER_OFF
import com.siti.mobile.Utils.CurrentData
import com.siti.mobile.Utils.DBHelper
import com.siti.mobile.Utils.DBHelperKt
import com.siti.mobile.Utils.IncreaseCalls
import com.siti.mobile.Utils.KEY_AREA_CODE
import com.siti.mobile.Utils.KEY_AUTHCODE
import com.siti.mobile.Utils.KEY_AUTH_TOKEN
import com.siti.mobile.Utils.KEY_BOOTUP_ACTIVITY
import com.siti.mobile.Utils.KEY_FIRST_TIME
import com.siti.mobile.Utils.KEY_FIRST_TIME_LIVE_TV
import com.siti.mobile.Utils.KEY_LANGUAGE
import com.siti.mobile.Utils.KEY_LEANBACK_ENABLED
import com.siti.mobile.Utils.KEY_LIVESTREAM
import com.siti.mobile.Utils.KEY_LIVE_CATEGORY
import com.siti.mobile.Utils.KEY_LOW_PROFILE
import com.siti.mobile.Utils.KEY_MAC
import com.siti.mobile.Utils.KEY_PASSWORD
import com.siti.mobile.Utils.KEY_SERIES_CATEGORY
import com.siti.mobile.Utils.KEY_SERIES_STREAM
import com.siti.mobile.Utils.KEY_SERVER_IP
import com.siti.mobile.Utils.KEY_SERVER_STATUS
import com.siti.mobile.Utils.KEY_SUBSCRIBE_STATUS
import com.siti.mobile.Utils.KEY_USERNAME
import com.siti.mobile.Utils.KEY_VOD_STREAM
import com.siti.mobile.Utils.PREFERENCES_STRING_DEFAULT_VALUE
import com.siti.mobile.Utils.RetrofitClient
import com.siti.mobile.Utils.SERVER_GLOBAL_IP_LOGIN
import com.siti.mobile.Utils.StateChangedServer
import com.siti.mobile.Utils.VALUE_FULL_SCREEN_ACTIVITY
import com.siti.mobile.Utils.VALUE_HOME_ACTIVITY
import com.siti.mobile.Utils.VALUE_LANGUAGE_EN
import com.siti.mobile.Utils.changeLocalToGlobalIfRequired
import com.siti.mobile.Utils.checkStateServer
import com.siti.mobile.Utils.sharedPrefFile
import com.siti.mobile.lco.LCOCheckCallback
import com.siti.mobile.lco.LCOCheckRepository.checkLCO
import com.siti.mobile.lco.LCOCheckResponse
import com.siti.mobile.mvvm.common.data.AppUpdateModel
import com.siti.mobile.mvvm.config.helpers.AnalyticsEventsHelper
import com.siti.mobile.mvvm.config.helpers.AuthHelper
import com.siti.mobile.mvvm.fullscreen.view.PlayerScreen
import com.siti.mobile.mvvm.login.view.LoginActivity
import com.siti.mobile.mvvm.preview.view.PreviewScreen
import com.siti.mobile.network.main.UpdateLocalDB
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.jakewharton.threetenabp.AndroidThreeTen
import io.ktor.util.InternalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale


const val TAG = "SplashActivity"


class SplashActivity : AppCompatActivity() {

    private lateinit var mPreferences: SharedPreferences
    private lateinit var containerAppUpdate: FrameLayout
    private lateinit var btnUpdate: Button

    private lateinit var dbHelper : DBHelper
    private lateinit var dbHelperKt : DBHelperKt

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                enableAlarmBootUp()
            } else {
                Toast.makeText(this, "You have denied launcher permission", Toast.LENGTH_SHORT).show()
            }
        }


    fun requestPermissionBootUp(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECEIVE_BOOT_COMPLETED)
        }else{
            enableAlarmBootUp()
        }
    }

    @OptIn(InternalAPI::class)
    private suspend fun saveStreamToken(onSaved : () -> Unit) {
//        try{
//            val client = HttpClient(Android)
//            val rp = client.post("http://117.216.44.13:8888/login"){
//                contentType(ContentType.Application.Json)
//                body = getUser()
//            }
//            if(rp.status == HttpStatusCode.Accepted) {
//                val tokenResponse = StreamTokenResponse(rp.body<String>())
//                mPreferences.edit().putString(KEY_PREF_TOKEN_STREAM, tokenResponse.token).apply()
//                Log.w(TAG, "token saved to Stream: ${tokenResponse.token}")
//                onSaved()
//            }else{
//                mPreferences.edit().putString(KEY_PREF_TOKEN_STREAM, "null").apply()
//                Log.w(TAG, "failed token saved: ${rp.status}")
//                onSaved()
//            }
//        }catch (e : Exception){
//            Log.w(TAG , "${e.message}")
            onSaved()
   //     }

    }




    private fun getUser() : String {
        val userName = mPreferences.getString(KEY_USERNAME, "default") ?: "default"
        val mac = mPreferences.getString(KEY_MAC, PREFERENCES_STRING_DEFAULT_VALUE) ?: "default"
        val password = mPreferences.getString(KEY_PASSWORD, PREFERENCES_STRING_DEFAULT_VALUE) ?: "default"
        return Gson().toJson(StreamUser(userName, password, mac))
    }

    fun disableLauncher(){
        val targetActivity = "com.digitalview.sdsiptv.mvvm.splash.view.SplashActivity"

        // Deshabilitar el filtro de intención LAUNCHER

        // Deshabilitar el filtro de intención LAUNCHER
        val pm = packageManager
        val componentName = ComponentName(this, targetActivity)

        // Eliminar la categoría LEANBACK_LAUNCHER

        // Eliminar la categoría LEANBACK_LAUNCHER
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)
        //filled_mac
       // disableLauncher()
        requestPermissionBootUp()
        AndroidThreeTen.init(this)


        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions
        val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
        dbHelper = DBHelper(applicationContext)
        dbHelperKt = DBHelperKt(applicationContext)
//        mPreferences.edit().putString(KEY_SERVER_IP, SERVER_LOCAL_IP_LOGIN).apply()
//        mPreferences.edit().putString(KEY_SOCKET, SERVER_LOCAL_IP_SOCKET).apply()
        setLanguage(mPreferences);
      //  proceedToLoginScreen()
        //startIntroPlay()
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_SECURE,
//            WindowManager.LayoutParams.FLAG_SECURE
//        )

        btnUpdate = findViewById(R.id.btnUpdate)
        containerAppUpdate = findViewById(R.id.containerAppUpdate)
        AnalyticsEventsHelper().eventAppOpen()

        requestUpdate()
//        startProcessToLogin()
        Log.i(TAG, "Density: ${resources.displayMetrics.densityDpi}")

        val userId = mPreferences.getInt("userId", 0)
        Log.i(TAG, "userId: ${userId}")
//        val generator = FrequencyGenerator()
//
//        generator.startTransmittingDigits(userId.toString()); // Digits
// generator.startTransmittingDigits("012")

    }

    private fun requestUpdate(){
        val AuthToken = mPreferences.getString(KEY_AUTH_TOKEN, PREFERENCES_STRING_DEFAULT_VALUE)
        if(AuthToken.isNullOrEmpty() || AuthToken == PREFERENCES_STRING_DEFAULT_VALUE){
            startProcessToLogin()
            return
        }
        val serverIpUpdate  : String = mPreferences.getString(KEY_SERVER_IP, SERVER_GLOBAL_IP_LOGIN) ?: SERVER_GLOBAL_IP_LOGIN;
        if(serverIpUpdate == PREFERENCES_STRING_DEFAULT_VALUE){
            startProcessToLogin()
            return
        }
//        startProcessToLogin()
        val apiInterface = RetrofitClient.getClient(serverIpUpdate).create(
            ApiInterface::class.java
        )
        val appUpdate = apiInterface.getAppUpdate("b $AuthToken");
        appUpdate.enqueue(object : Callback<AppUpdateResponse> {
            override fun onResponse(
                call: Call<AppUpdateResponse>,
                response: Response<AppUpdateResponse>
            ) {
                if(response.code() == 200 && response.body()!!.data.isNotEmpty()) {
                    if(response.body()!!.data.isNotEmpty()) {
                        val lastUpdated = response.body()!!.data.first()
                        println("print lastUpdated -->"+lastUpdated)
                        if(lastUpdated.app_version.toFloat() > BuildConfig.VERSION_CODE) {
                            URL_APK = changeLocalToGlobalIfRequired(lastUpdated.url)
                            showDialogUpdate(changeLocalToGlobalIfRequired(lastUpdated.url))
                        }else{
                            startProcessToLogin()
                        }
                    }

                   // startProcessToLogin()
                }else{
//                    Toast.makeText(this@SplashActivity, "Check your internet connection", Toast.LENGTH_SHORT).show()
                    startProcessToLogin()
                }
            }

            override fun onFailure(call: Call<AppUpdateResponse>, t: Throwable) {
//                Toast.makeText(this@SplashActivity, "Check your internet connection", Toast.LENGTH_SHORT).show()
                startProcessToLogin()
            }

        })
    }

    private fun enableAlarmBootUp(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val receiver = ComponentName(this, BootUpReceiver::class.java)
            packageManager.setComponentEnabledSetting(
                receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            val intent = Intent("android.intent.action.BOOT_COMPLETED")
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 10000, // 10 segundos de espera
                pendingIntent
            )
        } else {
            // Registra tu BroadcastReceiver en el archivo AndroidManifest.xml y pide el permiso RECEIVE_BOOT_COMPLETED
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (containerAppUpdate.isVisible) {
            btnUpdate.requestFocus()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun setLanguage(prefs : SharedPreferences){
        val actualLanguage = prefs.getString(KEY_LANGUAGE, VALUE_LANGUAGE_EN);
        if(actualLanguage.equals(VALUE_LANGUAGE_EN)){
            val locale = Locale("en", "US")
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            baseContext.resources.updateConfiguration(
                config,
                baseContext.resources.displayMetrics
            )
        }else{
            val locale = Locale("ar")
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            baseContext.resources.updateConfiguration(
                config,
                baseContext.resources.displayMetrics
            )
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun proceedToLoginScreen(){
        // [XXX] To show dialog First Time, only change false to true
        if (mPreferences.getBoolean(KEY_FIRST_TIME, false)) {
            mPreferences.edit().putBoolean(KEY_FIRST_TIME, false).apply()
//            PlayerManager(this@SplashActivity).showDialogFirstTime(
//                layoutInflater
//            ) {
                dataLoaded = true;
                intentGo = Intent(this@SplashActivity, LoginActivity::class.java);
                goToLoginScreen(intentGo)
//            }
        } else {
            dataLoaded = true;
            intentGo = Intent(this@SplashActivity, LoginActivity::class.java);
            goToLoginScreen(intentGo)
        }
    }

    var loggedIn : Boolean? = false

    private fun startProcessToLogin() {
        val AuthCode: String? =
            mPreferences.getString(KEY_AUTHCODE, PREFERENCES_STRING_DEFAULT_VALUE)
        val ServerIP = mPreferences.getString(KEY_SERVER_IP, PREFERENCES_STRING_DEFAULT_VALUE)?: ""
        CurrentData.ip = ServerIP
        if (AuthCode != PREFERENCES_STRING_DEFAULT_VALUE && ServerIP != PREFERENCES_STRING_DEFAULT_VALUE && ServerIP != null) {
            val macAddr = mPreferences.getString(KEY_MAC, "") ?: ""
            val areaCode = mPreferences.getString(KEY_AREA_CODE, "") ?: ""
            checkLCO(macAddr, object : LCOCheckCallback {
                @androidx.annotation.OptIn(UnstableApi::class)
                override fun onSuccess(response: LCOCheckResponse) {
                    if (areaCode != response.opcode) {
                        PreviewScreen.lcoValid = false
                    } else {
                        PreviewScreen.lcoValid = true
                    }
                    updateLocalDB()
                }

                override fun onFailure(t: Throwable) {
                    updateLocalDB()
                }

            })


//            isUserLoggedIn(ServerIP, object : OnResultLogin { TODO descomentar LINEA
//                override fun Sucess() {
//                    loggedIn = true;
//                    updateLocalDB()
//                }
//
//                override fun Failure() {
//                    logOut()
//                }
//
//            })
        } else {
            loggedIn = false;
         //   if(videoEnded){
                proceedToLoginScreen();
          //  }
        }
    }
    private lateinit var URL_APK: String

    private fun startUpdatingApk(url: String) {
        val downloadApk = DownloadApk(this@SplashActivity)
        downloadApk.startDownloadingApk(url, "panmetro_update")
        val containerAppUpdate = findViewById<FrameLayout>(R.id.containerAppUpdate)
        containerAppUpdate.visibility = View.GONE
        Toast.makeText(this@SplashActivity, "Updating app", Toast.LENGTH_SHORT).show()
    }

    private fun showDialogUpdate(url: String) {
        containerAppUpdate.setVisibility(View.VISIBLE)
        btnUpdate.requestFocus()
        btnUpdate.setOnClickListener(View.OnClickListener {
            if (checkPermissions()) startUpdatingApk(url) else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    } else {
                        startUpdatingApk(url)
                    }
                }else{
                    requestPermissions()
                }

            }
        })
    }

    private fun checkPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 205
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == 205 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showDialogUpdate(URL_APK)
        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String?>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//    }


    private fun checkIfUpdateNeeded(isleanback : Boolean) {
        goToHomeScreenLoaded(mPreferences.getString(KEY_SERVER_IP, PREFERENCES_STRING_DEFAULT_VALUE) ?: "null", isleanback);
        return;
        val db = FirebaseFirestore.getInstance()
        db.collection(APP_NAME_COLLECTION)
            .document(APP_UPDATE_DOCUMENT)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val appUpdate = documentSnapshot.toObject(
                    AppUpdateModel::class.java
                )
                if (appUpdate != null) {
                    URL_APK = appUpdate.app_url
                    if (appUpdate.version_number > BuildConfig.VERSION_CODE) {
                        showDialogUpdate(URL_APK)
                    }else{
                        goToHomeScreenLoaded(mPreferences.getString(KEY_SERVER_IP, PREFERENCES_STRING_DEFAULT_VALUE) ?: "null", isleanback)
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this@SplashActivity,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
                    goToHomeScreenLoaded(mPreferences.getString(KEY_SERVER_IP, PREFERENCES_STRING_DEFAULT_VALUE) ?: "null", isleanback)
                Log.w(TAG, e.message!!)
            }
    }
    private fun goToLoginScreen(intent : Intent) {
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }
    private fun goToHomeScreen() {
        CoroutineScope(Dispatchers.IO).launch {
            saveStreamToken {
                startActivity(intentGo)
                overridePendingTransition(0, 0)
                finish()
            }
        }
    }
    private fun isUserLoggedIn(ServerIP: String, callback: OnResultLogin): Boolean {
        var paramObject: JSONObject? = null
        try {
            paramObject = JSONObject()
            paramObject.put(
                KEY_USERNAME,
                mPreferences.getString(KEY_USERNAME, PREFERENCES_STRING_DEFAULT_VALUE)
            )
            paramObject.put(
                KEY_MAC,
                mPreferences.getString(KEY_MAC, PREFERENCES_STRING_DEFAULT_VALUE)
            )
            paramObject.put(
                KEY_PASSWORD,
                mPreferences.getString(KEY_PASSWORD, PREFERENCES_STRING_DEFAULT_VALUE)
            )
        } catch (e: JSONException) {
            Log.i(TAG, "Json Exception: " + e.message)
        }
        paramObject?.let {
            val apiInterface = RetrofitClient.getClient(ServerIP).create(ApiInterface::class.java)
            val call = apiInterface.getLogin(it.toString())

            call.enqueue(object : Callback<Login> {
                override fun onResponse(call: Call<Login>, response: Response<Login>) {
                    if (response.code() == 200) callback.Sucess()
                    else callback.Failure()
                }

                override fun onFailure(call: Call<Login>, t: Throwable) {
                    callback.Sucess()
                }

            })
        }
        return true;
    }

    private fun logOut() {
        val preferencesEditor = mPreferences.edit()
        preferencesEditor.putString(KEY_AUTHCODE, PREFERENCES_STRING_DEFAULT_VALUE)
        preferencesEditor.putString(KEY_SUBSCRIBE_STATUS, PREFERENCES_STRING_DEFAULT_VALUE)
        //   preferencesEditor.putString("subscribeStatus", PREFERENCES_STRING_DEFAULT_VALUE)
        preferencesEditor.putString(KEY_LIVESTREAM, PREFERENCES_STRING_DEFAULT_VALUE)
        preferencesEditor.putString(KEY_SERVER_IP, PREFERENCES_STRING_DEFAULT_VALUE)

        preferencesEditor.putString(KEY_LIVE_CATEGORY, PREFERENCES_STRING_DEFAULT_VALUE)
        preferencesEditor.putString(KEY_VOD_STREAM, PREFERENCES_STRING_DEFAULT_VALUE)
        preferencesEditor.putString(KEY_VOD_STREAM, PREFERENCES_STRING_DEFAULT_VALUE)
        preferencesEditor.putString(KEY_SERIES_STREAM, PREFERENCES_STRING_DEFAULT_VALUE)
        preferencesEditor.putString(KEY_SERIES_CATEGORY, PREFERENCES_STRING_DEFAULT_VALUE)
        preferencesEditor.putString("LAST_PLAYED_URL", PlayerLiveContainer.nullUrl);
        preferencesEditor.apply()
        intentGo = Intent(
            applicationContext,
            LoginActivity::class.java
        )
        intentGo.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_NEW_TASK // To clean up all activities

        AuthHelper().signOut()
        dataLoaded = true;
        startActivity(intentGo)
        overridePendingTransition(0, 0)
        finish()

    }

    var videoEnded = false;
    var dataLoaded = false;
    private lateinit var intentGo : Intent
    private var player : ExoPlayer? = null

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onPause() {
        if(Util.SDK_INT < 23){
            closeDb()
        }
        player?.let {
            if(it.isPlaying){
                it.stop()
                it.release()
                player = null;
            }
        }
        super.onPause()
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onStop() {
        if(Util.SDK_INT >= 23){
            closeDb()
        }
        player?.let {
            if(it.isPlaying){
                it.stop()
                it.release()
                player = null;
            }
        }
        super.onStop()

    }

    private fun startIntroPlay() {
        val videoView = findViewById<PlayerView>(R.id.videoViewIntro)
        val path = "android.resource://" + packageName + "/" + R.raw.intro_video
        player = ExoPlayer.Builder(this).build()
        videoView.player = player
        val mediaItem = MediaItem.fromUri(Uri.parse(path))
        player!!.addMediaItem(mediaItem)
        player!!.prepare()
        player!!.playWhenReady = true
        player!!.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when(playbackState) {
                    ExoPlayer.STATE_ENDED -> {
                        videoEnded = true;
                        if(dataLoaded){
                            startActivity(intentGo)
                            finish()
                        }else if(loggedIn != null && !loggedIn!!){
                            proceedToLoginScreen()
                        }
                    }
                }
                super.onPlaybackStateChanged(playbackState)
            }
        })
    }

    interface OnResultLogin {
        fun Sucess()
        fun Failure()
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun goToHomeScreenLoaded(ServerIP : String, isLeanback : Boolean){
//        if (mPreferences.getString(
//                KEY_BOOTUP_ACTIVITY,
//                VALUE_HOME_ACTIVITY
//            ) == VALUE_HOME_ACTIVITY
//        ) {
//            intentActivity = Intent(this, PreviewScreen::class.java)
//        } else {
//            intentActivity = Intent(this, PlayerScreen::class.java)
//        }

        val intentActivity : Intent = Intent(this, PlayerScreen::class.java)
//        else Intent(applicationContext, MainActivity::class.java)
        if (ServerIP != "null") {
            checkStateServer(ServerIP, 3001, object : StateChangedServer {
                @androidx.annotation.OptIn(UnstableApi::class)
                override fun stateChanged(value: Boolean) {
                    if (!value) {
                        val countServerOff = mPreferences.getInt(COUNT_SERVER_OFF, 0)
                        mPreferences.edit().putInt(COUNT_SERVER_OFF, countServerOff + 1)
                            .apply()
                        if (countServerOff + 1 >= 10) {
                            mPreferences.edit().putInt(COUNT_SERVER_OFF, 0).apply()
                            logOut()
                        }
                    } else {
                        mPreferences.edit().putInt(COUNT_SERVER_OFF, 0).apply()
                    }
                    intentActivity.putExtra(KEY_SERVER_STATUS, value)
                    intentGo = intentActivity;
                    if (mPreferences.getBoolean(KEY_FIRST_TIME, true)) {
                        mPreferences.edit().putBoolean(KEY_FIRST_TIME, false).apply()
//                        PlayerManager(this@SplashActivity).showDialogFirstTime(
//                            layoutInflater
//                        ) {
                            dataLoaded = true;
                            //if(videoEnded){
                                goToHomeScreen();
                          //  }
//                        }
                    } else {
                        dataLoaded = true;
                      //  if(videoEnded){
                            goToHomeScreen();
                    //    }
                    }
                }
            })
        }
    }

    private fun closeDb() {
        dbHelper.closeDb();
        dbHelperKt.closeDb();
    }

    private fun updateLocalDB() {
        val textViewCurrentProgress = findViewById<TextView>(R.id.tvCurrentProgress)
        val progressBarCurrentProgress = findViewById<ProgressBar>(R.id.progressBarLoadingSplash)
        val updateLocalDB = UpdateLocalDB(activity = this, dbHelperKt = dbHelperKt, dbHelper = dbHelper)
        updateLocalDB.updateLocalDB(object : IncreaseCalls {
            override fun call(callName : String) {
                val totalsCalls = updateLocalDB.totalTalls
                progressBarCurrentProgress.max = totalsCalls * 10
                if(totalsCalls > 0){
                    textViewCurrentProgress.visibility = View.VISIBLE
                    progressBarCurrentProgress.visibility = View.VISIBLE
                    textViewCurrentProgress.text = (((allCalls+1) / totalsCalls.toDouble()) * 100).toInt().toString() + " %"
                }
                progressBarCurrentProgress.progress = (allCalls+1) * 10
                allCalls++
                Log.i("INCREASE CALLS: ", "Call: $callName - NUM: $allCalls / $totalsCalls")
                if (allCalls >= totalsCalls) {
                    allCalls = 0
                    if (firstBoot) {
                        goToFullScreen = checkIfGoToFullScreen()
                    }
                    if (mPreferences.getBoolean(KEY_LEANBACK_ENABLED, false) && !goToFullScreen) {
                        checkIfUpdateNeeded(true)
                    } else if (!goToFullScreen) {
                        checkIfUpdateNeeded(false)
                    }
                    firstBoot = false
                }
            }

        })
    }

    private var goToFullScreen = false;
    private var allCalls = 0;
    private var firstBoot = true;

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun checkIfGoToFullScreen(): Boolean {
        if (mPreferences.getString(
                KEY_BOOTUP_ACTIVITY,
                VALUE_HOME_ACTIVITY
            ) == VALUE_FULL_SCREEN_ACTIVITY
            || mPreferences.getBoolean(KEY_LOW_PROFILE, false)
        ) {
//            setUpAlteredMenu(this)
            val intent = Intent(this, PlayerScreen::class.java)
            intent.putExtra(KEY_FIRST_TIME_LIVE_TV, true)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
            return true
        }
        return false
    }

}