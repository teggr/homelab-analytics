# Homelab Analytics

A lightweight, self-hosted analytics application with a plain-JavaScript tracker and a Spring Boot backend.

## Build

Requires Java 21+.

```sh
./mvnw verify
java -jar backend/target/backend-0.1.0-SNAPSHOT.jar
```

Set environment credentials before starting:

```sh
export HOMELAB_ADMIN_USERNAME=admin
export HOMELAB_ADMIN_PASSWORD='replace-with-a-long-random-password'
export HOMELAB_DATA_DIR=/var/lib/homelab-analytics
export HOMELAB_SECURE_COOKIE=true
java -jar backend/target/backend-0.1.0-SNAPSHOT.jar
```

No committed default credentials are used. If credentials are omitted, the application generates a temporary password and prints it once at startup.

Open `http://127.0.0.1:8080/login` locally or the HTTPS address of the deployment.

## Install the tracker

Create a website in the private dashboard, then install:

```html
<script async src="https://analytics.example.com/tracker.js"
        data-site="PUBLIC_SITE_KEY"></script>
```

The tracker sends no cookies or persistent browser identifiers and honors Do Not Track.

## Operations

- H2 file database: `${HOMELAB_DATA_DIR}/analytics`
- Retention: pageviews older than 366 days are removed nightly
- Deployment mode: persistent single-instance H2; do not point multiple instances at the same database
- Configure access logs not to record request bodies or full URLs
- Behind TLS, set `HOMELAB_SECURE_COOKIE=true`
- Country is approximate and derived from a browser timezone mapping
- Visitors are estimated arrival pageviews, not exact people

## Browser verification

The server-rendered dashboard can be checked at `/login`, `/`, and `/sites/{id}`.