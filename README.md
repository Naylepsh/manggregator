## MANGgregAtor

### Installation

- `sbt stage`

### Usage

- `./target/universal/stage/bin/manggregator <mode> <args>`

### Modes & args

#### Single Crawl

Crawls all the enabled assets from the databases and displays the recent release (where "recent" is the given argument by the user)

- `single-crawl yyyy-MM-dd`

#### Server

API giving the access to all functionalities. Docs will be displayed at `http://localhost:8080/docs/`

- `server`
