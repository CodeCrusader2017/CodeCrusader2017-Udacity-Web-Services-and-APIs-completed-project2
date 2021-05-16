package com.udacity.boogle.maps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BoogleMapsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoogleMapsApplication.class, args);
	}

}

//Note: can test with http://localhost:9191/maps?lat=20.0&lon=30.0 on web
// or curl http://localhost:9191/maps?lat=20.0&lon=30.0 on a command line
