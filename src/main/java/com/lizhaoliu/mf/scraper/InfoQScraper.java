package com.lizhaoliu.mf.scraper;

import com.lizhaoliu.mf.model.NewsEntry;
import org.joda.time.DateTime;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizhaoliu on 1/8/15.
 */
public class InfoQScraper extends AbstractWebScraper {

  public InfoQScraper(String... urls) {
    super(urls);
  }

  @Nonnull
  @Override
  Iterable<NewsEntry> getEntitiesFromPage(@Nonnull WebDriver webDriver) {
    List<NewsEntry> results = new ArrayList<>();
    for (WebElement e : webDriver.findElements(By.className("news_type_block"))) {
      WebElement a = e.findElement(By.cssSelector("h2")).findElement(By.cssSelector("a"));
      NewsEntry entry = new NewsEntry();
      entry.setTitle(a.getText());
      entry.setLink(a.getAttribute(HREF));
      entry.setDateTime(new DateTime().toString(DATETIME_FORMAT));
      entry.setSource(a.getAttribute(HREF));
      results.add(entry);
    }
    return results;
  }
}
