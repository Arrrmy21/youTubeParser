package com.example.onyshchenko.youtubeparser.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class HookPageTask implements Callable<HookPageTask> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HookPageTask.class);
    private final String url;
    private Document document;

    public Document getDocument() {
        return document;
    }

    public HookPageTask(String url) {
        this.url = url;
    }

    @Override
    public HookPageTask call() {

        try {
            LOGGER.info("Calling task by address: {}", url);
            document = Jsoup.connect(url).header("accept-language", "en-EN").get();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }
}
