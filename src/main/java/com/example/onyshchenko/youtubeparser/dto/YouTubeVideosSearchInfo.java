package com.example.onyshchenko.youtubeparser.dto;

import java.util.List;

public class YouTubeVideosSearchInfo {

    private YouTubeChannelInfo youTubeChannelInfo;
    private int size;
    private List<YouTubeVideoInfo> youTubeVideoInfoList;

    public YouTubeChannelInfo getYouTubeChannelInfo() {
        return youTubeChannelInfo;
    }

    public void setYouTubeChannelInfo(YouTubeChannelInfo youTubeChannelInfo) {
        this.youTubeChannelInfo = youTubeChannelInfo;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<YouTubeVideoInfo> getYouTubeVideoInfoList() {
        return youTubeVideoInfoList;
    }

    public void setYouTubeVideoInfoList(List<YouTubeVideoInfo> youTubeVideoInfoList) {
        this.youTubeVideoInfoList = youTubeVideoInfoList;
    }
}
