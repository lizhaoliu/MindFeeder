package com.lizhaoliu.mf.rest;

import com.lizhaoliu.mf.model.NewsEntryRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
@RequestMapping("/rest")
public class RestHandler {

  private static final Logger LOGGER = Logger.getLogger(RestHandler.class);

  private static final int PAGE_SIZE = 20;

  @Autowired
  private NewsEntryRepository newsEntryRepository;

  @Autowired
  private JedisPool jedisPool;

  /**
   * Get JSON object of news
   *
   * @return
   */
  @RequestMapping(value = "/news", method = RequestMethod.GET)
  @ResponseBody
  public Object getNews(@RequestParam(value = "page") int page) {
    Jedis jedis = jedisPool.getResource();
    try {
      final String key = "/news" + page;
      final String jsonResp = jedis.get(key);
      if (jsonResp == null) {
        return newsEntryRepository.findAll(new PageRequest(page,
          PAGE_SIZE, Sort.Direction.DESC, "dateTime"));
      }
      return jsonResp;
    } finally {
      jedisPool.returnResource(jedis);
    }
  }
}
