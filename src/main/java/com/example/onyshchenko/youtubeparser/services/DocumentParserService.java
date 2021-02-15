package com.example.onyshchenko.youtubeparser.services;

import com.example.onyshchenko.youtubeparser.dto.ChannelMetadata;
import com.example.onyshchenko.youtubeparser.dto.YouTubeChannelInfo;
import com.example.onyshchenko.youtubeparser.dto.YouTubeVideoInfo;
import com.example.onyshchenko.youtubeparser.handler.ContentNotFountException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class DocumentParserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParserService.class);

    private static final DateTimeFormatter VIDEO_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d", Locale.ENGLISH);
    private static final DateTimeFormatter CHANNEL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private static final String CONTENT = "content";
    private static final String HREF = "href";
    private static final String ITEM = "itemprop";

    public Optional<YouTubeVideoInfo> convertVideoDocumentToJavaObject(Document document) {

        try {
            LOGGER.info("Converting document data to YouTubeVideoInfo.");
            YouTubeVideoInfo youTubeVideoInfo = new YouTubeVideoInfo();

            Element element = document.getElementById("watch7-content");

            String views = element.getElementsByAttributeValue(ITEM, "interactionCount").get(0).attributes().get(CONTENT);
            youTubeVideoInfo.setViews(Integer.parseInt(views));

            String datePublished = element.getElementsByAttributeValue(ITEM, "datePublished").get(0).attributes().get(CONTENT);
            LocalDate publishedDate = LocalDate.parse(datePublished, VIDEO_DATE_TIME_FORMATTER);
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

            DocumentContext context = prepareDocumentContextOfValuableData(document);
            String channelName = getCanonicalChannelNameFromContext(Objects.requireNonNull(context));
            youTubeChannelInfo.setCanonicalChannelName(channelName.substring(1));

            String channelId = document.getElementsByAttributeValue(ITEM, "channelId").get(0).attributes().get(CONTENT);
            youTubeChannelInfo.setId(channelId);

            return Optional.of(youTubeChannelInfo);
        } catch (Exception ex) {
            LOGGER.warn("Exception during CHANNEL document parsing: {}", document.title());
            throw new ContentNotFountException(ex);
        }
    }

    public ChannelMetadata getChannelMetaData(Document document) {

        DocumentContext context = prepareDocumentContextOfValuableData(document);

        Map<String, String> dateOfPublishing = getChannelMetadataFromContext(Objects.requireNonNull(context));

        Integer totalChannelViews = prepareChannelViews(dateOfPublishing);
        Long dateOfChannelCreation = prepareCreationDateFromStringValue(dateOfPublishing);

        return new ChannelMetadata(totalChannelViews, dateOfChannelCreation);
    }

    private Integer prepareChannelViews(Map<String, String> dateOfPublishing) {
        try {
            String channelViewsValue = JsonPath.parse(dateOfPublishing).read("$['viewCountText'].simpleText");

            return prepareViewsFromStringValue(channelViewsValue);
        } catch (Exception ex) {
            LOGGER.warn("Exception during getting views of channel. Check if they are present.");
            return 0;
        }
    }

    private Integer prepareViewsFromStringValue(String viewCountText) {
        String preparedForParsingValue;
        if (viewCountText.contains("views")) {
            preparedForParsingValue = viewCountText.substring(0, viewCountText.length() - 6).replace(",", "");
        } else {
            preparedForParsingValue = viewCountText.substring(0, viewCountText.length() - 11).replace("\u00A0", "");
        }

        return Integer.parseInt(preparedForParsingValue);
    }

    private Long prepareCreationDateFromStringValue(Map<String, String> dateOfPublishing) {
        try {
            String channelCreationDate = JsonPath.parse(dateOfPublishing).read("$.joinedDateText.runs[1].text");

            LocalDate publishedDate = LocalDate.parse(channelCreationDate, CHANNEL_DATE_TIME_FORMATTER);

            return publishedDate.toEpochDay();
        } catch (DateTimeParseException parseException) {
            LOGGER.warn("Date formatting exception. Probably month of creation 'Sept' that is out of pattern");
            String channelCreationDate = JsonPath.parse(dateOfPublishing).read("$.joinedDateText.runs[1].text");

            String[] date = channelCreationDate.split(" ");
            if (date[1].equalsIgnoreCase("Sept")) {
                String newDate = date[0] + " Sep " + date[2];
                LocalDate publishedDate = LocalDate.parse(newDate, CHANNEL_DATE_TIME_FORMATTER);

                return publishedDate.toEpochDay();
            } else return 0L;
        } catch (Exception ex) {
            LOGGER.error("Exception while formatting Date of channel creation.");
            return 0L;
        }
    }

    public List<String> getVideoIdsFromChannelDocument(Document document, int requiredSize) {

        try {
            Elements videos = document.getElementsByClass("style-scope ytd-grid-renderer").select("ytd-grid-video-renderer");
            LOGGER.debug("There are {} videos in document", videos.size());
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

    private DocumentContext prepareDocumentContextOfValuableData(Document document) {

        Optional<Node> node = document.body().childNodes().stream()
                .filter(e -> !e.childNodes().isEmpty())
                .filter(e -> e.childNode(0).toString().contains("var ytInitialData")).findFirst();

        if (!node.isPresent()) {
            return null;
        }
        String extractedStringBody = node.get().childNodes().get(0).toString();
        String formattedJson = extractedStringBody.substring(20, extractedStringBody.length() - 1);

        return JsonPath.parse(formattedJson);
    }

    private Map<String, String> getChannelMetadataFromContext(DocumentContext actualObj) {
        return actualObj.read("$['contents'].twoColumnBrowseResultsRenderer.tabs[5]" +
                ".tabRenderer.content.sectionListRenderer" +
                ".contents[0].itemSectionRenderer.contents[0].channelAboutFullMetadataRenderer");

    }

    private String getCanonicalChannelNameFromContext(DocumentContext actualObj) {
        return actualObj.read(("$['contents'].twoColumnBrowseResultsRenderer.tabs[5]" +
                ".tabRenderer.endpoint.browseEndpoint.canonicalBaseUrl"));
    }
}
