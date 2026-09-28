```
server:
  ssl:
    certificate: file:.certs/ssl/tls.crt
    certificate-private-key: file:.certs/ssl/tls.key

application:
  truststore-ca: file:.certs/ssl/ca.crt
  truststore-service-ca: file:.certs/ssl/service-ca.crt

management:
  server:
    ssl:
      certificate: file:.certs/ssl/tls.crt
      certificate-private-key: file:.certs/ssl/tls.key

```
