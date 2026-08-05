package com.ashish.QuickDish;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class QuickDishApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(QuickDishApplication.class);
        app.addInitializers(applicationContext -> {
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Dotenv dotenv = Dotenv.configure().load();

            Map<String, Object> dotenvProperties = new HashMap<>();
            dotenv.entries().forEach(entry -> dotenvProperties.put(entry.getKey(), entry.getValue()));

            environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", dotenvProperties));
        });
        app.run(args);
    }
}
