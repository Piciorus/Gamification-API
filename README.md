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

    @Value("${local.encryption.key:localDevKeyMustBe32CharactersLong}")
    private String encryptionKey;

    @Override
    public Map<String, String> encrypt(byte[] payload) {
        try {
            SecretKeySpec key = new SecretKeySpec(
                encryptionKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            String cipherText = Base64.getEncoder()
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
    public String decrypt(String cipherText, String keyVersion) {
        try {
            SecretKeySpec key = new SecretKeySpec(
                encryptionKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(
                Base64.getDecoder().decode(cipherText));
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Local decryption failed", e);
        }
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
