```
docker run --rm --entrypoint sh \
  trauth-sc-stack-transauth-kobil \
  -c "find / -name '*.war' -o -name '*.jar' 2>/dev/null | grep -v proc"
```


```
docker run --rm --entrypoint sh \
  trauth-sc-stack-transauth-kobil \
  -c "ls -la /app/"
```


```
# Stage 1 — extract
FROM busybox AS extractor

ARG NEXUS_USER
ARG NEXUS_PASS

RUN wget -q --no-check-certificate \
    --user="${NEXUS_USER}" \
    --password="${NEXUS_PASS}" \
    "https://nexus.pro.be.xpi.net.intra/repository/mvn-it-dev-classic/com/consorsbank/transauth/transauth-kobil-sc-delivery/15-0-3/transauth-kobil-sc-delivery-15-0-3.zip" \
    -O /tmp/app.zip && \
    unzip /tmp/app.zip -d /app

# Stage 2 — runtime
FROM i-ckdregistry.pro.be.xpi.net.intra/approved/eclipse-temurin:21-jre

COPY --from=extractor /app /app
RUN find /app -name "*.war" -exec mv {} /app/app.war \;

WORKDIR /app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.war"]
```
