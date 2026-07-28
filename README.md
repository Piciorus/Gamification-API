```
docker run --rm --entrypoint sh \
  apache/activemq-artemis:latest \
  -c "ls /var/lib/artemis-instance/etc/"
```


```
activemq-cnsEvent:
  image: apache/activemq-artemis:latest
  user: "0"
  restart: on-failure
  entrypoint: ["/bin/sh", "-c"]
  command: >
    "rm -f /var/lib/artemis-instance/etc/login.config &&
     /opt/activemq-artemis/bin/artemis run"
  ports:
    - "8162:8162"
    - "61617:61617"
  environment:
    - ARTEMIS_USER=admin
    - ARTEMIS_PASSWORD=admin
  volumes:
    - ./broker.xml:/var/lib/artemis-instance/etc/broker.xml
    - activemq-cnsEvent-data:/var/lib/artemis-instance
  networks:
    - consorsbank-private-nt
```
