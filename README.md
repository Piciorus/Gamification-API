
```
# Stage 1 — Oracle image has unzip
FROM i-ckdregistry.pro.be.xpi.net.intra/approved/gvenzl/oracle-xe:21-slim-faststart AS extractor

ARG NEXUS_USER
ARG NEXUS_PASS

RUN curl -sk --noproxy "*" \
    -u ${NEXUS_USER}:${NEXUS_PASS} \
    "https://nexus.pro.be.xpi.net.intra/repository/mvn-it-dev-classic/com/consorsbank/transauth/transauth-kobil-sc-delivery/15-0-3/transauth-kobil-sc-delivery-15-0-3.zip" \
    -o /tmp/app.zip && \
    unzip -o /tmp/app.zip -d /app && \
    find /app -type f

# Stage 2 — runtime
FROM i-ckdregistry.pro.be.xpi.net.intra/approved/eclipse-temurin:21-jre
COPY --from=extractor /app /app
RUN find /app -name "*.war" -exec mv {} /app/app.war \;
WORKDIR /app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.war"]
```

