package com.example.GymFlex;

import com.example.GymFlex.config.DatabaseConfigHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GymFlexApplication {

	static {
		DatabaseConfigHelper.configureDataSourceEnvironment();
	}

	public static void main(String[] args) {
		DatabaseConfigHelper.configureDataSourceEnvironment();
		SpringApplication.run(GymFlexApplication.class, args);
	}

}
