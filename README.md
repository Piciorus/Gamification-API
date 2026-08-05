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
