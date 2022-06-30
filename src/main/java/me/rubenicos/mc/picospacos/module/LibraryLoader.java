package me.rubenicos.mc.picospacos.module;

import com.saicone.ezlib.Ezlib;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LibraryLoader {

    private static final String DEF_REPO = "https://repo.maven.apache.org/maven2/";

    private static final Map<String, String> relocations = Map.of(
            group("com", "saicone", "rtag"), "rtag12",
            group("com", "osiris", "dyml"), "dyml68"
    );

    private static final List<Dependency> dependencies = Arrays.asList(
            new Dependency("rtag12.Rtag", path(group("com", "saicone", "rtag"), "rtag", "1.2.0"), "https://jitpack.io/"),
            new Dependency("rtag12.RtagItem", path(group("com", "saicone", "rtag"), "rtag-item", "1.2.0"), "https://jitpack.io/"),
            new Dependency("dyml68.DreamYaml", path("com.github.Osiris-Team", "Dream-Yaml", "6.8"), "https://jitpack.io/")
    );

    public static void load(JavaPlugin plugin) {
        Ezlib ezlib = new Ezlib();
        for (Dependency dependency : dependencies) {
            try {
                Class.forName(dependency.getTest());
            } catch (ClassNotFoundException e) {
                plugin.getLogger().info("Loading dependency " + dependency.getPath());
                ezlib.load(dependency.getPath(), dependency.getRepository(), relocations, true);
            }
        }
    }

    private static String group(String... path) {
        return String.join(".", path);
    }

    private static String path(String... args) {
        return String.join(":", args);
    }

    private static final class Dependency {

        private final String test;
        private final String path;
        private final String repository;

        public Dependency(String test, String path) {
            this(test, path, DEF_REPO);
        }

        public Dependency(String test, String path, String repository) {
            this.test = test;
            this.path = path;
            this.repository = repository;
        }

        public String getTest() {
            return test;
        }

        public String getPath() {
            return path;
        }

        public String getRepository() {
            return repository;
        }
    }
}
