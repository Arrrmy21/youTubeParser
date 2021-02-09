package com.example.onyshchenko.youtubeparser.services;

import com.example.onyshchenko.youtubeparser.dto.YouTubeVideoInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class DocumentParserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParserService.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d");
    private static final String CONTENT = "content";
    private static final String ITEM = "itemprop";

    public Optional<YouTubeVideoInfo> convertVideoDocumentToData(Document document) {

        try {
            YouTubeVideoInfo youTubeVideoInfo = new YouTubeVideoInfo();

            Element element = document.getElementById("watch7-content");
            if (element == null) {
                LOGGER.warn("Information about video content is absent.");
                return Optional.empty();
            }

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
}
