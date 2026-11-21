(function () {
  function draw(svg, values) {
    var w = svg.viewBox.baseVal.width || (svg.classList.contains("sparkline") ? 300 : 1000);
    var h = svg.viewBox.baseVal.height || 150;
    values = values.length ? values : [0];
    var max = Math.max.apply(null, values);
    var top = max > 0 ? max : 1;
    var grid = svg.querySelector(".chart-grid,.grid");
    var line = svg.querySelector(".chart-line,.spark-line");
    var area = svg.querySelector(".chart-area,.spark-area");
    if (!grid || !line || !area) return;
    grid.innerHTML = "";
    for (var i = 0; i <= 5; i++) {
      var y = 10 + (h - 30) * i / 5;
      var horizontal = document.createElementNS("http://www.w3.org/2000/svg", "line");
      horizontal.setAttribute("x1", "0"); horizontal.setAttribute("x2", w);
      horizontal.setAttribute("y1", y); horizontal.setAttribute("y2", y);
      grid.appendChild(horizontal);
    }
    var points = values.map(function (v, i) {
      return ((i / Math.max(1, values.length - 1)) * (w - 20) + 10) + "," +
        (h - 10 - (v / top) * (h - 30));
    });
    var d = "M " + points.join(" L ");
    line.setAttribute("d", d);
    area.setAttribute("d", d + " L " + (w - 10) + "," + (h - 10) + " L 10," + (h - 10) + " Z");
  }
  document.querySelectorAll(".sparkline,.main-chart").forEach(function (svg) {
    var raw = svg.getAttribute("data-values");
    var values = [];
    try { values = JSON.parse(raw.replace(/\[/g,"[").replace(/\]/g,"]")); } catch (_) {}
    if (!values.length) values = [0];
    draw(svg, values);
  });
  var filter = document.getElementById("site-filter");
  if (filter) {
    filter.addEventListener("input", function () {
      var term = filter.value.toLowerCase();
      document.querySelectorAll(".site-card").forEach(function (card) {
        card.hidden = !card.getAttribute("data-name").toLowerCase().includes(term);
      });
    });
  }
})();
