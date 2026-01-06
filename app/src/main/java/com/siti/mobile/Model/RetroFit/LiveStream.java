package com.siti.mobile.Model.RetroFit;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LiveStream {
    @SerializedName("data")
    @Expose
    public List<LiveStreamData> data = null;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("error")
    @Expose
    public String error;

    public List<LiveStreamData> getData() {
        return data;
    }

    public void setData(List<LiveStreamData> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public static class LiveStreamData {

        @SerializedName("channel_no")
        @Expose
        public Integer channelNo;
        @SerializedName("channel_id")
        @Expose
        public String channelId;
        @SerializedName("channel_name")
        @Expose
        public String channelName;
        @SerializedName("logo")
        @Expose
        public Object logo;
        @SerializedName("category_id")
        @Expose
        public List<Integer> categoryId;
        @SerializedName("description")
        @Expose
        public String description;
        @SerializedName("source")
        @Expose
        public String source;
        @SerializedName("drm_source")
        @Expose
        public Object drmSource;
        @SerializedName("drm_enabled")
        @Expose
        public Integer drmEnabled;

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer price) {
            this.price = price;
        }

        @SerializedName("price")
        @Expose
        public Integer price;

        public String getEncryptedSource() {
            return encryptedSource;
        }

        public void setEncryptedSource(String encryptedSource) {
            this.encryptedSource = encryptedSource;
        }

        @SerializedName("encrypted_source")
        @Expose
        public String encryptedSource;

        public String getStreamToken() {
            return streamToken;
        }

        public void setStreamToken(String streamToken) {
            this.streamToken = streamToken;
        }

        @SerializedName("stream_token")
        @Expose
        public String streamToken;

        public Integer getCatch_up() {
            return catch_up;
        }

        public void setCatch_up(Integer catch_up) {
            this.catch_up = catch_up;
        }

        public Integer getRecorder() {
            return recorder;
        }

        public void setRecorder(Integer recorder) {
            this.recorder = recorder;
        }

        @SerializedName("catch_up")
        @Expose
        public Integer catch_up;
        @SerializedName("recorder")
        @Expose
        public Integer recorder;
        @SerializedName("category_name")
        @Expose
        public String categoryName;

        public Integer getChannelNo() {
            return channelNo;
        }

        public void setChannelNo(Integer channelNo) {
            this.channelNo = channelNo;
        }

        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

        public String getChannelName() {
            return channelName;
        }

        public void setChannelName(String channelName) {
            this.channelName = channelName;
        }

        public Object getLogo() {
            return logo;
        }

        public void setLogo(Object logo) {
            this.logo = logo;
        }

        public List<Integer> getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(List<Integer> categoryId) {
            this.categoryId = categoryId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public Object getDrmSource() {
            return drmSource;
        }

        public void setDrmSource(Object drmSource) {
            this.drmSource = drmSource;
        }

        public Integer getDrmEnabled() {
            return drmEnabled;
        }

        public void setDrmEnabled(Integer drmEnabled) {
            this.drmEnabled = drmEnabled;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }
    }
}