package rabbit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class ConfigConstants {

    private static Properties properties;
    static {
        properties = new Properties();
        try {
            File config = new File("config/mongodb-config.properties");
            properties.load(new FileInputStream(config));
            // properties.load(ConfigConstants.class.getResourceAsStream("/config/mongodb-config.properties"));
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
