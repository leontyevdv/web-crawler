package io.leontyev.crawler;

import io.leontyev.crawler.crawler.CrawlerEngine;
import io.leontyev.crawler.crawler.KeyValue;
import io.leontyev.crawler.downloader.JsoupPageDownloadService;
import io.leontyev.crawler.downloader.PageDownloadService;
import io.leontyev.crawler.parser.GoogleHtmlPageParser;
import io.leontyev.crawler.parser.HtmlPageParser;
import io.leontyev.crawler.parser.JavaScriptLibrariesHtmlPageParser;
import io.leontyev.crawler.parser.page.HtmlPageLinks;
import io.leontyev.crawler.parser.page.HtmlPageScripts;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CrawlerApp {

    private static final Logger LOG = LoggerFactory.getLogger(CrawlerApp.class);
    private static final String GOOGLE_QUERY = "http://www.google.com/search?&ie=utf-8&oe=utf-8&q=";

    public static void main(String[] args) {
        Optional<String> searchTerm = Arrays.stream(args).findFirst();
        if (!searchTerm.isPresent()) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Please enter a query searchTerm: ");
            searchTerm = Optional.of(sc.nextLine());
        }

        String userQuery = encodeUserQuery(searchTerm.get());
        LOG.info("Requested: {}", userQuery);

        String url = GOOGLE_QUERY + userQuery;
        PageDownloadService downloadService = new JsoupPageDownloadService(2000);
        HtmlPageParser<HtmlPageLinks, Document> googleParser = new GoogleHtmlPageParser();
        HtmlPageParser<HtmlPageScripts, Document> jsLibsParser = new JavaScriptLibrariesHtmlPageParser();

        CrawlerEngine engine = new CrawlerEngine(downloadService, googleParser, jsLibsParser);
        Consumer<Stream<KeyValue>> resultConsumer = keyValueStream -> keyValueStream.forEach(System.out::println);

        engine.crawl(url, 5, resultConsumer);

        engine.stop();
    }

    private static String encodeUserQuery(String searchTerm) {
        try {
            return URLEncoder.encode(searchTerm, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
