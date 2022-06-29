package top.mffseal.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author mffseal
 */
@AllArgsConstructor
@Getter
public enum PackageType {
    REQUEST_TYPE(0),
    RESPONSE_TYPE(1);

    private final int code;

}
