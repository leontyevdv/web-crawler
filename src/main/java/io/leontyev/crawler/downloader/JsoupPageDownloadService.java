package io.leontyev.crawler.downloader;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsoupPageDownloadService implements PageDownloadService {

    private static final Logger LOG = LoggerFactory.getLogger(JsoupPageDownloadService.class);

    private final int timeout;

    public JsoupPageDownloadService(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public Document download(String url) {
        return downloadPage(url);
    }

    private Document downloadPage(String url) {
        LOG.info("Download page: {}", url);

        Document doc;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla").timeout(timeout).get();
        } catch (IllegalArgumentException e) {
            throw new DownloadPageException("Detected malformed url: " + url, e);
        } catch (HttpStatusException e) {
            throw new DownloadPageException("Wrong status: " + e.getStatusCode(), e);
        } catch (IOException e) {
            throw new DownloadPageException("Unable to GET " + url, e);
        }
        return doc;
    }
}
