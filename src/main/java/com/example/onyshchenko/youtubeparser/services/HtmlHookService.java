package com.example.onyshchenko.youtubeparser.services;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class HtmlHookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlHookService.class);
    private final WebDriver driver = new ChromeDriver(prepareDriverOprions());

    private ChromeOptions prepareDriverOprions() {
        WebDriverManager.getInstance(DriverManagerType.CHROME).setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-extensions");
        options.addArguments("no-sandbox");

        return options;
    }

    public Document getDocumentWithSelenium(String address, int size) {
        try {
            driver.get(address);
            if (size > 30) {
                driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("window.scrollBy(0,5000)", "");
            }
            return Jsoup.parse(driver.getPageSource());
        } catch (Exception ex) {
            LOGGER.error("Error while getting document from address: {}", address);
            return null;
        }
    }

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
