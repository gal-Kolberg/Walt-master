package com.walt.model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Driver extends NamedEntity implements DriverDistance {

    @ManyToOne
    City city;

    Date lastDelivery;
    int numberOfDeliveries;
    double totalDistance;

    public Driver() {
    }

    public Driver(String name, City city) {
        super(name);
        this.city = city;
        this.lastDelivery = new Date(1);
        this.numberOfDeliveries = 0;
        this.totalDistance = 0;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Date getLastDelivery() {
        return lastDelivery;
    }

    public void setLastDelivery(Date date) {
        this.lastDelivery = date;
    }

    public int getNumberOfDeliveries() {
        return numberOfDeliveries;
    }

    public void setNumberOfDeliveries(int numberOfDeliveries) {
        this.numberOfDeliveries = numberOfDeliveries;
    }

    public void setTotalDistance(double distance) {
        this.totalDistance = distance;
    }

    @Override
    public Driver getDriver() {
        return this;
    }

    @Override
    public double getTotalDistance() {
        return totalDistance;
    }
}
