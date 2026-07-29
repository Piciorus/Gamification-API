```
FROM i-ckdregistry.pro.be.xpi.net.intra/approved/busybox AS extractor

ARG NEXUS_USER
ARG NEXUS_PASS

RUN echo "=== Downloading ZIP ===" && \
    wget -v --no-check-certificate \
    --user="${NEXUS_USER}" \
    --password="${NEXUS_PASS}" \
    "https://nexus.pro.be.xpi.net.intra/repository/mvn-it-dev-classic/com/consorsbank/transauth/transauth-kobil-sc-delivery/15-0-3/transauth-kobil-sc-delivery-15-0-3.zip" \
    -O /tmp/app.zip && \
    echo "=== ZIP downloaded, size ===" && \
    ls -la /tmp/app.zip && \
    echo "=== Extracting ===" && \
    unzip -v /tmp/app.zip -d /app && \
    echo "=== Extracted files ===" && \
    find /app -type f -ls && \
    echo "=== Done ==="

FROM i-ckdregistry.pro.be.xpi.net.intra/approved/eclipse-temurin:21-jre

COPY --from=extractor /app /app

RUN echo "=== Files in /app ===" && \
    find /app -type f -ls && \
    echo "=== Moving WAR ===" && \
    find /app -name "*.war" -exec mv {} /app/app.war \; && \
    echo "=== Final /app contents ===" && \
    ls -la /app/

WORKDIR /app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.war"]
```


```
docker run --rm --entrypoint sh \
  --platform linux/amd64 \
  trauth-sc-stack-transauth-kobil \
  -c "find /app -type f -ls"
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
