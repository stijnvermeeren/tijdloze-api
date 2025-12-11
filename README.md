# tijdloze.rocks API

The tijdloze.rocks API is built using the [Scala programming language](https://www.scala-lang.org/) and the [Play Framework](https://www.playframework.com/).

The tijdloze.rocks API is available as a Docker image: [stijnvermeeren/tijdloze-api](https://hub.docker.com/repository/docker/stijnvermeeren/tijdloze-api). Instructions on how to use this Docker image together with the frontend of tijdloze.rocks can be found in the [README for the frontend project](https://github.com/stijnvermeeren/tijdloze-frontend). 

## Requirements

The tijdloze.rocks API is built using Play Framework 2.8, which requires Java SE 8 through SE 11.

## Running in development mode

### Database setup

The tijdloze.rocks API uses a PostgreSQL database.

Adjust the `slick.dbs.default` configuration values for Play accordingly (see _Configuration_ section below). The database host can also be configured using the `DB_HOST` environment variable.

The database needs to have a schema names `tijdloze`.

The database structure will be automatically generated when the application is first started, using the concept of "[Play Evolutions](https://www.playframework.com/documentation/3.0.x/Evolutions)".

Afterwards, application data can be loaded into the database:
- A daily dump with all data about artists, albums, songs and lists from the Tijdloze can be downloaded from <https://tijdloze.rocks/website/opendata> (`tijdloze.zip`) and loaded into the database using the `postgres-import.sql` script contained in the ZIP file. The ZIP file also contains a README with more detailed information.
- Sample data (dummy user and comments) for development purposes, that matches with the users from the `stijnvermeeren-tijdloze-dev.eu.auth0.com` Auth0 domain described below: [dev/insert-user-comment-data.sql](dev/insert-user-comment-data.sql)


### Auth0

tijdloze.rocks is designed to work with [Auth0](https://auth0.com/) for authentication and authorization.

An Auth0 domain `stijnvermeeren-tijdloze-dev.eu.auth0.com` has been set up that can be used for development purposes. The public key for this Auth0 domain can be found at [dev/stijnvermeeren-tijdloze-dev.pem](dev/stijnvermeeren-tijdloze-dev.pem). 

This Auth0 domain comes with four preconfigured "dummy" users, all with password "_secret_":
- `user1@example.com`
- `user2@example.com`
- `admin1@example.com`
- `admin2@example.com`
Sample data for these users is included in the `dev/insert-user-comment-data.sql` database script. This SQL script also assigns the admin role to the two last users.

To use your own Auth0 domain, the API must have access to the public key, so that it can verify the JWT that is sent with each authenticated request. You must point the `tijdloze.auth0.publickey.path` configuration value for Play to the `.pem` certificate file containing this public key (see _Configuration_ section below).

### Spotify

Some admin endpoints call the Spotify API. In order for these endpoints to work, you must [create your own Spotify keys](https://developer.spotify.com/documentation/general/guides/app-settings/#register-your-app). The configuration values `tijdloze.spotify.clientId` and `tijdloze.spotify.clientSecret` should be set using the personal keys you obtained from Spotify (see _Configuration_ section below). The Tijdloze API only makes use of the [client credentials flow](https://developer.spotify.com/documentation/general/guides/authorization-guide/#client-credentials-flow).

### Starting the API in development mode
`sbt run`

## Building and deploying

`sbt dist` produces a zip file in `target/universal`. To run the application, unzip this package and execute `bash bin/de-tijdloze-website-api`.

## Generating the open data exports

The script `export/export.sh` produces files `tijdloze-data.sql`, `tijdloze-schema-data.sql` and `tijdloze.tsv`, that contain the open data exports that are also published on tijdloze.rocks. The script must be provided with the required Postgres connection parameters, e.g. `sh ./export.sh -U tijdloze_exporter -d tijdloze -h 127.0.0.1`.

It is recommended to use a dedicated Postgres user with limited (read-only) privileges for this export script.
```postgresql
CREATE ROLE tijdloze_exporter WITH LOGIN PASSWORD 'secret';
GRANT CONNECT ON DATABASE tijdloze TO tijdloze_exporter;
GRANT USAGE ON SCHEMA tijdloze TO tijdloze_exporter;
GRANT SELECT ON artist, artist_id_seq, album, album_id_seq, song, song_id_seq, year, list_entry, list_entry_id_seq TO tijdloze_exporter;
ALTER ROLE tijdloze_exporter SET search_path TO tijdloze;
```

The password for this user can be stored in a `.pgpass` file, from where it will be read automatically, e.g.:
```
echo '127.0.0.1:*:*:tijdloze_exporter:secret' > ~/.pgpass
chmod 600 ~/.pgpass
```

To generate the data exports automatically on a server, it is recommended to create a dedicated script such as the following
```sh
#!/bin/sh

cd /home/stijn/tijdloze/export
./export.sh -U tijdloze_exporter -h 127.0.0.1 -d tijdloze
cp tijdloze.tsv /srv/tijdloze-data/
cp tijdloze-data.sql /srv/tijdloze-data/
cp tijdloze-schema-data.sql /srv/tijdloze-data/e-data/
```

This script can then be automatically executed as a crontab script. For example, to execute the script (assuming it is located at `/home/stijn/tijdloze/export/export-and-deploy.sh`) every evening at 22:15, add the line to the crontab file using the `crontab -e` command:
```
15 22 * * *   /home/stijn/tijdloze/export/export-and-deploy.sh
```

## Configuration

To set specific config values, use java system properties: `-Dsetting=value`

To use a specific config file, use the `config.file` system property, for example:
- (in development) `sbt -J-Dconfig.file=local/application.conf run`
- (in production) `bash bin/de-tijdloze-website-api -Dconfig.file=/path/to/application.conf`

To get started, copy the default configuration file at `conf/application.conf` to `local/application.conf`, fill in the missing values, and run while specifying `local/application.conf` as the config file (as described above).

## [Nginx](https://www.nginx.com/) proxy configuration example

In `/etc/nginx/sites-available/tijdloze-api.conf`:

```
server {
  listen 443 ssl;
  listen [::]:443 ssl;
  server_name api.tijdloze.rocks;

  ssl_certificate /etc/letsencrypt/live/tijdloze.stijnshome.be/fullchain.pem;
  ssl_certificate_key /etc/letsencrypt/live/tijdloze.stijnshome.be/privkey.pem;

  location /ws/ {
    proxy_pass http://127.0.0.1:9000/ws/;

    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
  }

  location / {
    proxy_pass http://127.0.0.1:9000;
  }
}

server {
  listen 80;
  listen [::]:80;
  server_name api.tijdloze.rocks;

  location /.well-known { 
    root /srv/httproot;
  }

  location / {
    return 301 https://$server_name$request_uri;
  }
}
```

### File limit

For dealing with many visitors, make sure that nginx is allowed to open plenty of files (see e.g. [this post](https://www.cyberciti.biz/faq/linux-unix-nginx-too-many-open-files/)). This is mainly because each visitor will create a websocket connection, and each socket connection requires a Linux file handle.

In `/etc/sysctl.conf` add `fs.file-max = 70000`.

In `/etc/security/limits.conf` add 
```
nginx       soft    nofile   10000
nginx       hard    nofile  30000
```
and then run `sysctl -p`

In `/etc/nginx/nginx.conf` add `worker_rlimit_nofile 30000;` and then run `nginx -s reload`.

Verify by using `cat /proc/{PID}/limits` where `{PID}` is the id of the `nginx` process.


## [Systemd](https://www.freedesktop.org/wiki/Software/systemd/) service configuration example

In `/etc/systemd/system/tijdloze-api.service` configure something like

```
[Unit]
Description=tijdloze.rocks API

[Service]
WorkingDirectory=/home/stijn/tijdloze-api
ExecStart=/bin/bash de-tijdloze-website-api-1.0-SNAPSHOT/bin/de-tijdloze-website-api -Dconfig.file=/home/stijn/tijdloze-api/application.conf
User=stijn
Type=simple
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Then you can use for example
- `systemctl daemon-reload` (after changing the service config)
- `systemctl start tijdloze-api`
- `systemctl status tijdloze-api`
- `systemctl restart tijdloze-api`
- `systemctl stop tijdloze-api`

## Docker

Build Docker image:
```
docker build --tag stijnvermeeren/tijdloze-api .
```

