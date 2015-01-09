package com.lizhaoliu.mf.scraper;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@EnableScheduling
@Service
public class ScraperService {

  private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);

  private List<? extends WebScraper> scraperList = ImmutableList.of(
    new RedditScraper("http://www.reddit.com/r/programming",
      "http://www.reddit.com/r/java/",
      "http://www.reddit.com/r/coding"),
    new HackerNewsScraper("https://news.ycombinator.com/"),
    new InfoQScraper("http://www.infoq.com/news/",
      "http://www.infoq.com/cn/news")
  );

  private ExecutorService exec = Executors.newCachedThreadPool(new ThreadFactory() {
    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r);
      t.setDaemon(true);
      return t;
    }
  });

  /**
   * initialDelay is to offset the time initializing ApplicationContext
   */
  @Scheduled(fixedRate = 1L * 1000 * 1800, initialDelay = 3000L)
  public void scrapePages() {
    List<Future<?>> futures = new ArrayList<>();
    for (final WebScraper scraper : scraperList) {
      logger.info("Started scraping scheduled task");
      Future<?> future = exec.submit(new Runnable() {
        @Override
        public void run() {
          logger.info(scraper.toString() + " has started.");
          scraper.scrapePage();
        }
      });
      futures.add(future);
    }
    for (Future<?> future : futures) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        logger.error("An error occured while scraping page: ", e);
      }
    }
  }

  public void stop() {
    exec.shutdownNow();
  }
}
