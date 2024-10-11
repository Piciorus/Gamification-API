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
}
