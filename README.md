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


### MongoDB

For simple maintenance/debugging tasks the Mongo shell can be used:
`docker exec -it chessdojo_mongo-db_1 mongo`

Some sample commands:
`show dbs`
`use <db>`
`db.games.find({_id : "b1a22603-2828-4835-bbcc-408a22f28008"});`
`db.games.deleteOne({_id : "74860980-4c1c-4e5a-a6b4-da5d4e1726f2"});`