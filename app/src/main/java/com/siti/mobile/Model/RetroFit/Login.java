package com.siti.mobile.Model.RetroFit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public  class Login {


    @Expose
    @SerializedName("error")
    private String error;
    @Expose
    @SerializedName("status")
    private String status;
    @Expose
    @SerializedName("message")
    private String message;
    @Expose
    @SerializedName("data")
    private Data data;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        @Expose
        @SerializedName("exp_date")
        private String expDate;
        @Expose
        @SerializedName("connections")
        private int connections;
        @Expose
        @SerializedName("adminId")
        private int adminId;
        @Expose
        @SerializedName("name")
        private String name;
        @Expose
        @SerializedName("username")
        private String username;
        @Expose
        @SerializedName("auth_token")
        private String auth_token;

        @Expose
        @SerializedName("areaCode")
        private String areaCode;
        @Expose
        @SerializedName("isIntervalUpdateInMins")
        private int intervalUpdate;


        @Expose
        @SerializedName("id")
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getExpDate() {
            return expDate;
        }

        public void setExpDate(String expDate) {
            this.expDate = expDate;
        }

        public int getConnections() {
            return connections;
        }

        public void setConnections(int connections) {
            this.connections = connections;
        }

        public int getAdminId() {
            return adminId;
        }

        public void setAdminId(int adminId) {
            this.adminId = adminId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getAuth_token() {
            return auth_token;
        }

        public void setAuth_token(String auth_token) {
            this.auth_token = auth_token;
        }

        public String getAreaCode() {
            return areaCode;
        }

        public void setAreaCode(String areaCode) {
            this.areaCode = areaCode;
        }

        public int getIntervalUpdate() {
            return intervalUpdate;
        }

        public void setIntervalUpdate(int intervalUpdate) {
            this.intervalUpdate = intervalUpdate;
        }
    }
}
