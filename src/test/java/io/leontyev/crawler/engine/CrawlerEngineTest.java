package io.leontyev.crawler.engine;

import io.leontyev.crawler.downloader.DownloadPageException;
import io.leontyev.crawler.downloader.PageDownloadService;
import io.leontyev.crawler.parser.GoogleHtmlPageParser;
import io.leontyev.crawler.parser.HtmlPageParser;
import io.leontyev.crawler.parser.JavaScriptLibrariesHtmlPageParser;
import io.leontyev.crawler.parser.page.HtmlPageLinks;
import io.leontyev.crawler.parser.page.HtmlPageScripts;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrawlerEngineTest {

    private @Mock Document googlePage;
    private @Mock Document page1;
    private @Mock Document page2;
    private @Mock Document page3;
    private @Mock Document page4;
    private @Mock Document page5;

    private @Mock PageDownloadService downloadService;

    private HtmlPageParser<HtmlPageLinks, Document> googlePageParser = new GoogleHtmlPageParser();
    private HtmlPageParser<HtmlPageScripts, Document> scriptPageParser = new JavaScriptLibrariesHtmlPageParser();

    @Test
    public void testPlainFlow() {
        // given
        givenOk();
        CrawlerEngine engine = new CrawlerEngine(downloadService, googlePageParser, scriptPageParser);

        List<KeyValue> results = new ArrayList<>();
        Consumer<Stream<KeyValue>> resultStreamConsumer = keyValueStream -> keyValueStream.forEach(results::add);

        // when
        engine.crawl("http://localhost:1080/search", 5, resultStreamConsumer);

        // then
        Assert.assertEquals(5, results.size());
    }

    @Test
    public void testGoogleDoesNotRespond() {
        // given
        givenGoogle404();
        CrawlerEngine engine = new CrawlerEngine(downloadService, googlePageParser, scriptPageParser);

        List<KeyValue> results = new ArrayList<>();
        Consumer<Stream<KeyValue>> resultStreamConsumer = keyValueStream -> keyValueStream.forEach(results::add);

        // when
        engine.crawl("http://localhost:1080/search", 5, resultStreamConsumer);
    }

    @Test
    public void testOneOfThePagesDoesNotRespond() {
        // TODO: Work here
    }

    @Test
    public void noLibrariesFound() {
        // TODO: Work here
    }

    private void givenOk() {
        Elements googlePageResults = mockGooglePageElements();

        when(googlePage.location()).thenReturn("http://localhost:1080/search");
        when(googlePage.select(any(String.class))).thenReturn(googlePageResults);
        when(downloadService.download("http://localhost:1080/search")).thenReturn(googlePage);

        Elements scriptResults = mockScriptResults();

        when(page1.location()).thenReturn("http://site1.com/some-uri-1");
        when(page1.select(any(String.class))).thenReturn(scriptResults);
        when(downloadService.download("http://site1.com/some-uri-1")).thenReturn(page1);

        when(page2.location()).thenReturn("http://site2.com/some-uri-2");
        when(page2.select(any(String.class))).thenReturn(scriptResults);
        when(downloadService.download("http://site2.com/some-uri-2")).thenReturn(page2);

        when(page3.location()).thenReturn("http://site3.com/some-uri-3");
        when(page3.select(any(String.class))).thenReturn(scriptResults);
        when(downloadService.download("http://site3.com/some-uri-3")).thenReturn(page3);

        when(page4.location()).thenReturn("http://site4.com/some-uri-4");
        when(page4.select(any(String.class))).thenReturn(scriptResults);
        when(downloadService.download("http://site4.com/some-uri-4")).thenReturn(page4);

        when(page5.location()).thenReturn("http://site5.com/some-uri-5");
        when(page5.select(any(String.class))).thenReturn(scriptResults);
        when(downloadService.download("http://site5.com/some-uri-5")).thenReturn(page5);
    }

    private void givenGoogle404() {
        when(googlePage.location()).thenReturn("http://localhost:1080/search");
        when(downloadService.download("http://localhost:1080/search")).thenThrow(new DownloadPageException("Google doesn't respond"));
    }

    private Elements mockGooglePageElements() {
        Elements googlePageResults = new Elements();
        googlePageResults.add(new Element("a").attr("href", "http://site1.com/some-uri-1"));
        googlePageResults.add(new Element("a").attr("href", "http://site2.com/some-uri-2"));
        googlePageResults.add(new Element("a").attr("href", "http://site3.com/some-uri-3"));
        googlePageResults.add(new Element("a").attr("href", "http://site4.com/some-uri-4"));
        googlePageResults.add(new Element("a").attr("href", "http://site5.com/some-uri-5"));
        return googlePageResults;
    }

    private Elements mockScriptResults() {
        Elements scriptResults = new Elements();
        scriptResults.add(new Element("script").attr("src", "http://site1.com/some-uri-1/jquery1.js"));
        scriptResults.add(new Element("script").attr("src", "http://site2.com/some-uri-2/jquery2.js"));
        scriptResults.add(new Element("script").attr("src", "http://site3.com/some-uri-3/jquery3.js"));
        scriptResults.add(new Element("script").attr("src", "http://site4.com/some-uri-4/jquery4.js"));
        scriptResults.add(new Element("script").attr("src", "http://site5.com/some-uri-5/jquery5.js"));
        return scriptResults;
    }

}