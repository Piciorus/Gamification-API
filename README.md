```
@Test
void should_ThrowKycPersonNotFoundException_When_ServiceCallsThrowsExceptionContainingPersonNotFound() {
    // given
    var commonException = new CommonException(
            CommonExceptionCode.SERVER_ERROR,
            List.of(KYC_PERSON_NOT_FOUND));
 
    when(kycRestApiMock.getRiskAndMdcInquiry(PERSON_NUMBER, SourceSystem.COBRA.getValue()))
            .thenThrow(commonException);
 
    // when
    var exception = assertThrows(CommonException.class,
            () -> underTest.getRiskAndMdcInquiry(PERSON_NUMBER));
 
    // then
    verify(kycRestApiMock, times(1))
            .getRiskAndMdcInquiry(PERSON_NUMBER, SourceSystem.COBRA.getValue());
    verify(riskAndMdcInquiryMapper, times(0)).mapResponseFromKyc(any());
 
    assertEquals(CustpmExceptionCode.KYC_PERSON_NOT_FOUND.getErrorCode(), exception.getErrorCode(),
            "Exception Error Code does not match KYC_PERSON_NOT_FOUND");
    assertEquals(List.of(PERSON_NUMBER, SourceSystem.COBRA.getValue()), exception.getMessageArgs(),
            "Exception message args should contain personNumber and source system");
}
```


```
package com.consorsbank.custpm.kyc.rest.adapter.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.consorsbank.common.error.handling.exception.CommonException;
import com.consorsbank.custpm.crm.ws.adapter.exception.CustpmExceptionCode;
import com.consorsbank.custpm.kyc.rest.adapter.configuration.KycRestProperties;
import com.consorsbank.custpm.kyc.rest.adapter.model.kycauth.LoginRequest;
import com.consorsbank.custpm.kyc.rest.adapter.service.kycauth.KycAuthRestApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KycTokenManagerTest {

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String TOKEN_RESPONSE_HEADER = "X-Auth-Token";
    private static final String REFRESH_TOKEN_RESPONSE_HEADER = "Set-Cookie";
    private static final Integer TOKEN_EXPIRATION_MARGIN = 5000;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String REFRESH_PREFIX = "jwt_refresh=";
    private static final Algorithm TEST_ALGORITHM = Algorithm.HMAC256("test-secret");

    @Mock
    private KycAuthRestApi kycAuthRestApi;

    @Mock
    private KycRestProperties kycConnectionProperties;

    private KycTokenManager underTest;

    @BeforeEach
    void setUp() {
        when(kycConnectionProperties.getTokenExpirationMargin()).thenReturn(TOKEN_EXPIRATION_MARGIN);
        underTest = new KycTokenManager(kycAuthRestApi, kycConnectionProperties);
    }

    // -----------------------------------------------------------------------
    // grantToken
    // -----------------------------------------------------------------------
    @Nested
    class GrantToken {

        @Test
        void givenValidCredentials_whenGrantToken_thenLoginIsCalledWithCorrectRequest() throws CommonException {
            // given
            stubAllProperties();
            var response = buildValidResponse();
            when(kycAuthRestApi.login(any(LoginRequest.class))).thenReturn(response);

            // when
            underTest.retrieveToken();

            // then
            var captor = ArgumentCaptor.forClass(LoginRequest.class);
            verify(kycAuthRestApi).login(captor.capture());
            assertThat(captor.getValue().getUsername()).isEqualTo(TEST_USERNAME);
            assertThat(captor.getValue().getPassword()).isEqualTo(TEST_PASSWORD);
        }

        @Test
        void givenValidLoginResponse_whenGrantToken_thenTokenIsReturned() throws CommonException {
            // given
            stubAllProperties();
            var tokenJwt = createTestJwt(Instant.now().plus(1, ChronoUnit.HOURS));
            var expectedBearer = BEARER_PREFIX + tokenJwt;
            var response = buildResponseEntity(expectedBearer, buildValidRefreshCookie());
            when(kycAuthRestApi.login(any(LoginRequest.class))).thenReturn(response);

            // when
            var token = underTest.retrieveToken();

            // then
            assertThat(token).isEqualTo(expectedBearer);
        }
    }

    // -----------------------------------------------------------------------
    // refreshToken
    // -----------------------------------------------------------------------
    @Nested
    class RefreshToken {

        @Test
        void givenExpiredToken_andValidRefreshToken_whenRetrieveToken_thenRefreshApiIsCalled() throws CommonException {
            // given — first grant a token that's already expired
            stubAllProperties();
            var expiredJwt = createTestJwt(Instant.now().minus(1, ChronoUnit.SECONDS));
            var refreshJwt = createTestJwt(Instant.now().plus(24, ChronoUnit.HOURS));
            var loginResponse = buildResponseEntity(
                    BEARER_PREFIX + expiredJwt,
                    REFRESH_PREFIX + refreshJwt + ";Path=/;HttpOnly");
            when(kycAuthRestApi.login(any(LoginRequest.class))).thenReturn(loginResponse);
            underTest.retrieveToken(); // initial login, token cached but already expired

            // set up refresh response
            var freshJwt = createTestJwt(Instant.now().plus(1, ChronoUnit.HOURS));
            var freshRefreshJwt = createTestJwt(Instant.now().plus(24, ChronoUnit.HOURS));
            var refreshResponse = buildResponseEntity(
                    BEARER_PREFIX + freshJwt,
                    REFRESH_PREFIX + freshRefreshJwt + ";Path=/;HttpOnly");
            when(kycAuthRestApi.refreshToken(anyString(), anyString())).thenReturn(refreshResponse);

            // when
            var token = underTest.retrieveToken();

            // then
            assertThat(token).startsWith(BEARER_PREFIX);
            verify(kycAuthRestApi).refreshToken(anyString(), anyString());
        }
    }

    // -----------------------------------------------------------------------
    // Header validation
    // -----------------------------------------------------------------------
    @Nested
    class HeaderValidation {

        @Test
        void givenNullTokenHeader_whenGrantToken_thenKycMissingHeaderError() {
            // given
            stubAllProperties();
            var response = buildResponseEntity(null, buildValidRefreshCookie());
            when(kycAuthRestApi.login(any(LoginRequest.class))).thenReturn(response);

            // when / then
            assertThatThrownBy(() -> underTest.retrieveToken())
                    .isInstanceOf(CommonException.class)
                    .satisfies(thrown -> assertThat(((CommonException) thrown).getErrorCode())
                            .isEqualTo(CustpmExceptionCode.KYC_MISSING_HEADER_ERROR.getErrorCode()));
        }

        @Test
        void givenNullRefreshTokenHeader_whenGrantToken_thenKycMissingHeaderError() {
            // given
            stubAllProperties();
            var tokenJwt = createTestJwt(Instant.now().plus(1, ChronoUnit.HOURS));
            var response = buildResponseEntity(BEARER_PREFIX + tokenJwt, null);
            when(kycAuthRestApi.login(any(LoginRequest.class))).thenReturn(response);

            // when / then
            assertThatThrownBy(() -> underTest.retrieveToken())
                    .isInstanceOf(CommonException.class)
                    .satisfies(thrown -> assertThat(((CommonException) thrown).getErrorCode())
                            .isEqualTo(CustpmExceptionCode.KYC_MISSING_HEADER_ERROR.getErrorCode()));
        }

        @Test
        void givenTokenWithoutBearerPrefix_whenGrantToken_thenKycInvalidHeaderError() {
            // given
            stubAllProperties();
            var response = buildResponseEntity("InvalidNoPrefix", buildValidRefreshCookie());
            when(kycAuthRestApi.login(any(LoginRequest.class))).thenReturn(response);

            // when / then
            assertThatThrownBy(() -> underTest.retrieveToken())
                    .isInstanceOf(CommonException.class)
                    .satisfies(thrown -> assertThat(((CommonException) thrown).getErrorCode())
                            .isEqualTo(CustpmExceptionCode.KYC_INVALID_HEADER_ERROR.getErrorCode()));
        }

        @Test
        void givenBearerPrefixOnly_whenGrantToken_thenKycInvalidHeaderError() {
            // given — "Bearer " with nothing after it fails length check
            stubAllProperties();
            var response = buildResponseEntity(BEARER_PREFIX, buildValidRefreshCookie());
            when(kycAuthRestApi.login(any(LoginRequest.class))).thenReturn(response);

            // when / then
            assertThatThrownBy(() -> underTest.retrieveToken())
                    .isInstanceOf(CommonException.class)
                    .satisfies(thrown -> assertThat(((CommonException) thrown).getErrorCode())
                            .isEqualTo(CustpmExceptionCode.KYC_INVALID_HEADER_ERROR.getErrorCode()));
        }

        @Test
        void givenRefreshTokenWithoutCorrectPrefix_whenGrantToken_thenKycInvalidHeaderError() {
            // given
            stubAllProperties();
            var tokenJwt = createTestJwt(Instant.now().plus(1, ChronoUnit.HOURS));
            var response = buildResponseEntity(BEARER_PREFIX + tokenJwt, "wrong_prefix=token;Path=/");
            when(kycAuthRestApi.login(any(LoginRequest.class))).thenReturn(response);

            // when / then
            assertThatThrownBy(() -> underTest.retrieveToken())
                    .isInstanceOf(CommonException.class)
                    .satisfies(thrown -> assertThat(((CommonException) thrown).getErrorCode())
                            .isEqualTo(CustpmExceptionCode.KYC_INVALID_HEADER_ERROR.getErrorCode()));
        }

        @Test
        void givenRefreshTokenWithoutSemicolon_whenGrantToken_thenKycInvalidHeaderError() {
            // given — refresh header must contain ";" per isValidRefreshTokenHeader
            stubAllProperties();
            var tokenJwt = createTestJwt(Instant.now().plus(1, ChronoUnit.HOURS));
            var response = buildResponseEntity(BEARER_PREFIX + tokenJwt, REFRESH_PREFIX + "some-token-no-semicolon");
            when(kycAuthRestApi.login(any(LoginRequest.class))).thenReturn(response);

            // when / then
            assertThatThrownBy(() -> underTest.retrieveToken())
                    .isInstanceOf(CommonException.class)
                    .satisfies(thrown -> assertThat(((CommonException) thrown).getErrorCode())
                            .isEqualTo(CustpmExceptionCode.KYC_INVALID_HEADER_ERROR.getErrorCode()));
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void stubAllProperties() {
        when(kycConnectionProperties.getTokenResponseHeader()).thenReturn(TOKEN_RESPONSE_HEADER);
        when(kycConnectionProperties.getRefreshTokenResponseHeader()).thenReturn(REFRESH_TOKEN_RESPONSE_HEADER);
        when(kycConnectionProperties.getUsername()).thenReturn(TEST_USERNAME);
        when(kycConnectionProperties.getPassword()).thenReturn(TEST_PASSWORD);
    }

    private String createTestJwt(Instant expiresAt) {
        return JWT.create()
                .withExpiresAt(Date.from(expiresAt))
                .withSubject("test-subject")
                .sign(TEST_ALGORITHM);
    }

    private String buildValidRefreshCookie() {
        var refreshJwt = createTestJwt(Instant.now().plus(24, ChronoUnit.HOURS));
        return REFRESH_PREFIX + refreshJwt + ";Path=/;HttpOnly";
    }

    private ResponseEntity<?> buildValidResponse() {
        var tokenJwt = createTestJwt(Instant.now().plus(1, ChronoUnit.HOURS));
        return buildResponseEntity(BEARER_PREFIX + tokenJwt, buildValidRefreshCookie());
    }

    private ResponseEntity<?> buildResponseEntity(String tokenValue, String refreshValue) {
        var headers = new HttpHeaders();
        if (tokenValue != null) {
            headers.add(TOKEN_RESPONSE_HEADER, tokenValue);
        }
        if (refreshValue != null) {
            headers.add(REFRESH_TOKEN_RESPONSE_HEADER, refreshValue);
        }
        return ResponseEntity.ok().headers(headers).build();
    }
}

```

```
package com.consorsbank.custpm.kyc.rest.adapter.token;

import com.consorsbank.common.error.handling.exception.CommonException;
import com.consorsbank.common.error.handling.exception.authorization.CommonExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TokenManagerTest {

    private static final Integer TOKEN_EXPIRATION_MARGIN = 5000; // ms
    private static final String TEST_TOKEN = "Bearer test-token";
    private static final String TEST_REFRESH_TOKEN = "jwt_refresh=test-refresh";

    private TestableTokenManager underTest;

    @BeforeEach
    void setUp() {
        underTest = new TestableTokenManager(TOKEN_EXPIRATION_MARGIN);
    }

    // --- retrieveToken ---

    @Test
    void givenNoTokenCached_whenRetrieveToken_thenGrantTokenIsCalled() throws CommonException {
        // given — fresh manager, no token stored

        // when
        var token = underTest.retrieveToken();

        // then
        assertThat(token).isEqualTo(TestableTokenManager.GRANTED_TOKEN);
        assertThat(underTest.getGrantTokenCallCount()).isEqualTo(1);
        assertThat(underTest.getRefreshTokenCallCount()).isZero();
    }

    @Test
    void givenValidCachedToken_whenRetrieveToken_thenNoNewCallIsMade() throws CommonException {
        // given
        underTest.retrieveToken(); // first call populates cache
        underTest.resetCallCounts();

        // when
        var token = underTest.retrieveToken();

        // then
        assertThat(token).isEqualTo(TestableTokenManager.GRANTED_TOKEN);
        assertThat(underTest.getGrantTokenCallCount()).isZero();
        assertThat(underTest.getRefreshTokenCallCount()).isZero();
    }

    @Test
    void givenExpiredToken_andValidRefreshToken_whenRetrieveToken_thenRefreshTokenIsCalled() throws CommonException {
        // given — store an expired token with a valid refresh token
        var expiredTime = Instant.now().minus(1, ChronoUnit.HOURS);
        var validRefreshExpiry = Instant.now().plus(24, ChronoUnit.HOURS);
        underTest.storeTokenValues(TEST_TOKEN, TEST_REFRESH_TOKEN, expiredTime, validRefreshExpiry);
        underTest.resetCallCounts();

        // when
        var token = underTest.retrieveToken();

        // then
        assertThat(token).isEqualTo(TestableTokenManager.REFRESHED_TOKEN);
        assertThat(underTest.getRefreshTokenCallCount()).isEqualTo(1);
        assertThat(underTest.getGrantTokenCallCount()).isZero();
    }

    @Test
    void givenExpiredToken_andExpiredRefreshToken_whenRetrieveToken_thenGrantTokenIsCalled() throws CommonException {
        // given — both tokens expired
        var expiredTime = Instant.now().minus(1, ChronoUnit.HOURS);
        underTest.storeTokenValues(TEST_TOKEN, TEST_REFRESH_TOKEN, expiredTime, expiredTime);
        underTest.resetCallCounts();

        // when
        var token = underTest.retrieveToken();

        // then
        assertThat(token).isEqualTo(TestableTokenManager.GRANTED_TOKEN);
        assertThat(underTest.getGrantTokenCallCount()).isEqualTo(1);
        assertThat(underTest.getRefreshTokenCallCount()).isZero();
    }

    @Test
    void givenExpiredToken_andNullRefreshToken_whenRetrieveToken_thenGrantTokenIsCalled() throws CommonException {
        // given — token expired, no refresh token
        var expiredTime = Instant.now().minus(1, ChronoUnit.HOURS);
        underTest.storeTokenValues(TEST_TOKEN, null, expiredTime, null);
        underTest.resetCallCounts();

        // when
        var token = underTest.retrieveToken();

        // then
        assertThat(token).isEqualTo(TestableTokenManager.GRANTED_TOKEN);
        assertThat(underTest.getGrantTokenCallCount()).isEqualTo(1);
        assertThat(underTest.getRefreshTokenCallCount()).isZero();
    }

    // --- generateNewToken ---

    @Test
    void givenSameLastUsedToken_whenGenerateNewToken_thenNewTokenIsGranted() throws CommonException {
        // given
        underTest.retrieveToken(); // cache GRANTED_TOKEN
        underTest.resetCallCounts();
        underTest.setNextGrantedToken("Bearer new-granted-token");

        // when
        underTest.generateNewToken(TestableTokenManager.GRANTED_TOKEN);

        // then — should refresh or grant since lastUsedToken matches current
        assertThat(underTest.getToken()).isEqualTo("Bearer new-granted-token");
    }

    @Test
    void givenDifferentLastUsedToken_whenGenerateNewToken_thenNoCallIsMade() throws CommonException {
        // given — another thread already refreshed the token
        underTest.retrieveToken(); // cache GRANTED_TOKEN
        underTest.resetCallCounts();

        // when
        underTest.generateNewToken("Bearer stale-token-from-another-thread");

        // then — token didn't match, no refresh needed
        assertThat(underTest.getGrantTokenCallCount()).isZero();
        assertThat(underTest.getRefreshTokenCallCount()).isZero();
    }

    // --- refreshOrGrantToken: 4xx fallback ---

    @Test
    void givenRefreshTokenThrows4xx_whenRetrieveToken_thenFallsBackToGrantToken() throws CommonException {
        // given — store expired token with valid refresh
        var expiredTime = Instant.now().minus(1, ChronoUnit.HOURS);
        var validRefreshExpiry = Instant.now().plus(24, ChronoUnit.HOURS);
        underTest.storeTokenValues(TEST_TOKEN, TEST_REFRESH_TOKEN, expiredTime, validRefreshExpiry);
        underTest.resetCallCounts();
        underTest.setRefreshThrows4xx(true);
        underTest.setNextGrantedToken("Bearer fallback-granted-token");

        // when
        var token = underTest.retrieveToken();

        // then — refresh failed with 4xx, fell back to grant
        assertThat(token).isEqualTo("Bearer fallback-granted-token");
        assertThat(underTest.getRefreshTokenCallCount()).isEqualTo(1);
        assertThat(underTest.getGrantTokenCallCount()).isEqualTo(1);
    }

    @Test
    void givenRefreshTokenThrows5xx_whenRetrieveToken_thenExceptionIsPropagated() {
        // given — store expired token with valid refresh
        var expiredTime = Instant.now().minus(1, ChronoUnit.HOURS);
        var validRefreshExpiry = Instant.now().plus(24, ChronoUnit.HOURS);
        underTest.storeTokenValues(TEST_TOKEN, TEST_REFRESH_TOKEN, expiredTime, validRefreshExpiry);
        underTest.resetCallCounts();
        underTest.setRefreshThrows5xx(true);

        // when / then
        assertThatThrownBy(() -> underTest.retrieveToken())
                .isInstanceOf(CommonException.class);
        assertThat(underTest.getRefreshTokenCallCount()).isEqualTo(1);
        assertThat(underTest.getGrantTokenCallCount()).isZero();
    }

    // --- isBadRequestStatusCode (indirectly tested via fallback above, but also directly) ---

    @Test
    void givenTokenAboutToExpireWithinMargin_whenRetrieveToken_thenTokenIsRefreshed() throws CommonException {
        // given — token expires within the margin window
        var almostExpired = Instant.now().plusMillis(TOKEN_EXPIRATION_MARGIN - 1000);
        var validRefreshExpiry = Instant.now().plus(24, ChronoUnit.HOURS);
        underTest.storeTokenValues(TEST_TOKEN, TEST_REFRESH_TOKEN, almostExpired, validRefreshExpiry);
        underTest.resetCallCounts();

        // when
        var token = underTest.retrieveToken();

        // then — margin makes it appear expired, triggers refresh
        assertThat(token).isEqualTo(TestableTokenManager.REFRESHED_TOKEN);
        assertThat(underTest.getRefreshTokenCallCount()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // Testable concrete subclass of the abstract TokenManager
    // -----------------------------------------------------------------------
    private static class TestableTokenManager extends TokenManager {

        static final String GRANTED_TOKEN = "Bearer granted-jwt-token";
        static final String REFRESHED_TOKEN = "Bearer refreshed-jwt-token";

        private int grantTokenCallCount = 0;
        private int refreshTokenCallCount = 0;
        private boolean refreshThrows4xx = false;
        private boolean refreshThrows5xx = false;
        private String nextGrantedToken = GRANTED_TOKEN;

        TestableTokenManager(Integer tokenExpirationMargin) {
            super(tokenExpirationMargin);
        }

        @Override
        protected void grantToken() throws CommonException {
            grantTokenCallCount++;
            var validExpiry = Instant.now().plus(1, ChronoUnit.HOURS);
            var refreshExpiry = Instant.now().plus(24, ChronoUnit.HOURS);
            storeTokenValues(nextGrantedToken, "jwt_refresh=granted-refresh", validExpiry, refreshExpiry);
        }

        @Override
        protected void refreshToken() throws CommonException {
            refreshTokenCallCount++;
            if (refreshThrows4xx) {
                // Simulate 4xx: cause is CommonException with httpStatus in 400-499 range
                var cause = new CommonException(CommonExceptionCode.SERVER_ERROR, List.of("Bad Request")) {
                    @Override
                    public int getHttpStatus() {
                        return 400;
                    }
                };
                var wrapper = new CommonException(CommonExceptionCode.SERVER_ERROR, List.of("Refresh failed"));
                wrapper.initCause(cause);
                throw wrapper;
            }
            if (refreshThrows5xx) {
                throw new CommonException(CommonExceptionCode.SERVER_ERROR, List.of("Internal Server Error"));
            }
            var validExpiry = Instant.now().plus(1, ChronoUnit.HOURS);
            var refreshExpiry = Instant.now().plus(24, ChronoUnit.HOURS);
            storeTokenValues(REFRESHED_TOKEN, "jwt_refresh=refreshed-refresh", validExpiry, refreshExpiry);
        }

        int getGrantTokenCallCount() { return grantTokenCallCount; }
        int getRefreshTokenCallCount() { return refreshTokenCallCount; }

        void resetCallCounts() {
            grantTokenCallCount = 0;
            refreshTokenCallCount = 0;
        }

        void setRefreshThrows4xx(boolean value) { this.refreshThrows4xx = value; }
        void setRefreshThrows5xx(boolean value) { this.refreshThrows5xx = value; }
        void setNextGrantedToken(String token) { this.nextGrantedToken = token; }
    }
}

```
