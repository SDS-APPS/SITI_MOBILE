package com.siti.mobile.Multicast;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.UdpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.extractor.ExtractorsFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class AsyncTaskHelper {

    public AsyncTaskHelper(int g_bEncryptionLevel)
    {
        this.g_bEncryptionLevel = g_bEncryptionLevel;
    }

    public native byte[] JNIAESDecrypt(String szLen, byte[] szEncryptedString);

    public native int JNIAESDecryptStart128();

    public native int JNIAESDecryptEnd128();

    private final String TAG = "AsyncTaskHelper";

    int g_cur_player_ch_index = 0;
    String g_multicast_ip = null;
    private String g_multicast_port = null;
    private String g_message;
    int g_local_port = 1234;
    private int g_bEncryptionLevel = 0;
    private boolean g_bEncryptedMulticast = false;

    public static String getIncrementIPV4Address(String ip, int nIndex) {
        String[] nums = ip.split("\\.");
        int i = (Integer.parseInt(nums[0]) << 24 | Integer.parseInt(nums[2]) << 8
                | Integer.parseInt(nums[1]) << 16 | Integer.parseInt(nums[3])) + nIndex;

        // If you wish to skip over .255 addresses.
        if ((byte) i == -1) i++;

        return String.format("%d.%d.%d.%d", i >>> 24 & 0xFF, i >> 16 & 0xFF,
                i >> 8 & 0xFF, i >> 0 & 0xFF);
    }

    public String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    public String encodeHexString(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            hexStringBuffer.append(byteToHex(byteArray[i]));
        }
        return hexStringBuffer.toString();
    }

    public String getUDPStreams()
    {
        AsyncTask_Start_Multicast multicast = new AsyncTask_Start_Multicast();
        try {
            return multicast.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class AsyncTask_Start_Multicast extends AsyncTask<String, String, String> {

        protected String doInBackground(String... async_input) {
            //String msg = "Hello";
            int nLen = 0;
            try {

                //	String szTSChunkName = "/storage/emulated/0/EZIPTV/1.ts";
                //	OutputStream output = new FileOutputStream(szTSChunkName);

                //t = 1234;
                //		InetAddress local = InetAddress.getByName("192.168.100.86");
                InetAddress local = InetAddress.getByName("0.0.0.0");
                //		InetAddress local = InetAddress.getByName("192.168.0.9");
                DatagramSocket sudp = new DatagramSocket();
                DatagramPacket p;

                JNIAESDecryptStart128();
                String new_multicast_IP = getIncrementIPV4Address(g_multicast_ip, g_cur_player_ch_index);
                //Log.i("Multicast IP",new_multicast_IP);
                InetAddress group = InetAddress.getByName(new_multicast_IP);
                MulticastSocket s = new MulticastSocket(Integer.valueOf(g_multicast_port));
                g_message = "[" + new_multicast_IP + ":" + g_multicast_port + "]";
                Log.i("Multicast IP", g_message);
                s.joinGroup(group);
                // byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                //byte[] bytes=null;
                // DatagramPacket hi = new DatagramPacket(bytes, bytes.length,
                //							 group, Integer.valueOf(g_multicast_port));
                // s.send(hi);

                //byte[] buf = new byte[1316];
                byte[] inbuffer = new byte[1316];
                byte[] revbuffer = new byte[1316];

                // g_bEncryptionLevel = 1


                int i = 0;
                boolean bFound = false;
                boolean bFirstTime = true;
                DatagramPacket recv = new DatagramPacket(inbuffer, 1316);
                p = new DatagramPacket(revbuffer, 1316, local, g_local_port);
                boolean g_ChannelStopped = false;
                while (!g_ChannelStopped) {
                    //DatagramPacket recv = new DatagramPacket(revbuffer, revbuffer.length);
                    s.receive(recv);
                    //g_message = "len. ["  + inbuffer.length + "]" ;
                    //Log.i(" ", g_message);
                    if (g_bEncryptionLevel == 1) {
                        byte[] unAES = new byte[16];
                        byte[] enAES = new byte[16];
                        byte[] revbuffer2 = new byte[1310];
                        String szLen = Integer.toString(16);
                        System.arraycopy(inbuffer, 0, enAES, 0, 16);
                        //g_message = "b. ["  + enAES.length + "]" + encodeHexString(enAES).substring(0,enAES.length);
                        //Log.i(" ", g_message);

                        unAES = JNIAESDecrypt(szLen, enAES);
                        //g_message = "c. ["  + unAES.length + "]" + encodeHexString(unAES).substring(0,unAES.length);
                        //Log.i(" ", g_message);

                        revbuffer2 = Arrays.copyOfRange(inbuffer, 16, inbuffer.length);
                        //g_message = "d. ["  + revbuffer2.length + "]" + encodeHexString(revbuffer2).substring(0,revbuffer2.length);
                        //Log.i(" ", g_message);

                        System.arraycopy(unAES, 0, revbuffer, 0, unAES.length);
                        System.arraycopy(revbuffer2, 0, revbuffer, unAES.length, revbuffer2.length);
                    } else if (g_bEncryptionLevel == 2) {
                        byte[] unAES = new byte[1312];
                        byte[] enAES = new byte[1312];
                        String szLen = Integer.toString(1312);
                        byte[] revbuffer2 = new byte[4];

                        System.arraycopy(inbuffer, 0, enAES, 0, 1312);
                        //g_message = "b. ["  + enAES.length + "]" + encodeHexString(enAES).substring(0,enAES.length);
                        //Log.i(" ", g_message);

                        unAES = JNIAESDecrypt(szLen, enAES); // decrypt 1312 bytes
                        //g_message = "c. ["  + unAES.length + "]" + encodeHexString(unAES).substring(0,unAES.length);
                        //Log.i(" ", g_message);
                        revbuffer2 = Arrays.copyOfRange(inbuffer, 1312, inbuffer.length); // last 4 bytes


                        System.arraycopy(unAES, 0, revbuffer, 0, unAES.length);
                        System.arraycopy(revbuffer2, 0, revbuffer, unAES.length, revbuffer2.length);

                    }
                    //String revbuffer1=new String(inbuffer);
                    //String szLen = Integer.toString(recv.getLength());

                    //byte[] revbuffer = JNIAESDecrypt(szLen, inbuffer);

                    int len = revbuffer.length;
                    //g_message="["+len+"]";
                    //Log.i("Len",g_message);
                    //g_message = "e. ["  + len + "]" + encodeHexString(revbuffer).substring(0,len);
                    //Log.i(" ", g_message);

                    if (bFirstTime) {
                        for (i = 0; i < len; i++) {
                            if (revbuffer[i] == 0x47) {
                                //g_message = "a. ["  + len + "]" + encodeHexString(revbuffer).substring(i,len);
                                //Log.i(" ", g_message);
                                if ((revbuffer[i + 1] == 0x40) || (revbuffer[i + 1] == 0x50)) {

                                    g_message = "i=[" + i + "]" + "Multicast Found PAT";
                                    Log.i("", g_message);
                                    bFound = true;
                                    break;
                                }
                            }

                        }
                        if (!bFound) {
                            continue;
                        }
                    }
                    if ((bFound) && (bFirstTime)) {
                        //g_message = "[" + i + "]" + "[" + len + "]" + encodeHexString(revbuffer);
                        //Log.i("2. newByte", g_message);
                        int newLen = len - i;
                        if (newLen > 0) {
                            byte[] newByte = Arrays.copyOfRange(revbuffer, i, len);
							/* for (int k=0;k<(newLen/188);k++)
							{
								newByte[k*188]=0x47;
							}

							 */
                            g_message = "1. [" + i + ":" + newLen + "]" + "[" + newByte.length + "]" + encodeHexString(newByte).substring(0, newLen);
                            ;
                            Log.i("", g_message);
                            DatagramPacket q = new DatagramPacket(newByte, newLen, local, g_local_port);
                            sudp.send(q);
                            bFirstTime = false; // not to search again
                        }
                    } else {
                        revbuffer[0] = 0x47;


                        //	g_message = "2. ["  + len + "]" + encodeHexString(revbuffer).substring(0,16);
                        //	Log.i(" ", g_message);
                        //p = new DatagramPacket(revbuffer,len, local, g_local_port);
                        sudp.send(p);
                    }
                }

                //	output.close();
                sudp.close();
                //	output.close();
                s.leaveGroup(group);
                JNIAESDecryptEnd128();


                return null;
            } catch (Exception ex) {
                Log.e(TAG, "error: " + ex.getMessage(), ex);
                return null;
            }

        }

        protected void onPostExecute(String result) {

        }

    }

    @OptIn(markerClass = UnstableApi.class)
    private DataSource.Factory buildDataSourceFactory() {
        return new UdpDataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return new UdpDataSource(5000, 100000);
            }
        };
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer(ExoPlayer player) {
     /*   player = ExoPlayerFactory.newSimpleInstance(this);


        //Log.i("g_url",g_url);
        mVideoView.setPlayer(player);*/

        g_bEncryptedMulticast = false;
        Get_MULTICAT_IP__Port("","0.0.0.0", 1234);
        MediaSource mediaSource = null;
        DataSource.Factory factory = buildDataSourceFactory();
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        String new_multicast_IP_port = "udp://0.0.0.0:1234";
        if (!g_bEncryptedMulticast) {
            new_multicast_IP_port = "udp://@" + getIncrementIPV4Address(g_multicast_ip, g_cur_player_ch_index) + ":" + g_multicast_port;
            Log.i("new_multicast_IP_port", new_multicast_IP_port);
//            mediaSource = new ExtractorMediaSource(Uri.parse(new_multicast_IP_port), factory,
//                    extractorsFactory, null, null); /// <<--- Old
            mediaSource = new ProgressiveMediaSource.Factory(factory).createMediaSource(
                    MediaItem.fromUri(Uri.parse(new_multicast_IP_port)));

        } else {
            Log.i("new_multicast_IP_port", new_multicast_IP_port);
            mediaSource = new ProgressiveMediaSource.Factory(factory).createMediaSource(
                    MediaItem.fromUri(Uri.parse(new_multicast_IP_port)));
        }


    //    mWaitingView.setVisibility(View.GONE);
        player.setPlayWhenReady(true);
        player.prepare(mediaSource, false, false);

        //spinner.setVisibility(View.GONE); */

    }

    boolean Get_MULTICAT_IP__Port(String token, String ezserver_ip, int ezserver_port) {

        String sendbuffer = null;
        String revbuffer = null;
        int keyword_pos = 0;
        int crln_pos = 0;
        byte inbuffer[] = new byte[1024];
        byte outbuffer[] = new byte[1024];
        Socket socket;
        try {
            // Connect EZserver
            socket = new Socket(ezserver_ip, ezserver_port);
            OutputStream outstream = socket.getOutputStream();
            InputStream instream = socket.getInputStream();

            // Send EZserver HTTP Command
            //sendbuffer="GET HTTP/1.1 /server/inquery_server_multicast_ip_port?token="+token+"\r\nUser-Agent=EZhometech\r\n\r\n";
            sendbuffer = "GET /server/inquery_server_multicast_ip_port?token=" + token + " HTTP/1.1\r\nHost: " + ezserver_ip + ":" + ezserver_port + "\r\nConnection: close\r\nUser-Agent: EZhometech\r\n\r\n";

            outbuffer = sendbuffer.getBytes();
            outstream.write(outbuffer);

            // Receive EZserver HTTP Response
            int toread = 1024;
            int len = 0;
            for (int index = 0; index < toread; ) {
                len = instream.read(inbuffer, index, toread - index);
                if (len < 0) break;
                index = index + len;
            }
            revbuffer = new String(inbuffer);
            // Close Socket
            outstream.close();
            instream.close();


            // Get token
            if (revbuffer.contains("200 OK") == true) {
                //Log.i("revbuffer",revbuffer);
                keyword_pos = revbuffer.indexOf("multicast_ip=");
                if (keyword_pos != -1) {
                    crln_pos = revbuffer.indexOf("\r\n", keyword_pos);
                    g_multicast_ip = revbuffer.substring(keyword_pos + 13, crln_pos);
                    keyword_pos = revbuffer.indexOf("multicast_port=");
                    if (keyword_pos != -1) {
                        crln_pos = revbuffer.indexOf("\r\n", keyword_pos);
                        g_multicast_port = revbuffer.substring(keyword_pos + 15, crln_pos);
                        keyword_pos = revbuffer.indexOf("AES=");
                        if (keyword_pos != -1) {
                            crln_pos = revbuffer.indexOf("\r\n", keyword_pos);
                            String encrypted_multicast = revbuffer.substring(keyword_pos + 4, crln_pos);
                            //Log.i("encrypted_multicast",encrypted_multicast);
                            if (encrypted_multicast.equals("1")) {
                                g_bEncryptedMulticast = true;
                                g_bEncryptionLevel=1;
                            } else if (encrypted_multicast.equals("2")) {
                                g_bEncryptedMulticast = true;
                                g_bEncryptionLevel=2;
                            } else{
                                g_bEncryptedMulticast = false;
                            }
                        }
                    }
                    g_message = g_multicast_ip + ":" + g_multicast_port;
                    Log.i("Recv", g_message);
                    return true;
                } else {
                    return false;
                }

            } else {
                return false;
            }
        } catch (Exception ex) {
            Log.e(TAG, "error: " + ex.getMessage(), ex);
            return false;
        }

    }

}
