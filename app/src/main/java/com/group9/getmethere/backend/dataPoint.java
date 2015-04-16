package com.group9.getmethere.backend;

public class dataPoint {

    public static final double  TOLERANCE   =   0.002000;   // Allowed lat/lon variation in matching points
    public final static double  NONE 	    =   Double.MAX_VALUE;

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

    // Returns the angle in radians
    public double angle( dataPoint dP, dataPoint origin ) {
        double latA = lat    - origin.lat;
        double lonA = lon    - origin.lon;
        double latB = dP.lat - origin.lat;
        double lonB = dP.lon - origin.lon;
        return Math.acos( ( latA * latB + lonA * lonB ) / ( Math.sqrt( Math.pow( latA, 2 ) + Math.pow( lonA, 2 ) ) * Math.sqrt( Math.pow( latB, 2 ) + Math.pow( lonB, 2 ) ) ) );
    }

/*    public double match( dataPoint dP ) {
//        System.out.format( "dataPoint: Testing: %f <= (%f + %f) && %f >= (%f - %f) && %f <= (%f + %f) && %f >= (%f - %f)...", dP.lat, lat, TOLERANCE, dP.lat, lat, TOLERANCE, dP.lon, lon, TOLERANCE, tP.lon, lon, TOLERANCE );
        if( dP.lat <= lat + TOLERANCE && dP.lat >= lat - TOLERANCE
         && dP.lon <= lon + TOLERANCE && dP.lon >= lon - TOLERANCE )
            return Math.abs( dP.lat - lat ) + Math.abs( dP.lon - lon );

        return NONE;
    }*/
}
