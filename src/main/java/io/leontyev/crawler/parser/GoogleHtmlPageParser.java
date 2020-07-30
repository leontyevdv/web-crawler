package io.leontyev.crawler.parser;

import io.leontyev.crawler.parser.page.HtmlPageLinks;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class GoogleHtmlPageParser implements HtmlPageParser<HtmlPageLinks, Document> {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleHtmlPageParser.class);
    private static final String HREF_SELECTOR = "div.kCrYT > a[abs:href]";

    @Override
    public HtmlPageLinks parse(Document result) {
        LOG.info("Parse: {}", result.location());

        Elements selectedLinks = result.select(HREF_SELECTOR);
        List<String> parsedLinks = selectedLinks.stream()
                .map(element -> element.attr("abs:href"))
                .collect(Collectors.toList());

        return new HtmlPageLinks(parsedLinks);
    }
}

