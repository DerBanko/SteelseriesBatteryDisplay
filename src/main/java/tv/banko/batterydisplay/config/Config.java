package tv.banko.batterydisplay.config;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Config {

    private final Map<String, Object> map;

    public Config() {
        this.map = new HashMap<>();

        try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
            if (stream == null) {
                return;
            }

            String doc = new String(stream.readAllBytes());

            Yaml yaml = new Yaml();
            this.map.putAll(yaml.load(doc));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(this.map.getOrDefault(key, null));
    }
}
