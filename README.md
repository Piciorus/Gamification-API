```

# Stage 1: use a full image that has unzip
FROM i-ckdregistry.pro.be.xpi.net.intra/approved/hashicorp/vault:1.16 AS extractor

ARG NEXUS_USER
ARG NEXUS_PASS

RUN wget -q --no-check-certificate \
    --user=${NEXUS_USER} \
    --password=${NEXUS_PASS} \
    "https://nexus.pro.be.xpi.net.intra/repository/mvn-it-dev-classic/com/consorsbank/transauth/transauth-kobil-sc-delivery/15-0-3/transauth-kobil-sc-delivery-15-0-3.zip" \
    -O /tmp/app.zip && \
    unzip /tmp/app.zip -d /app

# Stage 2: clean runtime
FROM i-ckdregistry.pro.be.xpi.net.intra/approved/eclipse-temurin:21-jre

COPY --from=extractor /app/*.war /app/app.war
COPY --from=extractor /app/*.jar /app/lib/

WORKDIR /app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.war"]
```


```
docker compose build --no-cache transauth-kobil && docker compose up
```
