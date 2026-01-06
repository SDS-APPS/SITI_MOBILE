package com.siti.mobile.Utils

import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.siti.mobile.Model.Room.RM_LiveStreamCategory
import com.siti.mobile.Model.advertisment.AdvertismentModel
import com.siti.mobile.Player.CurrentPlayerScreen
import com.siti.mobile.Player.PlayerManager
import com.siti.mobile.mvvm.fullscreen.view.PlayerScreen
import com.smarteist.autoimageslider.SliderView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.sql.Date
import javax.net.ssl.HttpsURLConnection

//    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
//        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
//        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
//        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
//        return noOfColumns;
//    }

const val blurRadius = 5;
const val blurSampling = 15;

var currentAdvertismentScreen = AdvertismentScreen.HOME;

fun getAdvertismentByPosition(position : Int, list : List<AdvertismentModel>) : List<AdvertismentModel>{
    return list.filter { position == it.position }
}

fun startLoopAdvertisment(slider : SliderView, advertisment : List<AdvertismentModel>, advertismentScreen: AdvertismentScreen){
            CoroutineScope(Dispatchers.IO).launch {
            while (advertismentScreen == currentAdvertismentScreen) {
                advertisment.forEachIndexed  {index,  currentAd ->
                    CoroutineScope(Dispatchers.Main).launch {
                        if(advertismentScreen != currentAdvertismentScreen) return@launch
                        try{
                           slider.currentPagePosition = index
                        }catch (e: Exception){
                            e.printStackTrace()
                        }


                    }
                    delay(currentAd.duration * 1000L)
                }
                delay(1000)
            }
        }
}

fun reverseAdvertisments(ads : List<AdvertismentModel>) = ads.reversed()

//fun startAdvertisment(context : Context, position: Int, imageView: ImageView, imageViewHelper : ImageView, advertisment : List<AdvertismentModel>, advertismentScreen: AdvertismentScreen) {
//        CoroutineScope(Dispatchers.IO).launch {
//            while (advertismentScreen == currentAdvertismentScreen) {
//                advertisment.filter { it.position == position }.forEach { currentAd ->
//                    CoroutineScope(Dispatchers.Main).launch {
//                        Picasso.get().load(currentAd.url).into(imageView)
//                        if(advertismentScreen != currentAdvertismentScreen) return@launch
//                        try{
//                            Glide.with(context)
//                                .applyDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
//                                .asDrawable()
//                                .load(convertHttpsToHttp(currentAd.url))
//                                .transition(DrawableTransitionOptions.withCrossFade())
//                                .listener(object : RequestListener<Drawable> {
//                                    override fun onLoadFailed(
//                                        e: GlideException?,
//                                        model: Any?,
//                                        target: Target<Drawable>?,
//                                        isFirstResource: Boolean
//                                    ): Boolean {
//                                        return true;
//                                    }
//
//                                    override fun onResourceReady(
//                                        resource: Drawable?,
//                                        model: Any?,
//                                        target: Target<Drawable>?,
//                                        dataSource: DataSource?,
//                                        isFirstResource: Boolean
//                                    ): Boolean {
//                                        resource?.let {
//                                            imageViewHelper.background = it
//                                        }
//                                        return true;
//                                    }
//
//                                })
//                                .into(imageView)
//                        }catch (e: Exception){
//                            e.printStackTrace()
//                        }
//
//
//                    }
//                    delay(currentAd.duration * 1000L)
//                }
//            }
//        }
//}


data class JoinVODStreamsDate(
    val vod_id: String?,
    val title: String?,
    val tmdb_id: String?,
    val category_id: String?,
    val country: String?,
    val director: String?,
    val actors: String?,
    val trailer: String?,
    val duration: Int?,
    val year: Int?,
    val rating: Double?,
    val tmdb_rating: Double?,
    val releaseDate: Date?,
    val description: String?,
    val comments: String?,
    val url: String?,
    val image: String?,
    val videoMusic: Int?,
    val referenceId: String?,
    val logoBase64: String?,
    val isFavorite: String?,
    val provider_id : Int?
)

var URL_CHECKING = ""

interface CallReconnect{
    fun run(delay : Boolean)
}

fun removeSafeMargins(layout: ViewGroup) {
    layout.removeSafeMarginsExt()
}

fun showSafeMargins(layout: ViewGroup) {
    layout.showSafeMarginsExt()
}

fun setMargins(layout : ViewGroup, top : Int = 0, bottom : Int = 0, start : Int = 0, end : Int = 0) {
    val marginTop = dpToPx(top) // Suponiendo que tienes la función de extensión dpToPx para convertir dp a píxeles.
    val marginEnd = dpToPx(end)
    val marginStart = dpToPx(start)
    val marginBottom = dpToPx(bottom)
    val params = layout.layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(marginStart, marginTop, marginEnd, marginBottom)
    layout.layoutParams = params
}

fun ViewGroup.removeSafeMarginsExt() {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(0, 0, 0, 0)
    layoutParams = params
}

// Método para mostrar nuevamente los márgenes
fun ViewGroup.showSafeMarginsExt() {
    val marginTop = dpToPx(10) // Suponiendo que tienes la función de extensión dpToPx para convertir dp a píxeles.
    val marginEnd = dpToPx(9)
    val marginBottom = dpToPx(9)
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(0, marginTop, marginEnd, marginBottom)
    layoutParams = params
}


@OptIn(UnstableApi::class)
fun checkMemory(url: String, callReconnect: CallReconnect, currentPlayerScreenWhenCalled: CurrentPlayerScreen, onFrameStuck : () -> Unit) {
    URL_CHECKING = url
    var founded = false
    Log.w("PlayerFragmentMemory", "*/*/*/****--*--*-*-*********** >>>>> CALLING CHECK MEMORY <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<***********" )
    CoroutineScope(Dispatchers.IO).launch {
        Log.w(TAG, "url: $url")
        Log.w(TAG, "actualUrl: ${PlayerScreen.actualUrl}")
        Log.w(TAG, "when called: $currentPlayerScreenWhenCalled")
        Log.w(TAG, "current player: ${PlayerManager.currentPlayerScreen}")
        while (PlayerScreen.actualUrl == url && !founded && currentPlayerScreenWhenCalled == PlayerManager.currentPlayerScreen) {
        //    Log.w("PlayerFragmentReconnect", "LastFrametimeInMillis: "+ PlayerActivity.lastFrameTimeInMillis)
            if(PlayerScreen.lastFrameTimeInMillis != 0L && PlayerScreen.lastFrameTimeInMillis + 5000 < System.currentTimeMillis()){
                CoroutineScope(Dispatchers.Main).launch {
                    if(currentPlayerScreenWhenCalled == PlayerManager.currentPlayerScreen){
                    Log.w("PlayerFragmentReconnect", "<<<<<<<<<<<<<<<<<<<<<< CALLING RECONNECT FROM CHECK MEMORY >>>>>>>>>>>>>>>>>>>>")
                    callReconnect.run(false);
                        founded = true
                    }
                    else{
                        founded = true
                    }
                }
                founded = true;
            }
            delay(1000)
        }
        URL_CHECKING = ""
    }
}


//https://iptvsds.in/uploads/Vods/V1000031.jpg
fun changeLocalToGlobalIfRequired(url : String?) : String {
    if(url.isNullOrBlank()) return ""
    var serverIp = CurrentData.ip;
    var newUrl = url;
    if(serverIp.contains(SERVER_GLOBAL_IP_EMPTY) || serverIp.isEmpty() || serverIp == "null") {
        if(url.contains(SERVER_LOCAL_IP_EMPTY)) {
            newUrl = url.replace(SERVER_LOCAL_IP_EMPTY, SERVER_GLOBAL_IP_EMPTY)
        }else if(url.contains("10.22.254.30")){
            newUrl = url.replace("10.22.254.30", "115.187.52.252")
        }else if(url.contains("192.168.10.42")){
            newUrl = url
           // newUrl = url.replace("192.168.10.42", "117.216.44.13")
        }
    }
    HttpsURLConnection.setDefaultHostnameVerifier { hostname, session ->
        true
    }
    if(!newUrl.contains("https")) newUrl = newUrl.replace("http", "https")
    return newUrl;
}



fun getStringResolution(width : Int, height : Int) = "${width}x$height"

fun getLogoResolution(width : Int, height : Int) : String {
    return if(width >= 3000) "UHD"
    else if(width >= 1800) "FHD"
    else if(width >= 1080 && height >= 700) "HD"
    else "SD"
}

fun getTakeOfList(list : List<Any>) : Int {
    if(list.size >= 10) return 10
    else return list.size
}

val Number.toPx get() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    Resources.getSystem().displayMetrics)

fun dpToPx(value : Number) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    value.toFloat(),
    Resources.getSystem().displayMetrics).toInt()

fun sortCategoryById(list : List<RM_LiveStreamCategory>) : List<RM_LiveStreamCategory>{
    return list.sortedBy { it.id }
}


