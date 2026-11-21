package dev.homelab.analytics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class AnalyticsServiceTest {
  @Test
  void rejectsDntAndDoesNotStorePageview() {
    SiteRepository sites = mock(SiteRepository.class);
    PageviewRepository pageviews = mock(PageviewRepository.class);
    AnalyticsService service = new AnalyticsService(sites, pageviews);

    boolean accepted = service.collect(
      new CollectionRequest("key", "/", "google.com", true, "Europe/London", "Desktop", "Chrome"),
      "https://example.com", "Mozilla/5.0 Chrome", "1");

    assertFalse(accepted);
    verify(pageviews, never()).save(any(Pageview.class));
  }

  @Test
  void rejectsUnknownPublicKey() {
    SiteRepository sites = mock(SiteRepository.class);
    PageviewRepository pageviews = mock(PageviewRepository.class);
    when(sites.findByPublicKey("unknown")).thenReturn(Optional.empty());
    AnalyticsService service = new AnalyticsService(sites, pageviews);

    boolean accepted = service.collect(
      new CollectionRequest("unknown", "/", "", true, "UTC", "Desktop", "Chrome"),
      "https://example.com", "Mozilla/5.0", null);

    assertFalse(accepted);
  }

  @Test
  void rejectsWrongOrigin() {
    Site site = new Site();
    site.setHostname("allowed.example");
    site.setPublicKey("key");
    SiteRepository sites = mock(SiteRepository.class);
    PageviewRepository pageviews = mock(PageviewRepository.class);
    when(sites.findByPublicKey("key")).thenReturn(Optional.of(site));
    AnalyticsService service = new AnalyticsService(sites, pageviews);

    boolean accepted = service.collect(
      new CollectionRequest("key", "/", "", true, "UTC", "Desktop", "Chrome"),
      "https://other.example", "Mozilla/5.0", null);

    assertFalse(accepted);
  }
}
