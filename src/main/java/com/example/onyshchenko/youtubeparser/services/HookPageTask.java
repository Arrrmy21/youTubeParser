package com.example.onyshchenko.youtubeparser.services;

import com.example.onyshchenko.youtubeparser.handler.ContentNotFountException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class HookPageTask implements Callable<HookPageTask> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HookPageTask.class);
    private static final String COOKIES = "VISITOR_INFO1_LIVE=wNMMyO3MtT4; " +
            "LOGIN_INFO=AFmmF2swRQIhAJRMcXF9QwtpMI3zB0X_F2AIWhfsvhTc-elFdDJEvjueAiBNKIZtnbNpQyTx-7COzfdVfkadQvpfWFYv4O9K8g6FUw:" +
            "QUQ3MjNmeHFfc3RUbUF5bFoyYXhBcDVkc0NVaGVpUThja0UzaldQM3ZTczVQb3B3MzlId2hFdDZlMEk1VWxfTmlCaFZVV3B0NF8xWXQxYnBLSENTRC0wNWxzNUFmcm" +
            "JVOHpQcHNlYXJtcmI3eVFkRXNlQkJhOWp3ZkNiaEhNTlgwV3VDckJwdDRpaVBTbG9xTWR3THNPV3FnNXhhM0VZY2Y4N3Q1ZjludWgzeDlHQ1pLVkZiUlI0; " +
            "HSID=AvK69x8e2n_dhh5Un; SSID=AeVVXAHnza6u3wsbQ; APISID=4wnuUu7oN8Rk6JKr/AT3nqjlMMl8MOscUz; " +
            "SAPISID=3_y5JPRmW19eKGsx/Av0WQNEbwCoCzl4bm; __Secure-3PAPISID=3_y5JPRmW19eKGsx/Av0WQNEbwCoCzl4bm; " +
            "_ga=GA1.2.1580475087.1583774890; _gcl_au=1.1.1709582087.1609747243; " +
            "SID=6AfMSZwhyIiw-MeR0tZahqXOCVbsVGb-LaekxCo-uS1MnnElV9vvmE6dNJbmB_eYpS7hnw.; " +
            "__Secure-3PSID=6AfMSZwhyIiw-MeR0tZahqXOCVbsVGb-LaekxCo-uS1MnnEl5pqt3vQMRDSAtRuYzb40Og.; YSC=KwWyQPS1TWw; " +
            "PREF=volume=35&f6=40000080&cvdm=grid&hl=en-GB&tz=Europe.Kiev&repeat=NONE&f1=50000000&al=ru&f4=4000000&f5=30000; " +
            "SIDCC=AJi4QfGEP38mTUaZib0uHoIRXKOeAOFo_Zs-hvxuv2KfmtqpR5OFa_cpK8xbTGc-o4W8jMCDLXAI; " +
            "__Secure-3PSIDCC=AJi4QfHxlNso9QohXRRDAFEfGlJjHf_-y7H7yDno87KEFrSioSr_Ne3n0yjl8NVZ1e7fE0z1HmSm";

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
            Map<String, String> headers = new HashMap<>();
            headers.put("accept-language", "en-GB");
            headers.put("cookie", COOKIES);

            document = Jsoup.connect(url)
                    .headers(headers)
                    .get();

        } catch (HttpStatusException e) {
            LOGGER.warn("HttpStatusException while getting document by address: {}. HttpStatus: {}", url, e.getStatusCode());
            if (e.getStatusCode() == 429) {
                LOGGER.error("Http error code = 429. Check cookies or other reasons why site blocking requests.");
            }
            throw new ContentNotFountException(e, e.getStatusCode());
        } catch (Exception ex) {
            LOGGER.error("Unknown exception");
        }

        return this;
    }
}
