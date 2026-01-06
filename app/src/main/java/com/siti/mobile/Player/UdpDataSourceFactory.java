package com.siti.mobile.Player;


import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.UdpDataSource;

public class UdpDataSourceFactory {
    @OptIn(markerClass = UnstableApi.class) public UdpDataSource.Factory getFactory() {
        UdpDataSource.Factory dataSource = new UdpDataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return new UdpDataSource(5000, 100000);
            }
        };
        return dataSource;
    }
}
