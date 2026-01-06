package com.siti.mobile.mvvm.login.view;

import static com.siti.mobile.Utils.KeyPreferencesKt.FALSE_STRING;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_ADMIN_ID;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_AREA_CODE;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_AUTHCODE;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_AUTH_TOKEN;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_BANNER;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_EXP_DATE;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_FIRST_LOGIN;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_HEADER_VALIDITY;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_INTERVAL;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_LEANBACK_ENABLED;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_MAC;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_NAME;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_PASSWORD;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_SERVER_IP;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_SERVER_IP_ADMIN;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_SOCKET;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_USERNAME;
import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_USER_ID;
import static com.siti.mobile.Utils.KeyPreferencesKt.PREFERENCES_STRING_DEFAULT_VALUE;
import static com.siti.mobile.Utils.KeyPreferencesKt.SERVER_GLOBAL_IP_ADMIN;
import static com.siti.mobile.Utils.KeyPreferencesKt.SERVER_GLOBAL_IP_LOGIN;
import static com.siti.mobile.Utils.KeyPreferencesKt.SERVER_GLOBAL_IP_SOCKET;
import static com.siti.mobile.Utils.KeyPreferencesKt.SERVER_IPTV_NAME;
import static com.siti.mobile.Utils.KeyPreferencesKt.SERVER_LOCAL_IP_ADMIN;
import static com.siti.mobile.Utils.KeyPreferencesKt.SERVER_LOCAL_IP_LOGIN;
import static com.siti.mobile.Utils.KeyPreferencesKt.SERVER_LOCAL_IP_SOCKET;
import static com.siti.mobile.Utils.KeyPreferencesKt.SERVER_LOCAL_NAME;
import static com.siti.mobile.Utils.KeyPreferencesKt.TRUE_STRING;
import static com.siti.mobile.Utils.KeyPreferencesKt.VALUE_LOGGED_IN;
import static com.siti.mobile.Utils.KeyPreferencesKt.sharedPrefFile;
import static com.siti.mobile.Utils.SocketUtilsKt.checkStateServer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;

import com.siti.mobile.BuildConfig;
import com.siti.mobile.mvvm.config.helpers.AnalyticsEventsHelper;
import com.siti.mobile.mvvm.config.helpers.AuthHelper;
import com.siti.mobile.Interface.ApiInterface;
import com.siti.mobile.R;
import com.siti.mobile.Utils.CurrentData;
import com.siti.mobile.Utils.Helper;
import com.siti.mobile.Utils.RetrofitClient;
import com.siti.mobile.Utils.StateChangedServer;
import com.siti.mobile.Utils.UserDatesMemory;
import com.siti.mobile.mvvm.loading.view.DataLoadingActivity;
import com.siti.mobile.mvvm.preview.view.PreviewScreen;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText Username, Password;
    private FrameLayout Login;
    private ApiInterface apiInterface;
    final String TAG = "LoginActivity";
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor preferencesEditor;
    private Map<String, String> serverMap;
    private ProgressBar LoginProgressBar;
    private JSONObject paramObject;
    private AuthHelper authHelper;
    private AnalyticsEventsHelper analyticsEventsHelper;
    private ImageView iconServerState;

    FrameLayout containerEtUsername, containerEtPassword, containerEtMacId, containerEtServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_login_new);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginMethods();
    }

    private void showKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
        }
    }

    String macAddress;
    String macSelected;

    private void hideKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void loginMethods() {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                try {
                    Certificate[] certificates = session.getPeerCertificates();
                    X509Certificate x509Certificate = (X509Certificate) certificates[0];
                    x509Certificate.checkValidity();
                    X500Principal subject = x509Certificate.getSubjectX500Principal();
                    return hostname.equalsIgnoreCase(subject.getName());
                } catch (Exception e) {
                    return false;
                }
            }
        });
        Objects.requireNonNull(getSupportActionBar()).hide();
        authHelper = new AuthHelper();
        analyticsEventsHelper = new AnalyticsEventsHelper();

        iconServerState = findViewById(R.id.iconServerstate);
        Username = findViewById(R.id.loginEtUsername);
        containerEtUsername = findViewById(R.id.containerEtUsername);
        Password = findViewById(R.id.loginEtPassword);
        containerEtPassword = findViewById(R.id.containerEtPassword);
        Login = findViewById(R.id.btn_login);

        Username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    showKeyboard(Username);
                    containerEtUsername.setBackgroundResource(R.drawable.login_server_field_selected);
                }else{
                    containerEtUsername.setBackgroundResource(R.drawable.login_server_field);
                }
            }
        });

        Password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    showKeyboard(Password);
                    containerEtPassword.setBackgroundResource(R.drawable.login_server_field_selected);
                }else{
                    containerEtPassword.setBackgroundResource(R.drawable.login_server_field);
                    hideKeyboard(Password);
                }
            }
        });


        //filled_mac
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        serverMap = new HashMap<String, String>();

//        serverMap.put(SERVER_LOCAL_NAME, SERVER_LOCAL_IP_LOGIN);
        if(BuildConfig.DEBUG){
            serverMap.put(SERVER_IPTV_NAME, SERVER_GLOBAL_IP_LOGIN);
        }

        List<String> providerName = new ArrayList<>();
//        providerName.add(SERVER_LOCAL_NAME);
        if(BuildConfig.DEBUG) {
            providerName.add(SERVER_IPTV_NAME);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.dropdown_menu_popup_item,
                providerName);
        AutoCompleteTextView editTextFilledExposedDropdown = findViewById(R.id.filled_exposed_dropdown);
        containerEtServer = findViewById(R.id.containerEtserver);
        editTextFilledExposedDropdown.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    containerEtServer.setBackgroundResource(R.drawable.login_server_field_selected);
                }else{
                    containerEtServer.setBackgroundResource(R.drawable.login_server_field);
                }
            }
        });
        editTextFilledExposedDropdown.setAdapter(adapter);
        editTextFilledExposedDropdown.setText(providerName.get(0), false);

//        if(BuildConfig.DEBUG) {
//            editTextFilledExposedDropdown.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    editTextFilledExposedDropdown.showDropDown();
//                }
//            });
//        }


        EditText editTextFilledMac = findViewById(R.id.loginEtMacId);
        containerEtMacId = findViewById(R.id.containerEtMacId);
        editTextFilledMac.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    containerEtMacId.setBackgroundResource(R.drawable.login_macid_field_selected);
                }else{
                    containerEtMacId.setBackgroundResource(R.drawable.login_macid_field);
                }
            }
        });
        //   List<String> macProviderName = new ArrayList<>();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //    String macAddress = wifiManager.getConnectionInfo().getMacAddress();
        String DeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //    macProviderName.add(macAddress);

//            ArrayAdapter<String> macadapter =
//                    new ArrayAdapter<>(
//                            getApplicationContext(),
//                            R.layout.dropdown_menu_popup_item,
//                            macProviderName);
        macAddress = getMacAddress();
        if(macAddress != null && !macAddress.isEmpty()) {
            editTextFilledMac.setText(macAddress);
        }else{
            editTextFilledMac.setText(DeviceId);
            macAddress = DeviceId;
        }


        checkServerState(serverMap.get(editTextFilledExposedDropdown.getText().toString()));

        editTextFilledExposedDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkServerState(serverMap.get(adapter.getItem(position)));
            }
        });

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        preferencesEditor = mPreferences.edit();
        String AuthCode = mPreferences.getString(KEY_AUTHCODE, PREFERENCES_STRING_DEFAULT_VALUE);
        String setUsername = mPreferences.getString(KEY_USERNAME, PREFERENCES_STRING_DEFAULT_VALUE);
        String setPassword = mPreferences.getString(KEY_PASSWORD, PREFERENCES_STRING_DEFAULT_VALUE);

        if (!setUsername.equals(PREFERENCES_STRING_DEFAULT_VALUE)) {
            Username.setText(setUsername);
        }

        if (!setPassword.equals(PREFERENCES_STRING_DEFAULT_VALUE)) {
            Password.setText(setPassword);
        }

        preferencesEditor = mPreferences.edit();
        preferencesEditor.putString(KEY_BANNER, FALSE_STRING);
        preferencesEditor.apply();

        if (!AuthCode.equals(PREFERENCES_STRING_DEFAULT_VALUE)) {
            Intent intent = new Intent(getApplicationContext(), PreviewScreen.class);
            startActivity(intent);
            finish();
        }
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Username.setFocusable(false);
                Password.setFocusable(false);

                String username = Username.getText().toString().trim();
                String password = Password.getText().toString().trim();
                String serverSelectedDropDown = editTextFilledExposedDropdown.getText().toString();
                String serverSelected = "";
                 macSelected = editTextFilledMac.getText().toString();
                //    String macSelectedTest ="kevin-user-mac-test";

                if(serverSelectedDropDown.equals(SERVER_LOCAL_NAME)){
                    serverSelected = SERVER_LOCAL_NAME;
                }else{
                    serverSelected = SERVER_IPTV_NAME;
                }

                if (serverSelected.equals("")) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.select_server), Toast.LENGTH_SHORT).show();
                }
                if (macSelected.equals("")) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.select_uid), Toast.LENGTH_SHORT).show();
                } else {
                    Login.setEnabled(false);
//                    LoginProgressBar.setVisibility(View.VISIBLE);
                    preferencesEditor = mPreferences.edit();
                    String selectedOption = editTextFilledExposedDropdown.getText().toString();
                    String serverIp = serverMap.get(selectedOption);
                    String serverSocketIp;
                    if(selectedOption.equals(SERVER_LOCAL_NAME)) serverSocketIp = SERVER_LOCAL_IP_SOCKET;
                    else serverSocketIp = SERVER_GLOBAL_IP_SOCKET;
                    preferencesEditor.putString(KEY_SERVER_IP, serverIp);
                    if(serverIp.contains(SERVER_LOCAL_IP_LOGIN)){
                        preferencesEditor.putString(KEY_SERVER_IP_ADMIN, SERVER_LOCAL_IP_ADMIN);
                    }else{
                        preferencesEditor.putString(KEY_SERVER_IP_ADMIN, SERVER_GLOBAL_IP_ADMIN);
                    }
                    CurrentData.ip = serverIp;
                    preferencesEditor.putString(KEY_SOCKET, serverSocketIp);

                    preferencesEditor.apply();
                    String nServerIP = mPreferences.getString(KEY_SERVER_IP, PREFERENCES_STRING_DEFAULT_VALUE);
                    apiInterface = RetrofitClient.getClient(nServerIP).create(ApiInterface.class);
                    try {
                        paramObject = new JSONObject();
                        paramObject.put(KEY_USERNAME, username);
//                        if (BuildConfig.DEBUG) {
//                            macSelected = "00:15:C0:98:7D:FF";
//                        }
                        paramObject.put(KEY_MAC, macSelected);
                        // macAddress
                        paramObject.put(KEY_PASSWORD, password);
                    } catch (JSONException e) {
                        Log.i(TAG, "Json Exception: " + e.getMessage());
                    }

                    Call<com.siti.mobile.Model.RetroFit.Login> call = apiInterface.getLogin(paramObject.toString());

                    call.enqueue(new Callback<com.siti.mobile.Model.RetroFit.Login>() {
                        @Override
                        public void onResponse(Call<com.siti.mobile.Model.RetroFit.Login> call, Response<com.siti.mobile.Model.RetroFit.Login> response) {
//                            LoginProgressBar.setVisibility(View.GONE);
                            Login.setEnabled(true);
                            if (response.code() == 200) {
                                hideKeyboard(Password);
                                hideKeyboard(Username);
                                com.siti.mobile.Model.RetroFit.Login obj = response.body();
                                Log.i(TAG, "onResponse: " + obj.getData().getAreaCode());

                                mPreferences.edit();
                                preferencesEditor.putString(KEY_FIRST_LOGIN, TRUE_STRING);
                                preferencesEditor.putString(KEY_AUTHCODE, VALUE_LOGGED_IN);
                                preferencesEditor.putString(KEY_MAC, macSelected);
                                preferencesEditor.putInt(KEY_INTERVAL, obj.getData().getIntervalUpdate());

                                preferencesEditor.putString(KEY_USERNAME, obj.getData().getUsername());
                                preferencesEditor.putString(KEY_PASSWORD, password);
                                preferencesEditor.putString(KEY_NAME, obj.getData().getName());
                                preferencesEditor.putInt(KEY_ADMIN_ID, obj.getData().getAdminId());
                                preferencesEditor.putString(KEY_EXP_DATE, obj.getData().getExpDate());
                                preferencesEditor.putString(KEY_AUTH_TOKEN, obj.getData().getAuth_token());
                                preferencesEditor.putString(KEY_AREA_CODE, obj.getData().getAreaCode());
                                preferencesEditor.putInt(KEY_USER_ID, obj.getData().getId());

                                preferencesEditor.putString(KEY_HEADER_VALIDITY, Helper.timeStamp(obj.getData().getExpDate()));

                                preferencesEditor.apply();
                                preferencesEditor.clear();

                                UserDatesMemory.recentLoggedIn = true;
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), DataLoadingActivity.class);
                                intent.putExtra("IS_LEANBACK", !mPreferences.getBoolean(KEY_LEANBACK_ENABLED, false));
                                authHelper.signInAnonymously();
                                analyticsEventsHelper.eventLogIn();
                                startActivity(intent);
                                finish();


                            } else {
                                if(response.code() == 400){
                                    Toast.makeText(LoginActivity.this, "User in blacklist", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                                }
                                Log.i(TAG, "onResponse: " + response.code());

                            }
                        }

                        @Override
                        public void onFailure(Call<com.siti.mobile.Model.RetroFit.Login> call, Throwable t) {
                            Log.i(TAG, "onFailure: " + t.getMessage());
                //            LoginProgressBar.setVisibility(View.GONE);
                            Login.setEnabled(true);

                            Toast.makeText(LoginActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private String loadFileAsString(String filePath) throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    /*
     * Get the STB MacAddress
     */
    private String getMacAddress(){
        try {
            return loadFileAsString("/sys/class/net/eth0/address")
                    .toUpperCase().substring(0, 17);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void checkServerState(String serverIp)
    {
        iconServerState.setImageResource(R.drawable.ic_server_state);
        checkStateServer(serverIp,3005, new StateChangedServer() {
            @Override
            public void stateChanged(boolean value) {
                if(value) iconServerState.setImageResource(R.drawable.ic_server_online);
                else iconServerState.setImageResource(R.drawable.ic_server_offline);
            }
        });
    }
//    public static String getMacAddr() {
//        try {
//            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
//            for (NetworkInterface nif : all) {
//                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
//
//                byte[] macBytes = nif.getHardwareAddress();
//                if (macBytes == null) {
//                    return "";
//                }
//                StringBuilder res1 = new StringBuilder();
//                for (byte b : macBytes) {
//                    res1.append(String.format("%02X:", b));
//                }
//                if (res1.length() > 0) {
//                    res1.deleteCharAt(res1.length() - 1);
//                }
//                return res1.toString();
//            }
//        } catch (Exception ignored) {}
//        return "02:00:00:00:00:00";
//    }

}
