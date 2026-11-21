package dev.homelab.analytics;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pageviews", indexes = {
  @Index(name = "idx_pageviews_site_time", columnList = "site_id,receivedAt"),
  @Index(name = "idx_pageviews_arrival", columnList = "arrival")
})
public class Pageview {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "site_id", nullable = false)
  private Site site;
  @Column(nullable = false)
  private Instant receivedAt;
  @Column(nullable = false, length = 2048)
  private String pathname;
  @Column(nullable = false, length = 253)
  private String referralHostname = "Direct";
  @Column(nullable = false)
  private boolean arrival;
  @Column(nullable = false, length = 32)
  private String device = "Unknown";
  @Column(nullable = false, length = 32)
  private String browser = "Unknown";
  @Column(nullable = false, length = 64)
  private String country = "Unknown";

  public UUID getId() { return id; }
  public Site getSite() { return site; }
  public void setSite(Site site) { this.site = site; }
  public Instant getReceivedAt() { return receivedAt; }
  public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
  public String getPathname() { return pathname; }
  public void setPathname(String pathname) { this.pathname = pathname; }
  public String getReferralHostname() { return referralHostname; }
  public void setReferralHostname(String referralHostname) { this.referralHostname = referralHostname; }
  public boolean isArrival() { return arrival; }
  public void setArrival(boolean arrival) { this.arrival = arrival; }
  public String getDevice() { return device; }
  public void setDevice(String device) { this.device = device; }
  public String getBrowser() { return browser; }
  public void setBrowser(String browser) { this.browser = browser; }
  public String getCountry() { return country; }
  public void setCountry(String country) { this.country = country; }
}
