package com.example.onyshchenko.youtubeparser.services;

import com.example.onyshchenko.youtubeparser.dto.ChannelMetadata;
import com.example.onyshchenko.youtubeparser.dto.YouTubeChannelInfo;
import com.example.onyshchenko.youtubeparser.dto.YouTubeVideoInfo;
import com.example.onyshchenko.youtubeparser.dto.YouTubeVideosSearchInfo;
import com.example.onyshchenko.youtubeparser.handler.ContentNotFountException;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class YouTubeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(YouTubeService.class);

    public static final String VIDEO_ADDRESS = "https://www.youtube.com/watch?v=";
    public static final String CHANNEL_ADDRESS = "https://www.youtube.com/channel/";
    public static final String YOUTUBE_ADDRESS = "https://www.youtube.com/";
    public static final String VIDEOS_GRID_ENDING = "/videos?sort=p&flow=grid";

    @Autowired
    private HtmlHookService htmlHookService;
    @Autowired
    private DocumentParserService documentParserService;

    public Optional<YouTubeVideoInfo> getYouTubeVideoInfo(String videoId) {

        Document document = htmlHookService.getDocumentFromUrl(VIDEO_ADDRESS + videoId);

        if (document.getElementById("watch7-content") == null) {
            LOGGER.warn("Information about video content is absent.");
            return Optional.empty();
        }

        return documentParserService.convertVideoDocumentToJavaObject(document);
    }

    public Optional<YouTubeChannelInfo> getYouTubeChannelInfo(String channelId) {

        Document mainChannelPageDocument = htmlHookService.getDocumentFromUrl(CHANNEL_ADDRESS + channelId);
        if (mainChannelPageDocument == null) {
            LOGGER.warn("Information about channel is absent.");
            return Optional.empty();
        }
        Optional<YouTubeChannelInfo> youTubeChannelInfo = documentParserService
                .convertChannelDocumentToJavaObject(mainChannelPageDocument);
        if (youTubeChannelInfo.isPresent()) {

            String urlForMetaData = prepareUrlForMetadata(youTubeChannelInfo.get()) + "/about";
            Document detailedChannelInfo = htmlHookService.getDocumentFromUrl(urlForMetaData);

            ChannelMetadata metadata = documentParserService.getChannelMetaData(detailedChannelInfo);
            youTubeChannelInfo.get().setViews(metadata.getTotalChannelViews());
            youTubeChannelInfo.get().setRegistrationDate(metadata.getDateOfChannelCreation());

            return youTubeChannelInfo;
        } else {
            return Optional.empty();
        }
    }

    private String prepareUrlForMetadata(YouTubeChannelInfo channelInfo) {

        if (channelInfo.getCanonicalChannelName().equals(channelInfo.getId())) {
            return YOUTUBE_ADDRESS + "channel/" + channelInfo.getCanonicalChannelName();
        } else {
            return YOUTUBE_ADDRESS + channelInfo.getCanonicalChannelName();
        }
    }

    public Optional<YouTubeVideosSearchInfo> getChannelDetailedInfo(String id, int size) {

        LOGGER.debug("Entering method getChannelDetailedInfo() with channelId: {} and size: {}", id, size);
        Optional<YouTubeChannelInfo> channelInfo = getYouTubeChannelInfo(id);
        if (!channelInfo.isPresent()) {
            return Optional.empty();
        }
        Document channelVideosPage = getDataFromUrlWithSelenium(channelInfo.get(), size);

        List<YouTubeVideoInfo> videoList = new ArrayList<>(size);
        if (channelVideosPage != null) {
            List<String> idOfChannelVideos = documentParserService.getVideoIdsFromChannelDocument(channelVideosPage, size);

            videoList = idOfChannelVideos.stream().parallel()
                    .map(this::getYouTubeVideoInfo)
                    .filter(Optional::isPresent)
                    .map(Optional::get).collect(Collectors.toList());
        }

        YouTubeVideosSearchInfo response = new YouTubeVideosSearchInfo();
        response.setYouTubeChannelInfo(channelInfo.get());
        response.setSize(videoList.size());
        response.setYouTubeVideoInfoList(videoList);

        return Optional.of(response);
    }

    public Document getDataFromUrlWithSelenium(YouTubeChannelInfo channel, int size) {

        LOGGER.debug("Entering method getDataFromUrlWithSelenium() for channel: {} and size: {}", channel.getId(), size);
        try {
            String address = prepareUrlForMetadata(channel) + VIDEOS_GRID_ENDING;

            return htmlHookService.getDocumentWithSelenium(address, size);
        } catch (Exception ex) {
            LOGGER.info("Error while getting Document.");
            throw new ContentNotFountException(ex);
        }
    }
}