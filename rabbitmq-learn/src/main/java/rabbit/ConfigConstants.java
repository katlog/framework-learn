package rabbit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigConstants {

    private static Properties properties;
    static {
        properties = new Properties();
        try {
            Path rootPath = Paths.get(System.getProperty("user.dir"));
            File config;
            // 兼容junit
            if (rootPath.endsWith("rabbitmq-learn")) {
                config = rootPath.getParent().resolve("config/mongodb-config.properties").toFile();
            } else {
                config = new File("config/mongodb-config.properties");
            }
            properties.load(new FileInputStream(config));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static final String IP_ADDRESS = properties.getProperty("IP_ADDRESS");
    public static final int PORT = Integer.parseInt(properties.getProperty("PORT"));
    public static final String USER_NAME = properties.getProperty("USER_NAME");
    public static final String PASSWORD = properties.getProperty("PASSWORD");



}
