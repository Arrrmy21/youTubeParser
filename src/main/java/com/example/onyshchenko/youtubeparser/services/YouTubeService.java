package com.example.onyshchenko.youtubeparser.services;

import com.example.onyshchenko.youtubeparser.dto.YouTubeVideoInfo;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class YouTubeService {

    public static final String VIDEO_ADDRESS = "https://www.youtube.com/watch?v=";

    @Autowired
    private HtmlHookService htmlHookService;
    @Autowired
    private DocumentParserService documentParserService;

    public Optional<YouTubeVideoInfo> getYouTubeVideoInfo(String videoId) {

        Document document = htmlHookService.getDataFromUrl(VIDEO_ADDRESS + videoId);

        return documentParserService.convertVideoDocumentToData(document);
    }
}
