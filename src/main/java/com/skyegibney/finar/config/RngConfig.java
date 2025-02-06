package com.skyegibney.finar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class RngConfig {
    @Bean
    Random rng() {
        return new Random();
    }
}
