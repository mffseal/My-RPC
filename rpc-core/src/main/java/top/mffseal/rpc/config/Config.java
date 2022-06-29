package top.mffseal.rpc.config;

import top.mffseal.rpc.serializer.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置类，用于配置序列化实现和基于配置文件的服务注册。
 *
 * @author mffseal
 */
public class Config {
    static Properties properties;

    static {
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
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
            return Serializer.Library.Jackson;
        } else {
            return Serializer.Library.valueOf(value);
        }
    }

    /**
     * 配置服务器监听端口。
     *
     * @return 服务器监听端口号。
     */
    public static int getServerPort() {
        String value = properties.getProperty("server.port");
        if (value == null) {
            return 8080;
        } else {
            return Integer.parseInt(value);
        }
    }


}
