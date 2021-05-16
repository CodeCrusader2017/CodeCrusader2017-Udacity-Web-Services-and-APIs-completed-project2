//To run application:
//   (a). Run the "eureka" microservice server and check it is running at http://localhost:8761 with NO service instances.
//   (b). Run the "pricing-service-client" microservice client and check it is available at eureka http://localhost:8761 running under PORT 8090.
//   (c). Run the "pricing-service" microservice client and check it is available at eureka http://localhost:8761 running under PORT 8082.
//   (d). Run the "boogle-maps" API running under port 9191 (note: this is not a microservice registered with eureka).
//   (e). Run the "Vehicles-api" API running under port 8080 (note: this should have been an API and a microservice, but due to POM issues is just an API).
//   (f). Pre check the "boogle-maps" API is active at http://localhost:9191/maps?lat=20.0&lon=30.0
//   (g). Pre check the "pricing-service" is active at http://localhost:8082/prices, and http://localhost:8082/prices/1  or /2.

package com.udacity.vehicles;

import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.domain.manufacturer.ManufacturerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Launches a Spring Boot application for the Vehicles API, initializes the car manufacturers
 * in the database, and launches web clients to communicate with maps and pricing.
 */
@SpringBootApplication
@EnableJpaAuditing
public class VehiclesApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehiclesApiApplication.class, args);
    }

    /**
     * Initializes the car manufacturers available to the Vehicle API.
     * @param repository where the manufacturer information persists.
     * @return the car manufacturers to add to the related repository
     */
    @Bean
    CommandLineRunner initDatabase(ManufacturerRepository repository) {
        return args -> {
            repository.save(new Manufacturer(100, "Audi"));
            repository.save(new Manufacturer(101, "Chevrolet"));
            repository.save(new Manufacturer(102, "Ford"));
            repository.save(new Manufacturer(103, "BMW"));
            repository.save(new Manufacturer(104, "Dodge"));
        };
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * Web Client for the maps (location) API
     * @param endpoint where to communicate for the maps API
     * @return created maps endpoint
     */
    @Bean(name="maps")
    public WebClient webClientMaps(@Value("${maps.endpoint}") String endpoint) {
        return WebClient.create(endpoint);
    }

    /**
     * Web Client for the pricing API
     * @param endpoint where to communicate for the pricing API
     * @return created pricing endpoint
     */
    @Bean(name="pricing")
    public WebClient webClientPricing(@Value("${pricing.endpoint}") String endpoint) {
        return WebClient.create(endpoint);
    }

}

//To run application:
//   (a). Run the "eureka" microservice server and check it is running at http://localhost:8761 with NO service instances.
//   (b). Run the "pricing-service-client" microservice client and check it is available at eureka http://localhost:8761 running under PORT 8090.
//   (c). Run the "pricing-service" microservice client and check it is available at eureka http://localhost:8761 running under PORT 8082.
//   (d). Run the "boogle-maps" API running under port 9191 (note: this is not a microservice registered with eureka).
//   (e). Run the "Vehicles-api" API running under port 8080 (note: this should have been an API and a microservice, but due to POM issues is just an API).
//   (f). Pre check the "boogle-maps" API is active at http://localhost:9191/maps?lat=20.0&lon=30.0
//   (g). Pre check the "pricing-service" is active at http://localhost:8082/prices, and http://localhost:8082/prices/1  or /2.

//To function test application:
//   (h). Log into postman at https://identity.getpostman.com/login,
//   (i). In Postman navigate to create new --> My workspace ---> Create a request. Set to POST, and in BODY select raw radio button, and in text dropdown select JSON.
//        Then paste in data from TEST DATA 1 below (removing the comment tags). Set the URL to http://localhost:8080/cars. Then press Send.
//   (j). In Postman do another POST for the data in TEST DATA 2 below.
//   (k). In Postman, set to GET for http://localhost:8080/cars, and http://localhost:8080/cars/1 and http://localhost:8080/cars/2 etc and check data
//        is returned correctly (note: where a car ID has been supplied, ensure that the address and price details are returned).
//   (l). In Postman try a PUT (update) on some of the data from test data 1 or 2 via http://localhost:8080/cars.
//   (m). In Postman try a DELETE on either test data 1 or 2 via http://localhost:8080/cars.

//To test Swagger documentation for the "vehicles-api", go to http://localhost:8080/swagger-ui.html.

//TEST DATA 1:
//{
//        "condition":"USED",
//        "details":{
//        "body":"sedan",
//        "model":"Impala",
//        "manufacturer":{
//        "code":101,
//        "name":"Chevrolet"
//        },
//        "numberOfDoors":4,
//        "fuelType":"Gasoline",
//        "engine":"3.6L V6",
//        "mileage":32280,
//        "modelYear":2018,
//        "productionYear":2018,
//        "externalColor":"white"
//        },
//        "location":{
//        "lat":40.73061,
//        "lon":-73.935242
//        }
//        }

//TEST DATA 2:
//        {
//        "condition":"NEW",
//        "details":{
//        "body":"Nishant",
//        "model":"Imhdfggggf",
//        "manufacturer":{
//        "code":102,
//        "name":"Chevtaa"
//        },
//        "numberOfDoors":4,
//        "fuelType":"Gasoline",
//        "engine":"3.6L V6",
//        "mileage":32280,
//        "modelYear":2018,
//        "productionYear":2018,
//        "externalColor":"white"
//        },
//        "location":{
//        "lat":40.73061,
//        "lon":-73.935242
//        }
//        }
