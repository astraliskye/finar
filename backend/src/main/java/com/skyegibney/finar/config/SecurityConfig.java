package com.skyegibney.finar.config;

import com.skyegibney.finar.auth.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  HttpSessionSecurityContextRepository httpSessionSecurityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  SecurityContextHolderStrategy securityContextHolderStrategy() {
    return SecurityContextHolder.getContextHolderStrategy();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
    daoAuthenticationProvider.setUserDetailsService(customUserDetailsService);
    daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
    daoAuthenticationProvider.setHideUserNotFoundExceptions(true);

    return new ProviderManager(daoAuthenticationProvider);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/login", "/register", "/me", "/health")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .logout(
            logout ->
                logout
                    .permitAll()
                    .logoutSuccessHandler(
                        (request, response, authentication) ->
                          response.setStatus(HttpServletResponse.SC_OK)
                        ))
        .csrf(AbstractHttpConfigurer::disable)
        .build();
  }
}
