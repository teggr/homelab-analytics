package dev.homelab.analytics;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
public class RetentionService {
  private final PageviewRepository pageviews;

  public RetentionService(PageviewRepository pageviews) {
    this.pageviews = pageviews;
  }

  @Scheduled(cron = "0 15 3 * * *", zone = "Europe/London")
  @Transactional
  public void enforceRetention() {
    pageviews.deleteOlderThan(Instant.now().minus(366, ChronoUnit.DAYS));
  }
}
