package io.leontyev.crawler.downloader;

import org.jsoup.nodes.Document;

public interface PageDownloadService {

    Document download(String url);

}
