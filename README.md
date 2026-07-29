```
FROM i-ckdregistry.pro.be.xpi.net.intra/approved/eclipse-temurin:21-jre

ARG NEXUS_USER
ARG NEXUS_PASS

RUN curl -sk --noproxy "*" \
    -u ${NEXUS_USER}:${NEXUS_PASS} \
    "https://nexus.pro.be.xpi.net.intra/repository/mvn-it-dev-classic/com/consorsbank/transauth/transauth-kobil-sc-delivery/15-0-3/transauth-kobil-sc-delivery-15-0-3.zip" \
    -o /tmp/app.zip && \
    echo "=== ZIP size ===" && \
    ls -la /tmp/app.zip

RUN echo "=== Java version ===" && \
    java -version && \
    echo "=== Attempting extraction ===" && \
    mkdir -p /app && \
    java -jar /tmp/app.zip 2>&1 | head -20 || true && \
    echo "=== Files in /tmp after extraction ===" && \
    find /tmp -type f 2>/dev/null && \
    echo "=== Files in /app after extraction ===" && \
    find /app -type f 2>/dev/null && \
    echo "=== Done ==="

WORKDIR /app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/transauth-kobil-sc.war"]
```

```
docker rmi trauth-sc-stack-transauth-kobil -f
docker compose build --no-cache --progress=plain transauth-kobil 2>&1 | tee /tmp/build.log
cat /tmp/build.log | grep -A2 "==="
```
