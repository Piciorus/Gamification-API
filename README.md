```
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home

sudo keytool -import \
  -alias corporate-root \
  -keystore $JAVA_HOME/lib/security/cacerts \
  -file ~/Downloads/your-corp-cert.crt \
  -storepass changeit \
  -noprompt

```


```
keytool -list \
  -keystore $JAVA_HOME/lib/security/cacerts \
  -storepass changeit | grep corporate-root

```
