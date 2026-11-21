package dev.homelab.analytics;

import java.time.*;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
  private final SiteRepository sites;
  private final PageviewRepository pageviews;

  public AnalyticsService(SiteRepository sites, PageviewRepository pageviews) {
    this.sites = sites;
    this.pageviews = pageviews;
  }

  @Transactional
  public boolean collect(CollectionRequest request, String origin, String userAgent, String dnt) {
    if ("1".equals(dnt)) return false;
    Optional<Site> siteOpt = sites.findByPublicKey(request.site());
    if (siteOpt.isEmpty()) return false;
    Site site = siteOpt.get();
    if (!originAllowed(origin, site)) return false;
    if (userAgent != null && Pattern.compile("bot|crawl|spider|slurp|headless", Pattern.CASE_INSENSITIVE).matcher(userAgent).find()) return false;
    String path = sanitizePath(request.path());
    if (path == null || isExcluded(path, site.getExcludedPaths())) return false;

    String referrer = normalizeHostname(request.referrer());
    boolean internal = referrer.equals("Direct") || isKnownHost(referrer, site);
    boolean arrival = Boolean.TRUE.equals(request.arrival()) && !internal;

    Pageview view = new Pageview();
    view.setSite(site);
    view.setReceivedAt(Instant.now());
    view.setPathname(path);
    view.setReferralHostname(internal ? "Direct" : referrer);
    view.setArrival(arrival);
    view.setDevice(normalize(request.device(), "Desktop", "Mobile", "Unknown"));
    view.setBrowser(normalize(request.browser(), "Chrome", "Firefox", "Safari", "Edge", "Other", "Unknown"));
    view.setCountry(countryFromTimezone(request.timezone()));
    pageviews.save(view);
    return true;
  }

  private boolean originAllowed(String origin, Site site) {
    if (origin == null || origin.isBlank()) return false;
    try {
      var host = java.net.URI.create(origin).getHost();
      if (host == null) return false;
      host = host.toLowerCase(Locale.ROOT);
      return isKnownHost(host, site);
    } catch (RuntimeException ex) {
      return false;
    }
  }

  private boolean isKnownHost(String host, Site site) {
    if (host == null) return false;
    host = host.toLowerCase(Locale.ROOT);
    if (host.equals(site.getHostname().toLowerCase(Locale.ROOT))) return true;
    for (String alias : site.getAliases().split("[,\\n]")) {
      if (host.equals(alias.trim().toLowerCase(Locale.ROOT))) return true;
    }
    return false;
  }

  private String sanitizePath(String value) {
    if (value == null || !value.startsWith("/") || value.length() > 2048) return null;
    int query = value.indexOf('?');
    int fragment = value.indexOf('#');
    int end = value.length();
    if (query >= 0) end = Math.min(end, query);
    if (fragment >= 0) end = Math.min(end, fragment);
    String path = value.substring(0, end);
    return path.replaceAll("[\\p{Cntrl}]", "");
  }

  private boolean isExcluded(String path, String patterns) {
    for (String pattern : patterns.split("[,\\n]")) {
      String p = pattern.trim();
      if (!p.isEmpty()) {
        if (p.endsWith("*") && path.startsWith(p.substring(0, p.length() - 1))) return true;
        if (path.equals(p)) return true;
      }
    }
    return false;
  }

  private String normalizeHostname(String value) {
    if (value == null || value.isBlank()) return "Direct";
    try {
      String h = value.toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
      if (h.length() > 253) return "Unknown";
      return h;
    } catch (RuntimeException ex) {
      return "Unknown";
    }
  }

  private String normalize(String value, String... allowed) {
    if (value == null) return "Unknown";
    for (String a : allowed) if (a.equalsIgnoreCase(value)) return a;
    return "Unknown";
  }

  private String countryFromTimezone(String zone) {
    if (zone == null) return "Unknown";
    Map<String, String> known = Map.ofEntries(
      Map.entry("Europe/London", "GB"), Map.entry("Europe/Paris", "FR"),
      Map.entry("Europe/Berlin", "DE"), Map.entry("America/New_York", "US"),
      Map.entry("America/Chicago", "US"), Map.entry("America/Los_Angeles", "US"),
      Map.entry("Asia/Tokyo", "JP"), Map.entry("Australia/Sydney", "AU"),
      Map.entry("Europe/Dublin", "IE"), Map.entry("Europe/Madrid", "ES"),
      Map.entry("Europe/Rome", "IT"), Map.entry("America/Toronto", "CA"),
      Map.entry("Asia/Calcutta", "IN"), Map.entry("Asia/Kolkata", "IN")
    );
    return known.getOrDefault(zone, "Unknown");
  }
}
