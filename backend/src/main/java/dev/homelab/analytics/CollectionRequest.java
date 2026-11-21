package dev.homelab.analytics;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectionRequest(
  @NotBlank @Size(max = 64) String site,
  @NotBlank @Size(max = 2048) String path,
  @Size(max = 253) String referrer,
  Boolean arrival,
  @Size(max = 64) String timezone,
  @Size(max = 32) String device,
  @Size(max = 32) String browser
) {}
