package com.group9.getmethere.backend;

import android.util.Log;
import java.io.IOException;

// TESTING?
import java.util.List;

public class pathRequest {

    // Logging
    private static final String TAG = "GetMeThere [pathRequest] ";
    //

    // Debug
    public static boolean DEBUG = false;
    //

    // Defines
    private static final String polylineStart   = "\"encodedPolyline\":\"";
    private static final String polylineEnd     = "\"\\}],";
    private static final double MAX_DISTANCE    = 0.000700;
    //

    public dataPolyLine getPath( int rID ) {
        System.out.format( "[pathRequest] Using rID: %d\n", rID );
        // Generate a new request to buscms.com
        cmsRequest cms = new cmsRequest();
        if( cms.send( rID ) ) {
            // We have some data to process!
            try {
                String [] data = cms.inputData.readLine().split( polylineStart );

                if( data.length > 1 ) {
                    String polyline = data[ 1 ].split( polylineEnd )[ 0 ];
                    Log.i( TAG, "[getPath] Got polyline: " + polyline );

                    // We have our encoded polyline - decode it
                    polylineCodec pC = new polylineCodec();
                
/*                    // TEST
                    List <Double> inVals = pC.pointsDecode( polyline );
                    // Display the results
                    for( int i = 1; i < inVals.size(); i += 2 )
                        System.out.format( "%f, %f\n", inVals.get( i - 1 ), inVals.get( i ) );*/

/*                    // TEST
                    dataPolyLine pL = pC.pointsDecode( polyline );
                    double total = 0;
                    for( int i = 0; i < pL.size(); i++ ) {
                        dataLine dL = pL.get( i );
                        total += pL.distance( i, i );
                        System.out.format( "%d: (length: %f\tcalc'd: %f)\t%f, %f to \t%f, %f\n", i, dL.length(), pL.distance( 0, i ), dL.from.lat, dL.from.lon, dL.to.lat, dL.to.lon );
                    }
                    System.out.format( "Total was: %f\n", total );
                    // END TEST*/

                return pC.pointsDecode( polyline );
            }

            }
            catch( IOException e ) {
                Log.e( TAG, "[getPath] IOException: " + e );
            }
        }

        return null;
    }

    private int matchedToLineIndex( dataPolyLine pL, int pLStart, int pLEnd, dataPoint dP ) {
      int pLStep = 1;
      if( pLStart > pLEnd )
        pLStep = -1;

      int indexClosest = -1;
      double closest = dataPoint.NONE;

      for( int i = pLStart; i != pLEnd; i += pLStep ) {
        dataLine pLLine = pL.get( i );
if( DEBUG )                
        System.out.format( "_%d: beside? %b\n", i, pLLine.isBeside( dP ) );
        if( pLLine.isBeside( dP ) ) {
          double current = pLLine.distanceTo( dP );
if( DEBUG )
          System.out.format( "distance: %f\n", current );
          if( current < closest && current < MAX_DISTANCE ) {
            closest = current;
            indexClosest = i;
          }
        }
      }

      return indexClosest;
    }

    public boolean matchToJourney( dataPolyLine pL, tndsParse tnds, dataService service, int journey ) {
        if( pL == null )
            return false;
        int links = service.numberOfStops( journey ) - 1;
 
        // First, find out which direction we're heading in
        int start, end;
        int link = 1;
        do {
          start = matchedToLineIndex( pL, 0, pL.size(), tnds.stops.get( service.linkNo( journey, link++ ).stopRefFrom ).location );
        } while( start == -1 && link <= links );
        link = links;
        do {
          end = matchedToLineIndex( pL, pL.size() - 1, -1, tnds.stops.get( service.linkNo( journey, link-- ).stopRefTo ).location );
        } while( end == -1 && link > 0 );
//        System.out.format( "Found start %d and end %d\n", start, end );

        // Set our start and end indices accordingly
        int startLine = 0, endLine = pL.size();
        boolean forwards = true;
        if( start > end ) {
          startLine = pL.size() - 1;  endLine = -1; forwards = false;
        }
//        System.out.format( "Searching from %d to !%d\n", startLine, endLine );

        // Step through each link (stops n to n+1)
        int lineIndex = startLine, lineIndexPrev = startLine;
        for( int l = 1; l <= links; l++ ) {
            // Get a link
            dataPatternLink dPL = service.linkNo( journey, l );

            // Find a matching line section (we get -1 if none was found within specified limits)
            lineIndex = matchedToLineIndex( pL, startLine, endLine, tnds.stops.get( dPL.stopRefTo ).location );

            // Check the outcome
            if( lineIndex != -1 ) {
if( DEBUG )
              Log.i( TAG, "[matchToJourney] Sections " + lineIndexPrev + " to " + lineIndex + " chosen (for " + tnds.stops.get( dPL.stopRefTo ).location.lat + ", " + tnds.stops.get( dPL.stopRefTo ).location.lon + ")" );
//              System.out.format( "Sections %d to %d chosen (for %f, %f)\n", lineIndexPrev, lineIndex, tnds.stops.get( dPL.stopRefTo ).location.lat, tnds.stops.get( dPL.stopRefTo ).location.lon );

              // Add the relevant line to the link section
              setPolyLine( dPL, pL, tnds.stops.get( dPL.stopRefFrom ).location, tnds.stops.get( dPL.stopRefTo ).location, lineIndexPrev, lineIndex, forwards );
                lineIndexPrev = lineIndex;
                startLine = lineIndex;
            }
            else {
              // Note the error, and add a dummy line to the link section (no progress better than random progress?)
              Log.e( TAG, "[matchToJourney] WARNING: NO ROUTE MATCH FOR JOURNEY " + journey + ", LINK " + l + "!" );
              setPolyLine( dPL, pL, tnds.stops.get( dPL.stopRefFrom ).location, tnds.stops.get( dPL.stopRefTo ).location, lineIndexPrev, lineIndexPrev, forwards );
            }
        }

        return true;
    }

    private void setPolyLine( dataPatternLink dPL, dataPolyLine pL, dataPoint pFrom, dataPoint pTo, int indexFrom, int indexTo, boolean forwards ) {
      if( forwards )
        dPL.setPolyLine( pL.get( indexFrom ).intersect( pFrom ), pL, indexFrom, indexTo, pL.get( indexTo ).intersect( pTo ), forwards );
      else {
        dPL.setPolyLine( pL.get( indexTo ).intersect( pTo ), pL, indexTo, indexFrom, pL.get( indexFrom ).intersect( pFrom ), forwards );
      }
    }

}
