# De Tijdloze Website API

The Tijdloze Website API is built using the [Scala programming language](https://www.scala-lang.org/) and the [Play Framework](https://www.playframework.com/).

The Tijdloze Website API is available as a Docker image: [stijnvermeeren/tijdloze-api](https://hub.docker.com/repository/docker/stijnvermeeren/tijdloze-api). Instructions on how to use this Docker image together with the frontend of the Tijdloze Website can be found in the [README for the frontend project](https://github.com/stijnvermeeren/tijdloze-frontend). 

## Running in development mode

### Database setup

The Tijdloze Website API uses a MySQL database.

#### Using Docker image 

The simplest way to set up the database is by using the Docker image [stijnvermeeren/tijdloze-db](https://hub.docker.com/repository/docker/stijnvermeeren/tijdloze-db).

The Docker image has a database `tijdloze` with
- The structure of all tables used by the API.
- All data about artists, albums, songs and list entries.
- Four dummy users from the `stijnvermeeren-tijdloze-dev.eu.auth0.com` Auth0 domain, all with password "_secret_":
  - `user1@example.com`
  - `user2@example.com`
  - `admin1@example.com` (Admin user) 
  - `admin2@example.com` (Admin user)

To start this database using [docker-compose](https://docs.docker.com/compose/), simply run the command `docker-compose up`. By default, the MySQL will be running on port 3306 and the password for the root user will be `secret` (only use this for local development, obviously). This can be changed by using environment variables (see [docker-compose.yml](docker-compose.yml)).

The default database configuration for Play (see [conf/application.conf](conf/application.conf)) should work out-of-the-box when using the provided docker-compose file.

#### Manual database setup

Alternatively, to use a different database server, adjust the `slick.dbs.default` configuration values for Play accordingly (see _Configuration_ section below). The database host can also be configured using the `DB_HOST` environment variable.

An SQL file to fill a database with the same structure and data as in the Docker image, can be found at [docker/db/init.sql](docker/db/init.sql).

### Auth0

The Tijdloze Website is designed to work with [Auth0](https://auth0.com/) for authentication and authorization.

An Auth0 domain `stijnvermeeren-tijdloze-dev.eu.auth0.com` has been set up that can be used for development purposes (though without the ability to create new users beyond the ones listed above under _Database setup_). The public key for this Auth0 domain can be found at [docker/stijnvermeeren-tijdloze-dev.pem](docker/stijnvermeeren-tijdloze-dev.pem). 

To use your own Auth0 domain, the API must have access to the public key, so that it can verify the JWT that is sent with each authenticated request. You must point the `tijdloze.auth0.publickey.path` configuration value for Play to the `.pem` certificate file containing this public key (see _Configuration_ section below).

### Spotify

Some admin endpoints call the Spotify API. In order for these endpoints to work, you must [create your own Spotify keys](https://developer.spotify.com/documentation/general/guides/app-settings/#register-your-app). The configuration values `tijdloze.spotify.clientId` and `tijdloze.spotify.clientSecret` should be set using the personal keys you obtained from Spotify (see _Configuration_ section below). The Tijdloze API only makes use of the [client credentials flow](https://developer.spotify.com/documentation/general/guides/authorization-guide/#client-credentials-flow).

### Starting the API in development mode
`sbt run`

## Building and deploying

`sbt dist` produces a zip file in `target/universal`. To run the application, unzip this package and execute `bash bin/de-tijdloze-website-api`.

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
  server_name api.tijdloze.stijnshome.be;

  ssl_certificate /etc/letsencrypt/live/tijdloze.stijnshome.be/fullchain.pem;
  ssl_certificate_key /etc/letsencrypt/live/tijdloze.stijnshome.be/privkey.pem;

  location /ws/ {
    proxy_pass http://127.0.0.1:9000/ws/;

    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
  }

  location / {
    proxy_pass http://127.0.0.1:9000/;
  }
}

server {
  listen 80;
  listen [::]:80;
  server_name api.tijdloze.stijnshome.be;

  location /.well-known { 
    root /srv/httproot;
  }

  location / {
    return 301 https://$server_name$request_uri;
  }
}
```

### File limit

For dealing with many visitors, make sure that nginx is allowed to open plenty of files (see e.g. [this post](https://www.cyberciti.biz/faq/linux-unix-nginx-too-many-open-files/)).

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
Description=Tijdloze Website API

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
