```
docker logs transauth-kobil-sc 2>&1 | grep -A5 "trustStore"
```


```
docker logs transauth-kobil-sc 2>&1 | grep -E "ERROR|Exception|Caused by" | head -20
```


```
docker exec transauth-kobil-sc cat /config/application-local-transauth-kobil.yml 2>&1 | head -5
```
