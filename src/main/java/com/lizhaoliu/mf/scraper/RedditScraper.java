package com.lizhaoliu.mf.scraper;

import com.lizhaoliu.mf.model.NewsEntry;
import org.joda.time.DateTime;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RedditScraper extends AbstractWebScraper {

  public RedditScraper(final String... urlList) {
    super(urlList);
  }

  @Override
  protected List<NewsEntry> getEntitiesFromPage(WebDriver webDriver) {
    List<NewsEntry> results = new ArrayList<>();
    for (WebElement e : webDriver.findElements(By.className("title.may-blank"))) {
      NewsEntry entry = new NewsEntry();
      entry.setTitle(e.getText());
      entry.setLink(e.getAttribute(HREF));
      entry.setDateTime(new DateTime().toString(DATETIME_FORMAT));
      entry.setSource(e.getAttribute(HREF));
      results.add(entry);
    }
    return results;
  }

  @Override
  public String toString() {
    return "RedditScraper";
  }
}
