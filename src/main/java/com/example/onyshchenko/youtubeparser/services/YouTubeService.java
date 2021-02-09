package com.example.onyshchenko.youtubeparser.services;

import com.example.onyshchenko.youtubeparser.dto.YouTubeChannelInfo;
import com.example.onyshchenko.youtubeparser.dto.YouTubeVideoInfo;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;


@Service
public class YouTubeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(YouTubeService.class);


    public static final String VIDEO_ADDRESS = "https://www.youtube.com/watch?v=";
    public static final String CHANNEL_ADDRESS = "https://www.youtube.com/channel/";
    public static final String YOUTUBE_ADDRESS = "https://www.youtube.com/";

    @Autowired
    private HtmlHookService htmlHookService;
    @Autowired
    private DocumentParserService documentParserService;

    public Optional<YouTubeVideoInfo> getYouTubeVideoInfo(String videoId) {

        Document document = htmlHookService.getDataFromUrl(VIDEO_ADDRESS + videoId);

        if (document.getElementById("watch7-content") == null) {
            LOGGER.warn("Information about video content is absent.");
            return Optional.empty();
        }

        return documentParserService.convertVideoDocumentToData(document);
    }

    public Optional<YouTubeChannelInfo> getYouTubeChannelInfo(String channelId) {

        Document document = htmlHookService.getDataFromUrl(CHANNEL_ADDRESS + channelId);
        if (document == null) {
            LOGGER.warn("Information about channel is absent.");
            return Optional.empty();
        }
        Optional<YouTubeChannelInfo> youTubeChannelInfo = documentParserService.convertChannelDocumentToData(document);
        if (youTubeChannelInfo.isPresent()) {


            String urlForMetaData = prepareUrlForMetadata(youTubeChannelInfo.get());
            Document detailedChannelInfo = htmlHookService.getDataFromUrl(urlForMetaData);

            Map<String, Number> metaDataMap = documentParserService.getChannelMetaData(detailedChannelInfo);

            youTubeChannelInfo.get().setViews(metaDataMap.get("views").intValue());
            youTubeChannelInfo.get().setRegistrationDate(metaDataMap.get("registrationDate").longValue());

            return youTubeChannelInfo;
        } else {
            return Optional.empty();
        }
    }

    private String prepareUrlForMetadata(YouTubeChannelInfo channelInfo) {

        if (channelInfo.getCanonicalChannelName().equals(channelInfo.getId())) {
            return YOUTUBE_ADDRESS + "channel/" + channelInfo.getCanonicalChannelName() + "/about";
        } else {
            return YOUTUBE_ADDRESS + "c/" + channelInfo.getCanonicalChannelName() + "/about";
        }
    }
}
