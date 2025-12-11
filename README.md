package de.consorsbank.trading.brkprcsc.custpm.rest.adapter.feign;

import com.consorsbank.common.error.handling.exception.CommonException;
import com.consorsbank.common.error.handling.exception.authorization.CommonExceptionCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.consorsbank.trading.brkprcsc.custpm.rest.adapter.dtos.CustpmApiError;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustpmFeignErrorDecoderTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Response response;

    @Mock
    private Response.Body responseBody;

    private CustpmFeignErrorDecoder errorDecoder;

    private static final String METHOD_KEY = "CustpmClient#getCustomer()";

    @BeforeEach
    void setUp() {
        errorDecoder = new CustpmFeignErrorDecoder(objectMapper);
    }

    @Test
    void shouldSuccessfullyParseCustpmApiErrorInTryBlock() throws IOException {
        // Given - Valid JSON that will be successfully parsed
        String errorJson = "{\"timestamp\":\"2024-12-11T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"path\":\"/api/customer\"}";
        InputStream inputStream = new ByteArrayInputStream(errorJson.getBytes(StandardCharsets.UTF_8));
        
        CustpmApiError apiError = CustpmApiError.builder()
                .timestamp("2024-12-11T10:00:00")
                .status(404)
                .error("Not Found")
                .path("/api/customer")
                .build();

        when(response.body()).thenReturn(responseBody);
        when(responseBody.asInputStream()).thenReturn(inputStream);
        when(objectMapper.readValue(any(InputStream.class), eq(CustpmApiError.class)))
                .thenReturn(apiError);

        // When - The try block executes successfully, no IOException thrown
        CommonException result = assertThrows(CommonException.class, 
                () -> errorDecoder.decode(METHOD_KEY, response));

        // Then - Verify the try block was executed (ObjectMapper was called)
        verify(objectMapper, times(1)).readValue(any(InputStream.class), eq(CustpmApiError.class));
        verify(responseBody, times(1)).asInputStream();
        
        // Verify that CommonException is thrown with SERVER_ERROR (after successful parsing)
        assertEquals(CommonExceptionCode.SERVER_ERROR, result.getCommonExceptionCode());
        
        // Verify that details list is null or empty (not from catch block)
        assertTrue(result.getDetails() == null || result.getDetails().isEmpty(), 
                "Details should be empty when try block succeeds");
    }

    @Test
    void shouldThrowCommonExceptionWhenIOExceptionOccurs() throws IOException {
        // Given
        String errorMessage = "Failed to parse response";
        IOException ioException = new IOException(errorMessage);
        
        when(response.body()).thenReturn(responseBody);
        when(responseBody.asInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(objectMapper.readValue(any(InputStream.class), eq(CustpmApiError.class)))
                .thenThrow(ioException);

        // When
        CommonException result = assertThrows(CommonException.class, 
                () -> errorDecoder.decode(METHOD_KEY, response));

        // Then
        assertEquals(CommonExceptionCode.SERVER_ERROR, result.getCommonExceptionCode());
        assertNotNull(result.getDetails());
        assertEquals(1, result.getDetails().size());
        assertEquals(errorMessage, result.getDetails().get(0));
        
        verify(objectMapper).readValue(any(InputStream.class), eq(CustpmApiError.class));
    }

    @Test
    void shouldHandleNullResponseBody() throws IOException {
        // Given
        when(response.body()).thenReturn(null);

        // When & Then
        assertThrows(NullPointerException.class, 
                () -> errorDecoder.decode(METHOD_KEY, response));
    }

    @Test
    void shouldHandleEmptyResponseBody() throws IOException {
        // Given
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.asInputStream()).thenReturn(emptyStream);
        when(objectMapper.readValue(any(InputStream.class), eq(CustpmApiError.class)))
                .thenThrow(new IOException("No content to map"));

        // When
        CommonException result = assertThrows(CommonException.class,
                () -> errorDecoder.decode(METHOD_KEY, response));

        // Then
        assertEquals(CommonExceptionCode.SERVER_ERROR, result.getCommonExceptionCode());
        verify(objectMapper).readValue(any(InputStream.class), eq(CustpmApiError.class));
    }

    @Test
    void shouldHandleMalformedJson() throws IOException {
        // Given
        String malformedJson = "{invalid json}";
        InputStream inputStream = new ByteArrayInputStream(malformedJson.getBytes(StandardCharsets.UTF_8));
        
        when(response.body()).thenReturn(responseBody);
        when(responseBody.asInputStream()).thenReturn(inputStream);
        when(objectMapper.readValue(any(InputStream.class), eq(CustpmApiError.class)))
                .thenThrow(new IOException("Malformed JSON"));

        // When
        CommonException result = assertThrows(CommonException.class,
                () -> errorDecoder.decode(METHOD_KEY, response));

        // Then
        assertEquals(CommonExceptionCode.SERVER_ERROR, result.getCommonExceptionCode());
        assertTrue(result.getDetails().get(0).contains("Malformed JSON"));
    }

    @Test
    void shouldDecodeMultipleDifferentErrors() throws IOException {
        // Given - First error (404)
        String error404Json = "{\"timestamp\":\"2024-12-11T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"path\":\"/api/customer\"}";
        InputStream inputStream404 = new ByteArrayInputStream(error404Json.getBytes(StandardCharsets.UTF_8));
        
        CustpmApiError apiError404 = CustpmApiError.builder()
                .timestamp("2024-12-11T10:00:00")
                .status(404)
                .error("Not Found")
                .path("/api/customer")
                .build();

        Response response404 = mock(Response.class);
        Response.Body body404 = mock(Response.Body.class);
        
        when(response404.body()).thenReturn(body404);
        when(body404.asInputStream()).thenReturn(inputStream404);
        when(objectMapper.readValue(any(InputStream.class), eq(CustpmApiError.class)))
                .thenReturn(apiError404);

        // When - First decode
        CommonException result404 = assertThrows(CommonException.class,
                () -> errorDecoder.decode(METHOD_KEY, response404));

        // Then
        assertEquals(CommonExceptionCode.SERVER_ERROR, result404.getCommonExceptionCode());
        
        // Given - Second error (500)
        String error500Json = "{\"timestamp\":\"2024-12-11T10:01:00\",\"status\":500,\"error\":\"Internal Server Error\",\"path\":\"/api/account\"}";
        InputStream inputStream500 = new ByteArrayInputStream(error500Json.getBytes(StandardCharsets.UTF_8));
        
        CustpmApiError apiError500 = CustpmApiError.builder()
                .timestamp("2024-12-11T10:01:00")
                .status(500)
                .error("Internal Server Error")
                .path("/api/account")
                .build();

        Response response500 = mock(Response.class);
        Response.Body body500 = mock(Response.Body.class);
        
        when(response500.body()).thenReturn(body500);
        when(body500.asInputStream()).thenReturn(inputStream500);
        when(objectMapper.readValue(any(InputStream.class), eq(CustpmApiError.class)))
                .thenReturn(apiError500);

        // When - Second decode
        CommonException result500 = assertThrows(CommonException.class,
                () -> errorDecoder.decode(METHOD_KEY, response500));

        // Then
        assertEquals(CommonExceptionCode.SERVER_ERROR, result500.getCommonExceptionCode());
        verify(objectMapper, times(2)).readValue(any(InputStream.class), eq(CustpmApiError.class));
    }

    @Test
    void shouldVerifyObjectMapperIsCalledWithCorrectParameters() throws IOException {
        // Given
        String errorJson = "{\"timestamp\":\"2024-12-11T10:00:00\",\"status\":400,\"error\":\"Bad Request\",\"path\":\"/api/validate\"}";
        InputStream inputStream = new ByteArrayInputStream(errorJson.getBytes(StandardCharsets.UTF_8));
        
        CustpmApiError apiError = CustpmApiError.builder()
                .timestamp("2024-12-11T10:00:00")
                .status(400)
                .error("Bad Request")
                .path("/api/validate")
                .build();

        when(response.body()).thenReturn(responseBody);
        when(responseBody.asInputStream()).thenReturn(inputStream);
        when(objectMapper.readValue(any(InputStream.class), eq(CustpmApiError.class)))
                .thenReturn(apiError);

        // When
        assertThrows(CommonException.class, () -> errorDecoder.decode(METHOD_KEY, response));

        // Then
        verify(objectMapper, times(1)).readValue(any(InputStream.class), eq(CustpmApiError.class));
        verify(response, times(1)).body();
        verify(responseBody, times(1)).asInputStream();
        verifyNoMoreInteractions(objectMapper);
    }
}
