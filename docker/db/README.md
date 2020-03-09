# Tijdloze database Docker image

A Docker image with a MySQL database for running De Tijdloze Website in a development environment.

The Docker image has a database `tijdloze` with
- The structure of all tables used by the API.
- All data about artists, albums, songs and list entries.
- Four dummy users from the `stijnvermeeren-tijdloze-dev.eu.auth0.com` Auth0 domain:
  - `user1@example.com`
  - `user2@example.com`
  - `admin1@example.com` (Admin user) 
  - `admin2@example.com` (Admin user)

Required environment variables:
- `MYSQL_ROOT_PASSWORD`

## Building

```
docker build --tag stijnvermeeren/tijdloze-db .
```

## Running

```
docker run --env MYSQL_ROOT_PASSWORD=secret -p3306:3306 --name=tijdloze-db -d stijnvermeeren/tijdloze-db
```

Or use the provided docker-compose file:
```
docker-compose up
```

## Reset the database

To reset the database to its initial state, simply remove and recreate the container.
