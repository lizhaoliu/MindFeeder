package com.lizhaoliu.mf.model;

import com.lizhaoliu.mf.model.NewsEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.annotation.Nullable;

/**
 *
 */
public interface NewsEntryRepository extends JpaRepository<NewsEntry, Long> {

  /**
   *
   * @param link
   * @return
   */
  @Nullable
  NewsEntry findByLink(String link);
}
