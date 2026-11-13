package dev.lucasferraz.bardolindomarapi.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class NeonEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("DATABASE_URL");
        if (url == null || url.isBlank()) return;
        var pooled = connection(url);
        String directUrl = environment.getProperty("DATABASE_URL_UNPOOLED");
        if ((directUrl == null || directUrl.isBlank()) && pooled.pooled()) {
            throw new IllegalStateException("Defina DATABASE_URL_UNPOOLED para executar migrations sem pooling.");
        }
        var direct = directUrl == null || directUrl.isBlank() ? pooled : connection(directUrl);
        if (direct.pooled()) {
            throw new IllegalStateException("DATABASE_URL_UNPOOLED deve apontar para uma conexão direta.");
        }
        var properties = new LinkedHashMap<String, Object>();
        properties.put("DB_URL", pooled.jdbcUrl());
        properties.put("DB_USER", pooled.username());
        properties.put("DB_PASSWORD", pooled.password());
        properties.put("DB_MIGRATIONS_URL", direct.jdbcUrl());
        properties.put("DB_MIGRATIONS_USER", direct.username());
        properties.put("DB_MIGRATIONS_PASSWORD", direct.password());
        properties.entrySet().removeIf(entry -> {
            String explicit = environment.getProperty(entry.getKey());
            return explicit != null && !explicit.isBlank();
        });
        environment.getPropertySources().addFirst(new MapPropertySource("neonConnection", properties));
    }

    static Connection connection(String value) {
        try {
            var uri = URI.create(value);
            if (!("postgres".equals(uri.getScheme()) || "postgresql".equals(uri.getScheme()))
                    || uri.getHost() == null || uri.getRawPath() == null || uri.getRawPath().length() < 2
                    || uri.getRawUserInfo() == null || uri.getFragment() != null) {
                throw new IllegalArgumentException();
            }
            var credentials = uri.getRawUserInfo().split(":", 2);
            if (credentials.length != 2 || credentials[0].isBlank() || credentials[1].isBlank()) {
                throw new IllegalArgumentException();
            }
            String host = uri.getHost();
            if (host.contains(":") && !host.startsWith("[")) host = "[" + host + "]";
            String jdbcUrl = "jdbc:postgresql://" + host + (uri.getPort() < 0 ? "" : ":" + uri.getPort()) + uri.getRawPath();
            if (uri.getRawQuery() != null) {
                jdbcUrl += "?" + uri.getRawQuery().replace("channel_binding=", "channelBinding=");
            }
            return new Connection(jdbcUrl, decode(credentials[0]), decode(credentials[1]), uri.getHost().contains("-pooler."));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("URL Neon inválida: use postgresql://usuario:senha@host/banco, sem expor credenciais nos logs.");
        }
    }

    private static String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    record Connection(String jdbcUrl, String username, String password, boolean pooled) {
        @Override
        public String toString() { return "NeonConnection[credenciais omitidas]"; }
    }

    @Override
    public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
}
