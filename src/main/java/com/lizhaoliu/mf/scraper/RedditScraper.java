package com.lizhaoliu.mf.scraper;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.lizhaoliu.mf.model.NewsEntry;

/**
 * 
 */
public class RedditScraper extends AbstractWebScraper {

  public RedditScraper(final List<String> urlList) {
    super(urlList);
  }

  public RedditScraper(final String... urlList) {
    super(urlList);
  }

  @Override
  Iterable<NewsEntry> getEntitiesFromPage(WebDriver webDriver) {
    List<NewsEntry> results = new ArrayList<NewsEntry>();
    for (WebElement e : webDriver.findElements(By.className("title.may-blank"))) {
      NewsEntry entry = new NewsEntry();
      entry.setTitle(e.getText());
      entry.setLink(e.getAttribute(HREF));
      entry.setDateTime(new DateTime().toString(DATETIME_FORMAT));
      entry.setSource(webDriver.getCurrentUrl());
      results.add(entry);
    }
    return results;
  }

  @Override
  public String toString() {
    return "RedditScraper";
  }
}
