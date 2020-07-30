package io.leontyev.crawler.parser.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HtmlPageLinks {

    private final List<String> linkUrls;

    public HtmlPageLinks(List<String> linkUrls) {
        this.linkUrls = new ArrayList<>(linkUrls);
    }

    public List<String> getLinkUrls() {
        return Collections.unmodifiableList(linkUrls);
    }

}
