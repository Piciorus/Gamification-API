```

package de.consorsbank.core.trauthsc.pvm.service;

import de.consorsbank.core.trauthsc.pvm.mapper.PayloadVaultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Service
@ConditionalOnProperty(
        name = "spring.vault.enabled",
        havingValue = "false",
        matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class LocalTransitServiceImpl implements VaultTransitService {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${local.encryption.key:localDevKeyMustBe32CharactersLon}")
    private String encryptionKey;

    @Override
    public Map<String, String> encrypt(byte[] payload) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            var cipher = initCipher(Cipher.ENCRYPT_MODE, iv);
            byte[] encrypted = cipher.doFinal(payload);

            // Prepend IV to ciphertext
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encrypted, 0, encryptedWithIv, GCM_IV_LENGTH, encrypted.length);

            return Map.of(
                PayloadVaultMapper.CIPHER_TEXT, 
                Base64.getEncoder().encodeToString(encryptedWithIv),
                PayloadVaultMapper.HMAC, "local-hmac"
            );
        } catch (Exception e) {
            throw new RuntimeException("Local encryption failed", e);
        }
    }

    @Override
    public Map<String, String> decrypt(byte[] payload, int encryptedKeyVersion) {
        try {
            byte[] decoded = Base64.getDecoder().decode(payload);

            // Extract IV from first 12 bytes
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, GCM_IV_LENGTH);
            byte[] cipherText = new byte[decoded.length - GCM_IV_LENGTH];
            System.arraycopy(decoded, GCM_IV_LENGTH, cipherText, 0, cipherText.length);

            var cipher = initCipher(Cipher.DECRYPT_MODE, iv);
            return Map.of(
                PayloadVaultMapper.DECRYPT_CIPHER_TEXT,
                new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8),
                PayloadVaultMapper.HMAC, "local-hmac"
            );
        } catch (Exception e) {
            throw new RuntimeException("Local decryption failed", e);
        }
    }

    private Cipher initCipher(int mode, byte[] iv) throws Exception {
        var key = new SecretKeySpec(
            encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
        var cipher = Cipher.getInstance(AES_GCM);
        cipher.init(mode, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return cipher;
    }
}
```
