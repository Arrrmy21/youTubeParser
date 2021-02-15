package com.example.onyshchenko.youtubeparser.dto;

public class ChannelMetadata {

    private Integer totalChannelViews;
    private Long dateOfChannelCreation;

    public ChannelMetadata() {
    }

    public ChannelMetadata(Integer totalChannelViews, Long dateOfChannelCreation) {
        this.totalChannelViews = totalChannelViews;
        this.dateOfChannelCreation = dateOfChannelCreation;
    }

    public Integer getTotalChannelViews() {
        return totalChannelViews;
    }

    public void setTotalChannelViews(Integer totalChannelViews) {
        this.totalChannelViews = totalChannelViews;
    }

    public Long getDateOfChannelCreation() {
        return dateOfChannelCreation;
    }

    public void setDateOfChannelCreation(Long dateOfChannelCreation) {
        this.dateOfChannelCreation = dateOfChannelCreation;
    }
}
