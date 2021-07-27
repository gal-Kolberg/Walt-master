package com.walt;

import com.walt.dao.*;
import com.walt.model.City;
import com.walt.model.Customer;
import com.walt.model.Delivery;
import com.walt.model.Driver;
import com.walt.model.DriverDistance;
import com.walt.model.Restaurant;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;

import java.util.*;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    City jerusalem;
    City tlv;
    City bash;
    City haifa;

    Restaurant meat;
    Restaurant vegan;
    Restaurant cafe;
    Restaurant chinese;
    Restaurant mexican;

    Customer beethoven;
    Customer mozart;
    Customer chopin;
    Customer rachmaninoff;
    Customer bach;

    Driver mary;
    Driver patricia;
    Driver jennifer;
    Driver james;
    Driver john;
    Driver robert;
    Driver david;
    Driver daniel;
    Driver noa;
    Driver ofri;
    Driver nata;

    Date date = new Date();
    Date datePlusOneHour = Date.from(date.toInstant().plus(1, ChronoUnit.HOURS)); // mock one hour passed
    Date datePlusTwoHour = Date.from(date.toInstant().plus(2, ChronoUnit.HOURS)); // mock two hour passed

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData() {

        jerusalem = new City("Jerusalem");
        tlv = new City("Tel-Aviv");
        bash = new City("Beer-Sheva");
        haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);

        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa);

        createRestaurant(jerusalem, tlv);
    }

    private void createRestaurant(City jerusalem, City tlv) {
        meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        vegan = new Restaurant("vegan", tlv, "Only vegan");
        cafe = new Restaurant("cafe", tlv, "Coffee shop");
        chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        mexican = new Restaurant("restaurant", tlv, "mexican restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa) {
        beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        mary = new Driver("Mary", tlv);
        patricia = new Driver("Patricia", tlv);
        jennifer = new Driver("Jennifer", haifa);
        james = new Driver("James", bash);
        john = new Driver("John", bash);
        robert = new Driver("Robert", jerusalem);
        david = new Driver("David", jerusalem);
        daniel = new Driver("Daniel", tlv);
        noa = new Driver("Noa", haifa);
        ofri = new Driver("Ofri", haifa);
        nata = new Driver("Neta", jerusalem);

        driverRepository.saveAll(
                Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata));
    }

    @Test
    public void testBasics() {

        assertEquals(((List<City>) cityRepository.findAll()).size(), 4);
        assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
    }

    @Test
    public void testAssignDriver() {
        /*
         * Check if the driver was successfuly assigned to the delivery
         */
        try {
            Delivery delivery = waltService.createOrderAndAssignDriver(beethoven, vegan, date);
            assert (mary.equals(delivery.getDriver()));
        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }

        assertEquals(1, deliveryRepository.findAllDeliverysByDriver(mary).size());
    }

    @Test
    public void testAssignSecondDriver() {
        /*
         * Check if the second driver was successfuly assigned to the delivery When the
         * first driver is busy
         */
        try {
            waltService.createOrderAndAssignDriver(beethoven, vegan, date);
            Delivery delivery = waltService.createOrderAndAssignDriver(beethoven, vegan, date);
            assertEquals(patricia, delivery.getDriver());
        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }

        assertEquals(1, deliveryRepository.findAllDeliverysByDriver(mary).size());
        assertEquals(1, deliveryRepository.findAllDeliverysByDriver(patricia).size());
    }

    @Test
    public void testDifferentCity() {
        /*
         * Bad weather test Check if an exception is thrown because the customer and
         * resturant are in a different city
         */
        try {
            waltService.createOrderAndAssignDriver(mozart, chinese, date);
            fail("Should have thrown exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void testUnavailableDriver() {
        /*
         * Bad weather test Check if an exception is thrown because there is no
         * available driver in the city
         */
        try {
            // there are only 3 drivers in tlv so it should throw an exception
            waltService.createOrderAndAssignDriver(beethoven, vegan, date);
            waltService.createOrderAndAssignDriver(rachmaninoff, chinese, date);
            waltService.createOrderAndAssignDriver(bach, cafe, date);
            waltService.createOrderAndAssignDriver(bach, mexican, date);
            fail("Should have thrown exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void testOneDriverLeftAvailable() {
        /*
         * Check if the ony left driver in the city is chosen for the delivery
         */
        try {
            waltService.createOrderAndAssignDriver(beethoven, vegan, date);
            waltService.createOrderAndAssignDriver(rachmaninoff, chinese, date);
            Delivery delivery = waltService.createOrderAndAssignDriver(bach, cafe, date);
            assertTrue(daniel.equals(delivery.getDriver()));
        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }
    }

    @Test
    public void testAssignLessBusyDriver() {
        /*
         * Check if the less busy driver was assigned
         */
        try {
            waltService.createOrderAndAssignDriver(beethoven, vegan, date);
            Delivery delivery = waltService.createOrderAndAssignDriver(rachmaninoff, chinese, datePlusOneHour);
            assertTrue(patricia.equals(delivery.getDriver()));
        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }
    }

    @Test
    public void testBusyDriversComplex() {
        /*
         * Check if the less busy driver was assigned
         */
        try {
            waltService.createOrderAndAssignDriver(beethoven, vegan, date);
            waltService.createOrderAndAssignDriver(rachmaninoff, chinese, date);
            waltService.createOrderAndAssignDriver(bach, cafe, date);
            waltService.createOrderAndAssignDriver(beethoven, mexican, datePlusOneHour);
            Delivery delivery = waltService.createOrderAndAssignDriver(rachmaninoff, chinese, datePlusTwoHour);
            assertTrue(patricia.equals(delivery.getDriver()));
            assertEquals(2, driverRepository.findByName("Mary").getNumberOfDeliveries());
            assertEquals(2, driverRepository.findByName("Patricia").getNumberOfDeliveries());
            assertEquals(1, driverRepository.findByName("Daniel").getNumberOfDeliveries());
        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }
    }

    @Test
    public void testReport() {
        /*
         * Check if the report work correctly
         */
        try {
            waltService.createOrderAndAssignDriver(beethoven, vegan, date);
            waltService.createOrderAndAssignDriver(rachmaninoff, chinese, date);
            waltService.createOrderAndAssignDriver(bach, cafe, date);
            Delivery delivery = waltService.createOrderAndAssignDriver(mozart, meat, datePlusOneHour);
            waltService.createOrderAndAssignDriver(beethoven, mexican, datePlusTwoHour);
            waltService.createOrderAndAssignDriver(rachmaninoff, chinese, datePlusTwoHour);
            assertTrue(robert.equals(delivery.getDriver()));
            List<DriverDistance> driverDistances = waltService.getDriverRankReport();
            assertEquals(11, driverDistances.size());
        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }
    }

    @Test
    public void testReportByCity() {
        /*
         * Check if the report by city work correctly
         */
        try {
            waltService.createOrderAndAssignDriver(beethoven, vegan, date);
            waltService.createOrderAndAssignDriver(rachmaninoff, chinese, date);
            waltService.createOrderAndAssignDriver(bach, cafe, date);
            Delivery delivery = waltService.createOrderAndAssignDriver(mozart, meat, date);
            waltService.createOrderAndAssignDriver(beethoven, mexican, datePlusOneHour);
            waltService.createOrderAndAssignDriver(rachmaninoff, chinese, datePlusTwoHour);
            assertTrue(robert.equals(delivery.getDriver()));
            List<DriverDistance> driverDistancesTlv = waltService.getDriverRankReportByCity(tlv);
            List<DriverDistance> driverDistancesJerusalem = waltService.getDriverRankReportByCity(jerusalem);
            assertEquals(3, driverDistancesJerusalem.size());
            assertEquals(3, driverDistancesTlv.size());
        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }
    }
}
