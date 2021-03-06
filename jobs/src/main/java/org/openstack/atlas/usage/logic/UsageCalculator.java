package org.openstack.atlas.usage.logic;

public class UsageCalculator {
    public static Double calculateNewAverage(Double oldAverage, Integer oldDivisor, Integer newDataPoint) {
        Integer newDivisor = oldDivisor + 1;
        return (oldAverage * oldDivisor + newDataPoint) / newDivisor;
    }
}
