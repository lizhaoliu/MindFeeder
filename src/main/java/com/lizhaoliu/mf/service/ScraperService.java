package com.lizhaoliu.mf.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.lizhaoliu.mf.model.NewsEntry;
import com.lizhaoliu.mf.model.NewsEntryRepository;
import com.lizhaoliu.mf.scraper.HackerNewsScraper;
import com.lizhaoliu.mf.scraper.InfoQScraper;
import com.lizhaoliu.mf.scraper.RedditScraper;
import com.lizhaoliu.mf.scraper.WebScraper;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

@EnableScheduling
@Service
public class ScraperService {

  private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);

  private static final int PAGE_SIZE = 20;

  private final BlockingQueue<NewsEntry> newsEntryBlockingQueue = Queues.newLinkedBlockingQueue();

  @Autowired
  private JedisPool jedisPool;

  @Autowired
  private NewsEntryRepository newsEntryRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private List<? extends WebScraper> scraperList = ImmutableList.of(
      new RedditScraper(
          "http://www.reddit.com/r/programming",
          "http://www.reddit.com/r/java/",
          "http://www.reddit.com/r/coding"
      ),
      new HackerNewsScraper(
          "https://news.ycombinator.com/"
      ),
      new InfoQScraper(
          "http://www.infoq.com/news/"
//      "http://www.infoq.com/cn/news"
      )
  );

  private ExecutorService exec = Executors.newCachedThreadPool(new ThreadFactory() {
    @Override
    public Thread newThread(Runnable runnable) {
      Thread thread = new Thread(runnable);
      thread.setName("ScraperServiceWorker");
      thread.setDaemon(true);
      return thread;
    }
  });

  /**
   * Add an instance of {@link com.lizhaoliu.mf.model.NewsEntry} into entity queue which will be saved into DB
   *
   * @param newsEntry instance of an {@link com.lizhaoliu.mf.model.NewsEntry}
   * @return true if the element was successfully added to the entity queue, else false
   */
  public boolean save(@Nonnull NewsEntry newsEntry) {
    Preconditions.checkNotNull(newsEntry, "newsEntry should not be null.");

    return newsEntryBlockingQueue.offer(newsEntry);
  }

  /**
   * Stop the scraper service
   */
  public void stop() {
    exec.shutdownNow();
  }

  /**
   * initialDelay is to offset the time initializing ApplicationContext
   */
  @Scheduled(fixedRate = 1L * 1000 * 1800, initialDelay = 3000L)
  public void scrapePages() {
    doScrapePages();
    updateDb();
    updateCache();
  }

  private void doScrapePages() {
    List<Future<?>> futures = new ArrayList<>();
    for (final WebScraper scraper : scraperList) {
      logger.info("Started scraping scheduled task");
      Future<?> future = exec.submit(() -> {
        logger.info(scraper.toString() + " has started.");
        scraper.scrapePage();
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

  private void updateDb() {
    int numSaved = 0;
    while (!newsEntryBlockingQueue.isEmpty()) {
      NewsEntry newsEntry = null;
      try {
        newsEntry = newsEntryBlockingQueue.take();
        if (newsEntryRepository.findByLink(newsEntry.getLink()) == null) {
          newsEntryRepository.save(newsEntry);
          ++numSaved;
        }
      } catch (Exception e) {
        logger.warn(String.format("An exception happened while taking and saving %s from the queue: ", newsEntry) +
            e.getMessage());
      }
    }
    logger.info(String.format("Completed saving %d entities into database", numSaved));
  }

  private void updateCache() {
    Jedis jedis = jedisPool.getResource();
    try {
      Page<NewsEntry> page;
      for (int pageId = 0;
           (page = newsEntryRepository.findAll(new PageRequest(pageId, PAGE_SIZE, Sort.Direction.DESC, "dateTime")))
               .getNumberOfElements() > 0;
           ++pageId) {
        try {
          final String json = objectMapper.writeValueAsString(page);
          final String key = "/news" + pageId;
          jedis.set(key, json);
          logger.info("Updated Redis on key = " + key);
        } catch (IOException e) {
          logger.warn("Failed to update Redis: " + e.getMessage());
        }
      }
    } finally {
      jedisPool.returnResource(jedis);
    }
  }
}
