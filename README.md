// AFTER — program to DataSource interface, no Atomikos coupling at all
public class DbHealthCheckIndicator 
        extends AbstractHealthIndicator 
        implements InitializingBean {

    private final DataSource dataSource; // ← interface, not Atomikos class
    private final int connectionTimeout;
    private String databaseVendor;

    public DbHealthCheckIndicator(
            @Qualifier("brkprcDataSource") DataSource brkprcDataSource) {
        super("DataSource health check failed");
        this.dataSource = brkprcDataSource; // ← no cast needed
        this.connectionTimeout = 5;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        int errorCode = check();
        if (errorCode != 1) {
            builder.down()
                   .withDetail("database", databaseVendor)
                   .withDetail("time", LocalDateTime.now())
                   .build();
        } else {
            builder.up()
                   .withDetail("database", databaseVendor)
                   .withDetail("time", LocalDateTime.now())
                   .build();
        }
    }

    protected int check() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection(); // ← standard JDBC, no Atomikos
            databaseVendor = conn.getMetaData().getDatabaseProductName();
            if (!conn.isValid(this.connectionTimeout)) {
                return 0;
            }
            return 1;
        } catch (SQLException e) {
            return 0;
        } finally {
            JdbcUtils.closeConnection(conn);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(this.dataSource != null,
            "DataSource for DbHealthCheck must be specified");
    }
}



@ParameterizedTest
@CsvSource({
        "request/generateSignatures/json/generateSignaturesValidRequest.json," +
        "response/generateSignatures/json/generateSignaturesValidResponse.json,application/json",
        "request/generateSignatures/xml/generateSignaturesValidRequest.xml," +
        "response/generateSignatures/xml/generateSignaturesValidResponse.xml,application/xml"
})
void givenValidRequest_whenGenerateSignatures_thenReturnValidResponse(
        final String requestFilePath,
        final String responseFilePath,
        final String mediaType) throws Exception {
    // given
    when(transactionService.generateBoundedSignature(TX_ID_QUERY_PARAM, Boolean.FALSE))
            .thenReturn(BOUND_SIGNATURE);
    when(deviceService.generateUnboundSignature(APP_INSTANCE_ID_QUERY_PARAM))
            .thenReturn(UNBOUND_SIGNATURE);

    // when
    mvc.perform(post(GENERATE_SIGNATURES_PATH)
                    .header(X_REQUEST_ID_HEADER, X_REQUEST_ID_HEADER_VALUE)
                    .header(AUTHORIZATION_HEADER, AUTHORIZATION)
                    .param("appInstanceId", APP_INSTANCE_ID_QUERY_PARAM)
                    .param("txId", TX_ID_QUERY_PARAM)
                    .accept(MediaType.valueOf(mediaType))
                    .contentType(MediaType.valueOf(mediaType))
                    .content(TestUtils.readFile(requestFilePath)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists(X_CORRELATION_ID_HEADER))
            .andExpect(header().string(X_CORRELATION_ID_HEADER, X_CORRELATION_ID_HEADER_VALUE))
            .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf(mediaType)))
            .andExpect(getResultMatcher(responseFilePath, mediaType));

    verify(transactionService).generateBoundedSignature(TX_ID_QUERY_PARAM, Boolean.FALSE);
    verify(deviceService).generateUnboundSignature(APP_INSTANCE_ID_QUERY_PARAM);
    verifyNoMoreInteractions(transactionService, deviceService);
}

@ParameterizedTest
@CsvSource({
        "request/generateSignatures/json/generateSignaturesEmptyTxId.json," +
        "response/generateSignatures/json/generateSignaturesEmptyTxIdResponse.json,application/json",
        "request/generateSignatures/xml/generateSignaturesEmptyTxId.xml," +
        "response/generateSignatures/xml/generateSignaturesEmptyTxIdResponse.xml,application/xml"
})
void givenEmptyTxId_whenGenerateSignatures_thenReturnEmptyBoundSignature(
        final String requestFilePath,
        final String responseFilePath,
        final String mediaType) throws Exception {
    // given
    when(deviceService.generateUnboundSignature(APP_INSTANCE_ID_QUERY_PARAM))
            .thenReturn(UNBOUND_SIGNATURE);

    // when
    mvc.perform(post(GENERATE_SIGNATURES_PATH)
                    .header(X_REQUEST_ID_HEADER, X_REQUEST_ID_HEADER_VALUE)
                    .header(AUTHORIZATION_HEADER, AUTHORIZATION)
                    .param("appInstanceId", APP_INSTANCE_ID_QUERY_PARAM)
                    .param("txId", "")
                    .accept(MediaType.valueOf(mediaType))
                    .contentType(MediaType.valueOf(mediaType))
                    .content(TestUtils.readFile(requestFilePath)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists(X_CORRELATION_ID_HEADER))
            .andExpect(header().string(X_CORRELATION_ID_HEADER, X_CORRELATION_ID_HEADER_VALUE))
            .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf(mediaType)))
            .andExpect(getResultMatcher(responseFilePath, mediaType));

    verify(deviceService).generateUnboundSignature(APP_INSTANCE_ID_QUERY_PARAM);
    verifyNoMoreInteractions(deviceService);
    verifyNoInteractions(transactionService);
}

@ParameterizedTest
@CsvSource({
        "request/generateSignatures/json/generateSignaturesEmptyAppInstanceId.json," +
        "response/generateSignatures/json/generateSignaturesEmptyAppInstanceIdResponse.json,application/json",
        "request/generateSignatures/xml/generateSignaturesEmptyAppInstanceId.xml," +
        "response/generateSignatures/xml/generateSignaturesEmptyAppInstanceIdResponse.xml,application/xml"
})
void givenEmptyAppInstanceId_whenGenerateSignatures_thenReturnEmptyUnboundSignature(
        final String requestFilePath,
        final String responseFilePath,
        final String mediaType) throws Exception {
    // given
    when(transactionService.generateBoundedSignature(TX_ID_QUERY_PARAM, Boolean.FALSE))
            .thenReturn(BOUND_SIGNATURE);

    // when
    mvc.perform(post(GENERATE_SIGNATURES_PATH)
                    .header(X_REQUEST_ID_HEADER, X_REQUEST_ID_HEADER_VALUE)
                    .header(AUTHORIZATION_HEADER, AUTHORIZATION)
                    .param("appInstanceId", "")
                    .param("txId", TX_ID_QUERY_PARAM)
                    .accept(MediaType.valueOf(mediaType))
                    .contentType(MediaType.valueOf(mediaType))
                    .content(TestUtils.readFile(requestFilePath)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists(X_CORRELATION_ID_HEADER))
            .andExpect(header().string(X_CORRELATION_ID_HEADER, X_CORRELATION_ID_HEADER_VALUE))
            .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf(mediaType)))
            .andExpect(getResultMatcher(responseFilePath, mediaType));

    verify(transactionService).generateBoundedSignature(TX_ID_QUERY_PARAM, Boolean.FALSE);
    verifyNoMoreInteractions(transactionService);
    verifyNoInteractions(deviceService);
}

@ParameterizedTest
@CsvSource({
        "request/generateSignatures/json/generateSignaturesValidRequest.json,application/json",
        "request/generateSignatures/xml/generateSignaturesValidRequest.xml,application/xml"
})
void givenValidRequest_whenGenerateSignatures_thenThrowExceptionFromTransactionService(
        final String requestFilePath,
        final String mediaType) throws Exception {
    // given
    when(transactionService.generateBoundedSignature(TX_ID_QUERY_PARAM, Boolean.FALSE))
            .thenThrow(new RuntimeException(ERROR_MESSAGE));

    // when
    mvc.perform(post(GENERATE_SIGNATURES_PATH)
                    .header(X_REQUEST_ID_HEADER, X_REQUEST_ID_HEADER_VALUE)
                    .header(AUTHORIZATION_HEADER, AUTHORIZATION)
                    .param("appInstanceId", APP_INSTANCE_ID_QUERY_PARAM)
                    .param("txId", TX_ID_QUERY_PARAM)
                    .accept(MediaType.valueOf(mediaType))
                    .contentType(MediaType.valueOf(mediaType))
                    .content(TestUtils.readFile(requestFilePath)))
            .andDo(print())
            .andExpect(status().isInternalServerError());

    verify(transactionService).generateBoundedSignature(TX_ID_QUERY_PARAM, Boolean.FALSE);
    verifyNoMoreInteractions(transactionService);
}private static final String GENERATE_SIGNATURES_PATH = "/generate-signatures";request/generateSignatures/json/generateSignaturesValidRequest.json      → {}
response/generateSignatures/json/generateSignaturesValidResponse.json    → {"boundSignature":"test-bound-signature","unboundSignature":"test-unbound-signature"}
response/generateSignatures/json/generateSignaturesEmptyTxIdResponse.json → {"boundSignature":"","unboundSignature":"test-unbound-signature"}
response/generateSignatures/json/generateSignaturesEmptyAppInstanceIdResponse.json → {"boundSignature":"test-bound-signature","unboundSignature":""}





private static final String TX_ID_QUERY_PARAM = "test-tx-id";
private static final String APP_INSTANCE_ID_QUERY_PARAM = "test-app-instance-id";
private static final String BOUND_SIGNATURE = "test-bound-signature";
private static final String UNBOUND_SIGNATURE = "test-unbound-signature";


@Test
void givenValidRequest_whenGenerateSignatures_withBothIds_thenReturnValidResponse() {
    // given
    when(transactionService.generateBoundedSignature(TX_ID_QUERY_PARAM, Boolean.FALSE))
            .thenReturn(BOUND_SIGNATURE);
    when(deviceService.generateUnboundSignature(APP_INSTANCE_ID_QUERY_PARAM))
            .thenReturn(UNBOUND_SIGNATURE);

    // when
    final ResponseEntity<GenerateSignaturesResponse> response = underTest.generateSignatures(
            AUTHORIZATION, X_REQUEST_ID_HEADER_VALUE, APP_INSTANCE_ID_QUERY_PARAM, TX_ID_QUERY_PARAM);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getBoundSignature()).isEqualTo(BOUND_SIGNATURE);
    assertThat(response.getBody().getUnboundSignature()).isEqualTo(UNBOUND_SIGNATURE);
}

@Test
void givenValidRequest_whenGenerateSignatures_withEmptyTxId_thenReturnEmptyBoundSignature() {
    // given
    when(deviceService.generateUnboundSignature(APP_INSTANCE_ID_QUERY_PARAM))
            .thenReturn(UNBOUND_SIGNATURE);

    // when
    final ResponseEntity<GenerateSignaturesResponse> response = underTest.generateSignatures(
            AUTHORIZATION, X_REQUEST_ID_HEADER_VALUE, APP_INSTANCE_ID_QUERY_PARAM, "");

    // then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getBoundSignature()).isEmpty();
    assertThat(response.getBody().getUnboundSignature()).isEqualTo(UNBOUND_SIGNATURE);
    verifyNoInteractions(transactionService);
}

@Test
void givenValidRequest_whenGenerateSignatures_withEmptyAppInstanceId_thenReturnEmptyUnboundSignature() {
    // given
    when(transactionService.generateBoundedSignature(TX_ID_QUERY_PARAM, Boolean.FALSE))
            .thenReturn(BOUND_SIGNATURE);

    // when
    final ResponseEntity<GenerateSignaturesResponse> response = underTest.generateSignatures(
            AUTHORIZATION, X_REQUEST_ID_HEADER_VALUE, "", TX_ID_QUERY_PARAM);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getBoundSignature()).isEqualTo(BOUND_SIGNATURE);
    assertThat(response.getBody().getUnboundSignature()).isEmpty();
    verifyNoInteractions(deviceService);
}

@Test
void givenValidRequest_whenGenerateSignatures_thenThrowExceptionFromTransactionService() {
    // given
    when(transactionService.generateBoundedSignature(TX_ID_QUERY_PARAM, Boolean.FALSE))
            .thenThrow(new RuntimeException(ERROR_MESSAGE));

    // when
    final RuntimeException exception = assertThrows(RuntimeException.class,
            () -> underTest.generateSignatures(
                    AUTHORIZATION, X_REQUEST_ID_HEADER_VALUE, APP_INSTANCE_ID_QUERY_PARAM, TX_ID_QUERY_PARAM));

    // then
    assertThat(exception).isNotNull().hasMessage(ERROR_MESSAGE);
}

@Test
void givenValidRequest_whenGenerateSignatures_thenThrowExceptionFromDeviceService() {
    // given
    when(transactionService.generateBoundedSignature(TX_ID_QUERY_PARAM, Boolean.FALSE))
            .thenReturn(BOUND_SIGNATURE);
    when(deviceService.generateUnboundSignature(APP_INSTANCE_ID_QUERY_PARAM))
            .thenThrow(new RuntimeException(ERROR_MESSAGE));

    // when
    final RuntimeException exception = assertThrows(RuntimeException.class,
            () -> underTest.generateSignatures(
                    AUTHORIZATION, X_REQUEST_ID_HEADER_VALUE, APP_INSTANCE_ID_QUERY_PARAM, TX_ID_QUERY_PARAM));

    // then
    assertThat(exception).isNotNull().hasMessage(ERROR_MESSAGE);
}



```
docker logs transauth-kobil-sc 2>&1 | grep -A5 "trustStore"
```


```
docker logs transauth-kobil-sc 2>&1 | grep -E "ERROR|Exception|Caused by" | head -20
```


```
docker exec transauth-kobil-sc cat /config/application-local-transauth-kobil.yml 2>&1 | head -5
```
