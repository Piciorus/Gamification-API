```
FROM i-ckdregistry.pro.be.xpi.net.intra/approved/eclipse-temurin:21-jre

ARG NEXUS_USER
ARG NEXUS_PASS

RUN curl -sk --noproxy "*" \
    -u ${NEXUS_USER}:${NEXUS_PASS} \
    "https://nexus.pro.be.xpi.net.intra/repository/mvn-it-dev-classic/com/consorsbank/transauth/transauth-kobil-sc-delivery/15-0-3/transauth-kobil-sc-delivery-15-0-3.zip" \
    -o /tmp/app.zip

RUN java -XshowSettings:all 2>&1 | head && \
    mkdir -p /app && \
    java -jar /tmp/app.zip 2>/dev/null; \
    find /tmp -name "*.war" -exec cp {} /app/transauth-kobil-sc.war \; && \
    rm /tmp/app.zip

WORKDIR /app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/transauth-kobil-sc.war"]
```
