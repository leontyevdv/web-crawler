package io.leontyev.crawler.parser;

import io.leontyev.crawler.parser.page.HtmlPageScripts;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class JavaScriptLibrariesHtmlPageParser implements HtmlPageParser<HtmlPageScripts, Document> {

    private static final Logger LOG = LoggerFactory.getLogger(JavaScriptLibrariesHtmlPageParser.class);

    private static final String SCRIPT_ELEMENT_SELECTOR = "script[src]";
    private static final String ABSOLUTE_SRC_ATTRIBUTE = "abs:src";

    @Override
    public HtmlPageScripts parse(Document result) {
        LOG.info("Parse: {}", result.location());

        Elements scriptElements = result.select(SCRIPT_ELEMENT_SELECTOR);

        List<String> scriptLinks = scriptElements.stream()
                .map(element -> element.attr(ABSOLUTE_SRC_ATTRIBUTE))
                .collect(Collectors.toList());

        return new HtmlPageScripts(scriptLinks);
    }
}
