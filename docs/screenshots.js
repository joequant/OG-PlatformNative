/// @export "setup"
var casper = require('casper').create({
        viewportSize : {width : 1400, height : 1000}
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
    this.click("div[row=\"32\"] .r1");
    this.wait(500);
});

casper.then(function() {
    this.capture("dexy--security.png");
});

/// @export "all-time-series"
casper.then(function() {
    this.click("a.og-timeseries");
    this.wait(500);
});

casper.then(function() {
    this.capture("dexy--all-timeseries.png");
});

/// @export "time-series"
casper.then(function() {
    this.click("div[row=\"2\"] .r1");
    this.wait(500);
});

casper.then(function() {
    this.capture("dexy--timeseries.png");
});
    
/// @export "run"
casper.run();
