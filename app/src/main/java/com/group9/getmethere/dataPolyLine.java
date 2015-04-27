package com.group9.getmethere;

import java.util.List;
import java.util.ArrayList;

public class dataPolyLine {

//    private List <dataLine> polyline;
    private dataLines polyline;
    private dataPoint pointTemp;
    private int pointCount;

    public dataPolyLine() {
        pointCount = 0;
        polyline = new dataLines();
//        polyline = new ArrayList <dataLine> ();
    }

    public void add( double lat, double lon ) {
        if( pointCount == 0 )
            pointTemp = new dataPoint( lat, lon );
        else {
            if( pointCount == 1 )
                polyline.add( String.valueOf( pointCount - 1 ), new dataLine( pointTemp, new dataPoint( lat, lon ) ) );
            else
                polyline.add( String.valueOf( pointCount - 1 ), new dataLine( polyline.get( String.valueOf( pointCount - 2 ) ).to, new dataPoint( lat, lon ) ) );
        }

        pointCount ++;
    }

    public dataLine get( int index ) {
        if( index < polyline.size() )
            return polyline.get( String.valueOf( index ) );

        return null;
    }

    public int size() {
        return polyline.size();
    }

    // From beginning (from) of <indexFrom> to *end* (to) of <indexTo>
    public double distance( int indexFrom, int indexTo ) {
        double total = 0;
        while( indexFrom <= indexTo ) {
            total += get( indexFrom ).length();
            indexFrom ++;
        }

        return total;
    }
}
