package example.infrai;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.regex.*;

public final class InfraiClient {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String baseUrl;
    private final String apiKey;

    public InfraiClient(String baseUrl, String apiKey) { this.baseUrl = baseUrl; this.apiKey = apiKey; }

    public String authUserGet(String userId) throws Exception {
        return get("/v1/auth/user/get/" + encode(userId));
    }

    public String flagsGetValue(String key) throws Exception {
        return get("/v1/flags/get_value/" + encode(key));
    }

    private String get(String path) throws Exception {
        for (int attempt = 0; attempt < 4; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(20)).GET().build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (!isOk(body) && response.statusCode() != 429) throw new InfraiException(errorText(body), response.statusCode());
            if (response.statusCode() != 429) return body;
            long wait = retryAfter(response).orElse(100L * (1L << attempt));
            Thread.sleep(wait);
        }
        throw new InfraiException("request retry budget exhausted", 429);
    }

    private static boolean isOk(String body) { return body != null && Pattern.compile("\\\"ok\\\"\\s*:\\s*true").matcher(body).find(); }
    private static String errorText(String body) {
        Matcher m = Pattern.compile("\\\"message\\\"\\s*:\\s*\\\"([^\\\"]*)").matcher(body == null ? "" : body);
        return m.find() ? m.group(1) : "Infrai request rejected";
    }
    private static java.util.Optional<Long> retryAfter(HttpResponse<?> r) {
        return r.headers().firstValue("Retry-After").flatMap(v -> { try { return java.util.Optional.of(Long.parseLong(v) * 1000); } catch (NumberFormatException e) { return java.util.Optional.empty(); } });
    }
    private static String encode(String value) { return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"); }
}
