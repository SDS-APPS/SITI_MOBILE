package com.siti.mobile.mvvm.loading.view;

import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_FIRST_TIME;
import static com.siti.mobile.Utils.KeyPreferencesKt.sharedPrefFile;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.siti.mobile.mvvm.common.data.DBEPGHelper;
import com.siti.mobile.mvvm.fullscreen.view.PlayerScreen;
import com.siti.mobile.mvvm.preview.view.PreviewScreen;
import com.siti.mobile.network.main.UpdateLocalDB;
import com.siti.mobile.R;
import com.siti.mobile.Utils.DBHelper;
import com.siti.mobile.Utils.DBHelperKt;
import com.siti.mobile.Utils.IncreaseCalls;

public class DataLoadingActivity extends AppCompatActivity implements IncreaseCalls {

    private SharedPreferences mPreferences;
    private int allCalls = 0;
    private String TAG = "DataLoadingActivity";

    private DBHelper dbHelper;
    private DBHelperKt dbHelperKt;
    private DBEPGHelper dbepgHelper;

    private TextView textViewCurrentProgress;
    private ProgressBar progressBarCurrentProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_loading);
        dbHelper = new DBHelper(this);
        dbHelperKt = new DBHelperKt(this);
        textViewCurrentProgress = findViewById(R.id.tvCurrentProgress);
        progressBarCurrentProgress = findViewById(R.id.progressBarLoadingSplash);
        dbepgHelper = new DBEPGHelper(this);
        toFullScreen();
        mPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE);
        if(mPreferences.getBoolean(KEY_FIRST_TIME, true)){
      //      progressBarCurrentProgress.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#134A9B")));
            mPreferences.edit().putBoolean(KEY_FIRST_TIME, false).apply();
        }else{
         //   progressBarCurrentProgress.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#134A9B")));
        }
        updateLocalDB();
    }

    private void toFullScreen(){
        getSupportActionBar().hide();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    UpdateLocalDB helper;
    void updateLocalDB(){
        helper = new UpdateLocalDB(this, dbHelperKt, dbHelper);
        helper.updateLocalDB(this);
    }

    void closeDb(){
        dbepgHelper.closeDb();
        dbHelperKt.closeDb();
        dbHelper.closeDb();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onPause() {
        if(Util.SDK_INT < 23) {
            closeDb();
        }
        super.onPause();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onStop() {
        if(Util.SDK_INT >= 23){
            closeDb();
        }
        super.onStop();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void call(String message) {
        int totalCalls = helper.getTotalTalls();
        allCalls++;
        progressBarCurrentProgress.setMax(totalCalls * 10);
        progressBarCurrentProgress.setProgress((allCalls) * 10);
        if(totalCalls > 0){
            progressBarCurrentProgress.setVisibility(View.VISIBLE);
            textViewCurrentProgress.setText(((int)((allCalls / (double)totalCalls) * 100)) + " %");
            textViewCurrentProgress.setVisibility(View.VISIBLE);
        }
        if (allCalls >= totalCalls) {
            allCalls = 0;
//            if(mPreferences.getString(KEY_BOOTUP_ACTIVITY, VALUE_HOME_ACTIVITY).equals(VALUE_HOME_ACTIVITY)){
            if(false){
                startActivity(new Intent(this, PreviewScreen.class));
                overridePendingTransition(0,0);
                finish();
            }else{
                startActivity(new Intent(this, PlayerScreen.class));
                overridePendingTransition(0,0);
                finish();
            }
        }
    }

}