<build>
    <plugins>
        <plugin>
            <artifactId>maven-resources-plugin</artifactId>
            <version>3.2.0</version>
            <executions>
                <execution>
                    <id>copy-api-mustache</id>
                    <phase>process-resources</phase>
                    <goals>
                        <goal>copy-resources</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>${project.build.directory}/classes</outputDirectory>
                        <resources>
                            <resource>
                                <directory>/path/to/original/project/src/main/resources</directory>
                                <includes>
                                    <include>api.mustache</include>
                                </includes>
                            </resource>
                        </resources>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SoapLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(SoapLoggingAspect.class);

    @Around("@annotation(org.springframework.ws.server.endpoint.annotation.PayloadRoot)")
    public Object logSoapRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        Object response;
        try {
            // Log the request payload
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                logger.info("SOAP Request Payload: {}", args[0]);
            }

            // Proceed with the actual SOAP method
            response = joinPoint.proceed();

            // Log the response payload
            logger.info("SOAP Response Payload: {}", response);
        } catch (Throwable t) {
            logger.error("Error processing SOAP request/response: {}", t.getMessage());
            throw t;
        }
        return response;
    }
}
