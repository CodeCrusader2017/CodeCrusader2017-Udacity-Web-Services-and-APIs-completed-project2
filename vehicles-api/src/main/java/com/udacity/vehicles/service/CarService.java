package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * Implements the car service create, read, update or delete information about vehicles,
 * as well as gather related location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository carRepository;

    private final PriceClient priceClient;
    private final MapsClient mapsClient;

    public CarService(CarRepository carRepository, PriceClient priceClient, MapsClient mapsClient) {

        /**
         * TODO: Add the Maps and Pricing Web Clients you create in `VehiclesApiApplication` as arguments and set them here.
         */

        this.carRepository = carRepository;
        this.priceClient = priceClient;
        this.mapsClient = mapsClient;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return carRepository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        /**
         * TODO: Find the car by ID from the `repository` if it exists.
         *   If it does not exist, throw a CarNotFoundException
         *   Remove the below code as part of your implementation.
         */
        Car car;
        Optional<Car> optionalCar = carRepository.findById(id);
        if (optionalCar.isPresent()) {
            car = optionalCar.get();
        }
        else {
            throw new CarNotFoundException();
        }


        /**
         * TODO: Use the Pricing Web client you create in `VehiclesApiApplication`
         *   to get the price based on the `id` input'
         * TODO: Set the price of the car
         * Note: The car class file uses @transient, meaning you will need to call
         *   the pricing service each time to get the price.
         */
        car.setPrice(priceClient.getPrice(id));


        /**
         * TODO: Use the Maps Web client you create in `VehiclesApiApplication`
         *   to get the address for the vehicle. You should access the location
         *   from the car object and feed it to the Maps service.
         * TODO: Set the location of the vehicle, including the address information
         * Note: The Location class file also uses @transient for the address,
         * meaning the Maps service needs to be called each time for the address.
         */
        car.setLocation(mapsClient.getAddress(car.getLocation()));

        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return carRepository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        //This line of code added to ensure condition is updated, as suggested by mentor in project submit review
                        carToBeUpdated.setCondition(car.getCondition());
                        return carRepository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return carRepository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        /**
         * TODO: Find the car by ID from the `repository` if it exists. If it does not exist, throw a CarNotFoundException
         */
        Optional<Car> optionalCar = carRepository.findById(id);
        if (optionalCar.isPresent()) {
            carRepository.delete(optionalCar.get());  //TODO: Delete the car from the repository - do this via get Car object from optional
        }
        else {
            throw new CarNotFoundException();
        }
    }
}




// Note: extra information from mentor in "https://knowledge.udacity.com/questions/322557"
// as to why we need to do this to activate the end points
// - pricing.endpoint=http://localhost:8082
// and
// - maps.endpoint=http://localhost:9191
// in the application.properties for VehiclesApi:
//
//"...The code above is provided to create bean for the maps and pricing endpoint, to get
// it registered with the RPC client. These endpoints are needed because we have to use
// these ports configured in file `application.properties`. After addition of this code
// you got pricing and maps endpoint registered for the use of reactive calls to endpoint..."

//Reference material used to get this section to work:
//https://docs.spring.io/spring-boot/docs/2.0.3.RELEASE/reference/html/boot-features-webclient.html
//https://howtodoinjava.com/spring-webflux/webclient-get-post-example/
//WebClient.RequestBodyUriSpec xx = webClient.get();
//Note to self: https://knowledge.udacity.com/questions/500317  says that you do not need to use web client directy
//Also to read:
//https://www.baeldung.com/spring-boot-data-sql-and-schema-sql
//https://classroom.udacity.com/nanodegrees/nd035/parts/49128b96-8489-4719-a3ea-c0beaaa46ff9/modules/585c4b4f-654f-4a9a-acbd-0fe5576a42ae/lessons/74043f56-683f-40d5-a7db-f974764ec5f7/concepts/b6e86a5d-5c38-4dba-9625-ac702134c827
//https://en.wikipedia.org/wiki/HATEOAS
