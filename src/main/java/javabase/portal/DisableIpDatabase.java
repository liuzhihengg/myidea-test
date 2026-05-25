package javabase.portal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static javabase.portal.IpDatabaseItemsFetcher.COOKIE;

public class DisableIpDatabase {

    private static final String URL =
            "https://portal.corp.qunar.com/applist/app/v2/conf/disable_ip_account";
    private static final String APP_CODE = "f_tts_policy_provider";

    private static final Map<String, String> HEADERS = Map.of(
            "Content-Type", "application/json",
            "Cookie", COOKIE
    );

    public static void main(String[] args) throws Exception {
        List<JsonNode> items = IpDatabaseItemsFetcher.fetch(APP_CODE, "默认prod配置");
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        for (int i = 0; i < items.size(); i++) {
            ObjectNode body = JsonUtil.objectNode();
            body.putArray("data").add(items.get(i));
            body.put("app_code", APP_CODE);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.toJson(body), StandardCharsets.UTF_8));
            HEADERS.forEach(requestBuilder::header);

            HttpResponse<String> response = client.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String namespace = items.get(i).path("namespace").asText("<unknown>");
            System.out.printf("[%d/%d] namespace=%s status=%d body=%s%n",
                    i + 1, items.size(), namespace, response.statusCode(), response.body());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Request failed at index " + i + ", namespace=" + namespace);
            }
        }
    }
}
