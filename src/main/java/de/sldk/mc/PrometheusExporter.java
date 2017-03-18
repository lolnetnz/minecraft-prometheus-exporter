package de.sldk.mc;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.eclipse.jetty.server.Server;

public class PrometheusExporter extends Plugin {

    private Server server;

    @Override
    public void onEnable() {

        Configuration config = reloadConfig("config.yml");
        int port = config.getInt("port");
        server = new Server(port);

        server.setHandler(new MetricsController());

        try {
            server.start();

            getLogger().info("Started Prometheus metrics endpoint on port " + port);

        } catch (Exception e) {
            getLogger().severe("Could not start embedded Jetty server");
        }
    }

    @Override
    public void onDisable() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Configuration reloadConfig(String fileName) {
        Configuration config = null;
        if (!fileName.contains(".yml")) {
            fileName += ".yml";
        }

        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }
        File configFile = new File(this.getDataFolder(), fileName);
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                if (this.getResourceAsStream(fileName) == null) {
                    System.out.println("Failed to obtain " + fileName + " from jar file");
                }
                try (InputStream is = this.getResourceAsStream(fileName);
                        OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file: " + fileName, e);
            }
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException ex) {
            Logger.getLogger(PrometheusExporter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        saveConfig(config, fileName);
        return config;
    }

    private void saveConfig(Configuration config, String fileName) {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(this.getDataFolder(), fileName));
        } catch (IOException ex) {
            Logger.getLogger(PrometheusExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
