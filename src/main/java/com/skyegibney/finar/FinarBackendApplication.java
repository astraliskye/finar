package com.skyegibney.finar;

import com.skyegibney.finar.models.User;
import com.skyegibney.finar.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class FinarBackendApplication {
	private final UserRepository userRepository;

	public FinarBackendApplication(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(FinarBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner insertUser() {
		return args -> {
			userRepository.save(new User(0, "astra", new BCryptPasswordEncoder().encode("password"), "astra"));
			userRepository.save(new User(0, "user", new BCryptPasswordEncoder().encode("password"), "user"));
			userRepository.save(new User(0, "kronk", new BCryptPasswordEncoder().encode("password"), "kronk"));
		};
	}
}
