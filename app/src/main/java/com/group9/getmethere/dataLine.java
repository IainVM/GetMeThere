package com.group9.getmethere;

public class dataLine {

    public dataPoint from, to;

    public dataLine( dataPoint a, dataPoint b ) {
        from = a;
        to   = b;
    }

    public double length() {
       double lengthLat = Math.abs( from.lat - to.lat );
       double lengthLon = Math.abs( from.lon - to.lon );
       return Math.hypot( lengthLat, lengthLon );
    }

    // Returns the distance from this line to point dP
    public double distanceTo( dataPoint dP ) {
        double startTodP = Math.hypot( Math.abs( from.lat - dP.lat ), Math.abs( from.lon - dP.lon ) );
//        double rad = to.angle( dP, from );
        return startTodP * Math.sin( to.angle( dP, from ) );
    }

    // Returns true if dP lies within an area whose boundaries are perpendicular to the line
    public boolean isBeside( dataPoint dP ) {
        double degFrom = Math.toDegrees( to.angle(   dP, from ) );
        double degTo   = Math.toDegrees( from.angle( dP, to ) );
        if( ( degFrom <= 90 || degFrom >= 270 ) && ( degTo <= 90 || degFrom >= 270 ) )
            return true;

        return false;
    }

/*    // Returns the distance from this line to point dP
    public double triangulate( dataPoint dP ) {
        double radA = to.angle( dP, from );
        double radB = from.angle( dP, to );
        return length() * ( ( Math.sin( radA ) * Math.sin( radB ) ) / ( Math.sin( radA + radB ) ) );
    }*/

    // Returns the distance between point <from> and the right-angle intersection of point dP
    public double intersectFrom( dataPoint dP ) {
        return distanceTo( dP ) / Math.tan( to.angle( dP, from ) );
    }

    // Returns the distance between point <to> and the right-angle intersection of point dP
    public double intersectTo( dataPoint dP ) {
        return distanceTo( dP ) / Math.tan( from.angle( dP, to ) );
    }

    // Returns a new dataPoint at the intersection of the line and point dP
    public dataPoint intersect( dataPoint dP ) {
        pointCalculate pCalc = new pointCalculate();
        return pCalc.calcPoint( from, to, intersectFrom( dP ) / length() );
    }
}
