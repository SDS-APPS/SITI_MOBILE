package com.siti.mobile.Model.RetroFit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.util.List;

public class Fingerprint {
    @SerializedName("data")
    @Expose
    private JSONObject data;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("error")
    @Expose
    private String error;

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
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

    public static class Data {


        @SerializedName("duration_date")
        @Expose
        private String durationDate;
        @SerializedName("text")
        @Expose
        private String text;
        @SerializedName("resellerId")
        @Expose
        private Integer resellerId;
        @SerializedName("userId")
        @Expose
        private String userId;
        @SerializedName("duration")
        @Expose
        private Long duration;
        @SerializedName("bg_color")
        @Expose
        private String bgColor;
        @SerializedName("font_color")
        @Expose
        private String fontColor;
        @SerializedName("font_size")
        @Expose
        private String fontSize;
        @SerializedName("font_text")
        @Expose
        private String fontText;
        @SerializedName("package_id")
        @Expose
        private String packageId;
        @SerializedName("x_axis")
        @Expose
        private String xAxis;
        @SerializedName("y_axis")
        @Expose
        private String yAxis;
        @SerializedName("fingerprint_type")
        @Expose
        private String fingerprintType;
        @SerializedName("forced")
        @Expose
        private Integer forced;
        @SerializedName("udid")
        @Expose
        private Integer udid;
        @SerializedName("startDttm")
        @Expose
        private String startDttm;
        @SerializedName("endDttm")
        @Expose
        private String endDttm;


        @SerializedName("areaCode")
        @Expose
        private String areaCode;

        @SerializedName("timeInterval")
        @Expose
        private int timeInterval;

        @SerializedName("repeatTimes")
        @Expose
        private int repeatTimes;

        @SerializedName("visibility")
        @Expose
        private String visibility;

        @SerializedName("backgroundTransperncy")
        @Expose
        private int backgroundTransperncy;

        public String getAreaCode() {
            return areaCode;
        }

        public void setAreaCode(String areaCode) {
            this.areaCode = areaCode;
        }

        public int getTimeInterval() {
            return timeInterval;
        }

        public void setTimeInterval(int timeInterval) {
            this.timeInterval = timeInterval;
        }

        public int getRepeatTimes() {
            return repeatTimes;
        }

        public void setRepeatTimes(int repeatTimes) {
            this.repeatTimes = repeatTimes;
        }

        public String getVisibility() {
            return visibility;
        }

        public void setVisibility(String visibility) {
            this.visibility = visibility;
        }

        public int getBackgroundTransperncy() {
            return backgroundTransperncy;
        }

        public void setBackgroundTransperncy(int backgroundTransperncy) {
            this.backgroundTransperncy = backgroundTransperncy;
        }

        @SerializedName("channels")
        @Expose
        private List<String> channels = null;

        public String getDurationDate() {
            return durationDate;
        }

        public void setDurationDate(String durationDate) {
            this.durationDate = durationDate;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Integer getResellerId() {
            return resellerId;
        }

        public void setResellerId(Integer resellerId) {
            this.resellerId = resellerId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Long getDuration() {
            return duration;
        }

        public void setDuration(Long duration) {
            this.duration = duration;
        }

        public String getBgColor() {
            return bgColor;
        }

        public void setBgColor(String bgColor) {
            this.bgColor = bgColor;
        }

        public String getFontColor() {
            return fontColor;
        }

        public void setFontColor(String fontColor) {
            this.fontColor = fontColor;
        }

        public String getFontSize() {
            return fontSize;
        }

        public void setFontSize(String fontSize) {
            this.fontSize = fontSize;
        }

        public String getFontText() {
            return fontText;
        }

        public void setFontText(String fontText) {
            this.fontText = fontText;
        }

        public String getPackageId() {
            return packageId;
        }

        public void setPackageId(String packageId) {
            this.packageId = packageId;
        }

        public String getxAxis() {
            return xAxis;
        }

        public void setxAxis(String xAxis) {
            this.xAxis = xAxis;
        }

        public String getyAxis() {
            return yAxis;
        }

        public void setyAxis(String yAxis) {
            this.yAxis = yAxis;
        }

        public String getFingerprintType() {
            return fingerprintType;
        }

        public void setFingerprintType(String fingerprintType) {
            this.fingerprintType = fingerprintType;
        }

        public Integer getForced() {
            return forced;
        }

        public void setForced(Integer forced) {
            this.forced = forced;
        }

        public Integer getUdid() {
            return udid;
        }

        public void setUdid(Integer udid) {
            this.udid = udid;
        }

        public String getStartDttm() {
            return startDttm;
        }

        public void setStartDttm(String startDttm) {
            this.startDttm = startDttm;
        }

        public String getEndDttm() {
            return endDttm;
        }

        public void setEndDttm(String endDttm) {
            this.endDttm = endDttm;
        }

        public List<String> getChannels() {
            return channels;
        }

        public void setChannels(List<String> channels) {
            this.channels = channels;
        }
    }


}
