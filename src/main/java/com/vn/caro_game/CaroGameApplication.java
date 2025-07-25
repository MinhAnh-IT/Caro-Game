package com.vn.caro_game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.vn.caro_game")
public class CaroGameApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaroGameApplication.class, args);
	}

}
