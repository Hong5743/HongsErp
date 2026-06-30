package com.hongs.hongs_erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HongsErpApplication {

	public static void main(String[] args) {
		SpringApplication.run(HongsErpApplication.class, args);
	}

}
