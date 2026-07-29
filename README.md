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
