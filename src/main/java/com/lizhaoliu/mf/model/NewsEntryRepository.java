package com.lizhaoliu.mf.model;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.annotation.Nullable;

/**
 * JPA repository for {@link NewsEntry}
 */
public interface NewsEntryRepository extends JpaRepository<NewsEntry, Long> {

  /**
   * @param link
   * @return
   */
  @Nullable
  NewsEntry findByLink(String link);
}
