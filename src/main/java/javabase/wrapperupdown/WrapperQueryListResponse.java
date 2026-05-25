package javabase.wrapperupdown;

import lombok.Data;

import java.util.List;

/**
 * @author liuzhiheng
 * @date 2025/6/12
 */
@Data
public class WrapperQueryListResponse {

    private String domain;

    private int code;

    private String msg;

    private List<WrapperInfo> statusList;
}
