(function () {
  "use strict";
  if (navigator.doNotTrack === "1" || navigator.doNotTrack === "yes" || window.doNotTrack === "1") return;
  var script = document.currentScript;
  if (!script) return;
  var key = script.getAttribute("data-site");
  if (!key) return;
  var endpoint = script.src.replace(/\/tracker\.js(?:\?.*)?$/, "/api/collect");
  var sentPath = "";
  var sentInitial = false;

  function isBot() {
    var ua = navigator.userAgent || "";
    return /bot|crawl|spider|slurp|headless/i.test(ua);
  }

  function collect(arrival) {
    if (isBot()) return;
    var path = location.pathname || "/";
    if (path === sentPath && sentInitial) return;
    sentInitial = true;
    sentPath = path;
    var ref = "";
    try {
      ref = document.referrer ? new URL(document.referrer).hostname : "";
    } catch (_) {}
    var payload = JSON.stringify({
      site: key,
      path: path.slice(0, 2048),
      referrer: ref.slice(0, 253),
      arrival: arrival,
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone || "",
      device: /Mobi|Android|iPhone|iPad/i.test(navigator.userAgent) ? "Mobile" : "Desktop",
      browser: /Edg/i.test(navigator.userAgent) ? "Edge" :
        /Firefox/i.test(navigator.userAgent) ? "Firefox" :
        /CriOS|Chrome/i.test(navigator.userAgent) ? "Chrome" :
        /Safari/i.test(navigator.userAgent) ? "Safari" : "Other"
    });
    fetch(endpoint, {
      method: "POST",
      mode: "cors",
      credentials: "omit",
      headers: {"Content-Type": "application/json"},
      body: payload,
      keepalive: true
    }).catch(function () {});
  }

  function route() {
    collect(false);
  }

  if (document.readyState === "complete") collect(true);
  else window.addEventListener("load", function () { collect(true); }, {once: true});

  ["pushState", "replaceState"].forEach(function (method) {
    var original = history[method];
    if (typeof original !== "function") return;
    history[method] = function () {
      original.apply(this, arguments);
      window.dispatchEvent(new Event("homelab-analytics-route"));
    };
  });
  window.addEventListener("homelab-analytics-route", route);
  window.addEventListener("popstate", route);
})();
