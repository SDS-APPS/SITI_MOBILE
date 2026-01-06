package com.siti.mobile.Player;


import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.UdpDataSource;

public class UdpAESDataSourceFactory {
    @OptIn(markerClass = UnstableApi.class) public UdpDataSource.Factory getFactory(String ipToKey, String portToKey) {
        UdpDataSource.Factory dataSource = new UdpAESDataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return new UdpAESDataSource(1328, 100000, ipToKey, portToKey);
            }
        };
        return dataSource;
    }
}

