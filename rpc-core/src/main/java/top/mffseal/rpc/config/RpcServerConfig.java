package top.mffseal.rpc.config;

import io.netty.handler.logging.LogLevel;
import top.mffseal.rpc.serializer.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * rpc服务端配置类。
 *
 * @author mffseal
 */
public class RpcServerConfig {
    static Properties properties;

    static {
        try (InputStream in = RpcServerConfig.class.getResourceAsStream("/rpcServer.properties")) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * 配置序列化架构。
     *
     * @return 具体的序列化实现（枚举类型）。
     */
    public static Serializer.Library getSerializerLibrary() {
        String value = properties.getProperty("serializer.library");
        if (value == null) {
            return Serializer.Library.values()[0];
        } else {
            return Serializer.Library.valueOf(value);
        }
    }

    public static boolean getKryoCompress() {
        String value = properties.getProperty("serializer.kryo.compress");
        if (value == null) {
            return false;
        } else {
            return Boolean.parseBoolean(value);
        }
    }

    /**
     * 配置服务器监听端口。
     *
     * @return 服务器监听端口号。
     */
    public static int getPort() {
        String value = properties.getProperty("port");
        if (value == null) {
            return 8080;
        } else {
            return Integer.parseInt(value);
        }
    }

    /**
     * 配置服务器IP地址。
     *
     * @return IP地址
     */
    public static String getHost() {
        String value = properties.getProperty("host");
        if (value == null) {
            return "localhost";
        } else {
            return value;
        }
    }

    /**
     * 配置netty日志级别。
     *
     * @return 日志级别
     */
    public static LogLevel getNettyLogLevel() {
        String value = properties.getProperty("netty.loglevel");
        if (value == null) {
            return LogLevel.ERROR;
        } else {
            return LogLevel.valueOf(value);
        }
    }

    /**
     * 配置服务管理平台地址
     *
     * @return 地址
     */
    public static String getNamingServerHost() {
        String value = properties.getProperty("namingServer.host");
        if (value == null) {
            return "localhost";
        } else {
            return value;
        }
    }

    /**
     * 配置服务管理平台端口。
     *
     * @return 端口
     */
    public static int getNamingServerPort() {
        String value = properties.getProperty("namingServer.port");
        if (value == null) {
            return 8848;
        } else {
            return Integer.parseInt(value);
        }
    }


}
