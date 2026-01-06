package com.siti.mobile.Utils;

import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_FORENSIC_DELAY_MS;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_FORENSIC_SHOW_MS;
import static com.siti.mobile.Utils.KeyPreferencesKt.sharedPrefFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;

import com.siti.mobile.FrequencyGenerator;
import com.siti.mobile.mvvm.config.helpers.JavaHelper;
import com.siti.mobile.mvvm.fullscreen.view.PlayerScreen;
import com.siti.mobile.mvvm.login.view.LoginActivity;
import com.siti.mobile.Log.FirestoreLog;
import com.siti.mobile.Log.TypeObjectLog;
import com.siti.mobile.R;
import com.siti.mobile.Utils.qr.QRHelper;
import com.siti.mobile.network.keys.NetworkPackageKeys;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketHelper extends AppCompatActivity {

    final String TAG = "SocketHelper";
    private SharedPreferences mPreferences;
    SharedPreferences.Editor preferencesEditor;

    String socChannels = "-1", socBGColor = "#000000", socFontColor = "#FFFFFF", socFontSize = "16", socFontFamily = "sans-serif", socFingerprintText = "fingerprint", socFingerprintType = "scroll", socForced = "0", socUUID = "UUID";
    String socAreaCode = "", socVisibility = "covert";
    String startDttm;
    int socTimeInterval = 0, socRepeatTimes = 1, socBackgroundTransparency = 100;
    int positionChangeTime = 3000;
    Date startDate, endDate;
    final String datePattern = "yyyy-MM-dd HH-mm-ss";
    final SimpleDateFormat DateFormat = new SimpleDateFormat(datePattern);
    int socReseller = -1, socUser = -1;
    float socXAxis = 0.5f, socYAxis = 0.5f;
    long socDuration = 3000;
    Context context;
    Guideline guidelineX, guidelineY;
    TextView Fingerprint, tvDate, tvHour;
    ImageView Forensic;
    String pageChannel;
    LinearLayout fakeLayout;
    Timer durationTimer = null;
    Timer durationTimerBlinking = null;
    String appName;
    FirestoreLog firestoreLog;
    Activity activity;
    Boolean isForensic;
    ForensicWatermarkReceiver forensicWatermarkReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        firestoreLog = new FirestoreLog();
        appName = getResources().getString(R.string.app_name) + "----------   ";
    }

    public void setActivity(Context context) {
        this.context = context;
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onStop() {
        super.onStop();
        if(Util.SDK_INT > 23){
            if( FrequencyGenerator.Companion.getAudioTrack() != null){
                try{
                    FrequencyGenerator.Companion.getAudioTrack().stop();
                    FrequencyGenerator.Companion.getAudioTrack().release();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }

    long timeCAched = 0L;
    boolean isCached = false;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onPause() {
        long diff = System.currentTimeMillis() - lastLoopShowed;
        long finalDiff = 0;
        if(isCached){
            finalDiff = timeCAched - diff;
        }else if(isShowing){
            finalDiff = socDuration - diff;
        }else{
            finalDiff = socTimeInterval - diff;
        }

        if(preferencesEditor != null){
            preferencesEditor.putLong("time-"+startDttm, finalDiff).apply();
            preferencesEditor.putBoolean("isShowing", isShowing).apply();
            handlerRepeatFP.removeCallbacks(runnableRepeatRP);
        }

        if(Util.SDK_INT <= 23){
            if( FrequencyGenerator.Companion.getAudioTrack() != null){
                try{
                    FrequencyGenerator.Companion.getAudioTrack().stop();
                    FrequencyGenerator.Companion.getAudioTrack().release();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        super.onPause();
    }

    public void socketConnection(Activity activity, Socket socket, Guideline guidelineX, Guideline guidelineY, TextView Fingerprint, ImageView Forensic, String pageChannel, LinearLayout fakeLayout, TextView tvDate, TextView tvHour, ForensicWatermarkReceiver forensicWatermarkReceiver) {
        this.forensicWatermarkReceiver = forensicWatermarkReceiver;
        mPreferences = getApplicationContext().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        preferencesEditor = mPreferences.edit();
        if(lastLoopShowed != 0L){
            long diff = System.currentTimeMillis() - lastLoopShowed;
            long finalDiff = 0;
            if(isCached){
                finalDiff = timeCAched - diff;
            }else if(isShowing){
                finalDiff = socDuration - diff;
            }else{
                finalDiff = socTimeInterval - diff;
            }

            preferencesEditor.putLong("time-"+startDttm, finalDiff).apply();
            preferencesEditor.putBoolean("isShowing", isShowing).apply();
            handlerRepeatFP.removeCallbacks(runnableRepeatRP);
        }
        this.guidelineX = guidelineX;
        this.guidelineY = guidelineY;
        this.Fingerprint = Fingerprint;
        this.Forensic = Forensic;
        this.pageChannel = pageChannel;
        this.fakeLayout = fakeLayout;
        this.activity = activity;
        this.tvDate = tvDate;
        this.tvHour = tvHour;

        Log.i(TAG, "socketConnection: suresh" + pageChannel);

        fakeLayout.setVisibility(View.GONE);
        loadData();

        socket.off("android-push");
        socket.off("edit-package" + mPreferences.getInt("userId", 0));
        Log.w(TAG,  "UserID: " + mPreferences.getInt("userId", 0));
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Socket Connected ");
                    }
                });
            }
        });
        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "Socket error:: " + args[0].toString());
            }
        });
        socket.on("package-update", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UtilLoginJava utilLoginJava = new UtilLoginJava(activity);
                        utilLoginJava.login();
                        Toast.makeText(SocketHelper.this, getResources().getString(R.string.sync_packages), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        socket.on("assign-package" + mPreferences.getInt("userId", 0), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UtilLoginJava utilLoginJava = new UtilLoginJava(activity);
                        utilLoginJava.login();
                        Toast.makeText(SocketHelper.this, getResources().getString(R.string.sync_packages), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        socket.off("key-rotation");
        socket.on("key-rotation", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.w(TAG, "EVENT KEY ROTATION");
                        Toast.makeText(SocketHelper.this, "Key Rotation", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        socket.on("system-time", new Emitter.Listener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void call(Object... args) {
                JSONObject object = (JSONObject) args[0];
                try {
                    String timeString = object.getString("time");
                    SimpleDateFormat formatDateComplete = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date dateReceived = formatDateComplete.parse(timeString);

                    SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd");
                    SimpleDateFormat formatDateFullScreen = new SimpleDateFormat("dd-MMM");
                    String dateFormatted = formatDate.format(dateReceived);
                    String dateFormattedFullScreen = formatDateFullScreen.format(dateReceived);
                    PlayerScreen.Companion.setDateFullScreen(dateFormattedFullScreen);

                    SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm");
                    String hourFormatted = formatHour.format(dateReceived);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CurrentTimeContainer.INSTANCE.setDate(dateFormatted);
                            CurrentTimeContainer.INSTANCE.setHour(hourFormatted);
                            if(tvDate != null){
                                if(PlayerScreen.Companion.isActive()) {
                                    tvDate.setText(dateFormattedFullScreen);
                                }else{
                                    tvDate.setText(dateFormatted);
                                }

                            }
                            if(tvHour != null){
                                tvHour.setText(hourFormatted + " ");
                            }

                        }
                    });

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        socket.on("force-logout", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject object = (JSONObject) args[0];

                            System.out.println("inside refresh socket called " + object);

                            socUser = object.optInt("userId", -1);
                            socAreaCode = object.optString("areacode", "");

                            int prefUserId = mPreferences.getInt("userId", 0);
                            String prefAreaCode = mPreferences.getString("areaCode", "");

                            if (socUser == prefUserId || Objects.equals(socAreaCode, prefAreaCode)) {

                                Log.i(TAG, "Socket force-logout received ");
                                Toast.makeText(SocketHelper.this, getResources().getString(R.string.force_logout), Toast.LENGTH_SHORT).show();

                                preferencesEditor = mPreferences.edit();
                                preferencesEditor.putString("AuthCode", "null");
                                preferencesEditor.putString("subscribeStatus", "null");
                                preferencesEditor.putString("subscribeStatus", "null");
                                preferencesEditor.putString("LiveStream", "null");
                                preferencesEditor.putString("serverIP", "null");

                                preferencesEditor.putString("LiveCategory", "null");
                                preferencesEditor.putString("VODStream", "null");
                                preferencesEditor.putString("VODCategory", "null");
                                preferencesEditor.putString("SeriesStream", "null");
                                preferencesEditor.putString("SeriesCategory", "null");
                                preferencesEditor.putString("LAST_PLAYED_URL", "null");
                                preferencesEditor.putInt(NetworkPackageKeys.LIVE_TV, 0);
                                preferencesEditor.putInt(NetworkPackageKeys.VOD, 0);
                                preferencesEditor.putInt(NetworkPackageKeys.SOD, 0);
                                preferencesEditor.putInt(NetworkPackageKeys.MOD, 0);
                                preferencesEditor.putBoolean(NetworkPackageKeys.FIRST_LOAD, true).apply();
                                preferencesEditor.putString("LAST_PLAYED_URL", "");
                                preferencesEditor.apply();

                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                System.out.println("else called");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }



                    }
                });
            }
        });
        socket.on("edit-package" + mPreferences.getInt("userId", 0), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Socket edit-package received ");
                        UtilLoginJava utilLoginJava = new UtilLoginJava(activity);
                        utilLoginJava.login();
                        Toast.makeText(SocketHelper.this, getResources().getString(R.string.sync_packages), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });

        socket.on("refresh", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject object = (JSONObject) args[0];

                            System.out.println("inside refresh socket called " + object);

                            socUser = object.optInt("userId", -1);
                            socAreaCode = object.optString("areacode", "");

                            int prefUserId = mPreferences.getInt("userId", 0);
                            String prefAreaCode = mPreferences.getString("areaCode", "");

                            if (socUser == prefUserId || Objects.equals(socAreaCode, prefAreaCode)) {
                                Log.i(TAG, "Socket refresh received");
                                UtilLoginJava utilLoginJava = new UtilLoginJava(activity);
                                utilLoginJava.login();
                                Toast.makeText(activity, activity.getResources().getString(R.string.refresh), Toast.LENGTH_SHORT).show();
                                if (activity instanceof Activity) {
                                    ((Activity) activity).finish();
                                }
                            } else {
                                System.out.println("else called");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        socket.on("update-package" + mPreferences.getInt("userId", 0), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Socket Update");
                        UtilLoginJava utilLoginJava = new UtilLoginJava(activity);
                        utilLoginJava.login();
                        Toast.makeText(SocketHelper.this, getResources().getString(R.string.sync_packages), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
        socket.on("android-push", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("android push called ");
                        JSONObject object = (JSONObject) args[0];
//                        Log.i(TAG, "run: suresh" + object.toString());
                        parseData(object);
                        firestoreLog.log(TypeObjectLog.Fingerprint, "OBJECT", object.toString());
                        FirebaseCrashlytics.getInstance().log(appName+ object.toString());
                        calculate(guidelineX, guidelineY, Fingerprint, Forensic, pageChannel, fakeLayout, object);

                    }
                });
            }
        });
        socket.on("mail", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject object = (JSONObject) args[0];
                Toast.makeText(activity, "MessageReceived: " + object.toString(), Toast.LENGTH_LONG).show();
            }
        });
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.close();
            }
        });
        JavaHelper.disableSSLCertificateVerify();
        socket.connect();
    }

    void parseData(JSONObject object) {
        try {
            socReseller = object.getInt("resellerId");
            socUser = object.getInt("userId");
            socDuration = object.getInt("duration") * 1000L;
            socBGColor = object.getString("bg_color");
            String timeIntervalString = object.getString("timeInterval");
            if(!timeIntervalString.isEmpty()) {
                socTimeInterval = Integer.parseInt(timeIntervalString) * 1000;
            }
            String repeatTimesString = object.getString("repeatTimes");
            if(!repeatTimesString.isEmpty()){
                socRepeatTimes = Integer.parseInt(repeatTimesString);
            }

            socFontColor = object.getString("font_color");
            socFontSize = object.getString("font_size");
            socFontFamily = object.getString("font_text");
            socFingerprintText = object.getString("text");
            socFingerprintType = object.getString("fingerprint_type");
            socForced = object.getString("forced");
            socUUID = object.getString("udid");
            startDate = DateFormat.parse(object.getString("startDttm"));
            endDate = DateFormat.parse(object.getString("endDttm"));
            socChannels = object.getString("channelId");

            socAreaCode = object.getString("areaCode");
            socVisibility = object.getString("visibility");
            socBackgroundTransparency = object.getInt("backgroundTransperncy");


        } catch (JSONException | ParseException e) {
            //Log.i(TAG, "parseData: " + e);
           // firestoreLog.log(TypeObjectLog.Fingerprint, "EXCEPTION", e.toString());
         //   FirebaseCrashlytics.getInstance().log(appName+"parseData" + e);
            endDate = new Date();
        }
    }


    void saveData(JSONObject object) {
        preferencesEditor = mPreferences.edit();
        preferencesEditor.putString("fingerPrint", object.toString());
        preferencesEditor.apply();
    }


    void loadData() {
        String json = mPreferences.getString("fingerPrint", "");
        if (!json.equals("")) {
            JSONObject object = null;
            try {
                object = new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (object != null) {
                parseData(object);
                calculate(guidelineX, guidelineY, Fingerprint, Forensic, pageChannel, fakeLayout, object);
            }
        }

    }

    int repeatedTimes = 1;
    boolean isShowing;
    long lastLoopShowed = 0L;
    Handler handlerRepeatFP = new Handler();
    Runnable runnableRepeatRP = new Runnable() {
        @Override
        public void run() {
            lastLoopShowed = System.currentTimeMillis();
            if(isShowing){
                Log.w(TAG, "Making GONE, repeated times: " + repeatedTimes);
                Fingerprint.setVisibility(View.GONE);
                if(forensicWatermarkReceiver != null){
                    forensicWatermarkReceiver.onFinalized();
                }
                isShowing = false;
            }else{
                if(forensicWatermarkReceiver != null){
                    forensicWatermarkReceiver.onReceived();
                }
                if(!isForensic){
                    Fingerprint.setVisibility(View.VISIBLE);
                }
                repeatedTimes++;
                isShowing = true;
                preferencesEditor.putInt(startDttm, repeatedTimes).apply();
                Log.w(TAG, "Making VISIBLE");
            }
            if(repeatedTimes >= socRepeatTimes){
                Fingerprint.setVisibility(View.GONE);
                preferencesEditor.putBoolean("isShowing", true).apply();
                Log.w(TAG, "Repeat times is major, will never repeat");
            }if(!isShowing){
                handlerRepeatFP.postDelayed(this, socTimeInterval);
                Log.w(TAG, "will repeat on: " + socTimeInterval);
            }else{
                handlerRepeatFP.postDelayed(this, socDuration);
                Log.w(TAG, "will repeat on: " + socDuration);
            }
            isCached = false;
        }
    };

    @OptIn(markerClass = UnstableApi.class)
    public void calculate(Guideline guidelineX, Guideline guidelineY, TextView Fingerprint, ImageView Forensic, String channel, LinearLayout fakeLayout, JSONObject object) {

        Date now = new Date();
        String test = textToBinaryEncrypt("new test");

        try {



            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.MILLISECOND, (int) socDuration);

            Date finalDate = calendar.getTime();

            long duration = finalDate.getTime() - now.getTime();
            long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
            if (diffInSeconds > 0) {
                try {
                    int userId = mPreferences.getInt("userId", 0);
                    int adminId = mPreferences.getInt("adminID", 0);
                    String areaCode = mPreferences.getString("areaCode", "");
//                    if ((areaCode.equals(socAreaCode) || (socReseller == adminId || socReseller == -1))  && (socUser == userId || socUser == -1)) {
                    if ((areaCode.equals(socAreaCode)) || socAreaCode.isEmpty() && (socUser == userId || socUser == -1)) {

                        saveData(object);
                        socDuration = object.getInt("duration") * 1000L;
                        socBGColor = object.getString("bg_color");
                        socFontColor = object.getString("font_color");
                        socFontSize = object.getString("font_size");
                        socFontFamily = object.getString("font_text");
                        socBGColor = bgColorTrans(socBackgroundTransparency, socBGColor);
                        socFingerprintText = object.getString("text");
                        if(!object.getString("timeInterval").isEmpty()){
                            socTimeInterval = Integer.parseInt(object.getString("timeInterval")) * 1000;
                            mPreferences.edit().putInt(KEY_FORENSIC_SHOW_MS, Integer.parseInt(object.getString("timeInterval"))).apply();
                            mPreferences.edit().putInt(KEY_FORENSIC_DELAY_MS, Integer.parseInt(object.getString("timeInterval"))).apply();
                        }
                        if(!object.getString("repeatTimes").isEmpty()){
                            socRepeatTimes = Integer.parseInt(object.getString("repeatTimes"));
                        }

                        String visibility = object.getString("visibility");
                        isForensic = visibility.contains("ForensicWaterMark");
                        if(isForensic && PlayerScreen.Companion.isActive()) {
                            long called = System.currentTimeMillis();
                            FrequencyGenerator.Companion.setCalled(called);
                            FrequencyGenerator.Companion.startTransmittingDigits(userId+"", diffInSeconds, called);
                        }

//                        if(forensicWatermarkReceiver != null){
//                            if(isForensic){
//                                forensicWatermarkReceiver.onReceived();
//                            }else{
//                                forensicWatermarkReceiver.onFinalized();
//                            }
//                        }
                        startDttm = object.getString("startDttm");
                        repeatedTimes= mPreferences.getInt(startDttm, 0);
                        isShowing = mPreferences.getBoolean("isShowing", true);
                        if(mPreferences.getInt(startDttm, -1) == -1) {
                            isShowing = true;
                            mPreferences.edit().putInt(startDttm, 0).apply();
                        }
                        handlerRepeatFP.removeCallbacks(runnableRepeatRP);
                        if(repeatedTimes > socRepeatTimes){
                            return;
                        }

                        long time = mPreferences.getLong("time-"+startDttm, 0L);

                        if(time > 0){
                            isCached = true;
                            timeCAched = time;
                            handlerRepeatFP.postDelayed(runnableRepeatRP, time);
                            Log.w(TAG, "Delay with cached time" + time);
                        }else{
                            if(isShowing){
                                handlerRepeatFP.postDelayed(runnableRepeatRP, socDuration);
                                Log.w(TAG, "Delay with duration: " + socDuration);
                            }else{
                                handlerRepeatFP.postDelayed(runnableRepeatRP, socTimeInterval);
                                Log.w(TAG, "Delay with interval: " + socTimeInterval);
                            }

                        }

                        lastLoopShowed = System.currentTimeMillis();
                        try {
                            Fingerprint.setBackgroundColor(
                                    Color.parseColor(socBGColor)
                            );
                            Fingerprint.setTextColor(Color.parseColor(socFontColor));
                            Fingerprint.setTextSize(Integer.parseInt(socFontSize));


                            if (object.getDouble("x_axis") == -1.0 || object.getDouble("y_axis") == -1.0) {
                                socXAxis = randomAxis();
                                socYAxis = randomAxis();
                            } else {
//                                Log.i(TAG, "calculate: custom xaxis and yaxis");
                                socXAxis =
                                        BigDecimal.valueOf(object.getDouble("x_axis")).floatValue();
                                socYAxis =
                                        BigDecimal.valueOf(object.getDouble("y_axis")).floatValue();
                            }

                        } catch (IllegalArgumentException e) {
                            Log.i(TAG, "run: IllegalArgumentException" + e.getLocalizedMessage());
                        } catch (Exception e) {
                            Log.i(TAG, "run: Exception" + e.getLocalizedMessage());
                        }

                        if (socFontFamily.equalsIgnoreCase("sans-serif")) {
                            Fingerprint.setTypeface(Typeface.SANS_SERIF);
                        } else if (socFontFamily.equalsIgnoreCase("sans-serif-medium")) {
                            Fingerprint.setTypeface(Typeface.MONOSPACE);
                        } else if (socFontFamily.equalsIgnoreCase("sans-serif-smallcaps")) {
                            Fingerprint.setTypeface(Typeface.DEFAULT_BOLD);
                        }

                        if (socForced.equalsIgnoreCase("1")) {
                            fakeLayout.setVisibility(View.VISIBLE);
                         //   fakeLayout.requestFocus();
                        } else {
                            fakeLayout.setVisibility(View.GONE);
                        }

                 //       if (!socFingerprintType.equalsIgnoreCase("scrollText")) {
//                            Log.i(TAG, "calculate: inside fingerprint");
                            if (socUUID.equalsIgnoreCase("1")) {
                                socFingerprintText = socFingerprintText + " " + mPreferences.getString("mac", "UUID");
                                if(socVisibility.contains("covert")){
                                   socFingerprintText = textToBinaryEncrypt(socFingerprintText);
//                                   socFingerprintText = socFingerprintText;
                                }
                                if(!isForensic) {
                                    Fingerprint.setText(socFingerprintText);
                                }else{
                                    Forensic.setImageBitmap(QRHelper.INSTANCE.getQrCodeBitmap(socFingerprintText));
                                    Fingerprint.setVisibility(View.GONE);
                                }
                            } else {
                                if(!isForensic){
                                    if(socVisibility.contains("covert")){
                                        Fingerprint.setText(textToBinaryEncrypt(object.getString("text")));
                                    }else{
                                        Fingerprint.setText(object.getString("text"));
                                    }

                                }else{
                                    Forensic.setImageBitmap(QRHelper.INSTANCE.getQrCodeBitmap(object.getString("text")));
                                }

                            }

            //            }


                        if (socChannels.equals("[]") && isShowing && !isForensic) {
                            Fingerprint.setVisibility(View.VISIBLE);
                        } else {
                            ArrayList<String> strList = new ArrayList<String>();
                            JSONArray jsonArray = new JSONArray(socChannels);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                strList.add(jsonArray.getString(i));
                            }
                            if (channel != null && strList.contains(channel) && isShowing) {
                                if(!isForensic){
                                    Fingerprint.setVisibility(View.VISIBLE);
                                }else{
//                                    Forensic.setVisibility(View.VISIBLE);
                                }

                            } else {
                                Fingerprint.setVisibility(View.GONE);
                                Forensic.setVisibility(View.GONE);
                            }
                        }

                        if (!socVisibility.equals("covert")) {
                            positionChangeTime = (int) socDuration;
                        }

                        fingerPrintType(
                                guidelineX,
                                guidelineY,
                                Fingerprint,
                                Forensic,
                                socFingerprintType,
                                socXAxis,
                                socYAxis, socFingerprintText
                        );

                        if (object.getDouble("x_axis") == -1.0 || object.getDouble("y_axis") == -1.0 ||
                                socVisibility.equals("covert") && socFingerprintType.equals("fingerprint")) {
                            if (socVisibility.equals("covert")) {
                                positionChangeTime = 10;
                            } else {
                                positionChangeTime = 3000;
                            }
                            if(durationTimer != null){
                                durationTimer.cancel();
                            }
                            durationTimer = new Timer();
                            durationTimer.scheduleAtFixedRate(
                                    new TimerTask() {
                                        @Override
                                        public void run() {
                                            socXAxis = randomAxis();
                                            socYAxis = randomAxis();

                                            Fingerprint.postDelayed(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            fingerPrintType(
                                                                    guidelineX,
                                                                    guidelineY,
                                                                    Fingerprint,
                                                                    Forensic,
                                                                    socFingerprintType,
                                                                    socXAxis,
                                                                    socYAxis,
                                                                    socFingerprintText
                                                            );
                                                        }
                                                    },
                                                    positionChangeTime
                                            );
                                        }
                                    },
                                    10,
                                    positionChangeTime
                            );
                        }
                        Fingerprint.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Fingerprint.setVisibility(View.GONE);
                                        fakeLayout.setVisibility(View.GONE);
                                        if (durationTimer != null) {
                                            durationTimer.cancel();
                                        }
                                        if (durationTimerBlinking != null) {
                                            durationTimerBlinking.cancel();
                                        }
                                        // Reset repeat count and flags
                                        preferencesEditor.putInt(startDttm, 0).apply();
                                        preferencesEditor.putBoolean("isShowing", false).apply();
                                        preferencesEditor.putLong("time-" + startDttm, 0L).apply();
                                    }
                                },
                                diffInSeconds * 1000
                        );
                    }
                } catch (Exception e) {
                    Log.i(TAG, "run: suresh " + e);
                }

            } else {
                preferencesEditor = mPreferences.edit();
                preferencesEditor.remove("fingerPrint");
                preferencesEditor.apply();
            }

        } catch (Exception e) {
          //  Log.i(TAG, "calculate: " + e.getLocalizedMessage());
          //  firestoreLog.log(TypeObjectLog.Fingerprint, "EXCEPTION", e.getLocalizedMessage());
            //FirebaseCrashlytics.getInstance().log(appName+e.getLocalizedMessage());
        }


    }


    public float randomAxis() {
        final int min = 2;
        final int max = 8;
        return (float) (new Random().nextInt((max - min) + 1) + min) / 10;
    }

    public String bgColorTrans(int transparency, String bgColour) {
        int a = (transparency / 10) * 10;
        int b = a + 10;
        String formattedBg = bgColour;
        if(bgColour.isEmpty()){
            return "#00000000";
        }
        try {
            JSONObject hex = new JSONObject("{\"100\":\"FF\",\"90\":\"E6\",\"80\":\"CC\",\"70\":\"B3\",\"60\":\"99\",\"50\":\"80\",\"40\":\"66\",\"30\":\"4D\",\"20\":\"33\",\"10\":\"1A\",\"0\":\"00\"}");
            int roundedValue = (transparency - a > b - transparency) ? b : a;
            formattedBg = "#" + hex.getString(Integer.toString(roundedValue)) + bgColour.substring(1);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "calculate: " + e.getLocalizedMessage());
        }
        return formattedBg;
    }

    public void fingerPrintType(Guideline guidelineX, Guideline guidelineY, TextView fingerPrint, ImageView forensic, String fingerprintType, Float xAxis, Float yAxis, String socFingerprintText) {
        if (fingerprintType.equalsIgnoreCase("fingerprint")) {
            if(!isForensic){
                guidelineX.setGuidelinePercent(BigDecimal.valueOf(xAxis).floatValue());
                guidelineY.setGuidelinePercent(BigDecimal.valueOf(yAxis).floatValue());
                fingerPrint.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                fingerPrint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

                fingerPrint.setSelected(false);
                fingerPrint.setHorizontallyScrolling(false);
                fingerPrint.setHorizontalFadingEdgeEnabled(false);

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fingerPrint.getLayoutParams();
                params.leftMargin = 0;
                params.topMargin = 0;
                params.rightMargin = 0;
                params.bottomMargin = 0;
                fingerPrint.setGravity(Gravity.CENTER);
            }else{
                guidelineX.setGuidelinePercent(BigDecimal.valueOf(xAxis).floatValue());
                guidelineY.setGuidelinePercent(BigDecimal.valueOf(yAxis).floatValue());
                forensic.getLayoutParams().width = 75;
                forensic.getLayoutParams().height = 75;


                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) forensic.getLayoutParams();
                params.leftMargin = 0;
                params.topMargin = 0;
                params.rightMargin = 0;
                params.bottomMargin = 0;

                positionChangeTime = 100;
                if(durationTimerBlinking != null){
                    durationTimerBlinking.cancel();
                }
                durationTimerBlinking = new Timer();
                durationTimerBlinking.scheduleAtFixedRate(
                        new TimerTask() {
                            @Override
                            public void run() {
                                Fingerprint.postDelayed(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                if(forensic.getVisibility() == View.VISIBLE) {
                                                    forensic.setVisibility(View.GONE);
                                                }else{
                                                    forensic.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        },
                                        positionChangeTime
                                );
                            }
                        },
                        1,
                        positionChangeTime
                );
            }

        } else if (fingerprintType.equalsIgnoreCase("scrollText")) {

            if(!isForensic){
                Log.w(TAG, "ScrollText Executed");
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int displayWidth = displayMetrics.widthPixels;
                float textWidth = fingerPrint.getPaint().measureText(socFingerprintText);
                float difference = displayWidth - textWidth;
//                if (difference > 0) {
                    String newText = calculateWord(fingerPrint, displayWidth, socFingerprintText);
                    fingerPrint.setText(newText);

//                }


                guidelineX.setGuidelinePercent(BigDecimal.valueOf(Float.parseFloat("0.9f")).floatValue());
                guidelineY.setGuidelinePercent(BigDecimal.valueOf(Float.parseFloat("0.1f")).floatValue());

//
                fingerPrint.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                fingerPrint.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

                fingerPrint.setSelected(true);

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fingerPrint.getLayoutParams();
                params.leftMargin = 0;
                params.topMargin = 0;
                params.rightMargin = 0;
                params.bottomMargin = 0;
                fingerPrint.setGravity(Gravity.LEFT);
            }else{
                guidelineX.setGuidelinePercent(BigDecimal.valueOf(Float.parseFloat("0.9f")).floatValue());
                guidelineY.setGuidelinePercent(BigDecimal.valueOf(Float.parseFloat("0.1f")).floatValue());

                forensic.getLayoutParams().width = 75;
                forensic.getLayoutParams().height = 75;
//                           fingerPrint.setHorizontallyScrolling(true);
//                          fingerPrint.setMovementMethod(new ScrollingMovementMethod());

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) forensic.getLayoutParams();
                params.leftMargin = 0;
                params.topMargin = 0;
                params.rightMargin = 0;
                params.bottomMargin = 0;
            }

        } else if (fingerprintType.equalsIgnoreCase("fullscreen")) {
            if(!isForensic){
                guidelineX.setGuidelinePercent(BigDecimal.valueOf(Float.parseFloat("0.5f")).floatValue());
                guidelineY.setGuidelinePercent(BigDecimal.valueOf(Float.parseFloat("0.5f")).floatValue());
                fingerPrint.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                fingerPrint.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                fingerPrint.setSelected(false);
                fingerPrint.setHorizontallyScrolling(false);
                fingerPrint.setHorizontalFadingEdgeEnabled(false);


                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fingerPrint.getLayoutParams();
                fingerPrint.setGravity(Gravity.CENTER);
            }else{
                guidelineX.setGuidelinePercent(BigDecimal.valueOf(Float.parseFloat("0.5f")).floatValue());
                guidelineY.setGuidelinePercent(BigDecimal.valueOf(Float.parseFloat("0.5f")).floatValue());
                forensic.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                forensic.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;


                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) forensic.getLayoutParams();
            }

        }

    }

    private String textToBinary(String text) {
        byte[] bytes = text.getBytes(); // Convertir el texto a un arreglo de bytes
        BigInteger bigInteger = new BigInteger(bytes); // Convertir el arreglo de bytes a un valor BigInteger
        return bigInteger.toString(2); // Convertir el BigInteger a una cadena binaria
    }

    private String textToBinaryEncrypt(String text) {
//        return text;
        byte[] bytes = text.getBytes();
        try{
            byte[] bytesEncrypted = AES256Encryption.encrypt(bytes, "SDSIPTV");
            return Base64.encodeToString(bytesEncrypted, Base64.DEFAULT);
//            BigInteger bigInteger = new BigInteger(bytesEncrypted); // Convertir el arreglo de bytes a un valor BigInteger
//            String textToReturn = bigInteger.toString(2);
//            String textDecrypted = decryptText(textToReturn);
//            Log.w(TAG, "Text Decrypted: " + textDecrypted);
//            return textToReturn;
        }catch (Exception e){
            return "";
        }
    }

    private String decryptText(String text) {
        try{
            BigInteger bigInteger = new BigInteger(text, 2);
            byte[] encryptedData = bigInteger.toByteArray();
            byte[] bytesDecrypted = AES256Encryption.decrypt(encryptedData, "SDSIPTV");
            return new String(bytesDecrypted);
        }catch (Exception e){
            return "";
        }
    }


    private String calculateWord(TextView fingerprint, float displayWidth, String text) {
        String newText = text;
        while (fingerprint.getPaint().measureText(newText) < displayWidth ) {
            newText = newText + " ";
        }
        return "     "+newText+"     ";
    }




}
