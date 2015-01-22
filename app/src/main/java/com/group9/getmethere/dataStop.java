package com.group9.getmethere;

import java.util.ArrayList;

public class dataStop {

    public String stopName;
    public ArrayList < String > associatedServices;
    public dataPoint location = new dataPoint();

    public dataStop( String name, String service ) {
        stopName = name;
        associatedServices = new ArrayList < String > ();
    }

    public void addService( String service ) {
        associatedServices.add( service );
    }

    public void setLocation( double lat, double lon ) {
        location.set( lat, lon );
    }

    public double getLat() {
        return location.lat;
    }

    public double getLon() {
        return location.lon;
    }
}

