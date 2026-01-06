package com.siti.mobile.Utils;

import static com.siti.mobile.Utils.KeyPreferencesKt.KEY_SERVER_IP;
import static com.siti.mobile.Utils.KeyPreferencesKt.SERVER_GLOBAL_IP_SOCKET;
import static com.siti.mobile.Utils.KeyPreferencesKt.SERVER_LOCAL_IP_SOCKET;

import android.app.Activity;
import android.content.SharedPreferences;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.WebSocket;
import okhttp3.OkHttpClient;

public class SocketSingleton {
    private static Socket mSocket;

    private static void initSocket(Activity activity, SharedPreferences mPreferences) {
        try {
            IO.Options opts = new IO.Options();
            opts.transports = new String[]{WebSocket.NAME};
            opts.timeout = -1;
            opts.callFactory = getOkHttpClientTrust();
            opts.webSocketFactory = getOkHttpClientTrust();;
            opts.path = "/socket.io";
            if(mPreferences.getString(KEY_SERVER_IP, "").contains("172.31")) {
                mSocket = IO.socket(SERVER_LOCAL_IP_SOCKET, opts);
            }else{
                mSocket = IO.socket(SERVER_GLOBAL_IP_SOCKET, opts);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Socket getInstance(Activity activity, SharedPreferences mPreferences) {
        if (mSocket == null) {
            initSocket(activity, mPreferences);
        }
        return mSocket;
    }

    public static OkHttpClient getOkHttpClientTrust(){
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
        SSLContext sc = null;

        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }


        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sc.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                }).build();

        return okHttpClient;
    }

}
