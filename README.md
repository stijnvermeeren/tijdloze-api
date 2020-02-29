# De Tijdloze Website API

## Running in development mode

`sbt run`

## Building and deploying

`sbt dist` produces a zip file in `target/universal`. To run the application, unzip this package and execute `bash bin/de-tijdloze-website-api`.

## Configuration

To set specific config values, use java system properties: `-Dsetting=value`

To use a specific config file, use the `config.file` system property, for example:
- (in development) `sbt -J-Dconfig.file=application.local.conf run`
- (in production) `bash bin/de-tijdloze-website-api -Dconfig.file=/path/to/application.conf`

To get started, copy the default configuration file at `conf/application.conf` to `application.local.conf`, fill in the missing values, and run while specifying `application.local.conf` as the config file (as described above).
