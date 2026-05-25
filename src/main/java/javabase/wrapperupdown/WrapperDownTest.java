package javabase.wrapperupdown;

import lombok.Getter;
import org.apache.dubbo.common.utils.JsonUtils;

import java.util.Arrays;

public class WrapperDownTest {

    @Getter
    private static volatile int te;

    public static void main(String[] args) {
        te = 1;
        System.out.println(te);
        testWrapperQueryListResponse();
    }

    public static void testWrapperDownAllRequest() {
        WrapperDownAllRequest request = new WrapperDownAllRequest();
        request.setDomain("xep.trade.qunar.com");

        System.out.println(JsonUtils.getJson().toJson(request));
    }

    public static void testWrapperDownAllResponse() {
        WrapperDownAllResponse request = new WrapperDownAllResponse();
        request.setDomain("xep.trade.qunar.com");
        request.setMsg("Success");
        request.setCode(20000);

        System.out.println(JsonUtils.getJson().toJson(request));
    }

    public static void testWrapperDownWrapperRequest() {
        WrapperDownWrapperRequest request = new WrapperDownWrapperRequest();
        request.setDomain("xep.trade.qunar.com");
        request.setWrapperList(Arrays.asList("ttsgnd03652", "ttsgnd00329"));

        System.out.println(JsonUtils.getJson().toJson(request));
    }

    public static void testWrapperDownWrapperResponse() {
        WrapperDownWrapperResponse request = new WrapperDownWrapperResponse();
        request.setDomain("xep.trade.qunar.com");
        request.setMsg("Success");
        request.setCode(20000);
        request.setStatusList(Arrays.asList(new WrapperUpDownStatus("ttsgnd03652", true), new WrapperUpDownStatus("ttsgnd00329", false)));

        System.out.println(JsonUtils.getJson().toJson(request));
    }

    public static void testWrapperQueryListResponse() {
        WrapperQueryListResponse request = new WrapperQueryListResponse();
        request.setDomain("xep.trade.qunar.com");
        request.setMsg("Success");
        request.setCode(20000);
        request.setStatusList(Arrays.asList(new WrapperInfo("ttsgnd03652", 200, true, "2025-06-17 13:17:29", "代理人工操作上线", "quanr"), new WrapperInfo("ttsgnd00329", 200, false, "2025-06-17 19:15:23", "代理人工操作上线", "quanr")));

        System.out.println(JsonUtils.getJson().toJson(request));
    }

}
