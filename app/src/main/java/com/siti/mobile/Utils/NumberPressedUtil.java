package com.siti.mobile.Utils;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.siti.mobile.Model.JoinData.JoinLiveStreams;
import com.siti.mobile.R;

import java.util.List;
import java.util.Objects;

public class NumberPressedUtil {

    final private Activity activity;

    public NumberPressedUtil(Activity activity) {
        this.activity =  activity;
    }

    public static boolean CHANGING_BY_NUMBER = false;

    public String numberCode = "";

    Runnable runnableFinalizedPressingNumbers = null;

    Handler handler;

    public void onNumberPressed(
            int keyCode,
            List<JoinLiveStreams> channelData,
            List<JoinLiveStreams> channelDataOriginal,
            AdapterChannels channelAdapter,
            TextView tvNumberPress,
            TextView tvNameChannelSelected,
            ChannelSelectedCallback channelSelectedCallback)
    {

        boolean foundedInOriginalChannelData;
        if(numberCode.length() >= 3) return;
        if(handler != null){
            handler.removeCallbacksAndMessages(null);
        }
        else handler = new Handler(activity.getApplicationContext().getMainLooper());

        runnableFinalizedPressingNumbers = null;
        if(runnableFinalizedPressingNumbers == null)
        {
            runnableFinalizedPressingNumbers = new Runnable() {
                @Override
                public void run() {
                    if(!Objects.equals(numberCode, ""))
                    {
                        int numberPressed = Integer.parseInt(numberCode);
                        JoinLiveStreams channel = channelExist(channelData, numberPressed);
                        JoinLiveStreams channelOnOriginalChannelData = null;
                        if(channel != null)
                        {
                            if (channel.getSource() == null || Objects.equals(channel.getSource(), "null") || channel.getSource().isEmpty()) {
                                Toast.makeText(activity, activity.getResources().getString(R.string.channel_not_subscribed), Toast.LENGTH_SHORT).show();
                                hideNumberPressed(activity, tvNumberPress, tvNameChannelSelected);
                                return;
                            }
                            CHANGING_BY_NUMBER = true;
                            if(channelAdapter != null) {
                                channelSelectedCallback.onChannelSelected(channelAdapter.getIndexOfNewChannel(numberPressed), false);
                            }else{
                                channelSelectedCallback.onChannelSelected(numberPressed, false);
                            }

                        }else{
                            channelOnOriginalChannelData = channelExist(channelDataOriginal, numberPressed);
                            if(channelOnOriginalChannelData != null)
                            {
                                if (channelOnOriginalChannelData.getSource() == null || Objects.equals(channelOnOriginalChannelData.getSource(), "null") || channelOnOriginalChannelData.getSource().isEmpty()) {
                                    Toast.makeText(activity, activity.getResources().getString(R.string.channel_not_subscribed), Toast.LENGTH_SHORT).show();
                                    hideNumberPressed(activity, tvNumberPress, tvNameChannelSelected);
                                    return;
                                }
                                channelSelectedCallback.onChannelSelected(indexChannelExits, true);
                                CHANGING_BY_NUMBER = true;
                            }else{
                                Toast.makeText(activity, activity.getResources().getString(R.string.invalid_channel), Toast.LENGTH_SHORT).show();
                                numberCode = "";
                            }
                        }
                        hideNumberPressed(activity, tvNumberPress, tvNameChannelSelected);
                    }else{
                        Log.w("NumberPressedUtil", "NumberCode is Empty");
                    }
                    numberCode = "";
                }
            };
        }

        switch (keyCode)
        {
            case KeyEvent.KEYCODE_1:
                    numberCode += "1";
                break;
            case KeyEvent.KEYCODE_2:
                    numberCode += "2";
                break;
            case KeyEvent.KEYCODE_3:
                    numberCode += "3";
                break;
            case KeyEvent.KEYCODE_4:
                    numberCode += "4";
                break;
            case KeyEvent.KEYCODE_5:
                    numberCode += "5";
                break;
            case KeyEvent.KEYCODE_6:
                    numberCode += "6";
                break;
            case KeyEvent.KEYCODE_7:
                    numberCode += "7";
                break;
            case KeyEvent.KEYCODE_8:
                    numberCode += "8";
                    break;
            case KeyEvent.KEYCODE_9:
                numberCode += "9";
                break;
            case KeyEvent.KEYCODE_0:
                if(!numberCode.isEmpty())
                numberCode += "0";
                else return;
                break;
            case KeyEvent.KEYCODE_ENTER:
                if(!Objects.equals(numberCode, ""))
                {
                    int numberPressed = Integer.parseInt(numberCode);
                    JoinLiveStreams channel = channelExist(channelData, numberPressed);
                    JoinLiveStreams channelOnOriginalChannelData = null;
                    if(channel != null)
                    {
                        if (channel.getSource() == null || channel.getSource() == "null" || channel.getSource().isEmpty()) {
                            Toast.makeText(activity, activity.getResources().getString(R.string.channel_not_subscribed), Toast.LENGTH_SHORT).show();
                            hideNumberPressed(activity, tvNumberPress, tvNameChannelSelected);
                            return;
                        }
                        CHANGING_BY_NUMBER = true;
                        if(channelAdapter != null) {
                            channelSelectedCallback.onChannelSelected(channelAdapter.getIndexOfNewChannel(numberPressed), false);
                        }else{
                            channelSelectedCallback.onChannelSelected(numberPressed, false);
                        }

                    }else{
                        channelOnOriginalChannelData = channelExist(channelDataOriginal, numberPressed);
                        if(channelOnOriginalChannelData != null)
                        {
                            if (channelOnOriginalChannelData.getSource() == null || Objects.equals(channelOnOriginalChannelData.getSource(), "null") || channelOnOriginalChannelData.getSource().isEmpty()) {
                                Toast.makeText(activity, activity.getResources().getString(R.string.channel_not_subscribed), Toast.LENGTH_SHORT).show();
                                hideNumberPressed(activity, tvNumberPress, tvNameChannelSelected);
                                return;
                            }
                            channelSelectedCallback.onChannelSelected(indexChannelExits, true);
                            CHANGING_BY_NUMBER = true;
                        }else{
                            Toast.makeText(activity, activity.getResources().getString(R.string.invalid_channel), Toast.LENGTH_SHORT).show();
                            numberCode = "";
                        }
                    }
                    hideNumberPressed(activity, tvNumberPress, tvNameChannelSelected);
                }else{
                    Log.w("NumberPressedUtil", "NumberCode is Empty");
                }
                break;

        }


        tvNumberPress.setText(numberCode);
        JoinLiveStreams channel = channelExist(channelData, Integer.parseInt(numberCode));

        if(channel != null) {
            tvNameChannelSelected.setVisibility(View.VISIBLE);
            tvNameChannelSelected.setText(channel.getChannel_name());
        }else{
            JoinLiveStreams channelOnOriginal = channelExist(channelDataOriginal, Integer.parseInt(numberCode));
            if(channelOnOriginal != null){
                tvNameChannelSelected.setVisibility(View.VISIBLE);
                tvNameChannelSelected.setText(channelOnOriginal.getChannel_name());
            }else{
                tvNameChannelSelected.setText("Unknown");
            }

        }
        if(numberCode.length() >= 1)
        {
            showNumberPressed(activity, tvNumberPress, tvNameChannelSelected);
        }
    }

    public void cancelRunnable(){
        if(handler != null && runnableFinalizedPressingNumbers != null) {
            handler.removeCallbacks(runnableFinalizedPressingNumbers);
            runnableFinalizedPressingNumbers = null;
        }
        numberCode = "";
    }

    private void showNumberPressed(Activity activity, TextView tvNumberPress, TextView tvChannelName)
    {
        Animation anim = AnimationUtils.loadAnimation(activity, R.anim.scale_in_number_press);
        tvNumberPress.startAnimation(anim);
        tvChannelName.startAnimation(anim);
        anim.setFillAfter(true);
        handler.postDelayed(runnableFinalizedPressingNumbers, 5000);
        if(tvNumberPress.getVisibility() != View.VISIBLE){
            tvNumberPress.setVisibility(View.VISIBLE);
            tvChannelName.setVisibility(View.VISIBLE);
        }
    }

    private void hideNumberPressed(Activity activity, TextView tvNumberPress, TextView tvChannelName)
    {
        Animation anim = AnimationUtils.loadAnimation(activity, R.anim.scale_out_number_press);
        tvNumberPress.startAnimation(anim);
        tvChannelName.startAnimation(anim);
        tvChannelName.setVisibility(View.GONE);
        tvNumberPress.setAlpha(1f);
        numberCode = "";
        anim.setFillAfter(true);
        runnableFinalizedPressingNumbers = null;
    }

    int indexChannelExits = 0;

    private JoinLiveStreams channelExist(List<JoinLiveStreams> channelData, int channelNumber)
    {
        indexChannelExits = 0;
        for(JoinLiveStreams channel : channelData)
        {
            if(channel.getChannel_no() == channelNumber) return channel;
            indexChannelExits++;
        }
        return null;
    }

}

