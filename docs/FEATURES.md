# Feature ledger

| Feature | Status | Criteria | Evidence |
|---|---|---|---|
| Maven reactor, wrapper, executable JAR | built / verified | Exactly tracker and backend modules; `./mvnw verify` succeeds | Maven package and verify |
| Plain JavaScript tracker | built / verified | Plain JS, gzip below 5 KiB, DNT and bot guards | `tracker.js` gzip 978 bytes |
| Persistent H2 site/pageview model | built / verified | File-backed H2 and single-instance persistence | Application smoke test |
| Private login, logout and CSRF | built / verified | Environment credentials, form login, mutation CSRF | Browser login/site creation |
| Public collection endpoint | built / verified | Site/origin/DNT/bot validation | Live 204 test collection |
| Arrival visitor estimate | built | External referrer is an arrival; internal referral is not | `AnalyticsService` |
| 30-day overview and domain detail | built / verified | Real aggregates and SVG chart UI | Browser smoke test |
| Referral, page, device, browser, country lists | built / verified | Database aggregates and percentage breakdown | Browser detail smoke test |
| Retention | built | Calendar-day retention cutoff | Nightly scheduler |
| No-cookie privacy behavior | built | No cookies, storage or raw identifiers in tracker | Tracker source |
| Documentation and operation notes | built | Build, install, privacy and backup limitations | README |
| Noscript tracking | deferred | Later Milestone 2 | Not implemented |
| Email reports | deferred | Later Milestone 3; recipient policy required | Not implemented |
