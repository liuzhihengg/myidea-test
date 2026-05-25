package javabase.wrapperupdown;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liuzhiheng
 * @date 2025/6/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WrapperInfo {

    private String wrapperId;

    /**
     * 业务类型：BizTypeEnum
     */
    private int bizType;

    /**
     * wrapper是否在线
     */
    private boolean status;

    /**
     * 操作时间
     */
    private String updateTime;

    /**
     * 原因
     */
    private String reason;

    /**
     * 操作人
     */
    private String operator;
}
