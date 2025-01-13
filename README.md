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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.function.Function;

@Component
public class GenericCrmRestClient {

    private final RestTemplate restTemplate;
    private final CraRestClientConnectionProperties crmConnectionProperties;

    @Autowired
    public GenericCrmRestClient(
            CraRestClientConnectionProperties crmConnectionProperties,
            @Qualifier("crmRestClientTemplate") RestTemplate restTemplate) {
        this.crmConnectionProperties = crmConnectionProperties;
        this.restTemplate = restTemplate;
    }

    /**
     * Perform a generic CRM request using a functional approach
     *
     * @param endpoint    the relative endpoint (e.g., "/contact/{id}")
     * @param uriVariables map of URI variables to be replaced in the endpoint
     * @param executor    a function defining the HTTP operation (GET, POST, PUT, DELETE)
     * @param <T>         the type of the response object
     * @return the response object
     */
    public <T> T executeRequest(
            String endpoint,
            Map<String, String> uriVariables,
            Function<String, T> executor) {
        String url = crmConnectionProperties.getUrl() + endpoint;
        return executor.apply(url);
    }

    /**
     * Execute a POST, PUT, or DELETE request with a request body
     *
     * @param endpoint     the relative endpoint (e.g., "/contact/{id}")
     * @param requestBody  the request body
     * @param uriVariables map of URI variables to be replaced in the endpoint
     * @param executor     a function defining the HTTP operation
     * @param <T>          the type of the response object
     * @return the response object
     */
    public <T> T executeRequestWithBody(
            String endpoint,
            Object requestBody,
            Map<String, String> uriVariables,
            Function<RequestDetails, T> executor) {
        String url = crmConnectionProperties.getUrl() + endpoint;
        RequestDetails requestDetails = new RequestDetails(url, requestBody, uriVariables);
        return executor.apply(requestDetails);
    }

    /**
     * Inner class to encapsulate request details
     */
    public static class RequestDetails {
        private final String url;
        private final Object body;
        private final Map<String, String> uriVariables;

        public RequestDetails(String url, Object body, Map<String, String> uriVariables) {
            this.url = url;
            this.body = body;
            this.uriVariables = uriVariables;
        }

        public String getUrl() {
            return url;
        }

        public Object getBody() {
            return body;
        }

        public Map<String, String> getUriVariables() {
            return uriVariables;
        }
    }
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Component
public class GenericCrmRestClient {

    private final RestTemplate restTemplate;
    private final CraRestClientConnectionProperties crmConnectionProperties;

    @Autowired
    public GenericCrmRestClient(
            CraRestClientConnectionProperties crmConnectionProperties,
            @Qualifier("crmRestClientTemplate") RestTemplate restTemplate) {
        this.crmConnectionProperties = crmConnectionProperties;
        this.restTemplate = restTemplate;
    }

    /**
     * Perform a generic CRM request for methods like GET or DELETE
     *
     * @param endpoint    the relative endpoint (e.g., "/contact/{id}")
     * @param uriVariables map of URI variables to be replaced in the endpoint
     * @param executor    a function defining the HTTP operation (GET or DELETE)
     * @param <T>         the type of the response object
     * @return the response object
     */
    public <T> T executeRequest(
            String endpoint,
            Map<String, String> uriVariables,
            Function<String, T> executor) {
        String url = crmConnectionProperties.getUrl() + endpoint;
        return executor.apply(url);
    }

    /**
     * Perform a generic CRM request for methods like POST or PUT that include a request body
     *
     * @param endpoint     the relative endpoint (e.g., "/contact/{id}")
     * @param requestBody  the request body
     * @param uriVariables map of URI variables to be replaced in the endpoint
     * @param executor     a function defining the HTTP operation (POST or PUT)
     * @param <T>          the type of the response object
     * @return the response object
     */
    public <T> T executeRequestWithBody(
            String endpoint,
            Object requestBody,
            Map<String, String> uriVariables,
            BiFunction<String, Object, T> executor) {
        String url = crmConnectionProperties.getUrl() + endpoint;
        return executor.apply(url, requestBody);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class CraRestClient {

    private final RestTemplate restTemplate;
    private final CraRestClientConnectionProperties craConnectionProperties;
    private final OpenAPI openApiSpec;

    public CraRestClient(CraRestClientConnectionProperties craConnectionProperties,
                         RestTemplate restTemplate,
                         String openApiFilePath) {
        this.craConnectionProperties = craConnectionProperties;
        this.restTemplate = restTemplate;
        this.openApiSpec = new OpenAPIV3Parser().read(openApiFilePath); // Load OpenAPI spec
    }

    /**
     * Resolves the endpoint dynamically from OpenAPI specification using the operation ID.
     *
     * @param operationId the operation ID
     * @return the resolved endpoint
     */
    private String resolveEndpoint(String operationId) {
        Paths paths = openApiSpec.getPaths();
        for (Map.Entry<String, PathItem> entry : paths.entrySet()) {
            PathItem pathItem = entry.getValue();

            for (Map.Entry<PathItem.HttpMethod, io.swagger.v3.oas.models.Operation> operationEntry : pathItem.readOperationsMap().entrySet()) {
                if (operationId.equals(operationEntry.getValue().getOperationId())) {
                    return entry.getKey(); // Return the resolved endpoint path
                }
            }
        }
        throw new IllegalArgumentException("Operation ID not found in OpenAPI spec: " + operationId);
    }

    /**
     * Makes a GET request to a dynamically resolved endpoint.
     *
     * @param operationId  the OpenAPI operation ID
     * @param responseType the expected response type
     * @param uriVariables optional URI variables for path replacement
     * @param <T>          the response object type
     * @return the response object
     */
    public <T> T callRestEndpointWithoutBody(String operationId, Class<T> responseType, Map<String, ?> uriVariables) {
        String endpoint = resolveEndpoint(operationId);
        String url = craConnectionProperties.getUrl() + endpoint;
        return restTemplate.getForObject(url, responseType, uriVariables);
    }
}
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Arrays;

@Component
public class CriRestBaseService {

    private final RestTemplate restTemplate;
    private final OpenAPI openAPI;

    @Value("${com.rest.client.url}")
    private String baseUrl;

    @Autowired
    public CriRestBaseService(@Qualifier("craRestClientTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.openAPI = loadOpenApiSpec("src/main/resources");
    }

    /**
     * Loads the OpenAPI specification dynamically from a directory.
     *
     * @param directoryPath The directory containing the OpenAPI YAML files.
     * @return The parsed OpenAPI object.
     */
    private OpenAPI loadOpenApiSpec(String directoryPath) {
        File directory = new File(directoryPath);

        // Filter YAML files in the directory
        File[] yamlFiles = directory.listFiles((dir, name) -> name.endsWith(".yaml") || name.endsWith(".yml"));

        if (yamlFiles == null || yamlFiles.length == 0) {
            throw new IllegalArgumentException("No OpenAPI YAML files found in directory: " + directoryPath);
        }

        // Use the first YAML file found in the directory
        File yamlFile = yamlFiles[0];
        System.out.println("Loading OpenAPI spec from: " + yamlFile.getAbsolutePath());

        return new OpenAPIV3Parser().read(yamlFile.getAbsolutePath());
    }

    /**
     * Resolves the endpoint dynamically from the OpenAPI specification using the operation ID.
     *
     * @param operationId The operation ID.
     * @return The resolved endpoint.
     */
    public String resolveEndpoint(String operationId) {
        return openAPI.getPaths()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().readOperationsMap()
                        .values()
                        .stream()
                        .anyMatch(operation -> operationId.equals(operation.getOperationId())))
                .map(entry -> entry.getKey())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Operation ID not found in OpenAPI spec: " + operationId));
    }

    // Example usage
    public String getDynamicEndpoint(String operationId) {
        return resolveEndpoint(operationId);
    }
}
