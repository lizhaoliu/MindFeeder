package com.lizhaoliu.mf.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * DTO for a news entry
 */
@Entity
public class NewsEntry implements Serializable {

  private static final long serialVersionUID = -2490698293745394646L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Column(unique = false, nullable = false)
  private String title;

  @Column(unique = true, nullable = false)
  private String link;

  @Column(unique = false, nullable = false)
  private String dateTime;

  @Column(unique = false, nullable = true)
  private String source;

  @Column(unique = false, nullable = true)
  private String favicon;

  public String getTitle() {
    return title;
  }

  public String getLink() {
    return link;
  }

  public String getDateTime() {
    return dateTime;
  }

  public String getSource() {
    return source;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public void setDateTime(String dateTime) {
    this.dateTime = dateTime;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getFavicon() {
    return favicon;
  }

  public void setFavicon(String favicon) {
    this.favicon = favicon;
  }

  @Override
  public String toString() {
    return "NewsEntry{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", link='" + link + '\'' +
        ", dateTime='" + dateTime + '\'' +
        ", source='" + source + '\'' +
        ", favicon='" + favicon + '\'' +
        '}';
  }
}
