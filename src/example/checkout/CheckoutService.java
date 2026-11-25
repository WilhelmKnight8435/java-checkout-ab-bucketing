package example.checkout;

import example.infrai.InfraiClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CheckoutService {
    public record OrderResult(String orderId, String userId, int bucket, String fulfillment, String receipt) {}
    private final InfraiClient client;
    private final String experimentKey;

    public CheckoutService(InfraiClient client, String experimentKey) { this.client = client; this.experimentKey = experimentKey; }

    public OrderResult placeOrder(String userId, String orderId, double amount) throws Exception {
        String resolved = userIdFromEnvelope(client.authUserGet(userId));
        String flag = flagFromEnvelope(client.flagsGetValue(experimentKey));
        int bucket = bucket(resolved, experimentKey);
        boolean expedited = bucket < 50 && flag.contains("true");
        String fulfillment = expedited ? "expedited" : "standard";
        String receipt = "receipt:" + orderId + ":" + String.format("%.2f", amount);
        return new OrderResult(orderId, userId, bucket, fulfillment, receipt);
    }

    private static String userIdFromEnvelope(String body) {
        String data = field(body, "data");
        if (data.startsWith("{")) {
            String value = firstField(data, "user_id", "id");
            if (value != null) return value;
        }
        return data;
    }

    private static String flagFromEnvelope(String body) {
        String data = field(body, "data");
        String value = firstField(data, "value", "default_value");
        return value != null ? value : data;
    }

    private static String field(String json, String name) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(name) + "\\\"\\s*:\\s*(\\\"(?:\\\\.|[^\\\"])*\\\"|\\{.*?\\}|[^,}\\s]+)", Pattern.DOTALL).matcher(json == null ? "" : json);
        if (!matcher.find()) return "";
        return unquote(matcher.group(1).trim());
    }

    private static String firstField(String json, String... names) {
        for (String name : names) {
            String value = field(json, name);
            if (!value.isEmpty()) return value;
        }
        return null;
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && value.charAt(0) == '\"' && value.charAt(value.length() - 1) == '\"') {
            return value.substring(1, value.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return value;
    }

    public static int bucket(String userId, String key) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest((userId + ":" + key).getBytes(StandardCharsets.UTF_8));
        return ((digest[0] & 0xff) << 8 | (digest[1] & 0xff)) % 100;
    }
}
