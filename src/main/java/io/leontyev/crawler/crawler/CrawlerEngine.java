package io.leontyev.crawler.crawler;

import io.leontyev.crawler.downloader.PageDownloadService;
import io.leontyev.crawler.parser.HtmlPageParser;
import io.leontyev.crawler.parser.page.HtmlPageLinks;
import io.leontyev.crawler.parser.page.HtmlPageScripts;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrawlerEngine {

    private static final Logger LOG = LoggerFactory.getLogger(CrawlerEngine.class);

    private final PageDownloadService downloadService;
    private final HtmlPageParser<HtmlPageLinks, Document> googlePageParser;
    private final HtmlPageParser<HtmlPageScripts, Document> jsLibraryParser;
    private final ExecutorService ioExecutorService;
    private final ExecutorService calcExecutorService;

    public CrawlerEngine(PageDownloadService downloadService,
                         HtmlPageParser<HtmlPageLinks, Document> googlePageParser,
                         HtmlPageParser<HtmlPageScripts, Document> jsLibraryParser) {
        this(downloadService,
                googlePageParser,
                jsLibraryParser,

                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, new ThreadFactory() {
                    int count = 1;

                    @Override
                    public Thread newThread(Runnable runnable) {
                        return new Thread(runnable, "io-executor-" + count++);
                    }
                }),

                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
                    int count = 1;

                    @Override
                    public Thread newThread(Runnable runnable) {
                        return new Thread(runnable, "calc-executor-" + count++);
                    }
                }));
    }

    public CrawlerEngine(PageDownloadService downloadService,
                         HtmlPageParser<HtmlPageLinks, Document> googlePageParser,
                         HtmlPageParser<HtmlPageScripts, Document> jsLibraryParser,
                         ExecutorService ioExecutorService,
                         ExecutorService calcExecutorService) {

        this.downloadService = downloadService;
        this.googlePageParser = googlePageParser;
        this.jsLibraryParser = jsLibraryParser;
        this.ioExecutorService = ioExecutorService;
        this.calcExecutorService = calcExecutorService;
    }

    public void crawl(String url, int numResults, Consumer<Stream<KeyValue>> consumer) {
        CompletableFuture<Map<String, Long>> processor = requestGooglePage(url)
                .thenComposeAsync(this::findGoogleResults)
                .thenComposeAsync(this::downloadPages)
                .thenComposeAsync(this::parsePages)
                .thenComposeAsync(this::groupResults)
                .exceptionally(throwable -> {
                    LOG.error(throwable.getMessage(), throwable);
                    return Collections.emptyMap();
                });

        Map<String, Long> jsLibsOccurrences;
        try {
            jsLibsOccurrences = processor.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }

        consumer.accept(jsLibsOccurrences.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed())
                .limit(numResults)
                .map(entry -> new KeyValue(entry.getKey(), entry.getValue())));
    }

    public void stop() {
        ioExecutorService.shutdown();
        try {
            if (!ioExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                ioExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ioExecutorService.shutdownNow();
        }

        calcExecutorService.shutdown();
        try {
            if (!calcExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                calcExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            calcExecutorService.shutdownNow();
        }
    }

    private CompletableFuture<Document> requestGooglePage(String url) {
        return CompletableFuture
                .supplyAsync(() -> downloadService.download(url), ioExecutorService)
                .whenComplete((document, throwable) -> {
                    if (throwable != null) {
                        LOG.error(throwable.getMessage(), throwable);
                    }
                });
    }

    private CompletableFuture<HtmlPageLinks> findGoogleResults(Document googlePage) {
        return CompletableFuture.supplyAsync(() -> googlePageParser.parse(googlePage), calcExecutorService);
    }

    private CompletableFuture<List<Document>> downloadPages(HtmlPageLinks googleResults) {
        List<CompletableFuture<Document>> documents = googleResults.getLinkUrls().stream()
                .map(link -> CompletableFuture
                        .supplyAsync(() -> downloadService.download(link), ioExecutorService)
                        .exceptionally(throwable -> {
                            LOG.error("Error: {}", throwable.getMessage());
                            return null;
                        })
                ).collect(Collectors.toList());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(documents.toArray(new CompletableFuture[0]));

        return allFutures.thenApply(future -> documents.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }

    private CompletableFuture<List<HtmlPageScripts>> parsePages(List<Document> pageResults) {
        List<CompletableFuture<HtmlPageScripts>> pages = pageResults.stream()
                .filter(Objects::nonNull)
                .map(document -> CompletableFuture.supplyAsync(() -> jsLibraryParser.parse(document), calcExecutorService)).collect(Collectors.toList());

        CompletableFuture<Void> allPages = CompletableFuture.allOf(pages.toArray(new CompletableFuture[0]));

        return allPages.thenApply(future -> pages.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }

    private CompletableFuture<Map<String, Long>> groupResults(List<HtmlPageScripts> htmlPages) {
        return CompletableFuture.supplyAsync(() ->
                        htmlPages.stream()
                                .flatMap(htmlPage -> htmlPage.getScriptUrls().stream())
                                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                , calcExecutorService);
    }
}
