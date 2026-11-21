package dev.homelab.analytics;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, UUID> {
  Optional<Site> findByPublicKey(String publicKey);
  Optional<Site> findByHostnameIgnoreCase(String hostname);
}
