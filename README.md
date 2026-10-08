```
@Component
public class KycAuthRequestInterceptor implements RequestInterceptor {

    private final KYCTokenManager tokenManager;

    public KycAuthRequestInterceptor(KYCTokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public void apply(RequestTemplate template) {
        try {
            template.header("Authorization", "Bearer " + tokenManager.getToken());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve KYC token", e);
        }
    }
}

```

```
    configuration = {KYCRestErrorDecoder.class, KycAuthRequestInterceptor.class}

```
