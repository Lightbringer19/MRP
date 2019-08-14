package configuration;

import lombok.Data;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

public class YamlConfig {
    
    public Config config;
    
    public static void main(String[] args) {
        YamlConfig yamlConfig = new YamlConfig();
        System.out.println(yamlConfig.config.toString());
    }
    
    public YamlConfig() {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        InputStream inputStream = YamlConfig.class
           .getClassLoader()
           .getResourceAsStream("config.yaml");
        config = yaml.load(inputStream);
    }
    
    @Data
    public static class Config {
        
        private String box_user_id;
        private String mrp_authorization;
        private String scene_host;
        private String scene_username;
        private String scene_password;
        private String rp_host;
        private String rp_username;
        private String rp_password;
        private String beat_host;
        private String beat_username;
        private String beat_password;
        private String reddit_username;
        private String reddit_password;
        private String reddit_client_id;
        private String reddit_client_secret;
        private String repost_authorization;
        private String dmp_username;
        private String dmp_password;
        private String ew_username;
        private String ew_password;
        private String mp3_pool_username;
        private String mp3_pool_password;
        private String bj_username;
        private String bj_password;
        private String hl_username;
        private String hl_password;
        private String bpm_username;
        private String bpm_password;
        private String bpm_latino_username;
        private String bpm_latino_password;
        private String remixmp4_username;
        private String remixmp4_password;
    }
}
