```

@Test
void givenValidRequest_whenGenerateSignatures_withBothIds_thenReturnValidResponse() {
    // given
    when(deviceRestServiceMock.generateSignatures(APP_INSTANCE_ID, TX_ID))
            .thenReturn(GENERATE_SIGNATURES_RESPONSE);

    // when
    final ResponseEntity<GenerateSignaturesResponse> response = underTest.generateSignatures(
            AUTHORIZATION, X_REQUEST_ID_HEADER_VALUE, APP_INSTANCE_ID, TX_ID);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).isEqualTo(GENERATE_SIGNATURES_RESPONSE);
}

@Test
void givenValidRequest_whenGenerateSignatures_withEmptyTxId_thenReturnValidResponse() {
    // given
    when(deviceRestServiceMock.generateSignatures(APP_INSTANCE_ID, ""))
            .thenReturn(GENERATE_SIGNATURES_RESPONSE);

    // when
    final ResponseEntity<GenerateSignaturesResponse> response = underTest.generateSignatures(
            AUTHORIZATION, X_REQUEST_ID_HEADER_VALUE, APP_INSTANCE_ID, "");

    // then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(GENERATE_SIGNATURES_RESPONSE);
}

@Test
void givenValidRequest_whenGenerateSignatures_withEmptyAppInstanceId_thenReturnValidResponse() {
    // given
    when(deviceRestServiceMock.generateSignatures("", TX_ID))
            .thenReturn(GENERATE_SIGNATURES_RESPONSE);

    // when
    final ResponseEntity<GenerateSignaturesResponse> response = underTest.generateSignatures(
            AUTHORIZATION, X_REQUEST_ID_HEADER_VALUE, "", TX_ID);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(GENERATE_SIGNATURES_RESPONSE);
}

@Test
void givenValidRequest_whenGenerateSignatures_thenThrowExceptionFromService() {
    // given
    when(deviceRestServiceMock.generateSignatures(APP_INSTANCE_ID, TX_ID))
            .thenThrow(new RuntimeException(ERROR_MESSAGE));

    // when
    final RuntimeException exception = assertThrows(RuntimeException.class,
            () -> underTest.generateSignatures(
                    AUTHORIZATION, X_REQUEST_ID_HEADER_VALUE, APP_INSTANCE_ID, TX_ID));

    // then
    assertThat(exception).isNotNull().hasMessage(ERROR_MESSAGE);
}
```


```
private static final GenerateSignaturesResponse GENERATE_SIGNATURES_RESPONSE =
        new GenerateSignaturesResponse("test-bound-signature", "test-unbound-signature");
```
