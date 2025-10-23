package org.lld.atlassian.racetrack.shubham;

import java.util.HashMap;
import java.util.Map;

class Driver {
    String name;
    Driver(String name){
        this.name = name;
    }
}

class DriverStats {
    double totalLapTime;
    double lastLap;
    int lapCount;

    DriverStats(){
        totalLapTime = 0;
        lapCount = 0;
    }

    void addLap(double lapTime, boolean pitStop){
        lastLap  = lapTime;
        if(!pitStop){
            totalLapTime += lapTime;
            lapCount += 1;
        }
    }

    double getAverage() {
        return lapCount==0?0.0:totalLapTime / lapCount;
    }

    double lastLapGain() {
        double averageLap = getAverage();
        return lastLap - averageLap;
    }
}

public class CarRace {
    final Map<String, Driver> drivers;
    final Map<Driver, DriverStats> driverLapStats;

    CarRace(){
        drivers = new HashMap<>();
        driverLapStats = new HashMap<>();
    }

    void addLap(String driverName, double lapTime, boolean pitStop){
        Driver driver;
        if(drivers.containsKey(driverName)){
            driver = drivers.get(driverName);
        }
        else{
            driver = new Driver(driverName);
            drivers.put(driverName, driver);
            driverLapStats.put(driver, new DriverStats());
        }
        driverLapStats.get(driver).addLap(lapTime, pitStop);
    }

    Driver getLastLapHero(){
        Driver lastLapHero = null;
        double lastLapMin = Double.MAX_VALUE;
        for(Driver driver: drivers.values()){
            DriverStats stats = driverLapStats.get(driver);
            double lastLap = stats.lastLapGain();
            if(lastLap < lastLapMin){
                lastLapMin = lastLap;
                lastLapHero = driver;
            }
        }
        return lastLapHero;
    }

    public static void main(String[] args) {
        CarRace carRace = new CarRace();

        carRace.addLap("driver1", 100, false);
        carRace.addLap("driver2", 80, true);
        carRace.addLap("driver1", 120, true);
        carRace.addLap("driver1", 100, false);
        carRace.addLap("driver1", 130, false);
        carRace.addLap("driver2", 100, false);
        carRace.addLap("driver1", 110, false);
        carRace.addLap("driver2", 90, true);

        Driver lastLapHero = carRace.getLastLapHero();

        System.out.println(lastLapHero.name + " is the last lap hero");
    }

}