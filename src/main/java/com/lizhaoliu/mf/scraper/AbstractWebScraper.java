package com.lizhaoliu.mf.scraper;

import java.util.List;

import javax.annotation.Nonnull;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.lizhaoliu.mf.app.Application;
import com.lizhaoliu.mf.model.NewsEntry;
import com.lizhaoliu.mf.model.NewsEntryRepository;

public abstract class AbstractWebScraper implements WebScraper {

  private static final Logger logger = LoggerFactory.getLogger(AbstractWebScraper.class);

  static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  static final String HREF = "href";

  private final List<String> urlList;

  public AbstractWebScraper(@Nonnull final List<String> urlList) {
    Preconditions.checkNotNull(urlList);

    this.urlList = ImmutableList.copyOf(urlList);
  }

  public AbstractWebScraper(@Nonnull final String... urlList) {
    Preconditions.checkNotNull(urlList);

    this.urlList = ImmutableList.copyOf(urlList);
  }

  @Override
  public void scrapePage() {
    logger.info("URLs to be scraped: " + urlList);
    WebDriver webDriver = Application.getApplicationContext().getBean(WebDriver.class);
    NewsEntryRepository newsEntryRepo = Application.getApplicationContext().getBean(NewsEntryRepository.class);
    try {
      for (String source : urlList) {
        logger.info("Started scraping: " + source);
        webDriver.get(source);
        Iterable<NewsEntry> newsEntries = getEntitiesFromPage(webDriver);
        logger.info("Finished scraping: " + source);
        for (NewsEntry e : newsEntries) {
          try {
            if (newsEntryRepo.findByLink(e.getLink()) == null) {
              newsEntryRepo.save(e);
            }
          } catch (Exception e2) {
            logger.warn("An exception happended during save " + e, e2.getMessage());
          }
        }
        logger.info("Stored into database.");
        Thread.sleep(1000);
      }
    } catch (InterruptedException e) {
      logger.debug("Scraping task has been interrupted.");
    }
    webDriver.quit();
    logger.info("WebDriver has quited.");
  }

  /**
   * Parse the page source fetched by {@link WebDriver} and return a
   * {@link Iterable} collection of entities
   * 
   * @param webDriver
   *          the {@link WebDriver} which has <b>already got the page source</b>
   * @return an {@link Iterable} collection of scraped entities
   */
  @Nonnull
  abstract Iterable<NewsEntry> getEntitiesFromPage(@Nonnull WebDriver webDriver);
}
