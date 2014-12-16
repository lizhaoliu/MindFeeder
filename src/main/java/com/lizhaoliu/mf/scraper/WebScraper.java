package com.lizhaoliu.mf.scraper;

/**
 * 
 */
public interface WebScraper {

  /**
   * Scrape all pages in urlList and then store back into database
   */
  void scrapePage();
}
