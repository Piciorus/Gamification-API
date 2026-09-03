# Test from your Mac directly
curl -sk --noproxy "*" \
  -u ${NEXUS_USER}:${NEXUS_PASS} \
  -w "\nHTTP: %{http_code}\n" \
  "https://nexus.pro.be.xpi.net.intra/..." \
  -o /tmp/test.war && file /tmp/test.war



FROM i-ckdregistry.pro.be.xpi.net.intra/approved/eclipse-temurin:21-jre

ARG NEXUS_USER
ARG NEXUS_PASS

RUN apt-get update && apt-get install -y curl && \
    mkdir -p /app && \
    curl -sk --noproxy "*" \
      -u ${NEXUS_USER}:${NEXUS_PASS} \
      "https://nexus.pro.be.xpi.net.intra/repository/mvn-it-dev-servicelibs/com/consorsbank/transauth/transauth-kobil-sc-impl/16.0.0-SNAPSHOT/transauth-kobil-sc-impl-16.0.0-SNAPSHOT.war" \
      -o /app/app.war && \
    test -s /app/app.war || (echo "War failed" && exit 1) && \
    apt-get remove -y curl && apt-get autoclean

COPY cacerts /app/security/cacerts
COPY keystore-local /app/security/keystore

WORKDIR /app
EXPOSE 8081
ENTRYPOINT ["java", \
  "-Djavax.net.ssl.trustStore=/app/security/cacerts", \
  "-Djavax.net.ssl.trustStorePassword=changeit", \
  "-Djavax.net.ssl.trustStoreType=JKS", \
  "-jar", "/app/app.war"]



docker run --rm --entrypoint sh \
  $(docker compose images -q transauth-kobil) \
  -c "ls -lh /app/app.war && file /app/app.war"



FROM i-ckdregistry.pro.be.xpi.net.intra/approved/eclipse-temurin:21-jre
COPY app.war /app/app.war
COPY cacerts /app/security/cacerts
COPY keystore-local /app/security/keystore
WORKDIR /app
EXPOSE 8081
ENTRYPOINT ["java", \
  "-Djavax.net.ssl.trustStore=/app/security/cacerts", \
  "-Djavax.net.ssl.trustStorePassword=changeit", \
  "-Djavax.net.ssl.trustStoreType=JKS", \
  "-jar", "/app/app.war"]




curl -sS --noproxy "*" \
  -u "JKmr1DNp:5gcBLciH0wmiNsj3duOwzli5ovW2ZR1lDi02K0E0L0hH" \
  "https://nexus.pro.be.xpi.net.intra/repository/mvn-it-dev-servicelibs/com/consorsbank/transauth/transauth-kobil-sc-impl/16.0.0-SNAPSHOT/transauth-kobil-sc-impl-16.0.0-20260730.090113-3.war" \
  -o docker-compose/transauth-kobil/app.war



docker run --rm --entrypoint sh \
  $(docker compose images -q transauth-kobil) \
  -c "ls -lh /app/app.war && file /app/app.war"

docker inspect transauth-kobil-sc \
  --format '{{json .Mounts}}' | python3 -m json.tool

docker image inspect \
  $(docker compose images -q transauth-kobil) \
  --format '{{.Created}}'



'''
# AFTER
FROM i-ckdregistry.pro.be.xpi.net.intra/approved/curl:latest AS extractor
ARG NEXUS_USER
ARG NEXUS_PASS
ARG WAR_URL="https://nexus.pro.be.xpi.net.intra/repository/mvn-it-dev-servicelibs/com/consorsbank/transauth/transauth-kobil-sc-impl/16.0.0-SNAPSHOT/transauth-kobil-sc-impl-16.0.0-SNAPSHOT.war"

RUN mkdir -p /app && \
    curl -sS --fail-with-body --noproxy "*" \
      -u "${NEXUS_USER}:${NEXUS_PASS}" \
      "${WAR_URL}" \
      -o /app/app.war && \
    # fail loudly if Nexus returned an error page instead of an archive
    unzip -t /app/app.war > /dev/null

FROM i-ckdregistry.pro.be.xpi.net.intra/approved/eclipse-temurin:21-jre
COPY --from=extractor /app/app.war /app/app.war

'''
