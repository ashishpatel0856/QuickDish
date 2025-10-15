package com.ashish.QuickDish;

import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "QuickDish Food Ordering API",
				version = "1.0",
				description = "API documentation for QuickDish Food Delivery System"
		)
)
public class QuickDishApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.configure()
				.filename(".env") // default ".env"
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load();

		// Safe helper method to set system properties only if not null
		setSystemProperty("STRIPE_SECRET_KEY", dotenv.get("STRIPE_SECRET_KEY"));
		setSystemProperty("STRIPE_WEBHOOK_SECRET", dotenv.get("STRIPE_WEBHOOK_SECRET"));
		setSystemProperty("SPRING_MAIL_USERNAME", dotenv.get("SPRING_MAIL_USERNAME"));
		setSystemProperty("SPRING_MAIL_PASSWORD", dotenv.get("SPRING_MAIL_PASSWORD"));
		setSystemProperty("GOOGLE_MAPS_API_KEY", dotenv.get("GOOGLE_MAPS_API_KEY"));
		setSystemProperty("JWT_SECRET_KEY", dotenv.get("JWT_SECRET_KEY"));
		setSystemProperty("USER_DB", dotenv.get("USER_DB"));
		setSystemProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

		SpringApplication.run(QuickDishApplication.class, args);
	}

	private static void setSystemProperty(String key, String value) {
		if (value != null && !value.isBlank()) {
			System.setProperty(key, value);
		} else {
			System.out.println("⚠️ Environment variable '" + key + "' is missing or empty!");
		}
	}
}
