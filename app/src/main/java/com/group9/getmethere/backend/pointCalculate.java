package com.group9.getmethere.backend;

public class pointCalculate {

    public dataPoint calcPoint( dataPoint from, dataPoint to, double progress ) {
        double scaleLat = to.lat - from.lat;
        double scaleLon = to.lon - from.lon;
        return new dataPoint( from.lat + scaleLat * progress, from.lon + scaleLon * progress );
    }
}
