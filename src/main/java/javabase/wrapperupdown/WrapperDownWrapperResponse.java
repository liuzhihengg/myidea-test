package javabase.wrapperupdown;

import lombok.Data;

import java.util.List;

/**
 * @author liuzhiheng
 * @date 2025/6/12
 */
@Data
public class WrapperDownWrapperResponse {

    private String domain;

    private int code;

    private String msg;

    private List<WrapperUpDownStatus> statusList;
}
