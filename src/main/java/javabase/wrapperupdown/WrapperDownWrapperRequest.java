package javabase.wrapperupdown;

import lombok.Data;

import java.util.List;

/**
 * @author liuzhiheng
 * @date 2025/6/12
 */
@Data
public class WrapperDownWrapperRequest {

    private String domain;

    private List<String> wrapperList;
}
