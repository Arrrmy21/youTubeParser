package com.example.onyshchenko.youtubeparser.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HtmlHookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlHookService.class);

    public Document getDataFromUrl(String address) {

        try {
            LOGGER.info("Getting document from address: [{}]", address);
            Document doc = Jsoup.connect(address).header("accept-language", "en-EN").get();
            String docTitle = doc.title();
            LOGGER.info("Document received from site with title: [{}]", docTitle);

            return doc;
        } catch (Exception ex) {
            LOGGER.error("Error while getting Document.", ex);
            return null;
        }
    }
}
