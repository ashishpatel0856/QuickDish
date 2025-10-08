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
				.filename(".env") // default set hota h ".env"
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load();

		System.setProperty("STRIPE_SECRET_KEY", dotenv.get("STRIPE_SECRET_KEY"));
		System.setProperty("STRIPE_WEBHOOK_SECRET", dotenv.get("STRIPE_WEBHOOK_SECRET"));
		System.setProperty("SPRING_MAIL_USERNAME", dotenv.get("SPRING_MAIL_USERNAME"));
		System.setProperty("SPRING_MAIL_PASSWORD", dotenv.get("SPRING_MAIL_PASSWORD"));
		System.setProperty("GOOGLE_MAPS_API_KEY", dotenv.get("GOOGLE_MAPS_API_KEY"));
		System.setProperty("JWT_SECRET_KEY", dotenv.get("JWT_SECRET_KEY"));
		System.setProperty("USER_DB", dotenv.get("USER_DB"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));



		SpringApplication.run(QuickDishApplication.class, args);
	}
}
