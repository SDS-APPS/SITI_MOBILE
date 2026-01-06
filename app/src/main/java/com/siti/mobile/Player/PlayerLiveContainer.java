package com.siti.mobile.Player;


import androidx.media3.exoplayer.ExoPlayer;

public class PlayerLiveContainer {
   // public static String testUrl = "https://firebasestorage.googleapis.com/v0/b/storage-http-url-streas.appspot.com/o/http_mp4%2Fhttp_mp4_test.mp4?alt=media&token=8f20fc10-1232-4409-9aa1-1abf47c8f5f0";
    public static String nullUrl = "null";
    public static boolean inTransation = false;
    public static ExoPlayer player;
    public static String lastPlayedUrl;
    public static int lastPlayedUrlDrm;
    public static int counterReconnectExo = 0;
    public static boolean isReady = false;

    public static void releasePlayer(){
        if(player != null){
            player.release();
            player = null;
        }
    }
}
