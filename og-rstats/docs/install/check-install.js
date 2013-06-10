/// @export "setup"
var casper = require('casper').create({
        viewportSize : {width : 1000, height : 800}
});

/// @export "initial"
casper.start("http://localhost:8080", function() {
    this.capture("dexy--initial.png");
});

/// @export "run"
casper.run();
