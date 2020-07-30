package io.leontyev.crawler.parser;

public interface HtmlPageParser<T, V> {

    T parse(V result);

}
