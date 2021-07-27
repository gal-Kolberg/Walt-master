package com.walt;

import java.util.List;

import com.walt.model.*;
import java.util.stream.Collectors;

public class StringReportGenerator implements ReportGenerator {

    static public List<DriverDistance> generateReport(List<Driver> drivers) {
        for (Driver driver : drivers) {
            System.out.println(driver.getName() + ": " + driver.getTotalDistance());
        }

        return drivers.stream().collect(Collectors.toList());
    }
}
