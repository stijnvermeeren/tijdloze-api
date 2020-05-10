FROM adoptopenjdk/openjdk11:alpine-jre

ENV MYSQL_DATABASE=tijdloze

RUN apk add --no-cache bash

ADD target/universal/de-tijdloze-website-api-1.0-SNAPSHOT.zip /
ADD docker/stijnvermeeren-tijdloze-dev.pem /stijnvermeeren-tijdloze-dev.pem

RUN unzip /de-tijdloze-website-api-1.0-SNAPSHOT.zip
RUN mv /de-tijdloze-website-api-1.0-SNAPSHOT /api
RUN rm /de-tijdloze-website-api-1.0-SNAPSHOT.zip

EXPOSE 9000

CMD /api/bin/de-tijdloze-website-api -Dtijdloze.auth0.publickey.path=/stijnvermeeren-tijdloze-dev.pem
