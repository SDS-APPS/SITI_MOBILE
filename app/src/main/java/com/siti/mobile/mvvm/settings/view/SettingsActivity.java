package com.siti.mobile.mvvm.settings.view;

import static android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS;
import static android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS;
import static com.siti.mobile.Utils.KeyPreferencesKt.DEFAULT_BUFFER_PLAYBACK;
import static com.siti.mobile.Utils.KeyPreferencesKt.DEFAULT_BUFFER_PLAYBACK_AFTER_REBUFFER;
import static com.siti.mobile.Utils.KeyPreferencesKt.DEFAULT_CATEGORY_VIEW_ENABLED;
import static com.siti.mobile.Utils.KeyPreferencesKt.DEFAULT_MAX_BUFFER;
import static com.siti.mobile.Utils.KeyPreferencesKt.DEFAULT_MIN_BUFFER;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_ACTIVE_CONNECTIONS;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_AUDIT_MODE;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_BOOTUP_ACTIVITY;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MULTICAST;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_UNICAST;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_BUFFER_FOR_PLAYBACK_MS_MULTICAST;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_BUFFER_FOR_PLAYBACK_MS_UNICAST;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_EXP_DATE;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_FRAME_CHANGE;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_HW_SYNC;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_LANGUAGE;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_LEANBACK_ENABLED;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_LOW_PROFILE;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_MAX_BUFFER_MS_MULTICAST;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_MAX_BUFFER_MS_UNICAST;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_MAX_CONNECTIONS;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_MIN_BUFFER_MS_MULTICAST;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_MIN_BUFFER_MS_UNICAST;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_PLAY_WITH_DRM_SOURCE;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_PREFERENCES_IS_CATEGORY_VIEW_ENABLED;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_PASSWORD;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_USERNAME;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_USER_ID;
import static com.siti.mobile.Utils.KeyPreferencesKt.PREFERENCES_STRING_DEFAULT_VALUE;
import static com.siti.mobile.Utils.KeyPreferencesKt.VALUE_FULL_SCREEN_ACTIVITY;
import static com.siti.mobile.Utils.KeyPreferencesKt.VALUE_HOME_ACTIVITY;
import static com.siti.mobile.Utils.KeyPreferencesKt.VALUE_LANGUAGE_AR;
import static com.siti.mobile.Utils.KeyPreferencesKt.VALUE_LANGUAGE_EN;
import static com.siti.mobile.Utils.KeyPreferencesKt.sharedPrefFile;
import static com.siti.mobile.Utils.UtilKotlinKt.getAdvertismentByPosition;
import static com.siti.mobile.Utils.UtilKotlinKt.setCurrentAdvertismentScreen;
import static com.siti.mobile.Utils.UtilKotlinKt.startLoopAdvertisment;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Guideline;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;

import com.siti.mobile.mvvm.common.SliderAdAdapter;
import com.siti.mobile.mvvm.fullscreen.view.PlayerScreen;
import com.siti.mobile.mvvm.preview.domain.PreviewDomain;
import com.siti.mobile.mvvm.preview.view.PreviewScreen;
import com.siti.mobile.mvvm.splash.view.SplashActivity;
import com.siti.mobile.mvvm.config.helpers.AuthHelper;
import com.siti.mobile.Model.advertisment.AdvertismentModel;
import com.siti.mobile.Player.PlayerChangeType;
import com.siti.mobile.Player.PlayerLiveContainer;
import com.siti.mobile.Player.PlayerManager;
import com.siti.mobile.Player.PlayerType;
import com.siti.mobile.Player.StreamType;
import com.siti.mobile.R;
import com.siti.mobile.Utils.AdvertismentScreen;
import com.siti.mobile.Utils.CurrentTimeContainer;
import com.siti.mobile.Utils.Helper;
import com.siti.mobile.Utils.SocketHelper;
import com.siti.mobile.Utils.SocketSingleton;
import com.siti.mobile.mvvm.login.view.LoginActivity;
import com.siti.mobile.network.keys.NetworkPackageKeys;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@UnstableApi
public class SettingsActivity extends SocketHelper implements View.OnClickListener {

    String TAG = "SettingsActivity";
    private SharedPreferences mPreferences;
    SharedPreferences.Editor preferencesEditor;
    String username, password, exp_date, max_connections, active_cons;
    LinearLayout Logout, btnInfo, fake_layer, moreSettings;
    TextView Fingerprint;
    Guideline guidelineX, guidelineY;
    AuthHelper authHelper;

    private PlayerManager playerManager;
    private SliderView sliderAdviewLeft, sliderAdViewRight;
    
    private FrameLayout containerInfo;
    private FrameLayout containerPlayer;
    private FrameLayout containerChannelsView;
    private FrameLayout containerMoreSettings;
    private FrameLayout containerEngineering;
    private FrameLayout containerEngineeringPassInput;
    private FrameLayout containerSelectTypeEngineering;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_settings_new);
        getSupportActionBar().hide();
        authHelper = new AuthHelper();
        initViews();

        Logout.setOnClickListener(this);
        btnInfo.setOnClickListener(this);

        playerManager = new PlayerManager(this);
        settingsButtonsContainer = findViewById(R.id.settingsButtonsContainer);
    }

    FrameLayout settingsButtonsContainer;

    @Override
    public void onBackPressed() {
        
        if(containerInfo.getVisibility() == View.VISIBLE){
            containerInfo.setVisibility(View.GONE);
            settingsButtonsContainer.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        }else if(containerPlayer.getVisibility() == View.VISIBLE){
            containerPlayer.setVisibility(View.GONE);
            settingsButtonsContainer.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        }else if(containerChannelsView.getVisibility() == View.VISIBLE){
            containerChannelsView.setVisibility(View.GONE);
            settingsButtonsContainer.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        }else if(containerMoreSettings.getVisibility() == View.VISIBLE){
            containerMoreSettings.setVisibility(View.GONE);
            settingsButtonsContainer.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        }else if(containerEngineering.getVisibility() == View.VISIBLE){
            containerEngineering.setVisibility(View.GONE);
            settingsButtonsContainer.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        }else if(containerEngineeringPassInput.getVisibility() == View.VISIBLE){
            containerEngineeringPassInput.setVisibility(View.GONE);
            settingsButtonsContainer.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        }else if(containerSelectTypeEngineering.getVisibility() == View.VISIBLE){
            containerSelectTypeEngineering.setVisibility(View.GONE);
            settingsButtonsContainer.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        }else{
            startActivity(new Intent(this, PlayerScreen.class));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT < 23) {
         //   setCurrentAdvertismentScreen(AdvertismentScreen.NONE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 23) {
       //     setCurrentAdvertismentScreen(AdvertismentScreen.NONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<AdvertismentModel> advertismentModelFiltered = getAdvertismentByPosition(3, PreviewDomain.Companion.getAdvertisements());
        setCurrentAdvertismentScreen(AdvertismentScreen.SETTINGS);
        SliderAdAdapter sliderAdAdapter = new SliderAdAdapter(this);
        sliderAdAdapter.renewItems(advertismentModelFiltered);
        sliderAdviewLeft.setSliderAdapter(sliderAdAdapter);
        sliderAdViewRight.setSliderAdapter(sliderAdAdapter);
        startLoopAdvertisment(sliderAdviewLeft, advertismentModelFiltered, AdvertismentScreen.SETTINGS);
        startLoopAdvertisment(sliderAdViewRight, advertismentModelFiltered, AdvertismentScreen.SETTINGS);
        btnInfo.requestFocus();
    }

    private void initViews() {
        Fingerprint = findViewById(R.id.fingerprintTextView);
        guidelineX = findViewById(R.id.fingerprintGuidelineX);
        guidelineY = findViewById(R.id.fingerprintGuidelineY);
        fake_layer = findViewById(R.id.fingerprintFakeLaker);
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        Logout = findViewById(R.id.settingsBtnLogout);
        btnInfo = findViewById(R.id.settingsBtnInfo);
        moreSettings = findViewById(R.id.settingsBtnMoreSettings);

        username = mPreferences.getString(KEY_USERNAME, PREFERENCES_STRING_DEFAULT_VALUE);
        password = mPreferences.getString(KEY_PASSWORD, PREFERENCES_STRING_DEFAULT_VALUE);
        exp_date = mPreferences.getString(KEY_EXP_DATE, PREFERENCES_STRING_DEFAULT_VALUE);
        max_connections = mPreferences.getString(KEY_MAX_CONNECTIONS, PREFERENCES_STRING_DEFAULT_VALUE);
        active_cons = mPreferences.getString(KEY_ACTIVE_CONNECTIONS, PREFERENCES_STRING_DEFAULT_VALUE);
        sliderAdviewLeft = findViewById(R.id.imageSlider);
        sliderAdViewRight = findViewById(R.id.imageSlider2);

        containerInfo = findViewById(R.id.containerInfoDialog);
        containerPlayer = findViewById(R.id.containerSelectPlayer);
        containerChannelsView = findViewById(R.id.containerDialogChannelsView);
        containerMoreSettings = findViewById(R.id.containerMoreSettings);
        containerEngineering = findViewById(R.id.containerDialogEngineering);
        containerEngineeringPassInput = findViewById(R.id.containerEngineeringPassInput);
        containerSelectTypeEngineering = findViewById(R.id.containerSelectTypeEngineering);

        moreSettings.setOnClickListener(this);
//        btnChannelsView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(true){
//                    showDialogChannelViews();
//                }
//            }
//        });

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        fingerprintVisibilityListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView tvCurrentDate = findViewById(R.id.tvCurrentDate);
        TextView tvCurrentHour = findViewById(R.id.tvCurrentHour);
        tvCurrentDate.setText(CurrentTimeContainer.INSTANCE.getDate());
        tvCurrentHour.setText(CurrentTimeContainer.INSTANCE.getHour());
        Fingerprint.bringToFront();
        super.socketConnection(this, SocketSingleton.getInstance(SettingsActivity.this, mPreferences), guidelineX, guidelineY, Fingerprint, findViewById(R.id.fingerprintForensic), PreviewScreen.Companion.getLastChannelId(), fake_layer, tvCurrentDate, tvCurrentHour, null);

    }

    private void showDialogSelectPlayer() {
        containerPlayer.setVisibility(View.VISIBLE);
        settingsButtonsContainer.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        RadioButton unicastMediaPlayer = findViewById(R.id.unicastMediaPlayer);
        RadioButton unicastExoPlayer = findViewById(R.id.unicastExoPlayer);
        RadioButton multicastMediaPlayer = findViewById(R.id.multicastMediaPlayer);
        RadioButton multicastExoPlayer = findViewById(R.id.multicastExoPlayer);
        CheckBox hw_sync_enabled = findViewById(R.id.hwAvSync);

        PlayerType playerTypeUnicast = playerManager.getPlayerByTypeCast(StreamType.UNICAST);
        PlayerType playerTypeMulticast = playerManager.getPlayerByTypeCast(StreamType.MULTICAST);

        if (playerTypeUnicast == PlayerType.EXOPLAYER) unicastExoPlayer.setChecked(true);
        else unicastMediaPlayer.setChecked(true);

        if (playerTypeMulticast == PlayerType.EXOPLAYER) multicastExoPlayer.setChecked(true);
        else multicastMediaPlayer.setChecked(true);

        hw_sync_enabled.setChecked(mPreferences.getBoolean(KEY_HW_SYNC, true));

        Button btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (unicastMediaPlayer.isChecked())
                    savePlayerPreferences(StreamType.UNICAST, PlayerType.MEDIAPLAYER);
                else if (unicastExoPlayer.isChecked())
                    savePlayerPreferences(StreamType.UNICAST, PlayerType.EXOPLAYER);
                if (multicastMediaPlayer.isChecked())
                    savePlayerPreferences(StreamType.MULTICAST, PlayerType.MEDIAPLAYER);
                else if (multicastExoPlayer.isChecked())
                    savePlayerPreferences(StreamType.MULTICAST, PlayerType.EXOPLAYER);
                mPreferences.edit().putBoolean(KEY_HW_SYNC, hw_sync_enabled.isChecked()).apply();
                containerPlayer.setVisibility(View.GONE);
            }
        });

    }

//    private void showDialogChannelViews() {
//        new FirestoreLog().getChannelsViewed(this, new Function1<List<ChannelTimeModel>, Unit>() {
//            @Override
//            public Unit invoke(List<ChannelTimeModel> channelTimeModels) {
//                if(channelTimeModels.isEmpty()){
//                    Toast.makeText(SettingsActivity.this, "Nothing to show", Toast.LENGTH_SHORT).show();
//                    return null;
//                }
//
//                containerChannelsView.setVisibility(View.VISIBLE);
//                settingsButtonsContainer.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
//
//
//                ListView listView = findViewById(R.id.lvChannelsView);
//                ChannelsViewedAdapter adapter = new ChannelsViewedAdapter(SettingsActivity.this, channelTimeModels);
//                listView.setAdapter(adapter);
//
//                return null;
//            }
//        });
//    }

    private void savePlayerPreferences(StreamType streamType, PlayerType playerType) {
        PlayerLiveContainer.player = null;
        playerManager.savePlayerByTypeCast(streamType, playerType);
    }

    private void requestPasswordEngineering() {
        containerEngineeringPassInput.setVisibility(View.VISIBLE);
        settingsButtonsContainer.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        TextInputEditText et = findViewById(R.id.etPassword);
        LinearLayout btnGo = findViewById(R.id.btnGo);

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Objects.requireNonNull(et.getText()).toString().equals("1111")) {
                    et.setError("Wrong Password");
                }else{
                    containerEngineeringPassInput.setVisibility(View.GONE);
                    showDialogEnginering();
                }
            }
        });
    }

    private void showDialogEnginering() {
        containerSelectTypeEngineering.setVisibility(View.VISIBLE);
        settingsButtonsContainer.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        LinearLayout btnUnicast = findViewById(R.id.btnUnicast);
        LinearLayout btnMulticast = findViewById(R.id.btnMulticast);
        LinearLayout btnSelectStream = findViewById(R.id.btnSelectStream);
        LinearLayout btnSelectAuditMode = findViewById(R.id.btnAuditMode);

        boolean playWithDrm = mPreferences.getBoolean(KEY_PLAY_WITH_DRM_SOURCE, false);


        btnUnicast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                containerSelectTypeEngineering.setVisibility(View.GONE);
                showDialogBufferConfig(true);
            }
        });

        btnMulticast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                containerSelectTypeEngineering.setVisibility(View.GONE);
                showDialogBufferConfig(false);
            }
        });

        btnSelectStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                containerSelectTypeEngineering.setVisibility(View.GONE);
                showDialogSelectStream(playWithDrm);
            }
        });

        btnSelectAuditMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogAuditMode();
            }
        });
    }

    private void showDialogAuditMode(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_audit_mode, null);
        alertDialog.setView(view);
        AlertDialog dialog = alertDialog.create();
        SwitchMaterial switchAuditMode = view.findViewById(R.id.switchAuditMode);
        SwitchMaterial switchLowProfile = view.findViewById(R.id.switchLowProfile);
        LinearLayout btnSave = view.findViewById(R.id.btnSave);

        boolean isInAuditMode = mPreferences.getBoolean(KEY_AUDIT_MODE, false);
        switchAuditMode.setChecked(isInAuditMode);

        boolean isLowProfileEnabled = mPreferences.getBoolean(KEY_LOW_PROFILE, false);
        switchLowProfile.setChecked(isLowProfileEnabled);

        switchLowProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchLowProfile.isChecked()) switchAuditMode.setChecked(true);
            }
        });
        
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreferences.edit().putBoolean(KEY_AUDIT_MODE, switchAuditMode.isChecked()).apply();
                mPreferences.edit().putBoolean(KEY_LOW_PROFILE, switchLowProfile.isChecked()).apply();
                if(switchAuditMode.isChecked()) {
                    mPreferences.edit().putBoolean(KEY_AUDIT_MODE, false).apply();
                }
                dialog.cancel();
                dialog.hide();
            }
        });
        dialog.show();
    }

    private void showDialogSelectStream(boolean isDrmEnabled){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_select_stream, null);
        LinearLayout btnSave = view.findViewById(R.id.btnSave);
        alertDialog.setView(view);
        AlertDialog dialog = alertDialog.create();

        List<String> providerName = new ArrayList<>();
        providerName.add(getResources().getString(R.string.unicast));
        providerName.add(getResources().getString(R.string.multicast));

        int pos;
        if(isDrmEnabled) pos = 1;
        else pos = 0;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.dropdown_menu_popup_item,
                providerName);
        AutoCompleteTextView editTextFilledExposedDropdown = view.findViewById(R.id.filled_exposed_dropdown);
        editTextFilledExposedDropdown.setAdapter(adapter);
        editTextFilledExposedDropdown.setText(providerName.get(pos), false);

        editTextFilledExposedDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextFilledExposedDropdown.showDropDown();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String streamSelected = editTextFilledExposedDropdown.getText().toString();
                mPreferences.edit()
                        .putBoolean(KEY_PLAY_WITH_DRM_SOURCE,
                                streamSelected.equals(getResources().getString(R.string.multicast)))
                        .apply();
                dialog.hide();
                dialog.cancel();
                startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
                finish();
            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void showDialogBufferConfig(boolean isUnicast) {
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
//        View view = getLayoutInflater().inflate(R.layout.engineering_menu, null);
        
        containerEngineering.setVisibility(View.VISIBLE);
        settingsButtonsContainer.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextInputEditText etMinBuffer = findViewById(R.id.etMinBuffer);
        TextInputEditText etMaxBuffer = findViewById(R.id.etMaxBuffer);
        TextInputEditText etBufferForPlayback = findViewById(R.id.etBufferForPlayback);
        TextInputEditText etBufferForPlaybackAfterRebuffer = findViewById(R.id.etBufferForPlaybackAfterRebuffer);
        LinearLayout btnSave = findViewById(R.id.btnSave);
        ImageView btnRestore = findViewById(R.id.btnRestore);
        

        if(isUnicast) {
            tvTitle.setText(tvTitle.getText().toString() + " (UNICAST)");
        }else{
            tvTitle.setText(tvTitle.getText().toString() + " (MULTICAST)");
        }

        int currentMinBuffer, currentMaxBuffer, currentBufferForPlaybackMs, currentBufferForPlaybackMsAfterRebuffer;
        if(isUnicast){
            currentMinBuffer = mPreferences.getInt(KEY_MIN_BUFFER_MS_UNICAST, DEFAULT_MIN_BUFFER);
            currentMaxBuffer = mPreferences.getInt(KEY_MAX_BUFFER_MS_UNICAST, DEFAULT_MAX_BUFFER);
            currentBufferForPlaybackMs = mPreferences.getInt(KEY_BUFFER_FOR_PLAYBACK_MS_UNICAST, DEFAULT_BUFFER_PLAYBACK);
            currentBufferForPlaybackMsAfterRebuffer = mPreferences.getInt(KEY_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_UNICAST, DEFAULT_BUFFER_PLAYBACK_AFTER_REBUFFER);
        }else{
            currentMinBuffer = mPreferences.getInt(KEY_MIN_BUFFER_MS_MULTICAST, DEFAULT_MIN_BUFFER);
            currentMaxBuffer = mPreferences.getInt(KEY_MAX_BUFFER_MS_MULTICAST, DEFAULT_MAX_BUFFER);
            currentBufferForPlaybackMs = mPreferences.getInt(KEY_BUFFER_FOR_PLAYBACK_MS_MULTICAST, DEFAULT_BUFFER_PLAYBACK);
            currentBufferForPlaybackMsAfterRebuffer = mPreferences.getInt(KEY_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MULTICAST, DEFAULT_BUFFER_PLAYBACK_AFTER_REBUFFER);
        }


        etMinBuffer.setText(""+currentMinBuffer);
        etMaxBuffer.setText(""+currentMaxBuffer);
        etBufferForPlayback.setText(""+currentBufferForPlaybackMs);
        etBufferForPlaybackAfterRebuffer.setText(""+currentBufferForPlaybackMsAfterRebuffer);

        btnRestore.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) btnRestore.setImageResource(R.drawable.ic_restore_selected);
                else btnRestore.setImageResource(R.drawable.ic_restore);
            }
        });

        btnRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMinBuffer.setText(""+DEFAULT_MIN_BUFFER);
                etMaxBuffer.setText(""+DEFAULT_MAX_BUFFER);
                etBufferForPlayback.setText(""+DEFAULT_BUFFER_PLAYBACK);
                etBufferForPlaybackAfterRebuffer.setText(""+DEFAULT_BUFFER_PLAYBACK_AFTER_REBUFFER);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!confirmField(etMinBuffer) || !confirmField(etMaxBuffer) || !confirmField(etBufferForPlayback) || !confirmField(etBufferForPlaybackAfterRebuffer)) {
                    return;
                }
                int minBufferMs,maxBufferMs,bufferForPlayback,bufferForPlaybackAfterRebuffer;
                minBufferMs = Integer.parseInt(etMinBuffer.getText().toString());
                maxBufferMs = Integer.parseInt(etMaxBuffer.getText().toString());
                bufferForPlayback = Integer.parseInt(etBufferForPlayback.getText().toString());
                bufferForPlaybackAfterRebuffer = Integer.parseInt(etBufferForPlaybackAfterRebuffer.getText().toString());
                if(minBufferMs< 0) {
                    etMinBuffer.setError("Min Buffer must be greater or equals than 0");
                    return;
                }
                if(maxBufferMs < minBufferMs) {
                    etMaxBuffer.setError("Max buffer must be greater or equals than min buffer");
                    return;
                }
                if(bufferForPlayback > minBufferMs) {
                    etBufferForPlayback.setError("Buffer for playback must be minor or equals than min buffer");
                    return;
                }
                if(bufferForPlaybackAfterRebuffer < bufferForPlayback) {
                    etBufferForPlaybackAfterRebuffer.setError("Must be greater than or equal to Buffer for Playback");
                    return;
                }
                if(bufferForPlaybackAfterRebuffer > minBufferMs) {
                    etBufferForPlayback.setError("Buffer for playback after rebuffer must be minor or equals than min buffer");
                    return;
                }
                if(isUnicast) {
                    mPreferences.edit().putInt(KEY_MIN_BUFFER_MS_UNICAST, minBufferMs).apply();
                    mPreferences.edit().putInt(KEY_MAX_BUFFER_MS_UNICAST, maxBufferMs).apply();
                    mPreferences.edit().putInt(KEY_BUFFER_FOR_PLAYBACK_MS_UNICAST, bufferForPlayback).apply();
                    mPreferences.edit().putInt(KEY_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_UNICAST, bufferForPlaybackAfterRebuffer).apply();
                }else{
                    mPreferences.edit().putInt(KEY_MIN_BUFFER_MS_MULTICAST, minBufferMs).apply();
                    mPreferences.edit().putInt(KEY_MAX_BUFFER_MS_MULTICAST, maxBufferMs).apply();
                    mPreferences.edit().putInt(KEY_BUFFER_FOR_PLAYBACK_MS_MULTICAST, bufferForPlayback).apply();
                    mPreferences.edit().putInt(KEY_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MULTICAST, bufferForPlaybackAfterRebuffer).apply();
                }
               containerEngineering.setVisibility(View.GONE);
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.buffer_changed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean confirmField(TextInputEditText field) {
        if(!Objects.requireNonNull(field.getText()).toString().equals("")) return true;
        else{
            field.setError("This field is required");
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settingsBtnMoreSettings:
                if (true) {
                    containerMoreSettings.setVisibility(View.VISIBLE);
                    settingsButtonsContainer.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                    Button btnOk = findViewById(R.id.btnSaveMoreSettings);
                    RadioButton radioButtonHomeScreen = findViewById(R.id.radioButtonHomeScreen);
                    RadioButton radioButtonLiveTvScreen = findViewById(R.id.radioButtonLiveTvScreen);
                    RadioButton radioButtonBlackFrame = findViewById(R.id.radioButtonBlackFrame);
                    RadioButton radioButtonLastFrame = findViewById(R.id.radioButtonLastFrame);
                    RadioButton radioButtonLastChannel = findViewById(R.id.radioButtonLastChannel);
                    RadioButton radioButtonFirstChannel = findViewById(R.id.radioButtonFirstChannel);
                    RadioButton radioButtonLeanback = findViewById(R.id.radioButtonLeanback);
                    RadioButton radioButtonOriginal = findViewById(R.id.radioButtonOriginal);
                    RadioButton radioButtonEnglish = findViewById(R.id.radioButtonEnglish);
                    RadioButton radioButtonArabic = findViewById(R.id.radioButtonArabic);
                    RadioButton radioButtonCategoryView = findViewById(R.id.radioButtonCategoryView);
                    RadioButton radioButtonUpDown = findViewById(R.id.radioButtonUpDown);
                    RadioButton radioButtonAspect1 = findViewById(R.id.radioButtonAspect1);
                    RadioButton radioButtonAspect2 = findViewById(R.id.radioButtonAspect2);
                    RadioButton radioButtonAspect3 = findViewById(R.id.radioButtonAspect3);
                    RadioButton radioButtonAspect4 = findViewById(R.id.radioButtonAspect4);
                    RadioButton radioButtonAspect5 = findViewById(R.id.radioButtonAspect5);

                    String actualLanguage = mPreferences.getString(KEY_LANGUAGE, VALUE_LANGUAGE_EN);
                    boolean isCategoryViewEnabled = mPreferences.getBoolean(KEY_PREFERENCES_IS_CATEGORY_VIEW_ENABLED, DEFAULT_CATEGORY_VIEW_ENABLED);

                    if(actualLanguage.equals(VALUE_LANGUAGE_EN)) radioButtonEnglish.setChecked(true);
                    else radioButtonArabic.setChecked(true);

                    if(isCategoryViewEnabled){
                        radioButtonCategoryView.setChecked(true);
                    }else{
                        radioButtonUpDown.setChecked(true);
                    }

                    int currentRatio = mPreferences.getInt("ASPECT_RATIO", 3);
                    radioButtonAspect1.setChecked(currentRatio == 1);
                    radioButtonAspect2.setChecked(currentRatio == 2);
                    radioButtonAspect3.setChecked(currentRatio == 3);
                    radioButtonAspect4.setChecked(currentRatio == 4);
                    radioButtonAspect5.setChecked(currentRatio == 5);



                    if(mPreferences.getBoolean(KEY_LEANBACK_ENABLED, false)) {
                        radioButtonLeanback.setChecked(true);
                    }else{
                        radioButtonOriginal.setChecked(true);
                    }


                    if (playerManager.isStartAsLastChannel())
                        radioButtonLastChannel.setChecked(true);
                    else radioButtonFirstChannel.setChecked(true);

                    String screenSavedString = mPreferences.getString(KEY_BOOTUP_ACTIVITY, VALUE_HOME_ACTIVITY);
                    if (screenSavedString.equals(VALUE_HOME_ACTIVITY))
                        radioButtonHomeScreen.setChecked(true);
                    else radioButtonLiveTvScreen.setChecked(true);

                    String savedChannelChange = mPreferences.getString(KEY_FRAME_CHANGE, PlayerChangeType.LAST_FRAME.toString());
                    if (savedChannelChange.equals(PlayerChangeType.BLACK_FRAME.toString()))
                        radioButtonBlackFrame.setChecked(true);
                    else radioButtonLastFrame.setChecked(true);

                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (radioButtonHomeScreen.isChecked())
                                mPreferences.edit().putString(KEY_BOOTUP_ACTIVITY, VALUE_HOME_ACTIVITY).apply();
                            else
                                mPreferences.edit().putString(KEY_BOOTUP_ACTIVITY, VALUE_FULL_SCREEN_ACTIVITY).apply();


                            mPreferences.edit().putBoolean(KEY_PREFERENCES_IS_CATEGORY_VIEW_ENABLED, radioButtonCategoryView.isChecked()).apply();

                            if (radioButtonBlackFrame.isChecked())
                                playerManager.saveFrameChanged(PlayerChangeType.BLACK_FRAME);
                            else playerManager.saveFrameChanged(PlayerChangeType.LAST_FRAME);

                            boolean languageChanged = false;
                            if(radioButtonEnglish.isChecked() && !actualLanguage.equals(VALUE_LANGUAGE_EN)){
                                mPreferences.edit().putString(KEY_LANGUAGE, VALUE_LANGUAGE_EN).apply();
                                languageChanged = true;
                            }else if(radioButtonArabic.isChecked() && !actualLanguage.equals(VALUE_LANGUAGE_AR)){
                                mPreferences.edit().putString(KEY_LANGUAGE, VALUE_LANGUAGE_AR).apply();
                                languageChanged = true;
                            }

                            if(languageChanged){
                                Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }

                            if(radioButtonLeanback.isChecked()){
                                mPreferences.edit().putBoolean(KEY_LEANBACK_ENABLED, true).apply();
                                Intent i = new Intent(SettingsActivity.this, SplashActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.leanback_theme_applied), Toast.LENGTH_SHORT).show();
                                startActivity(i);
                                finish();
                            }else{
                                mPreferences.edit().putBoolean(KEY_LEANBACK_ENABLED, false).apply();
                            }
                            if(radioButtonAspect1.isChecked()){
                                mPreferences.edit().putInt("ASPECT_RATIO", 1).apply();
                            }else if(radioButtonAspect2.isChecked()){
                                mPreferences.edit().putInt("ASPECT_RATIO", 2).apply();
                            }else if(radioButtonAspect3.isChecked()){
                                mPreferences.edit().putInt("ASPECT_RATIO", 3).apply();
                            }else if(radioButtonAspect4.isChecked()){
                                mPreferences.edit().putInt("ASPECT_RATIO", 4).apply();
                            }else if(radioButtonAspect5.isChecked()){
                                mPreferences.edit().putInt("ASPECT_RATIO", 5).apply();
                            }
                            playerManager.saveStartAsLastChannel(radioButtonLastChannel.isChecked());
                            containerMoreSettings.setVisibility(View.GONE);
                        }
                    });
                }
                break;
            case R.id.settingsBtnInfo:
                if (true) {
                    containerInfo.setVisibility(View.VISIBLE);
                    settingsButtonsContainer.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                    TextView tvusername = findViewById(R.id.tv_profile_username);
                    TextView tvValidity = findViewById(R.id.tv_profile_validity);
                    TextView tvUnique = findViewById(R.id.tv_profile_uniqueid);
                    TextView tvAndroidVersion = findViewById(R.id.tv_profile_android_version);
                    TextView tvRamSize = findViewById(R.id.tv_profile_ram);
                    TextView tvStorage = findViewById(R.id.tv_profile_storage);
                    TextView tvDensity = findViewById(R.id.tvDensity);
                    TextView tvAppVersion = findViewById(R.id.tv_profile_app_version);
                    TextView tvUserId = findViewById(R.id.tv_profile_user_id);

                    float densityDpi = getResources().getDisplayMetrics().densityDpi;
                    tvDensity.setText(densityDpi + " dpi");

                try {
                    PackageManager pm = getPackageManager();
                    PackageInfo pInfo = pm.getPackageInfo(getPackageName(), 0);

                    int versionCode = pInfo.versionCode; // deprecated desde API 28
                    String versionName = pInfo.versionName;

                    tvAppVersion.setText(versionCode + "");

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                    username = mPreferences.getString("name", "null");
                    exp_date = mPreferences.getString("expDate", "null");

                    tvusername.setText(username.toUpperCase(Locale.ROOT));
                    tvValidity.setText(Helper.timeStamp(exp_date).toUpperCase(Locale.ROOT));
                    tvUnique.setText(mPreferences.getString("mac", "null").toUpperCase(Locale.ROOT));
                    tvAndroidVersion.setText(Build.VERSION.RELEASE);
                    tvUserId.setText(mPreferences.getInt(KEY_USER_ID, 0)+"");

                    Button btnOk = findViewById(R.id.btn_profile_ok);


                    ActivityManager actManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
                    ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
                    actManager.getMemoryInfo(memInfo);
                    long totalMemory = memInfo.totalMem;
//                    tvRamSize.setText("" + (((totalMemory / 1024) / 1024 / 1024)) + "GB");
                    tvRamSize.setText("2 GB");

                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                           containerInfo.setVisibility(View.GONE);
                            settingsButtonsContainer.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                            btnInfo.requestFocus();

                        }
                    });


                    StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
                    long bytesAvailable = stat.getAvailableBytes();

//                    tvStorage.setText(((((bytesAvailable / 1024) / 1024) / 1024)) + "GB");
                    tvStorage.setText("8 GB");

                }


                break;
            case R.id.settingsBtnLogout:
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
                preferencesEditor.putString("LAST_PLAYED_URL", PlayerLiveContainer.nullUrl);
                preferencesEditor.putInt(NetworkPackageKeys.LIVE_TV, 0);
                preferencesEditor.putInt(NetworkPackageKeys.VOD, 0);
                preferencesEditor.putInt(NetworkPackageKeys.SOD, 0);
                preferencesEditor.putInt(NetworkPackageKeys.MOD, 0);
                preferencesEditor.putString("LAST_PLAYED_URL", "");
                preferencesEditor.putBoolean(NetworkPackageKeys.FIRST_LOAD, true).apply();
                preferencesEditor.apply();

                Toast.makeText(getApplicationContext(),getResources().getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK); // To clean up all activities
                authHelper.signOut();
                startActivity(intent);
                finish();
                break;

        }
    }


    public void fingerprintVisibilityListener() {
        Fingerprint.setTag(Fingerprint.getVisibility());
        Fingerprint.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int newVis = Fingerprint.getVisibility();
                if ((int) Fingerprint.getTag() != newVis) {
                    if (Fingerprint.getVisibility() == View.VISIBLE) {
//                        if (infoDialog != null) {
//                            infoDialog.dismiss();
//
//                        }
//                        if (supportDialog != null) {
//                            supportDialog.dismiss();
//                        }
                    }
                }
            }
        });
    }
}
