dsa
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import javax.xml.bind.JAXBElement;

public class CRMGetLastCallAgentsServiceImplTest {

    @Test
    void testGetLastAgentsSuccessful() {
        // Test data
        Integer creCustomerId = 12345;

        // Create a mock of the web service template
        WebServiceTemplate getLastCallAgentsWebServiceTemplate = mock(WebServiceTemplate.class);

        // Create the request object and set the customer ID
        GetLastCallAgentsRequest request = new GetLastCallAgentsRequest();
        request.setContactObjid(Long.valueOf(creCustomerId));

        // Create the GetLastCallAgents request wrapper
        GetLastCallAgents getLastCallAgents = new GetLastCallAgents();
        getLastCallAgents.setRequest(request);

        // Create the expected response object
        GetLastCallAgentsResponse expectedResponse = new GetLastCallAgentsResponse();

        // Mock the JAXBElement for the request
        JAXBElement<GetLastCallAgents> getLastCallAgentsJAXBElement = new ObjectFactory().createGetLastCallAgents(getLastCallAgents);

        // Mock the JAXBElement for the response
        JAXBElement<GetLastCallAgentsResponse> getLastCallAgentsResponseJAXBElement = new JAXBElement<>(
            new QName("http://namespace", "GetLastCallAgentsResponse"),
            GetLastCallAgentsResponse.class,
            expectedResponse
        );

        // Mock the behavior of marshalSendAndReceive to return the mocked response
        when(getLastCallAgentsWebServiceTemplate.marshalSendAndReceive(any(JAXBElement.class)))
            .thenReturn(getLastCallAgentsResponseJAXBElement);

        // Instantiate the service and inject the mocked web service template
        CRMGetLastCallAgentsServiceImpl service = new CRMGetLastCallAgentsServiceImpl(getLastCallAgentsWebServiceTemplate);

        // Call the method under test
        GetLastCallAgentsResponse actualResponse = service.getLastAgents(creCustomerId);

        // Assert that the actual response is equal to the expected response
        assertThat(actualResponse).isEqualTo(expectedResponse);

        // Verify that the web service template was called exactly once
        verify(getLastCallAgentsWebServiceTemplate, times(1)).marshalSendAndReceive(any(JAXBElement.class));
    }
}import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${api.actuator-path}")
    private String actuatorPath;  // "/management"

    // Method to insert "mgmt" into the base URL specifically for actuator group
    private String getActuatorBaseUrlWithMgmt() {
        return baseUrl.replace("crmsps-sc-", "crmsps-sc-mgmt-");
    }

    @Bean
    public GroupedOpenApi applicationApi() {
        return GroupedOpenApi.builder()
                .group("1-Application")
                .displayName("Application")
                .packagesToScan("com.consorsbank.grmaps.rest.adeptor.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi actuatorApi() {
        return GroupedOpenApi.builder()
                .group("2-Actuator")
                .displayName("Actuators")
                .pathsToMatch(actuatorPath + "/**")  // Match paths under "/management"
                .addOpenApiCustomiser(openApi -> openApi.addServersItem(
                        new Server().url(getActuatorBaseUrlWithMgmt() + actuatorPath)))  // Insert "mgmt" for actuator group only
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url(baseUrl))  // Main application URL without "mgmt"
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")))
                .security(List.of(new SecurityRequirement().addList("Bearer Authentication")));
    }
}

