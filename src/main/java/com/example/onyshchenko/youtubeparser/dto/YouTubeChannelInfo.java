package com.example.onyshchenko.youtubeparser.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class YouTubeChannelInfo {

    private String id;
    private String url;
    private Long registrationDate;
    private int views;
    @JsonIgnore
    private String canonicalChannelName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Long registrationDate) {
        this.registrationDate = registrationDate;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getCanonicalChannelName() {
        return canonicalChannelName;
    }

    public void setCanonicalChannelName(String canonicalChannelName) {
        this.canonicalChannelName = canonicalChannelName;
    }
}
