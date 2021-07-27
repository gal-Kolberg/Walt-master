package com.walt;

import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WaltServiceImpl implements WaltService {

    @Autowired
    DriverRepository driverRepository;

    @Autowired
    DeliveryRepository deliveryRepository;

    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime)
            throws Exception {
        if (!customer.getCity().equals(restaurant.getCity())) {
            throw new Exception("ERROR: the customer and the resturant are not in the same city");
        }

        List<Driver> cityDrivers = driverRepository.findAllDriversByCity(restaurant.getCity());
        List<Driver> assignDriveres = new ArrayList<Driver>();
        Date availability = Date.from(deliveryTime.toInstant().minus(1, ChronoUnit.HOURS));
        Driver assignDriver;
        Random random = new Random();
        long distance = Long.valueOf(random.nextInt(20));

        for (Driver driver : cityDrivers) {
            if (driver.getLastDelivery().compareTo(availability) <= 0) {
                assignDriveres.add(driver);
            }
        }

        if (assignDriveres.size() == 0) {
            throw new Exception("No current available driver in " + restaurant.getCity().getName());
        } else {
            assignDriver = assignDriveres.get(0);

            for (Driver driver : assignDriveres) {
                if (driver.getNumberOfDeliveries() < assignDriver.getNumberOfDeliveries()) {
                    assignDriver = driver;
                }
            }
        }

        assignDriver.setLastDelivery(deliveryTime);
        assignDriver.setNumberOfDeliveries(assignDriver.getNumberOfDeliveries() + 1);
        assignDriver.setTotalDistance(assignDriver.getTotalDistance() + distance);
        driverRepository.save(assignDriver);

        Delivery delivery = new Delivery(assignDriver, restaurant, customer, deliveryTime, distance);
        deliveryRepository.save(delivery);
        return delivery;
    }

    @Override
    public List<DriverDistance> getDriverRankReport() {
        List<Driver> drivers = driverRepository.findAllDriversByOrderByTotalDistanceDesc().stream()
                .collect(Collectors.toList());
        return StringReportGenerator.generateReport(drivers);
    }

    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        List<Driver> drivers = driverRepository.findAllDriversByCityOrderByTotalDistanceDesc(city).stream()
                .collect(Collectors.toList());
        return StringReportGenerator.generateReport(drivers);
    }
}
