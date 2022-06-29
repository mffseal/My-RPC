package top.mffseal.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 客户端向服务端传递的参数对应的类。
 *
 * @author mffseal
 */
@Data
@AllArgsConstructor
public class HelloObject implements Serializable {
    private Integer id;
    private String message;

    public HelloObject() {
    }
}
