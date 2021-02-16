package com.example.onyshchenko.youtubeparser.dto;

public class YouTubeVideoInfo {

    private String id;
    private String url;
    private String title;
    private Integer duration;
    private Long publishedDate;
    //TODO: change to Long value. Max int value 2,147,483,647 while there are videos with more views
    private Integer views;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Long getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(Long publishedDate) {
        this.publishedDate = publishedDate;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }
}
