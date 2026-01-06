package com.siti.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.siti.mobile.mvvm.splash.view.SplashActivity;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Inicia tu actividad principal aquí
            Intent i = new Intent(context, SplashActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          //  context.startActivity(i);
        }
    }
}
