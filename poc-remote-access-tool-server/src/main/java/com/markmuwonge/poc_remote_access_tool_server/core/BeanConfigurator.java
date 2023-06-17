package com.markmuwonge.poc_remote_access_tool_server.core;

import java.util.Random;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfigurator {
	@Bean
	public Random random() {
		return new Random();
	}
}
