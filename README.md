
```
// Add these constants at the top of DeviceRestServiceImplTest
private static final String TX_ID = "tx-123";
private static final String BOUND_SIGNATURE = "bound-sig-abc";
private static final String UNBOUND_SIGNATURE = "unbound-sig-xyz";
private static final GenerateSignaturesResponse GENERATE_SIGNATURES_RESPONSE =
    new GenerateSignaturesResponse(BOUND_SIGNATURE, UNBOUND_SIGNATURE);

// --- Tests ---

@Test
void givenValidTxId_whenGenerateSignatures_thenReturnExpectedResponse() {
    // given
    when(transactionService.generateBoundedSignature(TX_ID, Boolean.FALSE))
        .thenReturn(BOUND_SIGNATURE);
    when(deviceService.generateUnboundSignature())
        .thenReturn(UNBOUND_SIGNATURE);

    // when
    final GenerateSignaturesResponse response = underTest.generateSignatures(TX_ID);

    // then
    assertThat(response).isNotNull().usingRecursiveComparison()
        .isEqualTo(GENERATE_SIGNATURES_RESPONSE);
    verify(transactionService).generateBoundedSignature(TX_ID, Boolean.FALSE);
    verify(deviceService).generateUnboundSignature();
}

@Test
void givenEmptyTxId_whenGenerateSignatures_thenBoundSignatureIsEmpty() {
    // given
    when(deviceService.generateUnboundSignature())
        .thenReturn(UNBOUND_SIGNATURE);

    // when
    final GenerateSignaturesResponse response = underTest.generateSignatures("");

    // then
    assertThat(response).isNotNull();
    assertThat(response.boundSignature()).isEqualTo("");
    assertThat(response.unboundSignature()).isEqualTo(UNBOUND_SIGNATURE);
    verifyNoInteractions(transactionService);
}

@Test
void givenNullTxId_whenGenerateSignatures_thenBoundSignatureIsEmpty() {
    // given
    when(deviceService.generateUnboundSignature())
        .thenReturn(UNBOUND_SIGNATURE);

    // when
    final GenerateSignaturesResponse response = underTest.generateSignatures(null);

    // then
    assertThat(response).isNotNull();
    assertThat(response.boundSignature()).isEqualTo("");
    assertThat(response.unboundSignature()).isEqualTo(UNBOUND_SIGNATURE);
    verifyNoInteractions(transactionService);
}

@Test
void givenTransactionServiceThrowsException_whenGenerateSignatures_thenThrowException() {
    // given
    when(transactionService.generateBoundedSignature(TX_ID, Boolean.FALSE))
        .thenThrow(new RuntimeException(ERROR_MESSAGE));
    when(deviceService.generateUnboundSignature())
        .thenReturn(UNBOUND_SIGNATURE);

    // when
    final RuntimeException exception = assertThrows(RuntimeException.class,
        () -> underTest.generateSignatures(TX_ID));

    // then
    assertThat(exception.getMessage()).isEqualTo(ERROR_MESSAGE);
}

@Test
void givenDeviceServiceThrowsException_whenGenerateSignatures_thenThrowException() {
    // given
    when(transactionService.generateBoundedSignature(TX_ID, Boolean.FALSE))
        .thenReturn(BOUND_SIGNATURE);
    when(deviceService.generateUnboundSignature())
        .thenThrow(new RuntimeException(ERROR_MESSAGE));

    // when
    final RuntimeException exception = assertThrows(RuntimeException.class,
        () -> underTest.generateSignatures(TX_ID));

    // then
    assertThat(exception.getMessage()).isEqualTo(ERROR_MESSAGE);
}

```
