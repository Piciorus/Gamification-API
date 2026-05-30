
<springProperty scope="context" name="DISABLE_LOGSTASH_ASYNC"
                source="DISABLE_LOGSTASH_ASYNC"
                default="false"/>

<appender name="LOGSTASH_ASYNC" class="ch.qos.logback.classic.AsyncAppender">
    <queueSize>10000</queueSize>
    <discardingThreshold>0</discardingThreshold>
    <neverBlock>true</neverBlock>
    <appender-ref ref="LOGSTASH_TCP_DMZR"/>
</appender>

<springProfile name="local-development-ssl | local-development-non-ssl">
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="info">
        <appender-ref ref="CONSOLE"/>
    </root>
</springProfile>

<springProfile name="openshift-d0 | openshift-d1 | openshift-p0 | openshift-t0 | openshift-t1 | openshift-u0 | openshift-u1 | openshift-x0">
    <root level="info">
        <appender-ref ref="ENRICHED_CONSOLE_DMZR"/>
        <appender-ref ref="LOGSTASH_ASYNC"/>
    </root>
</springProfile>


```
<springProperty scope="context" name="DISABLE_LOGSTASH_ASYNC"
                source="DISABLE_LOGSTASH_ASYNC"
                default="false"/>

<appender name="LOGSTASH_ASYNC" class="ch.qos.logback.classic.AsyncAppender">
    <queueSize>10000</queueSize>
    <discardingThreshold>0</discardingThreshold>
    <neverBlock>true</neverBlock>
    <appender-ref ref="LOGSTASH_TCP_DMZR"/>
</appender>

<springProfile name="local-development-ssl | local-development-non-ssl">
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="info">
        <appender-ref ref="CONSOLE"/>
    </root>
</springProfile>

<springProfile name="openshift-d0 | openshift-d1 | openshift-p0 | openshift-t0 | openshift-t1 | openshift-u0 | openshift-u1 | openshift-x0">
    <root level="info">
        <appender-ref ref="ENRICHED_CONSOLE_DMZR"/>
        <appender-ref ref="LOGSTASH_ASYNC"/>
    </root>
</springProfile>

```
<springProperty scope="context" name="DISABLE_LOGSTASH_ASYNC"
                source="DISABLE_LOGSTASH_ASYNC"
                default="false"/>

<appender name="LOGSTASH_ASYNC" class="ch.qos.logback.classic.AsyncAppender">
    <queueSize>10000</queueSize>
    <discardingThreshold>0</discardingThreshold>
    <neverBlock>true</neverBlock>
    <appender-ref ref="LOGSTASH_TCP_DMZR"/>
</appender>

<springProfile name="local-development-ssl | local-development-non-ssl">
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="info">
        <appender-ref ref="CONSOLE"/>
    </root>
</springProfile>

<springProfile name="openshift-d0 | openshift-d1 | openshift-p0 | openshift-t0 | openshift-t1 | openshift-u0 | openshift-u1 | openshift-x0">
    <root level="info">
        <appender-ref ref="ENRICHED_CONSOLE_DMZR"/>
        <appender-ref ref="LOGSTASH_ASYNC"/>
    </root>
</springProfile>
