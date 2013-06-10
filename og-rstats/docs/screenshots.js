/// @export "setup"
var casper = require('casper').create({
        viewportSize : {width : 1000, height : 800}
});

/// @export "initial"
casper.start("http://localhost:8080", function() {
    this.capture("dexy--initial.png");
});

/// @export "securities"
casper.then(function() {
    this.click("a.og-securities");
    this.wait(500);
});

casper.then(function() {
    this.capture("dexy--securities.png");
});

/// @export "security"
casper.then(function() {
    this.click("div[row=\"31\"] .og-link");
    this.wait(500);
});

casper.then(function() {
    this.capture("dexy--security.png");
});

/// @export "portfolios"
casper.then(function() {
    this.click("a.og-portfolios");
    this.wait(500);
});

casper.then(function() {
    this.capture("dexy--portfolios.png");
});

/// @export "portfolio"
casper.then(function() {
    this.click("div[row=\"2\"] .og-link");
    this.wait(500);
});

casper.then(function() {
    this.capture("dexy--portfolio.png");
});

/// @export "time-series"
casper.then(function() {
    this.click("a.og-timeseries");
    this.wait(500);
});

casper.then(function() {
    this.capture("dexy--timeseries.png");
});

/// @export "show-time-series"
casper.then(function() {
    this.click("div[row=\"2\"] .r1");
    this.wait(500);
});

casper.then(function() {
    this.capture("dexy--show-timeseries.png");
});

/// @export "run"
casper.run();
