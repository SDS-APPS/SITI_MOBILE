package com.siti.mobile.Model.JoinData;

public class JoinLiveStreams {
    private int id;
    private int channel_no;
    private String channel_id;
    private String channel_name;
    private String logo;
    private String category_id;
    private String description;
    private String source;
    private String drm_source;
    private int drm_enabled;
    private String category_name;
    private String referenceId;
    private String logoBase64;
    private String isFavorite;
    private int count_viewed;

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    private int price;

    public String getEncryptedSource() {
        return encryptedSource;
    }

    public void setEncryptedSource(String encryptedSource) {
        this.encryptedSource = encryptedSource;
    }

    public String encryptedSource;

    public String getStreamToken() {
        return streamToken;
    }

    public void setStreamToken(String streamToken) {
        this.streamToken = streamToken;
    }

    public String streamToken;

    public int getCatch_up() {
        return catch_up;
    }

    public void setCatch_up(int catch_up) {
        this.catch_up = catch_up;
    }

    public int getRecorder() {
        return recorder;
    }

    public void setRecorder(int recorder) {
        this.recorder = recorder;
    }

    private int catch_up;
    private int recorder;

    private boolean selected;

    public int getCount_viewed() {
        return this.count_viewed;
    }

    public void setCount_viewed(int count_viewed ){
        this.count_viewed = count_viewed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChannel_no() {
        return channel_no;
    }

    public void setChannel_no(int channel_no) {
        this.channel_no = channel_no;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getChannel_name() {
        return channel_name;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
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

    public String getDrm_source() {
        return drm_source;
    }

    public void setDrm_source(String drm_source) {
        this.drm_source = drm_source;
    }

    public int getDrm_enabled() {
        return drm_enabled;
    }

    public void setDrm_enabled(int drm_enabled) {
        this.drm_enabled = drm_enabled;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public String getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(String isFavorite) {
        this.isFavorite = isFavorite;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
