```
FROM i-ckdregistry.pro.be.xpi.net.intra/approved/tomcat:10-jre21

ARG NEXUS_USER
ARG NEXUS_PASS

RUN curl -sk \
    --noproxy "*" \
    -u ${NEXUS_USER}:${NEXUS_PASS} \
    "https://nexus.pro.be.xpi.net.intra/repository/mvn-it-dev-classic/com/consorsbank/transauth/transauth-kobil-sc-delivery/15-0-3/transauth-kobil-sc-delivery-15-0-3.zip" \
    -o /tmp/app.zip && \
    cd /tmp && unzip -o app.zip && \
    rm -f $CATALINA_HOME/webapps/ROOT -rf && \
    cp transauth-kobil-sc.war $CATALINA_HOME/webapps/ROOT.war && \
    cp transauth-kobil-sc-conf.jar $CATALINA_HOME/lib/ && \
    rm -rf /tmp/app.zip /tmp/transauth-kobil-sc*

EXPOSE 8080
```
