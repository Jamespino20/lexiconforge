package lexiconforge.utils.SettingsProperties;

import java.io.*;
import java.util.Properties;

public class ConfigManager {

    private static final String APPDATA = System.getenv("APPDATA");
    private static final File CONFIG_FILE = new File(APPDATA, "LexiconForge/config.properties");

    private static Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        if (!CONFIG_FILE.exists()) {
            System.out.println("No config.properties found in " + CONFIG_FILE
                    + ". Using defaults, will create if needed.");
            return;
        }
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            System.out.println("Loaded config.properties from " + CONFIG_FILE.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Could not load config.properties. Using defaults.");
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
        saveProperties();
    }

    private static void saveProperties() {
        // Ensure parent folder exists
        if (!CONFIG_FILE.getParentFile().exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
        }
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "LexiconForge Config");
            System.out.println("Saved config.properties to " + CONFIG_FILE.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Could not save config.properties.");
            e.printStackTrace();
        }
    }

    // Font settings
    public static String getFontSetting() {
        return properties.getProperty("font", "Times New Roman");
    }

    public static void setFontSetting(String fontName) {
        setProperty("font", fontName);
    }
}
