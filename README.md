
```
ls -la ~/development/trauth-sc/docker-compose/transauth-kobil/cacerts
```

```
docker logs transauth-kobil-sc 2>&1 | tail -30
```


```
Option B
```

```
docker run --rm \
  -v ~/development/trauth-sc/docker-compose/transauth-kobil/cacerts:/app/security/cacerts:ro \
  i-ckdregistry.pro.be.xpi.net.intra/approved/eclipse-temurin:21-jre \
  sh -c "ls -la /app/security/cacerts"
```

```
ENTRYPOINT ["java", \
  "-Djavax.net.ssl.trustStore=/app/security/cacerts", \
  "-Djavax.net.ssl.trustStorePassword=changeit", \
  "-Djavax.net.ssl.trustStoreType=JKS", \
  "-jar", "/app/app.war"]
```
