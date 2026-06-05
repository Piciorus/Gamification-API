```
@Configuration
@ConfigurationProperties(prefix = "tam.qrcode")
public class QrCodeConfig {

    private Map<String, Integer> sizes = new HashMap<>();
    private Map<String, Integer> validities = new HashMap<>();

    public Integer getQrCodeSize(String feid) {
        return sizes.getOrDefault(feid, sizes.get("default"));
    }

    public Integer getValidity(String service) {
        return validities.get(service);
    }

    // Getters and setters for Spring binding
    public Map<String, Integer> getSizes() { return sizes; }
    public void setSizes(Map<String, Integer> sizes) { this.sizes = sizes; }

    public Map<String, Integer> getValidities() { return validities; }
    public void setValidities(Map<String, Integer> validities) { this.validities = validities; }
}
```
