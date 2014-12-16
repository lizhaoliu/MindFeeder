package com.lizhaoliu.mf.rest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lizhaoliu.mf.model.NewsEntryRepository;

@RestController
@RequestMapping("/rest")
public class RestHandler {

  private static final Logger LOGGER = Logger.getLogger(RestHandler.class);

  private static final int PAGE_SIZE = 20;

  @Autowired
  private NewsEntryRepository newsEntryRepository;

  /**
   * Get JSON object of news
   *
   * @return
   */
  @RequestMapping(value = "/news", method = RequestMethod.GET)
  @ResponseBody
  public Object getNews(@RequestParam(value = "page") int page) {
    return newsEntryRepository.findAll(new PageRequest(page,
      PAGE_SIZE, Sort.Direction.DESC, "dateTime"));
  }
}