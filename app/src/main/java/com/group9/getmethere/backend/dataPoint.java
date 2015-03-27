package com.group9.getmethere.backend;

public class dataPoint {

    public double lat, lon;

    public dataPoint() {
    }

    public dataPoint(double lt, double ln) {
        set( lt, ln );
    }

    public void set( double lt, double ln ) {
        lat = lt;
        lon = ln;
    }
}
