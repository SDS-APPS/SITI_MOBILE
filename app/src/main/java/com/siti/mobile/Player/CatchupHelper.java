package com.siti.mobile.Player;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

@UnstableApi
public class CatchupHelper {

    private String TAG = "CatchupHelper";

    private PlayerManager playerManager;
    private ExoPlayer player;
    private MediaPlayer mediaPlayer;
    private FrameLayout containerForward;
    private FrameLayout containerRewind;
    private Long lastAdvance;
    private Long lastCalledForwardCurrentTime;
    private TextView tvRewindVelocity;
    private TextView tvForwardVelocity;

    public static boolean btnOkPressed;
    public static int actualKeyCode;
    public static int secsForward = 30 * 1000;
    public static int velocity = 2;
    public static boolean isInLoop = false;
    public static int firstRewind = 3000;
    public static int counter  = 2;

    public void onPressForward(int keyCodeReceived, PlayerView playerView){
//        btnOkPressed = false;
//        if(!isInLoop || actualKeyCode != keyCodeReceived){
//
//            actualKeyCode = keyCodeReceived;
//            if(actualKeyCode != 0){
//                new WhileKtPressed().loopForward(actualKeyCode, new LoopForward() {
//                    @Override
//                    public void method() {
//                        forwardOrRewind(actualKeyCode, secsForward, velocity);
//                        playerView.showController();
//                    }
//                });
//            }
//
//        }else{
//            if(velocity == 2){
//                velocity = 4;
//                secsForward = 60 * 1000;
//            }else if(velocity == 4){
//                velocity = 8;
//                secsForward = 120 * 1000;
//            }
//        }
    }

    public CatchupHelper(
            PlayerManager playerManager,
            ExoPlayer player,
            MediaPlayer mediaPlayer,
            FrameLayout containerForward,
            FrameLayout containerRewind,
            TextView tvRewindVelocity,
            TextView tvForwardVelocity){
        this.playerManager = playerManager;
        this.player = player;
        this.mediaPlayer = mediaPlayer;
        this.containerForward = containerForward;
        this.containerRewind = containerRewind;
        this.tvRewindVelocity = tvRewindVelocity;
        this.tvForwardVelocity = tvForwardVelocity;
    }
    public void onOkButtonPressed(
                                  FrameLayout containerPlayFR,
                                  FrameLayout containerPauseFR){
        btnOkPressed = true;
        showContainerPauseFR(containerForward, containerRewind, containerPlayFR, containerPauseFR);
        if(player != null && player.getDuration() > 0 && !isInLoop){
            if(player.isPlaying()){
                player.setPlayWhenReady(false);
            }
            else{
                player.setPlayWhenReady(true);
            }
        }
    }

    private void showContainerPauseFR(FrameLayout containerForward,
                                      FrameLayout containerRewind,
                                      FrameLayout containerPlayFR,
                                      FrameLayout containerPauseFR){
        containerForward.setVisibility(View.GONE);
        containerRewind.setVisibility(View.GONE);
        if(player.isPlaying()) {
            containerPlayFR.setVisibility(View.GONE);
            containerPauseFR.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    containerPauseFR.setVisibility(View.GONE);
                }
            }, 3000);
        }else{
            containerPauseFR.setVisibility(View.GONE);
            containerPlayFR.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    containerPlayFR.setVisibility(View.GONE);
                }
            }, 3000);
        }

    }

    private void forwardOrRewind(int keyCode, int secsForward, int velocity){
        lastAdvance = System.currentTimeMillis();
        if(keyCode != 0){
            lastCalledForwardCurrentTime = System.currentTimeMillis();
            int newValue = 0;
            if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
                newValue = secsForward * -1;
                tvRewindVelocity.setText(velocity+"x");
                containerForward.setVisibility(View.GONE);
                containerRewind.setVisibility(View.VISIBLE);
            }
            else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                newValue = secsForward;
                tvForwardVelocity.setText(velocity+"x");
                containerForward.setVisibility(View.VISIBLE);
                containerRewind.setVisibility(View.GONE);
            }
            if(playerManager.getCurrentPlayer() == PlayerType.EXOPLAYER){
                long newPos = player.getCurrentPosition() + newValue;
                if(newPos > player.getDuration()){
                    btnOkPressed = true;
                }
                Log.w(TAG, "<<<FORWARD OR REWIND NEW POS>>>: " + newPos);
                Log.w(TAG, "<<<FORWARD OR REWIND CURRENT POS>>>: " + player.getCurrentPosition());
                player.seekTo(newPos);
            }else{
                long newPos = mediaPlayer.getCurrentPosition() + newValue;
                if(newPos > mediaPlayer.getDuration()){
                    btnOkPressed = true;
                }
                Log.w(TAG, "<<<FORWARD OR REWIND NEW POS>>>: " + newPos);
                Log.w(TAG, "<<<FORWARD OR REWIND CURRENT POS>>>: " + player.getCurrentPosition());
                mediaPlayer.seekTo((int)newPos);
            }

//            new WhileKtPressed().loopHideProgress(lastCalledForwardCurrentTime, containerForward, containerRewind);
        }
    }
}
