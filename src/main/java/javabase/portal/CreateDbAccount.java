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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static javabase.portal.IpDatabaseItemsFetcher.COOKIE;

public class CreateDbAccount {

    private static final String URL =
            "https://portal.corp.qunar.com/applist/app/v2/conf/create_db_account";
    private static final String DEFAULT_APP_CODE = "f_fuwu_ttm";
    private static final String DEFAULT_ENV_NAME = "默认prod配置";
    private static final String OPERATOR = "zhiheng.liu";
    private static final Random RANDOM = new Random();

    private static final Map<String, String> HEADERS = Map.of(
            "Content-Type", "application/json",
            "Cookie", COOKIE
    );

    public static void main(String[] args) throws Exception {
        String appCode = args.length > 0 ? args[0] : DEFAULT_APP_CODE;
        String envName = args.length > 1 ? args[1] : DEFAULT_ENV_NAME;

        List<JsonNode> items = IpDatabaseItemsFetcher.fetch(appCode, envName);
        Map<String, JsonNode> namespaceItems = new LinkedHashMap<>();
        for (JsonNode item : items) {
            String namespace = item.path("namespace").asText();
            if (!namespace.isEmpty()) {
                namespaceItems.putIfAbsent(namespace, item);
            }
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        int index = 0;
        for (Map.Entry<String, JsonNode> entry : namespaceItems.entrySet()) {
            String namespace = entry.getKey();
            JsonNode item = entry.getValue();
            if (!namespace.toLowerCase(Locale.ROOT).contains("tts")) {
                System.out.println("skip namespace=" + namespace + ", reason=namespace does not contain tts");
                continue;
            }

            String clusterType = item.path("cluster_type").asText();
            if (clusterType.isEmpty()) {
                System.out.println("skip namespace=" + namespace + ", reason=cluster_type is empty");
                continue;
            }

            ObjectNode body = JsonUtil.objectNode();
            body.put("app_code", appCode);
            body.put("namespace", namespace);
            body.putArray("db_names").add("*");
            body.put("role", "privilegedlite");
            body.put("operator", OPERATOR);
            body.put("cluster_type", clusterType);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.toJson(body), StandardCharsets.UTF_8));
            HEADERS.forEach(requestBuilder::header);

            HttpResponse<String> response = client.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            System.out.printf("[%d/%d] namespace=%s status=%d body=%s%n",
                    ++index, namespaceItems.size(), namespace, response.statusCode(), response.body());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Request failed, namespace=" + namespace);
            }
            Thread.sleep(3000 + RANDOM.nextInt(2001));
        }
    }
}
