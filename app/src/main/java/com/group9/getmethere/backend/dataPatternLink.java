package com.group9.getmethere.backend;

import android.util.Log;

public class dataPatternLink {

        // Debug
        public static boolean DEBUG = false;
        //

        // Logging
        private static final String TAG = "GetMeThere [dataPatternLink] ";
        //

        // Return values
        public static final int NONE = -1;
        //

	public String stopRefFrom, stopRefTo;
	public String timingStatusFrom, timingStatusTo;
	public String routeLinkRef;
	public String direction;
	private int runTime, newRunTime, journey;

        // polyline route references
        dataLines lines;
        boolean linesHas;
//        int lineStart, lineEnd;

        public dataPatternLink() {
            journey = -1;
            lines = null;
            linesHas = false;
//            lineStart = -1;
//            lineEnd = -1;
        }

	public void setRunTime( String rT ) {
		String [] s = rT.split( "PT" );
		// If we don't have enough elements left to parse, show an error message
		if( s.length < 2 )
			Log.e( TAG, "[setRunTime] ERROR: incorrect parameter (" + rT + ")" );
		// BODGE BELOW: Strips out 'M' which remains in 57.xml (and runtimes of 0?? Can't be right)
		runTime = Integer.parseInt( s[ 1 ].split( "S" )[ 0 ].split( "M" )[ 0 ] );
		newRunTime = runTime;
	}

        public boolean hasLiveTime( int j ) {
            if( journey != -1 && j != -1 )
                if( journey == j )
                    return true;

            return false;
        }

        //  TODO: Update this to use tD, not j, to expire runTimes (so Sim can run correctly)
	public int getRunTime( dataTimeDate tD, int j ) {
                // Do we need to reset this run time?
                //  We might if we've got a stored journey reference, and a valid <j>...
                if( journey != -1 && j != -1 )
                    //  ...and we definitely do if it's NOT this journey!
                    if( journey != j ) {
//if( DEBUG )                        
                        Log.i( TAG, "[getRunTime] *Reset runTime* (was for journey " + journey + ", this is " + j + ")" );
                        journey = -1;
                        newRunTime = runTime;
                    }

if( DEBUG )
                if( journey != -1 )
                    Log.i( TAG, "[getRunTime] Returning modified runTime to " + stopRefTo );
                else
                    Log.i( TAG, "[getRunTime] Returning scheduled runTime to " + stopRefTo );

		return newRunTime;
	}

        public int getScheduledRunTime() {
                return runTime;
        }

	public void update( dataTimeDate tDNow, int j, dataSiriChange dSC, int currentAimedTime ) {
if( DEBUG )        
                Log.i( TAG, "[update] Updated from " + newRunTime + " to " + ( runTime + ( dSC.expected.time() - currentAimedTime ) ) + "(" + newRunTime + " + (" + dSC.expected.time() + " - " + currentAimedTime + ") )" );
                // Store the journey this modification is for
                journey = j;
if( DEBUG )
                Log.i( TAG, "[update] Expires with !journey " + journey );
		// And set the newRunTime
		newRunTime = newRunTime + ( dSC.expected.time() - currentAimedTime );
	}

/*        public void expire() {
if( DEBUG )        
                Log.i( TAG, "[update] Expiring link from " + stopRefFrom + " to " + stopRefTo );
                newRunTime = runTime;
        }*/

    public String getFrom() {
        return stopRefFrom;
    }

    public String getTo() {
        return stopRefTo;
    }

    /* Polyline related methods */
    public boolean hasLine() {
//        return lines != null;
        return linesHas;
    }

  public void setPolyLine( dataPoint from, dataPolyLine pL, int start, int end, dataPoint to, boolean forwards ) {
//        System.out.format( "Called with %d to %d\n", start, end );
        int index = 0;
        lines = new dataLines();

        dataLine lineStart = new dataLine( from, pL.get( start ).to );
        dataLine lineEnd   = new dataLine( pL.get( end ).from, to );
        int pLStart = 1, pLEnd = end - start, pLStep = 1;
        if( pLEnd < 1 ) pLEnd = 1;
        if( !forwards ) {
          dataLine lineTemp = lineEnd;
          lineEnd = lineStart;
          lineStart = lineTemp;
          pLStart = ( end - start ) - 1; pLEnd = 0; pLStep = -1;
          if( pLStart < 0 ) pLStart = 0;
        }

        // Add a start point
        lines.add( String.valueOf( index ), lineStart );
        index++;
 

        // Step through the line sections
//        System.out.format( "looping %d to %d step %d\n", pLStart, pLEnd, pLStep );
//        for( int i = 1; i != end - start; i++ ) {
        for( int i = pLStart; i != pLEnd; i += pLStep ) {
          lines.add( String.valueOf( index ), pL.get( start + i ) );
          index++;
        }
        
        // Add an end point
        lines.add( String.valueOf( index ), lineEnd );
        linesHas = true;
    }

    public int polyLineSize() {
        return lines.size();
    }

    public dataLine polyLineSection( int index ) {
        return lines.get( String.valueOf( index ) );
    }

    public double polyLineSections() {
        if( lines != null )
            return lines.length();

        return 0f;
    }

    public double length() {
        double total = 0;
        for( int index = 0; index < lines.length(); index++ )
            total += lines.get( String.valueOf( index ) ).length();

        return total;
    }

    // Returns the maximum line index for the given progress
    public int progressIndex( double progress ) {
        double target = lines.length() * progress;
        int index = 0;
        double total = lines.get( String.valueOf( index ) ).length();

        while( index < lines.size() ) {
            if( total > target )
                return index;
            index ++;
            if( index < lines.size() )
                total += lines.get( String.valueOf( index ) ).length();
        }

        // We can only ever reach the last section of the line!
        return polyLineSize() - 1;
    }

    public double progressLine( int maxIndex, double progress ) {
        double target = lines.length() * progress;
        for( int index = 0; index < maxIndex; index++ )
            target -= lines.get( String.valueOf( index ) ).length();

        // Remainder allows us to calculate progress along active line section
        return target / lines.get( String.valueOf( maxIndex ) ).length();
    }
}
