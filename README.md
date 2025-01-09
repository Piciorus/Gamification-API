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


import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPMessage;
import java.util.Set;
import java.util.logging.Logger;

public class SoapLoggingHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger LOGGER = Logger.getLogger(SoapLoggingHandler.class.getName());

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean isOutbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        SOAPMessage soapMessage = context.getMessage();
        try {
            if (isOutbound) {
                LOGGER.info("Outbound SOAP Request:");
            } else {
                LOGGER.info("Inbound SOAP Response:");
            }
            soapMessage.writeTo(System.out); // Write the message to System.out or a logger
            System.out.println(); // For formatting in console
        } catch (Exception e) {
            LOGGER.severe("Error logging SOAP message: " + e.getMessage());
        }
        return true; // Continue processing the message
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        LOGGER.severe("SOAP Fault Detected");
        return handleMessage(context);
    }

    @Override
    public void close(MessageContext context) {
        // Cleanup resources if needed
    }

    @Override
    public Set<QName> getHeaders() {
        return null; // Process all headers
    }
}

public I getPort() throws WebServiceException {
    try {
        C service = getServiceObject();
        I port = service.getPort(seiClass);

        if (port instanceof BindingProvider) {
            BindingProvider bp = (BindingProvider) port;
            Map<String, Object> reqCtx = bp.getRequestContext();

            // Set endpoint URL and timeouts
            reqCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceUrl);
            reqCtx.put("com.sun.xml.ws.connect.timeout", crmConnectTimeout);
            reqCtx.put("com.sun.xml.ws.request.timeout", crmRequestTimeout);

            // Attach SOAP handlers
            List<Handler> handlerChain = new ArrayList<>();
            handlerChain.add(new SoapLoggingHandler());
            bp.getBinding().setHandlerChain(handlerChain);
        }

        return port;

    } catch (Exception e) {
        String logMessage = "Failed to obtain service port: service class " + serviceClass + ", SEI class " + seiClass;
        throw new WebServiceException(logMessage, e);
    }
}

import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class LoggingInterceptor implements ClientInterceptor {

    @Override
    public boolean handleRequest(MessageContext messageContext) {
        try {
            StringWriter requestWriter = new StringWriter();
            TransformerFactory.newInstance()
                .newTransformer()
                .transform(messageContext.getRequest().getPayloadSource(), new StreamResult(requestWriter));
            System.out.println("SOAP Request: " + requestWriter.toString());
        } catch (Exception e) {
            System.err.println("Failed to log SOAP Request: " + e.getMessage());
        }
        return true; // Continue the processing of the request
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        try {
            StringWriter responseWriter = new StringWriter();
            TransformerFactory.newInstance()
                .newTransformer()
                .transform(messageContext.getResponse().getPayloadSource(), new StreamResult(responseWriter));
            System.out.println("SOAP Response: " + responseWriter.toString());
        } catch (Exception e) {
            System.err.println("Failed to log SOAP Response: " + e.getMessage());
        }
        return true; // Continue the processing of the response
    }

    @Override
    public boolean handleFault(MessageContext messageContext) {
        try {
            StringWriter faultWriter = new StringWriter();
            TransformerFactory.newInstance()
                .newTransformer()
                .transform(messageContext.getResponse().getPayloadSource(), new StreamResult(faultWriter));
            System.out.println("SOAP Fault: " + faultWriter.toString());
        } catch (Exception e) {
            System.err.println("Failed to log SOAP Fault: " + e.getMessage());
        }
        return true; // Continue the processing of the fault
    }
}
@Component
public class CrmService {

    private final GenericCrmRestClient crmRestClient;

    @Autowired
    public CrmService(GenericCrmRestClient crmRestClient) {
        this.crmRestClient = crmRestClient;
    }

    public RelatedContactResponse getRelatedContact(Long contactId) {
        Map<String, String> uriVariables = Map.of("id", String.valueOf(contactId));

        return crmRestClient.executeRequest(
                "/contact/{id}/related",
                uriVariables,
                url -> crmRestClient.getRestTemplate().getForObject(url, RelatedContactResponse.class, uriVariables)
        );
    }

    public void updateContact(Long contactId, ContactRequest contactRequest) {
        Map<String, String> uriVariables = Map.of("id", String.valueOf(contactId));

        crmRestClient.executeRequestWithBody(
                "/contact/{id}",
                contactRequest,
                uriVariables,
                requestDetails -> {
                    crmRestClient.getRestTemplate().put(
                            requestDetails.getUrl(),
                            requestDetails.getBody(),
                            requestDetails.getUriVariables()
                    );
                    return null; // No response for PUT
                }
        );
    }

    public void deleteContact(Long contactId) {
        Map<String, String> uriVariables = Map.of("id", String.valueOf(contactId));

        crmRestClient.executeRequest(
                "/contact/{id}",
                uriVariables,
                url -> {
                    crmRestClient.getRestTemplate().delete(url, uriVariables);
                    return null; // No response for DELETE
                }
        );
    }

    public ContactResponse createContact(ContactRequest contactRequest) {
        return crmRestClient.executeRequestWithBody(
                "/contact",
                contactRequest,
                Map.of(),
                requestDetails -> crmRestClient.getRestTemplate().postForObject(
                        requestDetails.getUrl(),
                        requestDetails.getBody(),
                        ContactResponse.class
                )
        );
    }
}

