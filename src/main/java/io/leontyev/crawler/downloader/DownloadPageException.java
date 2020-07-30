package io.leontyev.crawler.downloader;

public class DownloadPageException extends RuntimeException {

    public DownloadPageException(Exception e) {
        super(e);
    }

    public DownloadPageException(String message) {
        super(message);
    }

    public DownloadPageException(String message, Exception e) {
        super(message, e);
    }

}
