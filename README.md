# De Tijdloze Website API

## Running in development mode

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
