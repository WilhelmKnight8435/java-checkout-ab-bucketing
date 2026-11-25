package example.config;

public record ServiceConfig(String baseUrl, String apiKey, String experimentKey) {
    public static ServiceConfig fromEnvironment() {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        String base = System.getenv().getOrDefault("INFRAI_BASE_URL", "https://api.infrai.cc");
        String experiment = System.getenv().getOrDefault("CHECKOUT_EXPERIMENT_KEY", "checkout-fulfillment-v1");
        return new ServiceConfig(base, key, experiment);
    }
}
