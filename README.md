transauth-kobil:
  container_name: transauth-kobil-sc
  build:
    context: ./docker-compose/transauth-kobil
    dockerfile: Dockerfile
  ports:
    - "8081:8081"
  depends_on:
    oracle:
      condition: service_healthy
    artemis:
      condition: service_healthy
    draas:
      condition: service_started


FROM eclipse-temurin:21-jre

# Download ZIP from Nexus at build time
RUN apt-get update && apt-get install -y curl unzip && \
    curl -u <user>:<pass> \
    "https://nexus.pro.be.xpi.net.intra/repository/mvn-it-dev-classic/com/consorsbank/transauth/transauth-kobil-sc-delivery/15-0-3/transauth-kobil-sc-delivery-15-0-3.zip" \
    -o /tmp/app.zip && \
    unzip /tmp/app.zip -d /app && \
    rm /tmp/app.zip

WORKDIR /app
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/transauth-kobil-sc.jar"]





bbb
FROM eclipse-temurin:21-jre
COPY transauth-kobil-sc-15-0-3.zip /app/
RUN cd /app && unzip transauth-kobil-sc-15-0-3.zip
ENTRYPOINT ["java", "-jar", "/app/transauth-kobil-sc.jar"]


transauth-kobil:
  container_name: transauth-kobil-sc
  image: i-ckdregistry.pro.be.xpi.net.intra/cb-authse/transauth-kobil-sc:latest
  ports:
    - "8081:8081"
  depends_on:
    oracle:
      condition: service_healthy
    artemis:
      condition: service_healthy
    draas-app:
      condition: service_started
  environment:
    SPRING_PROFILES_ACTIVE: local
    SPRING_CONFIG_ADDITIONAL_LOCATION: file:/config/application-local-kobil.yml
    SPRING_ACTIVEMQ_BROKER_URL: tcp://artemis:61616
    SPRING_ACTIVEMQ_USER: localUser
    SPRING_ACTIVEMQ_PASSWORD: "12345"
    COM_CONSORS_DRAAS_SC_REST_BASEURI: http://draas-app:8080/rest/api
  volumes:
    - ./config/application-local-kobil.yml:/config/application-local-kobil.yml:ro
  networks:
    - consorsbank-private-nt



curl -X POST http://localhost:8090/rest/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your-user",
    "password": "your-password"
  }'


```
neo-simulator:
  container_name: neo-simulator
  image: i-ckdregistry.pro.be.xpi.net.intra/cb-authse/neo-simulator-sc:2.0.0-952927
  ports:
    - "8082:8080"
  depends_on:
    oracle:
      condition: service_healthy
    draas-app:
      condition: service_healthy
  environment:
    SPRING_PROFILES_ACTIVE: local
    SPRING_CONFIG_ADDITIONAL_LOCATION: file:/config/application-local-neo.yml
  volumes:
    - ./config/application-local-neo.yml:/config/application-local-neo.yml:ro
  networks:
    - consorsbank-private-nt

```
