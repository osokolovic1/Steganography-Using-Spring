package com.mms.steganography;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SteganographyApplication {

	public static void main(String[] args) {
		//SpringApplication.run(SteganographyApplication.class, args);
		SpringApplicationBuilder builder = new SpringApplicationBuilder(SteganographyApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);
	}
}
