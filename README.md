```
<springProperty scope="context" name="LOGSTASH_ENABLED"
                source="logging.logstash.enabled"
                default="false"/>

<!-- Appender only created if enabled -->
<if condition='property("LOGSTASH_ENABLED").equals("true")'>
    <then>
        <appender name="LOGSTASH_ASYNC" class="ch.qos.logback.classic.AsyncAppender">
            <queueSize>10000</queueSize>
            <discardingThreshold>0</discardingThreshold>
            <neverBlock>true</neverBlock>
            <appender-ref ref="LOGSTASH_TCP_DMZR"/>
        </appender>
    </then>
</if>

<root level="info">
    <appender-ref ref="ENRICHED_CONSOLE_DMZR"/>
    <if condition='property("LOGSTASH_ENABLED").equals("true")'>
        <then>
            <appender-ref ref="LOGSTASH_ASYNC"/>
        </then>
    </if>
</root>

```
