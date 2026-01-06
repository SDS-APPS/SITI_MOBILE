package com.siti.mobile.Player;

import static java.lang.Math.min;
import static androidx.media3.common.util.Assertions.checkNotNull;

import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.BaseDataSource;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSourceException;
import androidx.media3.datasource.DataSpec;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

/** A UDP {@link DataSource} that decrypts AES data. */
@UnstableApi public final class UdpAESDataSource extends BaseDataSource {

    private static final String TAG = "UdpAESDataSource";

    public static final class UdpDataSourceException extends DataSourceException {
        public UdpDataSourceException(Throwable cause, @PlaybackException.ErrorCode int errorCode) {
            super(cause, errorCode);
        }
    }

    public static final int DEFAULT_MAX_PACKET_SIZE = 2000;
    public static final int DEFAULT_SOCKET_TIMEOUT_MILLIS = 8 * 1000;
    public static final int UDP_PORT_UNSET = -1;

    private final int socketTimeoutMillis;
    private final byte[] encryptedPacketBuffer;
    private final DatagramPacket packet;

    @Nullable private Uri uri;
    @Nullable private DatagramSocket socket;
    @Nullable private MulticastSocket multicastSocket;
    @Nullable private InetAddress address;
    private boolean opened;

    private int packetRemaining;
    private byte[] unencryptedBytes;

    private final AESEncryptionHelper aesEncryptionHelper;

    public UdpAESDataSource() {
        this(DEFAULT_MAX_PACKET_SIZE);
    }

    public UdpAESDataSource(int maxPacketSize) {
        this(maxPacketSize, DEFAULT_SOCKET_TIMEOUT_MILLIS);
    }

    public UdpAESDataSource(int maxPacketSize, int socketTimeoutMillis, String ipForKey, String portForKey) {
        super(true);
        this.socketTimeoutMillis = socketTimeoutMillis;
        this.encryptedPacketBuffer = new byte[maxPacketSize];
        this.packet = new DatagramPacket(encryptedPacketBuffer, 0, maxPacketSize);
        this.aesEncryptionHelper = new AESEncryptionHelper(ipForKey, portForKey);
        this.aesEncryptionHelper.startDecryptionAES();
        Log.w(TAG, "UdpAESDataSource Created");
    }

    public UdpAESDataSource(int maxPacketSize, int socketTimeoutMillis) {
        super(true);
        this.socketTimeoutMillis = socketTimeoutMillis;
        this.encryptedPacketBuffer = new byte[maxPacketSize];
        this.packet = new DatagramPacket(encryptedPacketBuffer, 0, maxPacketSize);
        this.aesEncryptionHelper = null;
    }

    @Override
    public long open(DataSpec dataSpec) throws UdpDataSourceException {
        uri = dataSpec.uri;
        String host = checkNotNull(uri.getHost());
        int port = uri.getPort();

        Log.w(TAG, "Open Uri: " + uri);

        transferInitializing(dataSpec);

        try {
            address = InetAddress.getByName(host);
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);
            if (address.isMulticastAddress()) {
                multicastSocket = new MulticastSocket(socketAddress);
                multicastSocket.joinGroup(address);
                socket = multicastSocket;
            } else {
                socket = new DatagramSocket(socketAddress);
            }
            socket.setSoTimeout(socketTimeoutMillis);
        } catch (SecurityException e) {
            throw new UdpDataSourceException(e, PlaybackException.ERROR_CODE_IO_NO_PERMISSION);
        } catch (IOException e) {
            throw new UdpDataSourceException(e, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED);
        }

        opened = true;
        transferStarted(dataSpec);
        return C.LENGTH_UNSET;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws UdpDataSourceException {
        if (length == 0) {
            return 0;
        }

        if (packetRemaining == 0) {
            do {
                try {
                    checkNotNull(socket).receive(packet);
                    unencryptedBytes = aesEncryptionHelper != null
                            ? aesEncryptionHelper.decryptThread(encryptedPacketBuffer)
                            : new byte[0];
                } catch (SocketTimeoutException e) {
                    throw new UdpDataSourceException(e, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT);
                } catch (IOException e) {
                    throw new UdpDataSourceException(e, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED);
                }
            } while (unencryptedBytes == null);

            packetRemaining = unencryptedBytes.length;
            bytesTransferred(packetRemaining - 16); // O lo que quieras restar
        }

        int packetOffset = unencryptedBytes.length - packetRemaining;
        int bytesToRead = min(packetRemaining, length);
        System.arraycopy(unencryptedBytes, packetOffset, buffer, offset, bytesToRead);
        packetRemaining -= bytesToRead;
        return bytesToRead;
    }

    @Override
    @Nullable
    public Uri getUri() {
        return uri;
    }

    @Override
    public void close() {
        uri = null;
        if (multicastSocket != null) {
            try {
                multicastSocket.leaveGroup(checkNotNull(address));
            } catch (IOException ignored) {}
            multicastSocket = null;
        }
        if (socket != null) {
            socket.close();
            socket = null;
        }
        address = null;
        packetRemaining = 0;
        if (opened) {
            opened = false;
            transferEnded();
        }
    }

    public int getLocalPort() {
        return socket != null ? socket.getLocalPort() : UDP_PORT_UNSET;
    }
}
