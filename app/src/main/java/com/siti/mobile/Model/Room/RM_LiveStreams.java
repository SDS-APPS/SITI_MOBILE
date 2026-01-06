package com.siti.mobile.Model.Room;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Live_Streams")
public class RM_LiveStreams {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo
    private int channel_no;

    @ColumnInfo
    private String channel_id;

    @ColumnInfo
    private String channel_name;


    @ColumnInfo
    private String logo;

    @ColumnInfo
    private String category_id;

    @ColumnInfo
    private String description;

    @ColumnInfo
    private String source;

    @ColumnInfo
    private String drm_source;
    @ColumnInfo
    private int drm_enabled;

    @ColumnInfo
    private int catch_up;

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @ColumnInfo
    private int price;

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

    @ColumnInfo
    private int recorder;
    @ColumnInfo
    private String category_name;

    @ColumnInfo(name = "count_viewed")
    private Integer count_viewed = 0;

    public String getEncryptedSource() {
        return encryptedSource;
    }

    public void setEncryptedSource(String encryptedSource) {
        this.encryptedSource = encryptedSource;
    }

    @ColumnInfo
    public String encryptedSource;

    @ColumnInfo
    private boolean selected;

    public String getStreamToken() {
        return streamToken;
    }

    public void setStreamToken(String streamToken) {
        this.streamToken = streamToken;
    }

    @ColumnInfo
    public String streamToken;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getCount_viewed() {
        return count_viewed;
    }

    public void setCount_viewed(int count) {
        this.count_viewed = count;
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
}
