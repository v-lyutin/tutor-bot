package org.telegram.tutorbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.telegram.tutorbot.config.BotProperties;

@SpringBootApplication
@EnableConfigurationProperties(BotProperties.class)
public class TutorBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(TutorBotApplication.class, args);
	}
}
