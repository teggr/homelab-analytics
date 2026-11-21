package dev.homelab.analytics;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sites", indexes = @Index(name = "idx_sites_public_key", columnList = "publicKey", unique = true))
public class Site {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(nullable = false, unique = true, length = 64)
  private String publicKey;
  @Column(nullable = false, length = 253)
  private String hostname;
  @Column(length = 2000)
  private String aliases = "";
  @Column(nullable = false, length = 64)
  private String reportingZone = "Europe/London";
  @Column(length = 4000)
  private String excludedPaths = "";
  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  public UUID getId() { return id; }
  public String getPublicKey() { return publicKey; }
  public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
  public String getHostname() { return hostname; }
  public void setHostname(String hostname) { this.hostname = hostname; }
  public String getAliases() { return aliases; }
  public void setAliases(String aliases) { this.aliases = aliases; }
  public String getReportingZone() { return reportingZone; }
  public void setReportingZone(String reportingZone) { this.reportingZone = reportingZone; }
  public String getExcludedPaths() { return excludedPaths; }
  public void setExcludedPaths(String excludedPaths) { this.excludedPaths = excludedPaths; }
  public Instant getCreatedAt() { return createdAt; }
}
