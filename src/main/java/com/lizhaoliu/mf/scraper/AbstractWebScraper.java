package com.lizhaoliu.mf.scraper;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.lizhaoliu.mf.app.Application;
import com.lizhaoliu.mf.model.NewsEntry;
import com.lizhaoliu.mf.service.ScraperService;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractWebScraper implements WebScraper {

  private static final Logger logger = LoggerFactory.getLogger(AbstractWebScraper.class);

  private static final Pattern BASE_URL_EXTRACTOR_PATTERN = Pattern.compile("(https?://)?((.+?)/.*|.+)");

  static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  static final String HREF = "href";

  private final List<String> urlList;

  public AbstractWebScraper(@Nonnull final String... urlList) {
    Preconditions.checkNotNull(urlList);

    this.urlList = ImmutableList.copyOf(urlList);
  }

  @Override
  public void scrapePage() {
    logger.info("URLs to be scraped: " + urlList);
    WebDriver webDriver = Application.getApplicationContext().getBean(WebDriver.class);
    ScraperService scraperService = Application.getApplicationContext().getBean(ScraperService.class);
    for (String source : urlList) {
      logger.info("Started scraping: " + source);
      webDriver.get(source);
      Iterable<NewsEntry> newsEntries = getEntitiesFromPage(webDriver);
      logger.info("Finished scraping: " + source);
      for (NewsEntry e : newsEntries) {
        scraperService.save(decorateFields(e));
      }
    }
    webDriver.quit();
    logger.info("WebDriver has quited.");
  }

  private String getBaseUrl(final String url) {
    Matcher matcher = BASE_URL_EXTRACTOR_PATTERN.matcher(url);
    if (matcher.matches()) {
      final String baseUrl = matcher.group(3);
      if (baseUrl == null) {  // when URL not ending with a '/'
        return matcher.group(2);
      }
      return baseUrl;
    }
    return url;
  }

  private NewsEntry decorateFields(NewsEntry e) {
    e.setSource(getBaseUrl(e.getSource()));
    return e;
  }

  /**
   * Parse the page source fetched by {@link WebDriver} and return a
   * {@link Iterable} collection of entities
   *
   * @param webDriver the {@link org.openqa.selenium.WebDriver} which has <b>already got the page source</b>
   * @return a {@link java.util.List} of scraped entities
   */
  @Nonnull
  abstract List<NewsEntry> getEntitiesFromPage(@Nonnull WebDriver webDriver);
}
