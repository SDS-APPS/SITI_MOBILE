package com.siti.mobile.Utils;

import static com.siti.mobile.Utils.KeyPreferencesKt.sharedPrefFile;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import com.siti.mobile.mvvm.fullscreen.view.PlayerScreen;
import com.siti.mobile.mvvm.splash.view.SplashActivity;
import com.siti.mobile.mvvm.config.helpers.AnalyticsEventsHelper;
import com.siti.mobile.mvvm.config.helpers.AuthHelper;
import com.siti.mobile.Interface.ApiInterface;
import com.siti.mobile.Model.RetroFit.Login;
import com.siti.mobile.Player.PlayerManager;
import com.siti.mobile.network.keys.NetworkPackageKeys;

import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UtilLoginJava {

    final Activity activity;

    public UtilLoginJava(Activity activity){
        this.activity = activity;
    }

    final String TAG = "UtilLoginJava";
    private int MAGICAL_NUMBER = 123456;

    private void restartApp() {
        Intent intent = new Intent(activity, SplashActivity.class);
        int mPendingIntentId = MAGICAL_NUMBER;
        PendingIntent mPendingIntent = PendingIntent.getActivity(activity, mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager mgr = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    private void restarApp2(){
        Context ctx = activity.getApplicationContext();
        PackageManager pm = ctx.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(ctx.getPackageName());
        Intent mainIntent = Intent.makeRestartActivityTask(intent.getComponent());
        ctx.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }


    @OptIn(markerClass = UnstableApi.class)
    public void login() {

        SharedPreferences preferences = activity.getApplicationContext().getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String macSelected = preferences.getString("mac", "");
        String password = preferences.getString("password", "");
        String serverIP = preferences.getString("serverIP","");
        JSONObject paramObject = new JSONObject();

        preferences.edit().putString("LAST_PLAYED_URL", "null").apply();
        PlayerScreen.Companion.setActualUrl("");;





        try {
            paramObject.put("username", username);
            paramObject.put("mac", macSelected);
            paramObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiInterface apiInterface = RetrofitClient.getClient( serverIP).create(ApiInterface.class);
        Call<Login> call = apiInterface.getLogin(paramObject.toString());

        call.enqueue(new Callback<Login>() {
            @Override
            public void onResponse(Call<com.siti.mobile.Model.RetroFit.Login> call, Response<Login> response) {
                if (response.code() == 200) {


                    SharedPreferences.Editor preferencesEditor = preferences.edit();
                    preferencesEditor.putInt(NetworkPackageKeys.LIVE_TV, 0);
                    preferencesEditor.putInt(NetworkPackageKeys.VOD, 0);
                    preferencesEditor.putInt(NetworkPackageKeys.SOD, 0);
                    preferencesEditor.putInt(NetworkPackageKeys.MOD, 0);
                    preferencesEditor.putBoolean(NetworkPackageKeys.FIRST_LOAD, true).apply();
                    preferencesEditor.putString("LAST_PLAYED_URL", "");
                    preferencesEditor.apply();

                    com.siti.mobile.Model.RetroFit.Login obj = response.body();
                    Log.i(TAG, "onResponse: " + obj.getData().getAreaCode());

                    preferencesEditor.putString("firstlogin", "true");
                    preferencesEditor.putString("AuthCode", "loggedin");
                    preferencesEditor.putString("mac", macSelected);

                    preferencesEditor.putString("username", obj.getData().getUsername());
                    preferencesEditor.putString("password", password);
                    preferencesEditor.putString("name", obj.getData().getName());
                    preferencesEditor.putInt("adminID", obj.getData().getAdminId());
                    preferencesEditor.putString("expDate", obj.getData().getExpDate());
                    preferencesEditor.putString("authToken", obj.getData().getAuth_token());
                    preferencesEditor.putString("areaCode", obj.getData().getAreaCode());
                    preferencesEditor.putInt("userId", obj.getData().getId());
                    preferencesEditor.putString("LAST_PLAYED_URL", "");

                    preferencesEditor.apply();
//                    Intent intent = new Intent(activity, SplashActivity.class);
                    new AuthHelper().signInAnonymously();
                    new AnalyticsEventsHelper().eventLogIn();
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PlayerManager.Companion.setLastPlayedUrl("");
//                    activity.startActivity(intent);
//                    activity.finish();
                    restarApp2();
                } else {
                    Log.i(TAG, "onResponse: " + response.code());
                }

            }

            @Override
            public void onFailure(Call<com.siti.mobile.Model.RetroFit.Login> call, Throwable t) {
                Log.i(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}
