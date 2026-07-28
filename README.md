docker cp ./broker.xml activemq-cnsEvent-1:/var/lib/artemis-instance/etc/broker.xml
docker compose restart activemq-cnsEvent


```
  environment:
    - ARTEMIS_USER=admin
    - ARTEMIS_PASSWORD=admin
    - ARTEMIS_INSTANCE=/var/lib/artemis-instance

```
