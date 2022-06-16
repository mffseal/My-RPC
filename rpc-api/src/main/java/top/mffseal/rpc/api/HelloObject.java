package top.mffseal.rpc.api;

import java.io.Serializable;

/**
 * 需要实现Serializable, 调用过程中要从客户端传递到服务端
 * @author mffseal
 */
public class HelloObject implements Serializable {
    private Integer id;
    private String message;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HelloObject(Integer id, String message) {
        this.id = id;
        this.message = message;
    }
}
