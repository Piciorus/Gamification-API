```
@ParameterizedTest
@CsvSource({
        "request/generateSignatures/json/generateSignaturesValidRequest.json," +
        "response/generateSignatures/json/generateSignaturesValidResponse.json," + CONTENT_TYPE_JSON,
        "request/generateSignatures/xml/generateSignaturesValidRequest.xml," +
        "response/generateSignatures/xml/generateSignaturesValidResponse.xml," + CONTENT_TYPE_XML
})
void givenValidRequest_whenGenerateSignatures_thenReturnValidResponse(
        final String requestFilePath,
        final String responseFilePath,
        final String mediaType) throws Exception {
    // given
    when(deviceRestServiceMock.generateSignatures(APP_INSTANCE_ID, TX_ID))
            .thenReturn(GENERATE_SIGNATURES_RESPONSE);

    // when
    mvc.perform(post(GENERATE_SIGNATURES_PATH)
                    .header(X_REQUEST_ID_HEADER, X_REQUEST_ID_HEADER_VALUE)
                    .header(AUTHORIZATION_HEADER, AUTHORIZATION)
                    .param("appInstanceId", APP_INSTANCE_ID)
                    .param("txId", TX_ID)
                    .accept(MediaType.valueOf(mediaType))
                    .contentType(MediaType.valueOf(mediaType))
                    .content(TestUtils.readFile(requestFilePath)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists(X_CORRELATION_ID_HEADER))
            .andExpect(header().string(X_CORRELATION_ID_HEADER, X_CORRELATION_ID_HEADER_VALUE))
            .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf(mediaType)))
            .andExpect(getResultMatcher(responseFilePath, mediaType));

    verify(deviceRestServiceMock).generateSignatures(APP_INSTANCE_ID, TX_ID);
    verifyNoMoreInteractions(deviceRestServiceMock);
}

```



```

@ParameterizedTest
@CsvSource({
        "request/generateSignatures/json/generateSignaturesEmptyTxId.json," +
        "response/generateSignatures/json/generateSignaturesEmptyTxIdResponse.json," + CONTENT_TYPE_JSON,
        "request/generateSignatures/xml/generateSignaturesEmptyTxId.xml," +
        "response/generateSignatures/xml/generateSignaturesEmptyTxIdResponse.xml," + CONTENT_TYPE_XML
})
void givenEmptyTxId_whenGenerateSignatures_thenReturnEmptyBoundSignature(
        final String requestFilePath,
        final String responseFilePath,
        final String mediaType) throws Exception {
    // given
    when(deviceRestServiceMock.generateSignatures(APP_INSTANCE_ID, ""))
            .thenReturn(GENERATE_SIGNATURES_RESPONSE);

    // when
    mvc.perform(post(GENERATE_SIGNATURES_PATH)
                    .header(X_REQUEST_ID_HEADER, X_REQUEST_ID_HEADER_VALUE)
                    .header(AUTHORIZATION_HEADER, AUTHORIZATION)
                    .param("appInstanceId", APP_INSTANCE_ID)
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

    verify(deviceRestServiceMock).generateSignatures(APP_INSTANCE_ID, "");
    verifyNoMoreInteractions(deviceRestServiceMock);
}
```



```
@ParameterizedTest
@CsvSource({
        "request/generateSignatures/json/generateSignaturesEmptyAppInstanceId.json," +
        "response/generateSignatures/json/generateSignaturesEmptyAppInstanceIdResponse.json," + CONTENT_TYPE_JSON,
        "request/generateSignatures/xml/generateSignaturesEmptyAppInstanceId.xml," +
        "response/generateSignatures/xml/generateSignaturesEmptyAppInstanceIdResponse.xml," + CONTENT_TYPE_XML
})
void givenEmptyAppInstanceId_whenGenerateSignatures_thenReturnEmptyUnboundSignature(
        final String requestFilePath,
        final String responseFilePath,
        final String mediaType) throws Exception {
    // given
    when(deviceRestServiceMock.generateSignatures("", TX_ID))
            .thenReturn(GENERATE_SIGNATURES_RESPONSE);

    // when
    mvc.perform(post(GENERATE_SIGNATURES_PATH)
                    .header(X_REQUEST_ID_HEADER, X_REQUEST_ID_HEADER_VALUE)
                    .header(AUTHORIZATION_HEADER, AUTHORIZATION)
                    .param("appInstanceId", "")
                    .param("txId", TX_ID)
                    .accept(MediaType.valueOf(mediaType))
                    .contentType(MediaType.valueOf(mediaType))
                    .content(TestUtils.readFile(requestFilePath)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists(X_CORRELATION_ID_HEADER))
            .andExpect(header().string(X_CORRELATION_ID_HEADER, X_CORRELATION_ID_HEADER_VALUE))
            .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf(mediaType)))
            .andExpect(getResultMatcher(responseFilePath, mediaType));

    verify(deviceRestServiceMock).generateSignatures("", TX_ID);
    verifyNoMoreInteractions(deviceRestServiceMock);
}
```


```
@ParameterizedTest
@CsvSource({
        "request/generateSignatures/json/generateSignaturesValidRequest.json," + CONTENT_TYPE_JSON,
        "request/generateSignatures/xml/generateSignaturesValidRequest.xml," + CONTENT_TYPE_XML
})
void givenValidRequest_whenGenerateSignatures_thenThrowExceptionFromService(
        final String requestFilePath,
        final String mediaType) throws Exception {
    // given
    when(deviceRestServiceMock.generateSignatures(APP_INSTANCE_ID, TX_ID))
            .thenThrow(new RuntimeException(ERROR_MESSAGE));

    // when
    mvc.perform(post(GENERATE_SIGNATURES_PATH)
                    .header(X_REQUEST_ID_HEADER, X_REQUEST_ID_HEADER_VALUE)
                    .header(AUTHORIZATION_HEADER, AUTHORIZATION)
                    .param("appInstanceId", APP_INSTANCE_ID)
                    .param("txId", TX_ID)
                    .accept(MediaType.valueOf(mediaType))
                    .contentType(MediaType.valueOf(mediaType))
                    .content(TestUtils.readFile(requestFilePath)))
            .andDo(print())
            .andExpect(status().isInternalServerError());

    verify(deviceRestServiceMock).generateSignatures(APP_INSTANCE_ID, TX_ID);
    verifyNoMoreInteractions(deviceRestServiceMock);
}
```
