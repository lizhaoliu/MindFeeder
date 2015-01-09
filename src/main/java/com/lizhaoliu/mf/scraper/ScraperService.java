package com.lizhaoliu.mf.scraper;

import com.google.common.collect.ImmutableList;
import com.lizhaoliu.mf.model.NewsEntry;
import com.lizhaoliu.mf.model.NewsEntryRepository;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@EnableScheduling
@Service
public class ScraperService {

  private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);

  private static final int PAGE_SIZE = 20;

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
    updateCache();
  }

  private void updateCache() {
    Jedis jedis = jedisPool.getResource();
    try {
      Page<NewsEntry> page = null;
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
          logger.warn("Failed to update cache: ", e);
        }
      }
    } finally {
      jedisPool.returnResource(jedis);
    }
  }

  public void stop() {
    exec.shutdownNow();
  }
}
