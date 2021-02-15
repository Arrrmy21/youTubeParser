package com.example.onyshchenko.youtubeparser.controllers;

import com.example.onyshchenko.youtubeparser.dto.YouTubeChannelInfo;
import com.example.onyshchenko.youtubeparser.dto.YouTubeVideoInfo;
import com.example.onyshchenko.youtubeparser.dto.YouTubeVideosSearchInfo;
import com.example.onyshchenko.youtubeparser.services.YouTubeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/youtube")
@Validated
public class YouTubeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(YouTubeController.class);
    private static final String CHANNEL_PATTERN = "^UC[a-z,A-Z,0-9,!@#\\\\$%\\\\^\\\\&*\\\\)\\\\(+=._-]*";
    private static final String NO_CONTENT = "No content found by url.";

    @Autowired
    private YouTubeService youTubeService;

    @GetMapping(value = "/video/{id}", produces = {"application/json"})
    public ResponseEntity<Object> getVideoInfo(@PathVariable(value = "id") @Size(min = 11, max = 11) @NotBlank String id) {
        LOGGER.info("getVideoInfo() Controller entering with id: {}", id);
        Optional<YouTubeVideoInfo> videoInfo = youTubeService.getYouTubeVideoInfo(id);

        return videoInfo.<ResponseEntity<Object>>map(y -> new ResponseEntity<>(y, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(NO_CONTENT, HttpStatus.BAD_REQUEST));
    }

    @GetMapping(value = "/channel/{id}", produces = {"application/json"})
    public ResponseEntity<Object> getChannelInfo(@PathVariable(value = "id") @Pattern(regexp = CHANNEL_PATTERN)
                                                 @NotBlank @Size(min = 24, max = 24) String id) {
        LOGGER.info("getChannelInfo() Controller entering with id: {}", id);
        Optional<YouTubeChannelInfo> channelInfo = youTubeService.getYouTubeChannelInfo(id);

        return channelInfo.<ResponseEntity<Object>>map(y -> new ResponseEntity<>(y, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(NO_CONTENT, HttpStatus.BAD_REQUEST));
    }

    @GetMapping(value = "/channel/{id}/videos", produces = {"application/json"})
    public ResponseEntity<Object> getChannelDetailedInfo(@PathVariable(value = "id") @Pattern(regexp = CHANNEL_PATTERN)
                                                         @NotBlank @Size(min = 24, max = 24) String id,
                                                         @RequestParam(required = false, defaultValue = "10")
                                                         @Min(0) int size) {
        LocalDateTime startingTime = LocalDateTime.now();
        LOGGER.info("getChannelDetailedInfo() Controller entering with id: {} and size: {}", id, size);

        Optional<YouTubeVideosSearchInfo> channelInfo = youTubeService.getChannelDetailedInfo(id, size);

        LocalDateTime finishingTime = LocalDateTime.now();
        LOGGER.info("getChannelDetailedInfo() completed in [{}] milliseconds",
                Duration.between(startingTime, finishingTime).toMillis());

        return channelInfo.<ResponseEntity<Object>>map(y -> new ResponseEntity<>(y, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(NO_CONTENT, HttpStatus.BAD_REQUEST));
    }

}
