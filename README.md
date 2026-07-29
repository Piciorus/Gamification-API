```
docker run --rm --entrypoint sh \
  trauth-sc-stack-transauth-kobil \
  -c "find / -name '*.war' -o -name '*.jar' 2>/dev/null | grep -v proc"
```


```
docker run --rm --entrypoint sh \
  trauth-sc-stack-transauth-kobil \
  -c "ls -la /app/"
```
