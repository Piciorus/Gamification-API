```
package de.consorsbank.core.trauthsc.pvm.service;

import de.consorsbank.core.trauthsc.pvm.mapper.PayloadVaultMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalTransitServiceImplTest {

    private LocalTransitServiceImpl service;

    private static final String TEST_KEY = "localDevKeyMustBe32CharactersLon";
    private static final String TEST_PAYLOAD = "test-payload-data";

    @BeforeEach
    void setUp() {
        service = new LocalTransitServiceImpl();
        ReflectionTestUtils.setField(service, "encryptionKey", TEST_KEY);
    }

    @Test
    void encrypt_shouldReturnCipherTextAndHmac() {
        // when
        var result = service.encrypt(TEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));

        // then
        assertThat(result).containsKey(PayloadVaultMapper.CIPHER_TEXT);
        assertThat(result).containsKey(PayloadVaultMapper.HMAC);
        assertThat(result.get(PayloadVaultMapper.CIPHER_TEXT)).isNotBlank();
        assertThat(result.get(PayloadVaultMapper.CIPHER_TEXT))
            .isNotEqualTo(TEST_PAYLOAD);
    }

    @Test
    void encrypt_shouldProduceDifferentCipherTextEachTime() {
        // GCM uses random IV so same plaintext = different ciphertext
        var result1 = service.encrypt(TEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));
        var result2 = service.encrypt(TEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));

        assertThat(result1.get(PayloadVaultMapper.CIPHER_TEXT))
            .isNotEqualTo(result2.get(PayloadVaultMapper.CIPHER_TEXT));
    }

    @Test
    void decrypt_shouldReturnOriginalPayload() {
        // given
        var encrypted = service.encrypt(TEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));
        byte[] cipherBytes = encrypted.get(PayloadVaultMapper.CIPHER_TEXT)
            .getBytes(StandardCharsets.UTF_8);

        // when
        var result = service.decrypt(cipherBytes, 1);

        // then
        assertThat(result.get(PayloadVaultMapper.DECRYPT_CIPHER_TEXT))
            .isEqualTo(TEST_PAYLOAD);
    }

    @Test
    void decrypt_shouldReturnHmac() {
        // given
        var encrypted = service.encrypt(TEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));
        byte[] cipherBytes = encrypted.get(PayloadVaultMapper.CIPHER_TEXT)
            .getBytes(StandardCharsets.UTF_8);

        // when
        var result = service.decrypt(cipherBytes, 1);

        // then
        assertThat(result).containsKey(PayloadVaultMapper.HMAC);
        assertThat(result.get(PayloadVaultMapper.HMAC)).isNotBlank();
    }

    @Test
    void decrypt_shouldIgnoreKeyVersion() {
        // key version is irrelevant for local impl
        var encrypted = service.encrypt(TEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));
        byte[] cipherBytes = encrypted.get(PayloadVaultMapper.CIPHER_TEXT)
            .getBytes(StandardCharsets.UTF_8);

        var result1 = service.decrypt(cipherBytes, 1);
        var result2 = service.decrypt(cipherBytes, 99);

        assertThat(result1.get(PayloadVaultMapper.DECRYPT_CIPHER_TEXT))
            .isEqualTo(result2.get(PayloadVaultMapper.DECRYPT_CIPHER_TEXT));
    }

    @Test
    void decrypt_withInvalidPayload_shouldThrowRuntimeException() {
        assertThatThrownBy(() ->
            service.decrypt("invalid-base64!!!".getBytes(StandardCharsets.UTF_8), 1))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Local decryption failed");
    }

    @Test
    void encrypt_withEmptyPayload_shouldStillEncrypt() {
        var result = service.encrypt(new byte[0]);

        assertThat(result.get(PayloadVaultMapper.CIPHER_TEXT)).isNotBlank();
    }
}
```
