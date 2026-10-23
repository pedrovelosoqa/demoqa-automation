package br.com.desafioqa.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigManager {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPERTIES = loadProperties();

    private ConfigManager() {
    }

    public static String getBaseUrl() {
        return getRequiredProperty("base.url");
    }

    public static String getApiBaseUrl() {
        return getRequiredProperty("api.base.url");
    }

    public static boolean isApiRelaxedHttps() {
        return Boolean.parseBoolean(getRequiredProperty("api.relaxed.https"));
    }

    public static String getBrowser() {
        return getRequiredProperty("browser");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(getRequiredProperty("headless"));
    }

    public static int getExplicitWaitSeconds() {
        String value = getRequiredProperty("explicit.wait.seconds");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "A propriedade explicit.wait.seconds deve ser um número inteiro: " + value,
                    exception
            );
        }
    }

    private static String getRequiredProperty(String key) {
        String systemValue = System.getProperty(key);
        String value = systemValue != null && !systemValue.isBlank()
                ? systemValue
                : PROPERTIES.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Propriedade obrigatória não configurada: " + key);
        }
        return value.trim();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException("Arquivo não encontrado no classpath: " + CONFIG_FILE);
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível carregar " + CONFIG_FILE, exception);
        }
    }
}
