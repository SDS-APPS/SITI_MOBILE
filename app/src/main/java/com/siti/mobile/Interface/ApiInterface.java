package com.siti.mobile.Interface;

import com.siti.mobile.Model.GeneralResponse;
import com.siti.mobile.Model.RetroFit.ChannelStatisticsResponse;
import com.siti.mobile.Model.RetroFit.LandingChannelResponse;
import com.siti.mobile.Model.RetroFit.LiveCategory;
import com.siti.mobile.Model.RetroFit.Login;
import com.siti.mobile.Model.advertisment.AdvertismentResponse;
import com.siti.mobile.Model.app_update.AppUpdateResponse;
import com.siti.mobile.mvvm.common.data.AreaCodeResponse;
import com.siti.mobile.mvvm.common.data.ParkingChannelsResponse;
import com.siti.mobile.mvvm.common.data.models.CatchupChannelsResponse;
import com.siti.mobile.mvvm.common.data.post.OnlineCustomerRequest;
import com.siti.mobile.network.engineering.EngineeringResponse;
import com.siti.mobile.network.package_expiry.PackageExpiryResponse;
import com.siti.mobile.network.tune_version.TuneVersionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {

    @Headers("Content-Type: application/json")
    @POST("login")
    Call<Login> getLogin(@Body String body);

    @Headers("Content-Type: application/json")
    @POST("onlinecustomer")
    Call<GeneralResponse> refreshOnlineData(@Header("Authorization") String authorization, @Body OnlineCustomerRequest body);

    @Headers("Content-Type: application/json")
    @GET("getlandingchannel/parking")
    Call<ParkingChannelsResponse> getParkingChannels(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @GET("getlivecat")
    Call<LiveCategory> getLiveCategory(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @GET("getlivestreams")
    Call<com.siti.mobile.Model.RetroFit.LiveStream> getLiveStream(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @GET("getlivestreams/catchupchannels")
    Call<CatchupChannelsResponse> getCatchupChannels(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @GET("getlandingchannel")
    Call<LandingChannelResponse> getLandingChannel(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @POST("channelstatistics")
    Call<ChannelStatisticsResponse> postChannelStatistics(@Header("Authorization") String authorization, @Body String body);

    @Headers("Content-Type: application/json")
    @GET("getmobfingerprint")
    Call<String> getFingerprint(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @GET("getAdvertisment")
    Call<AdvertismentResponse> getAdvertisment(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @GET("getuserEngineering")
    Call<EngineeringResponse> getUserEngineering(@Header("Authorization") String authorization);


    @Headers("Content-Type: application/json")
    @GET("getappupdate")
    Call<AppUpdateResponse> getAppUpdate(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @GET("getPackageExpiry")
    Call<PackageExpiryResponse> getPackageExpiry(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @GET("getTuneVersion")
    Call<TuneVersionResponse> getTuneVersion(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @GET("getareacode")
    Call<AreaCodeResponse> getAreaCode(@Header("Authorization") String authorization);

}
