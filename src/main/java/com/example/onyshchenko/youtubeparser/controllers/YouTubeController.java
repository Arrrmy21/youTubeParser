package com.example.onyshchenko.youtubeparser.controllers;

import com.example.onyshchenko.youtubeparser.dto.YouTubeVideoInfo;
import com.example.onyshchenko.youtubeparser.services.YouTubeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/youtube")
public class YouTubeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(YouTubeController.class);

    @Autowired
    private YouTubeService youTubeService;

    @GetMapping(value = "/videos/{id}", produces = {"application/json"})
    public YouTubeVideoInfo getVideoInfo(@PathVariable(value = "id") String id) {

        LOGGER.info("getVideoInfo() Controller entering with id: {}", id);
        Optional<YouTubeVideoInfo> videoInfo = youTubeService.getYouTubeVideoInfo(id);

        return videoInfo.orElse(null);
    }

}
