package io.leontyev.crawler.parser.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HtmlPageScripts {

    private final List<String> scriptUrls;

    public HtmlPageScripts(List<String> scriptUrls) {
        this.scriptUrls = new ArrayList<>(scriptUrls);
    }

    public List<String> getScriptUrls() {
        return Collections.unmodifiableList(scriptUrls);
    }

}
