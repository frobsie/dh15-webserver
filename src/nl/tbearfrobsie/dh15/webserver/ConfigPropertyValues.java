package nl.tbearfrobsie.dh15.webserver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigPropertyValues {
    public static Properties props;
    private static String propFileName = "resources/config.properties";

    public static void load() throws IOException {
        Properties prop = new Properties();

        InputStream inputStream = new FileInputStream(ConfigPropertyValues.propFileName);

        prop.load(inputStream);
        ConfigPropertyValues.props = prop;
    }

    public static void write() throws IOException {
        FileOutputStream out = new FileOutputStream(ConfigPropertyValues.propFileName);
        ConfigPropertyValues.props.store(out, null);
        out.close();
    }

    public static String get(String key) {
        return ConfigPropertyValues.props.getProperty(key);
    }

    public static void set(String key, String value) {
        ConfigPropertyValues.props.setProperty(key, value);
    }
}
