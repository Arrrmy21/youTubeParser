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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class HtmlHookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlHookService.class);
    private final WebDriver driver = new ChromeDriver(prepareDriverOptions());

    private static final int THREAD_COUNT = 1000;
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(THREAD_COUNT);

    private ChromeOptions prepareDriverOptions() {
        WebDriverManager.getInstance(DriverManagerType.CHROME).setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-extensions");
        options.addArguments("no-sandbox");

        return options;
    }

    public Document getDocumentWithSelenium(String address, int size) {
        synchronized (driver) {
            try {
                driver.get(address);
                if (size > 30) {
                    LOGGER.info("Total required elements: {}. Scrolling the page.", size);
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("window.scrollTo(0, document.querySelector(\"ytd-app\").scrollHeight)");

                    Thread.sleep(1000);
                }
                return Jsoup.parse(driver.getPageSource());
            } catch (Exception ex) {
                LOGGER.error("Error while getting document from address: {}", address);
                return null;
            }
        }
    }

    public Document getDataFromUrl(String address) {

        HookPageTask task = new HookPageTask(address);
        Document document = null;
        try {
            document = EXECUTOR_SERVICE.submit(task).get().getDocument();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return document;
    }

}
