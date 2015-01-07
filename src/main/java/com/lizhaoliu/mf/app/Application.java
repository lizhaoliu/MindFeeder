package com.lizhaoliu.mf.app;

import com.google.common.base.Preconditions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.Nonnull;

/**
 * The main application entry point of MindFeeder
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.lizhaoliu.mf"}) // scan all Spring beans
@EnableJpaRepositories(basePackages = {"com.lizhaoliu.mf.model"}) // scan JPA repositories
@EntityScan(basePackages = {"com.lizhaoliu.mf.model"})  // scan JPA entities
public class Application {

  private static ConfigurableApplicationContext applicationContext;

  public static void main(String[] args) {
    Application.applicationContext = SpringApplication.run(Application.class, args);
  }

  /**
   * Get the {@link ConfigurableApplicationContext} of the running application
   *
   * @return the {@link ConfigurableApplicationContext} instance
   * @throws IllegalStateException if the applicationContext has not been initialized yet
   */
  @Nonnull
  public static ConfigurableApplicationContext getApplicationContext() {
    Preconditions.checkState(applicationContext != null, "applicationContext has not been initialized.");

    return applicationContext;
  }
}
