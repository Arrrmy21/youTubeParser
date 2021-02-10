package com.example.onyshchenko.youtubeparser.services;

import com.example.onyshchenko.youtubeparser.dto.YouTubeChannelInfo;
import com.example.onyshchenko.youtubeparser.dto.YouTubeVideoInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class DocumentParserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParserService.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d", Locale.ENGLISH);
    private static final DateTimeFormatter CHANNEL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final String CONTENT = "content";
    private static final String CONTENTS = "contents";
    private static final String HREF = "href";
    private static final String ITEM = "itemprop";

    public Optional<YouTubeVideoInfo> convertVideoDocumentToData(Document document) {

        try {
            LOGGER.info("Converting document data to YouTubeVideoInfo.");
            YouTubeVideoInfo youTubeVideoInfo = new YouTubeVideoInfo();

            Element element = document.getElementById("watch7-content");

            String views = element.getElementsByAttributeValue(ITEM, "interactionCount").get(0).attributes().get(CONTENT);
            youTubeVideoInfo.setViews(Integer.parseInt(views));

            String datePublished = element.getElementsByAttributeValue(ITEM, "datePublished").get(0).attributes().get(CONTENT);
            LocalDate publishedDate = LocalDate.parse(datePublished, DATE_TIME_FORMATTER);
            Long timeStamp = publishedDate.toEpochDay();
            youTubeVideoInfo.setPublishedDate(timeStamp);

            String preParsedDuration = element.getElementsByAttributeValue(ITEM, "duration").get(0).attributes().get(CONTENT);
            youTubeVideoInfo.setDuration(getDurationOfVideoInSeconds(preParsedDuration));

            String url = element.getElementsByAttributeValue(ITEM, "url").get(0).attributes().get("href");
            youTubeVideoInfo.setUrl(url);

            String title = element.getElementsByAttributeValue(ITEM, "name").get(0).attributes().get(CONTENT);
            youTubeVideoInfo.setTitle(title);

            String videoId = element.getElementsByAttributeValue(ITEM, "videoId").get(0).attributes().get(CONTENT);
            youTubeVideoInfo.setId(videoId);

            return Optional.of(youTubeVideoInfo);
        } catch (Exception ex) {
            LOGGER.warn("Exception during document parsing", ex);
            return Optional.empty();
        }
    }

    private Integer getDurationOfVideoInSeconds(String preParsedDuration) {

        String[] timeValues = preParsedDuration.substring(2).split("M");
        int minutes = Integer.parseInt(timeValues[0]);
        int seconds = Integer.parseInt(timeValues[1].substring(0, timeValues[1].length() - 1));

        return minutes * 60 + seconds;
    }

    public Optional<YouTubeChannelInfo> convertChannelDocumentToJavaObject(Document document) {
        try {
            LOGGER.info("Converting document data to YouTubeChannelInfo.");

            YouTubeChannelInfo youTubeChannelInfo = new YouTubeChannelInfo();

            String url = document.getElementsByAttributeValue(ITEM, "url").get(0).attributes().get(HREF);
            youTubeChannelInfo.setUrl(url);

            JsonObject channelNameJson = getNestedJsonObject(document, true);
            String channelName = channelNameJson.getAsJsonPrimitive("canonicalBaseUrl").getAsString();
            youTubeChannelInfo.setCanonicalChannelName(channelName.substring(1));

            String channelId = document.getElementsByAttributeValue(ITEM, "channelId").get(0).attributes().get(CONTENT);
            youTubeChannelInfo.setId(channelId);

            return Optional.of(youTubeChannelInfo);
        } catch (Exception ex) {
            LOGGER.warn("Exception during CHANNEL document parsing", ex);
            return Optional.empty();
        }
    }

    public Map<String, Number> getChannelMetaData(Document document) {
        Map<String, Number> metadataMap = new HashMap<>();

        try {
            JsonObject commonJson = getNestedJsonObject(document, false);

            Integer totalChannelViews = prepareViewsFromJson(commonJson.getAsJsonObject("viewCountText"));
            metadataMap.put("views", totalChannelViews);

            Long dateOfChannelCreation = prepareCreationDateFromJson(commonJson.get("joinedDateText")
                    .getAsJsonObject().get("runs").getAsJsonArray().get(1).getAsJsonObject());
            metadataMap.put("registrationDate", dateOfChannelCreation);

        } catch (Exception ex) {
            LOGGER.warn("Exception while getting detailed CHANNEL info.", ex);
        }
        return metadataMap;
    }

    private JsonObject getNestedJsonObject(Document document, boolean isMainChannelPage) {
        JsonObject actualObj = prepareDocumentAsJson(document);

        if (isMainChannelPage) {
            return getCanonicalChannelNameFromJson(actualObj);
        } else {
            return getChannelMetadataFromJson(actualObj);
        }

    }

    private Integer prepareViewsFromJson(JsonObject viewCountText) {

        String preParsedViewsValue = viewCountText.get("simpleText").getAsString();
        String preparedForParsingValue = preParsedViewsValue.substring(0, preParsedViewsValue.length() - 6).replace(",", "");

        return Integer.parseInt(preparedForParsingValue);
    }

    private Long prepareCreationDateFromJson(JsonObject asJsonObject) {

        String preParsedCreationDate = asJsonObject.get("text").getAsString();

        LocalDate publishedDate = LocalDate.parse(preParsedCreationDate, CHANNEL_DATE_TIME_FORMATTER);

        return publishedDate.toEpochDay();
    }

    public List<String> getVideoIdsFromChannelDocument(Document document, int requiredSize) {

        try {
            Elements videos = document.getElementsByClass("style-scope ytd-grid-renderer").select("ytd-grid-video-renderer");

            List<String> videoIds = new ArrayList<>();

            int actualVideosAmount = Math.min(Math.min(videos.size(), requiredSize), 50);

            for (int i = 0; i < actualVideosAmount; i++) {
                Element videoElement = videos.get(i);
                String videoId = videoElement.getElementsByClass("yt-simple-endpoint style-scope ytd-grid-video-renderer")
                        .get(0).attributes().get("href");
                videoIds.add(videoId.substring(9));
            }

            return videoIds;
        } catch (Exception ex) {
            LOGGER.error("Error while parsing document with channel's videos");
            return Collections.emptyList();
        }
    }

    private JsonObject prepareDocumentAsJson(Document document) {
        String views = document.body().childNodes().get(10).childNodes().get(0).toString();
        String formattedJson = views.substring(20, views.length() - 1);

        return new Gson().fromJson(formattedJson, JsonObject.class);
    }

    private JsonObject getChannelMetadataFromJson(JsonObject actualObj) {
        return actualObj.getAsJsonObject(CONTENTS).getAsJsonObject("twoColumnBrowseResultsRenderer").getAsJsonArray("tabs").get(5).getAsJsonObject()
                .getAsJsonObject("tabRenderer").getAsJsonObject(CONTENT).getAsJsonObject("sectionListRenderer").getAsJsonArray(CONTENTS)
                .getAsJsonArray().get(0).getAsJsonObject().getAsJsonObject("itemSectionRenderer").getAsJsonArray(CONTENTS).get(0).getAsJsonObject()
                .getAsJsonObject("channelAboutFullMetadataRenderer").getAsJsonObject();

    }

    private JsonObject getCanonicalChannelNameFromJson(JsonObject actualObj) {
        return actualObj.getAsJsonObject(CONTENTS).getAsJsonObject("twoColumnBrowseResultsRenderer").getAsJsonArray("tabs").get(5).getAsJsonObject()
                .getAsJsonObject("tabRenderer").getAsJsonObject("endpoint").getAsJsonObject("browseEndpoint");
    }
}
