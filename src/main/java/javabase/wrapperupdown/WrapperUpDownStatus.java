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
public class WrapperUpDownStatus {

    private String wrapperId;

    private boolean success;
}
