```
# Stage 1 — extract using python image from your registry
FROM i-ckdregistry.pro.be.xpi.net.intra/approved/python:3-slim AS extractor

ARG NEXUS_USER
ARG NEXUS_PASS

RUN pip install requests --quiet && \
    python3 -c "
import requests, zipfile, io, os
r = requests.get(
    'https://nexus.pro.be.xpi.net.intra/repository/mvn-it-dev-classic/com/consorsbank/transauth/transauth-kobil-sc-delivery/15-0-3/transauth-kobil-sc-delivery-15-0-3.zip',
    auth=('${NEXUS_USER}', '${NEXUS_PASS}'),
    verify=False
)
z = zipfile.ZipFile(io.BytesIO(r.content))
z.extractall('/app')
print([f for f in z.namelist()])
"

# Stage 2 — runtime
FROM i-ckdregistry.pro.be.xpi.net.intra/approved/eclipse-temurin:21-jre
COPY --from=extractor /app /app
RUN find /app -name "*.war" -exec mv {} /app/app.war \;
WORKDIR /app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.war"]

```
