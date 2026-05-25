package javabase.portal;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class IpDatabaseItemsFetcher {

    private static final String URL = "http://portal.corp.qunar.com/applist/app/v2/conf/query";
    private static final String APP_CODE = "f_fuwu_ttm";
    private static final String ENV_NAME = "默认prod配置";
    public static final String COOKIE = "ctt_june=1683616182042##iK3wWSPAWwPwawPwasDmaKfRWsHDEKgmaKHTEDEhXPPma23sWRiRVRD8WKDmiK3siK3saKg%3DaK3saKtnWsPNahPwaUvt; fid=e6acb35e-9417-492f-9dae-1800fe0a7b2d; QN601=a300a1062c8c3633679be7dbea6ad749; QN238=zh_cn; QN271AC=register_pc; user_id=zhiheng.liu; UBT_VID=1743128425102.f226BhBizrEr; _RSG=vxK0pbWK7PCiqEHWzLkBeA; _RDG=28a907e7dc87c424dc3a7759e879a3230b; _RGUID=c6475209-7301-4ef9-aa0b-b95f6044ed2c; _RF1=103.213.88.166; QN99=1459; env_tag=beta; activityLoginUid=zhiheng.liu; _xconfig_userId=zhiheng.liu; stability_user=zhiheng.liu; QN48=45c4edb2-e7c4-46d3-9848-4ed4c9082679; QunarGlobal=\"ftts_a5aa843:-5a0de450:19b547656d0:-7e52\"; _q=U.greattest002; QN43=3; QN42=%E8%B6%85%E7%BA%A7%E7%AE%A1%E7%90%86%E5%91%98; l-pswebapp1-8000-PORTAL-PSJSESSIONID=RBZM-5FGAvYRpEEaYhIJfiKj-hOpg7Qt!-1840114490; https%3a%2f%2fehr.corp.qunar.com%2fpsp%2fhcmprd%2femployee%2fhrms%2frefresh=list:%20%3Ftab%3Dhc_ux_manager_dashboard%7C%3Frp%3Dhc_ux_manager_dashboard%7C%3Ftab%3Dremoteunifieddashboard%7C%3Frp%3Dremoteunifieddashboard; _bfa=1.1743128425102.f226BhBizrEr.1.1772628400827.1772628857061.1.4.10650153142; PORTALAPPLISTID=\"2|1:0|10:1778053172|15:PORTALAPPLISTID|16:emhpaGVuZy5saXU=|f64a549d52b147a8443ce7d92862cca167e7145cc82c79819a9d4ab20e6bf487\"; ctf_june=1683616182042##iK3was3mahPwawPwasiRWSv8VRgnVPGIWK0RaPj8a23mVDGIX%3DD%3DESWRaKDNiK3siK3saKg%2BVRPmaSDwaK3wVuPwaUvt; cs_june=7805488a723609b1e80a560a63d306e5d8c9dfe7f6dd40243bca167e9cbf35e8b22e76cf21d4e2fcab85350ee00623732adda6f6ad2fbed6fb2e9c3ee7f2ff29b17c80df7eee7c02a9c1a6a5b97c117935a2bceb345acfeb9f45f221f4b9aa455a737ae180251ef5be23400b098dd8ca; QN219=\"3c55f694-764f-4651-a668-a7e30330072f-j(Af^gsa\"; QN271SL=3b30264377d76909b8420e42317c526a; QN271RC=3b30264377d76909b8420e42317c526a; csrfToken=a2hMyYLJZuSxqauObltzGkbmNjkVTwiQ; _s=s_Y2KWJISE6KBWC5YTSJ322VQK5U; _t=29752477; _v=pFZZBh1amRtwm6-Zh1w2VaIhcRKPzkvj8V5W6eOf7puRK0yLkbqO3gVW8MCDtWn4l1NCM96Uv9nnD1-UYr_KbQ-8nKcMIn9eyJL00rgpnQUFFjDDpUFWRfPojS5pLW7IKgSTaRsfvgz3igOKG0OjkO6FJ5oYevtTPGloZ1DqcHcD; _mdp=E4B9E247411218DAA355DECCE091ACAE; _uf=zhiheng.liu; _i=ueHd8SnyY2X9U8pAdf3OVWdnMvkX; QN300=#497560#416917#; new_dubai_user=\"2|1:0|10:1779433228|14:new_dubai_user|212:eyJqb2JfY29kZSI6ICJSRCIsICJxX2RvbWFpbl9uYW1lIjogInpoaWhlbmcubGl1IiwgInFfYWNjb3VudF9uYW1lIjogIuWImOW/l+aBkiIsICJtYW5hZ2VyX3VzZXJuYW1lIjogInpoYW5ncGVpenAuemhhbmciLCAibWFuYWdlciI6ICLlvKDln7kiLCAiaXNfZGJhIjogZmFsc2V9|270c418dcf1105d5314c1fef24a9e660221db6981584e0a0ab0f0e9d643d988c\"; console_record=2|1:0|10:1779447042|14:console_record|364:W3siaXBfcG9ydCI6ICIxOTIuMTY4LjEyLjgxXzMzMDciLCAicGFzc3dvcmQiOiAibDI3OHN3dTZEVmJ4eEI0amV1In0sIHsiaXBfcG9ydCI6ICIxMC42Ny4xMDEuMTQ1XzMzMDYiLCAicGFzc3dvcmQiOiAiTWQyMVR2UDBnSHprb1UjRiJ9LCB7ImlwX3BvcnQiOiAiMTkyLjE2OC4yMjQuODRfMzMwNiIsICJwYXNzd29yZCI6ICJ7dVpVdGw1NnEzIU9RMDh5In0sIHsiaXBfcG9ydCI6ICIxMC44OC4xMTQuMTg3XzMzMTYiLCAicGFzc3dvcmQiOiAiQ2Y4akZ4ZERlSlExZWE1N21hIn1d|aed8ea13b24257f600587f7df8d6238d8d3dcfc99acd88ed7e64334285e88a96; QN1=00010e80247c7e523df0ddd9; session=eyJ1c2VyX2lkIjoiemhpaGVuZy5saXUifQ.ahPOxQ.YuT-R0-aP3ZyRhwrKR5gzzarqyc; case_login_info=FD-411670,zhiheng.liu; _vi=wdXJzBxNCnBMcccfynqBdrADt_WJywi4HpACeq-Wp92ymfs0ZE-Ajjcadf8K327-iiX6au-bb2ufLohD7ITqOQACwULnCpxPxCix9SQCRwOUqhGF4u2JzwvWOR5huHH0cNrEbQ-OdsFtdKvqh2jvYscKpPOCW9GRniq-Eov5_j2Z; QN25=0eac5ab8-0764-4720-90d8-3ddb4ffeb2e6-9f992f90; QN271=170097ab-a0b6-4278-b901-1efa3c3880da; _todolist.u=47BE7895135A6BC36166941457B464BB83D546F65F62CAE7; _todolist.uid=zhiheng.liu; _todolist.uname=%E5%88%98%E5%BF%97%E6%81%92; _todolist.dept=%E6%8A%80%E6%9C%AF%E4%B8%AD%E5%BF%83%3E%E5%9B%BD%E5%86%85%E6%9C%BA%E7%A5%A8%E7%A0%94%E5%8F%91%3E%E4%B8%BB%E7%AB%99%E6%8A%A5%E4%BB%B7%E4%B8%8E%E8%87%AA%E8%90%A5%3E%E5%9B%BD%E5%86%85%E6%8A%A5%E4%BB%B7; qoa-ret=https%3A//oa.corp.qunar.com/webapp/index.html%23/cooperate/detail/8acd94fa9d7236eb019e5e7a884b6cd9";

    private static final Map<String, String> HEADERS = Map.of(
            "Content-Type", "application/json",
            "Cookie", COOKIE
    );

    private IpDatabaseItemsFetcher() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println(fetch());
    }

    public static List<JsonNode> fetch() throws Exception {
        return fetch(APP_CODE, ENV_NAME);
    }

    public static List<JsonNode> fetch(String appCode, String envName) throws Exception {
        String url = URL
                + "?app_code=" + URLEncoder.encode(appCode, StandardCharsets.UTF_8)
                + "&env_name=" + URLEncoder.encode(envName, StandardCharsets.UTF_8);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET();
        HEADERS.forEach(requestBuilder::header);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = client.send(requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Query failed, status=" + response.statusCode() + ", body=" + response.body());
        }

        JsonNode root = JsonUtil.readTree(response.body());
        if (!"success".equals(root.path("message").asText())) {
            throw new IOException("Query failed, body=" + response.body());
        }

        JsonNode ipList = root.path("data").path("rely").path("params").path("whitelist").path("ip");
        if (!ipList.isArray()) {
            throw new IOException("data.rely.params.whitelist.ip is not a list, body=" + response.body());
        }

        return JsonUtil.toList(ipList);
    }
}
