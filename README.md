```
@Service
@ConditionalOnProperty(
    name = "spring.vault.enabled",
    havingValue = "false",
    matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class LocalTransitServiceImpl implements VaultTransitService {

    @Value("${local.encryption.key:localDevKeyMustBe32CharactersLong!}")
    private String encryptionKey;

    @Override
    public Map<String, String> encrypt(byte[] payload) {
        try {
            var cipher = initCipher(Cipher.ENCRYPT_MODE);
            var cipherText = Base64.getEncoder()
                .encodeToString(cipher.doFinal(payload));
            return Map.of(
                PayloadVaultMapper.CIPHER_TEXT, cipherText,
                PayloadVaultMapper.HMAC, "local-hmac"
            );
        } catch (Exception e) {
            throw new RuntimeException("Local encryption failed", e);
        }
    }

    @Override
    public Map<String, String> decrypt(byte[] payload, int encryptedKeyVersion) {
        try {
            var cipher = initCipher(Cipher.DECRYPT_MODE);
            var decrypted = new String(cipher.doFinal(
                Base64.getDecoder().decode(payload)));
            return Map.of(
                PayloadVaultMapper.DECRYPT_CIPHER_TEXT, decrypted,
                PayloadVaultMapper.HMAC, "local-hmac"
            );
        } catch (Exception e) {
            throw new RuntimeException("Local decryption failed", e);
        }
    }

    private Cipher initCipher(int mode) throws Exception {
        var key = new SecretKeySpec(
            encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
        var cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(mode, key);
        return cipher;
    }
}
```


```
@Service
@ConditionalOnProperty(name = "spring.vault.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class VaultTransitServiceImpl implements VaultTransitService {
    // existing code unchanged
}
```


```
spring:
  vault:
    enabled: false

local:
  encryption:
    key: localDevKeyMustBe32CharactersLong  # exactly 32 chars for AES-256
```

```
@Service
@ConditionalOnProperty(name = "spring.vault.enabled", havingValue = "false", matchIfMissing = true)
public class LocalSecretReaderServiceImpl implements KvSecretReaderService {
    @Override
    public String readSecretValue(String key) {
        // return dummy value locally or read from application.yaml
        return "local-secret-" + key;
    }
}
```
