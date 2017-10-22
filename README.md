# Chessdojo

Manage chess games and positions.

## Usage

- docker-compose up
- lein figwheel
- open http://localhost:3449/ in your browser

## Development

I personally use "lein-test-refresh" to automatically rerun tests for Clojure.

`lein test-refresh :changes-only`

To run the Clojurescript tests involves more setup:
The package.json of the project install karma [https://karma-runner.github.io/1.0/index.html]
which is then called by doo [https://github.com/bensu/doo].

To run tests I use:

`lein doo chrome browser-test auto` 


To run the whole application using figwheel.

`lein figwheel`
