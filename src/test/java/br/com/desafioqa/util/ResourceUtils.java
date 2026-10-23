package br.com.desafioqa.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolve recursos do classpath para caminhos aceitos pelo upload do Selenium.
 */
public final class ResourceUtils {

    private ResourceUtils() {
    }

    public static Path getResourcePath(String resourceName) {
        URL resource = Thread.currentThread()
                .getContextClassLoader()
                .getResource(resourceName);

        if (resource == null) {
            throw new IllegalArgumentException("Recurso não encontrado no classpath: " + resourceName);
        }

        try {
            URI resourceUri = resource.toURI();
            if (!"file".equalsIgnoreCase(resourceUri.getScheme())) {
                throw new IllegalStateException(
                        "O recurso precisa estar disponível como arquivo para upload: " + resourceName);
            }
            return Paths.get(resourceUri).toAbsolutePath().normalize();
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Caminho inválido para o recurso: " + resourceName, exception);
        }
    }
}
