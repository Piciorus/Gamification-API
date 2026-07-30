
```
# First copy cacerts to your transauth-kobil docker-compose folder
cp /Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home/lib/security/cacerts \
   ~/development/trauth-sc/docker-compose/transauth-kobil/cacerts
```

```
FROM gvenzl/oracle-xe:21-slim-faststart AS extractor
# ... existing extraction ...

FROM i-ckdregistry.pro.be.xpi.net.intra/approved/eclipse-temurin:21-jre
COPY --from=extractor /tmp/extracted/transauth-kobil-sc.war /app/app.war
COPY --from=extractor /tmp/extracted/transauth-kobil-sc-conf.jar /app/lib/
COPY cacerts /app/security/cacerts    # ADD THIS

WORKDIR /app
EXPOSE 8081
ENTRYPOINT ["java", \
  "-Djavax.net.ssl.trustStore=/app/security/cacerts", \
  "-Djavax.net.ssl.trustStorePassword=changeit", \
  "-jar", "/app/app.war"]
```


```
Option B
```

```
transauth-kobil:
  volumes:
    - ./config/application-local-transauth-kobil.yml:/config/application-local-transauth-kobil.yml:ro
    - ./docker-compose/transauth-kobil/cacerts:/app/security/cacerts:ro
  environment:
    JAVAX_NET_SSL_TRUSTSTORE: /app/security/cacerts
    JAVAX_NET_SSL_TRUSTSTORE_PASSWORD: changeit
```

```
cp /Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home/lib/security/cacerts \
   ~/development/trauth-sc/docker-compose/transauth-kobil/cacerts
```
