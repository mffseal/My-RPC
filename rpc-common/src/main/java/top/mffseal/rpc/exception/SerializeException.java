package top.mffseal.rpc.exception;

import javax.sql.rowset.serial.SerialException;

/**
 * @author mffseal
 */
public class SerializeException extends RuntimeException{
    public SerializeException(String msg) {
        super(msg);
    }
}
