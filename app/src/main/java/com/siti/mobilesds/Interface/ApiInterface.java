package com.siti.mobilesds.Interface;

import com.siti.mobilesds.Model.GeneralResponse;
import com.siti.mobilesds.Model.RetroFit.ChannelStatisticsResponse;
import com.siti.mobilesds.Model.RetroFit.LandingChannelResponse;
import com.siti.mobilesds.Model.RetroFit.LiveCategory;
import com.siti.mobilesds.Model.RetroFit.Login;
import com.siti.mobilesds.Model.advertisment.AdvertismentResponse;
import com.siti.mobilesds.Model.app_update.AppUpdateResponse;
import com.siti.mobilesds.mvvm.common.data.AreaCodeResponse;
import com.siti.mobilesds.mvvm.common.data.BoxModel;
import com.siti.mobilesds.mvvm.common.data.InfoModel;
import com.siti.mobilesds.mvvm.common.data.ParkingChannelsResponse;
import com.siti.mobilesds.mvvm.common.data.ReleaseDateModel;
import com.siti.mobilesds.mvvm.common.data.UserAgentModel;
import com.siti.mobilesds.mvvm.common.data.UserVerificationModel;
import com.siti.mobilesds.mvvm.common.data.models.CatchupChannelsResponse;
import com.siti.mobilesds.mvvm.common.data.post.OnlineCustomerRequest;
import com.siti.mobilesds.network.engineering.EngineeringResponse;
import com.siti.mobilesds.network.package_expiry.PackageExpiryResponse;
import com.siti.mobilesds.network.tune_version.TuneVersionResponse;

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
    Call<com.siti.mobilesds.Model.RetroFit.LiveStream> getLiveStream(@Header("Authorization") String authorization);

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

    @Headers("Content-Type: application/json")
    @GET("getgeneralsettings/info")
    Call<InfoModel> getInfo(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @GET("getUserAgent")
    Call<UserAgentModel> getUserAgent(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @GET("getUserVerification")
    Call<UserVerificationModel> getUserVerification(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @GET("getAppReleaseDate")
    Call<ReleaseDateModel> getReleaseDate(@Header("Authorization") String authorization);

    @Headers("Content-Type: application/json")
    @GET("getboxmodel")
    Call<BoxModel> getBoxModel(@Header("Authorization") String authorization);

}
