package dev.homelab.analytics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebController {
  private final AnalyticsService analytics;
  private final SiteRepository sites;
  private final PageviewRepository pageviews;

  public WebController(AnalyticsService analytics, SiteRepository sites, PageviewRepository pageviews) {
    this.analytics = analytics;
    this.sites = sites;
    this.pageviews = pageviews;
  }

  @GetMapping("/login")
  public String login() { return "login"; }

  @GetMapping("/")
  public String overview(Model model) {
    List<Site> siteList = sites.findAll();
    Instant now = Instant.now();
    Instant from = now.minus(Duration.ofDays(30));
    List<Map<String, Object>> cards = new ArrayList<>();
    for (Site site : siteList) {
      long visitors = pageviews.countVisitors(site.getId(), from, now);
      long views = pageviews.countPageviews(site.getId(), from, now);
      cards.add(Map.of("site", site, "visitors", visitors, "pageviews", views,
        "series", dailySeries(site, from, now)));
    }
    model.addAttribute("cards", cards);
    model.addAttribute("siteCount", siteList.size());
    return "overview";
  }

  @GetMapping("/sites/{id}")
  public String detail(@PathVariable("id") UUID id,
      @RequestParam(name = "days", defaultValue = "30") int days, Model model) {
    Site site = sites.findById(id).orElseThrow();
    if (days < 1) days = 1;
    if (days > 366) days = 366;
    Instant now = Instant.now();
    Instant from = now.minus(Duration.ofDays(days));
    long visitors = pageviews.countVisitors(site.getId(), from, now);
    long views = pageviews.countPageviews(site.getId(), from, now);
    model.addAttribute("site", site);
    model.addAttribute("days", days);
    model.addAttribute("visitors", visitors);
    model.addAttribute("pageviews", views);
    model.addAttribute("series", dailySeries(site, from, now));
    model.addAttribute("referrals", boundedCounts(pageviews.referralCounts(site.getId(), from, now), 8));
    model.addAttribute("pages", boundedCounts(pageviews.pageCounts(site.getId(), from, now), 8));
    model.addAttribute("devices", boundedCounts(pageviews.deviceCounts(site.getId(), from, now), 8));
    model.addAttribute("browsers", boundedCounts(pageviews.browserCounts(site.getId(), from, now), 8));
    model.addAttribute("countries", boundedCounts(pageviews.countryCounts(site.getId(), from, now), 8));
    model.addAttribute("denominator", Math.max(1, views));
    return "detail";
  }

  @GetMapping("/sites/new")
  public String newSite(Model model) {
    model.addAttribute("site", new Site());
    model.addAttribute("editing", false);
    return "site-form";
  }

  @PostMapping("/sites")
  public String createSite(@RequestParam("hostname") String hostname,
      @RequestParam(name = "aliases", defaultValue = "") String aliases,
      @RequestParam(name = "reportingZone", defaultValue = "Europe/London") String reportingZone,
      @RequestParam(name = "excludedPaths", defaultValue = "") String excludedPaths) {
    Site site = new Site();
    site.setPublicKey(UUID.randomUUID().toString().replace("-", ""));
    site.setHostname(normalizeHostname(hostname));
    site.setAliases(aliases);
    site.setReportingZone(reportingZone);
    site.setExcludedPaths(excludedPaths);
    sites.save(site);
    return "redirect:/";
  }

  @GetMapping("/sites/{id}/edit")
  public String editSite(@PathVariable("id") UUID id, Model model) {
    model.addAttribute("site", sites.findById(id).orElseThrow());
    model.addAttribute("editing", true);
    return "site-form";
  }

  @PostMapping("/sites/{id}")
  public String updateSite(@PathVariable("id") UUID id,
      @RequestParam("hostname") String hostname,
      @RequestParam(name = "aliases", defaultValue = "") String aliases,
      @RequestParam(name = "reportingZone", defaultValue = "Europe/London") String reportingZone,
      @RequestParam(name = "excludedPaths", defaultValue = "") String excludedPaths) {
    Site site = sites.findById(id).orElseThrow();
    site.setHostname(normalizeHostname(hostname));
    site.setAliases(aliases);
    site.setReportingZone(reportingZone);
    site.setExcludedPaths(excludedPaths);
    sites.save(site);
    return "redirect:/sites/" + id;
  }

  @PostMapping("/api/collect")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> collect(@Valid @RequestBody CollectionRequest request,
      @RequestHeader(value = "Origin", required = false) String origin,
      @RequestHeader(value = "User-Agent", required = false) String userAgent,
      @RequestHeader(value = "DNT", required = false) String dnt,
      HttpServletRequest httpRequest) {
    boolean accepted = analytics.collect(request, origin, userAgent, dnt);
    if (!accepted) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    Optional<Site> site = sites.findByPublicKey(request.site());
    String allowedOrigin = site.map(s -> origin).orElse(null);
    return ResponseEntity.noContent()
      .header("Access-Control-Allow-Origin", allowedOrigin)
      .header("Vary", "Origin")
      .header("Cache-Control", "no-store")
      .build();
  }

  @RequestMapping(value = "/api/collect", method = RequestMethod.OPTIONS)
  public ResponseEntity<Void> collectOptions(
      @RequestHeader(value = "Origin", required = false) String origin,
      @RequestHeader(value = "Access-Control-Request-Headers", required = false) String headers) {
    return ResponseEntity.noContent()
      .header("Access-Control-Allow-Origin", origin)
      .header("Access-Control-Allow-Methods", "POST, OPTIONS")
      .header("Access-Control-Allow-Headers", "Content-Type")
      .header("Access-Control-Max-Age", "300")
      .build();
  }

  private String normalizeHostname(String hostname) {
    String h = hostname == null ? "" : hostname.trim().toLowerCase(Locale.ROOT);
    try {
      h = h.replaceFirst("^https?://", "").replaceFirst("/.*$", "");
    } catch (RuntimeException ignored) {}
    return h;
  }

  private List<Integer> dailySeries(Site site, Instant from, Instant to) {
    ZoneId zone = safeZone(site.getReportingZone());
    LocalDate start = LocalDate.ofInstant(from, zone);
    LocalDate end = LocalDate.ofInstant(to.minusMillis(1), zone);
    List<Integer> values = new ArrayList<>();
    for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
      ZonedDateTime dayStart = date.atStartOfDay(zone);
      ZonedDateTime dayEnd = dayStart.plusDays(1);
      values.add((int) pageviews.countPageviews(site.getId(),
        dayStart.toInstant(), dayEnd.toInstant()));
    }
    return values;
  }

  private ZoneId safeZone(String name) {
    try { return ZoneId.of(name); } catch (RuntimeException ex) { return ZoneId.of("Europe/London"); }
  }

  private List<Map<String, Object>> boundedCounts(List<Object[]> rows, int limit) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (Object[] row : rows) {
      if (result.size() >= limit) break;
      result.add(Map.of("name", String.valueOf(row[0]), "count", ((Number) row[1]).longValue()));
    }
    return result;
  }
}
