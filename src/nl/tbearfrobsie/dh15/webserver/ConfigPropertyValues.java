package nl.tbearfrobsie.dh15.webserver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigPropertyValues {
    public static Properties props;
    
    private static String propFileName = "resources/config.properties";
    
    public static String CONFIG_KEY_DOCROOT = "docroot";
    public static String CONFIG_KEY_DIRECTORYBROWSING = "directorybrowsing";
    public static String CONFIG_KEY_DEFAULTPAGE = "defaultpage";
    public static String CONFIG_KEY_STATUSROOT = "statusroot";
    public static String CONFIG_KEY_ERRORPAGE = "errorpage";
    public static String CONFIG_KEY_FORBIDDENPAGE = "forbiddenpage";
    public static String CONFIG_KEY_BADREQUESTPAGE = "badrequestpage";
    public static String CONFIG_KEY_PORT = "port";
    
    public static String CONFIG_KEY_CERT_STORETYPE = "cert.storetype";
    public static String CONFIG_KEY_CERT_KEYSTORE = "cert.keystore";
    public static String CONFIG_KEY_CERT_STOREPASSWORD = "cert.storepassword";
    public static String CONFIG_KEY_CERT_KEYPASSWORD = "cert.keypassword";
    
    public static String CONFIG_VALUE_STR_TRUE = "true";
    public static String CONFIG_VALUE_STR_FALSE = "false";

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
    	// replace %3b with ;
        return ConfigPropertyValues.props.getProperty(key).replace("%3B", ";").replace("%2F", "/");
    }

    public static void set(String key, String value) {
        ConfigPropertyValues.props.setProperty(key, value);
    }
}
