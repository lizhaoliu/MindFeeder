package com.lizhaoliu.mf.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsEntryRepository extends JpaRepository<NewsEntry, Long> {

  NewsEntry findByLink(String link);
}
