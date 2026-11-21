package dev.homelab.analytics;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PageviewRepository extends JpaRepository<Pageview, UUID> {
  @Query(value = "select count(p) from Pageview p where p.site.id=:siteId and p.receivedAt>=:from and p.receivedAt<:to")
  long countPageviews(@Param("siteId") UUID siteId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(value = "select count(p) from Pageview p where p.site.id=:siteId and p.receivedAt>=:from and p.receivedAt<:to and p.arrival=true")
  long countVisitors(@Param("siteId") UUID siteId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(value = "select coalesce(sum(case when p.arrival=:arrival then 1 else 0 end),0) from Pageview p where p.site.id=:siteId and p.receivedAt>=:from and p.receivedAt<:to")
  long countMetric(@Param("siteId") UUID siteId, @Param("from") Instant from, @Param("to") Instant to, @Param("arrival") boolean arrival);

  @Query(value = "select p.referralHostname from Pageview p where p.site.id=:siteId and p.receivedAt>=:from and p.receivedAt<:to and p.arrival=true group by p.referralHostname order by count(p) desc")
  List<String> topReferrals(@Param("siteId") UUID siteId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(value = "select p.pathname from Pageview p where p.site.id=:siteId and p.receivedAt>=:from and p.receivedAt<:to group by p.pathname order by count(p) desc")
  List<String> topPages(@Param("siteId") UUID siteId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(value = "select p.device from Pageview p where p.site.id=:siteId and p.receivedAt>=:from and p.receivedAt<:to group by p.device order by count(p) desc")
  List<String> topDevices(@Param("siteId") UUID siteId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(value = "select p.browser from Pageview p where p.site.id=:siteId and p.receivedAt>=:from and p.receivedAt<:to group by p.browser order by count(p) desc")
  List<String> topBrowsers(@Param("siteId") UUID siteId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(value = "select p.country from Pageview p where p.site.id=:siteId and p.receivedAt>=:from and p.receivedAt<:to group by p.country order by count(p) desc")
  List<String> topCountries(@Param("siteId") UUID siteId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(value = "select p.referralHostname, count(p) from Pageview p where p.site.id=:siteId and p.receivedAt>=:from and p.receivedAt<:to and p.arrival=true group by p.referralHostname order by count(p) desc")
  List<Object[]> referralCounts(@Param("siteId") UUID siteId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(value = "select p.pathname, count(p) from Pageview p where p.site.id=:siteId and p.receivedAt>=:from and p.receivedAt<:to group by p.pathname order by count(p) desc")
  List<Object[]> pageCounts(@Param("siteId") UUID siteId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(value = "select p.device, count(p) from Pageview p where p.site.id=:siteId and p.receivedAt>=:from and p.receivedAt<:to group by p.device order by count(p) desc")
  List<Object[]> deviceCounts(@Param("siteId") UUID siteId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(value = "select p.browser, count(p) from Pageview p where p.site.id=:siteId and p.receivedAt>=:from and p.receivedAt<:to group by p.browser order by count(p) desc")
  List<Object[]> browserCounts(@Param("siteId") UUID siteId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(value = "select p.country, count(p) from Pageview p where p.site.id=:siteId and p.receivedAt>=:from and p.receivedAt<:to group by p.country order by count(p) desc")
  List<Object[]> countryCounts(@Param("siteId") UUID siteId, @Param("from") Instant from, @Param("to") Instant to);

  @Modifying
  @Query("delete from Pageview p where p.receivedAt < :cutoff")
  int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
