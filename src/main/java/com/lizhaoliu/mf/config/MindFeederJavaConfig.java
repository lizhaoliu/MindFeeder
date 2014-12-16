package com.lizhaoliu.mf.config;

import javax.sql.DataSource;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.gargoylesoftware.htmlunit.BrowserVersion;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * JavaConfig for MindFeeder
 */
@Configuration
public class MindFeederJavaConfig {

  @Bean
  @Scope(value = BeanDefinition.SCOPE_SINGLETON)
  public DataSource getDataSource() throws URISyntaxException {
//    return new DriverManagerDataSource("jdbc:mysql://localhost:3306/mf", "root", "");
    URI dbUri = new URI(System.getenv("DATABASE_URL"));
    final String username = dbUri.getUserInfo().split(":")[0];
    final String password = dbUri.getUserInfo().split(":")[1];
    final String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
    return new DriverManagerDataSource(dbUrl, username, password);
  }

  @Bean
  @Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
  public WebDriver getWebDriver() {
    return new HtmlUnitDriver(BrowserVersion.CHROME);
  }
}
