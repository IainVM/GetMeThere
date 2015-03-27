package com.group9.getmethere.backend;

import android.util.Log;

public class dataPatternLink {

        // Debug
        public static boolean DEBUG = false;
        //

        // Logging
        private static final String TAG = "GetMeThere";
        //

	public String stopRefFrom, stopRefTo;
	public String timingStatusFrom, timingStatusTo;
	public String routeLinkRef;
	public String direction;
	private int runTime, newRunTime, journey;

        public dataPatternLink() {
            journey = -1;
        }

	public void setRunTime( String rT ) {
		String [] s = rT.split( "PT" );
		// If we don't have enough elements left to parse, show an error message
		if( s.length < 2 )
			Log.e( TAG, "ERROR: dataPatternLink - incorrect parameter (" + rT + ")" );
		// BODGE BELOW: Strips out 'M' which remains in 57.xml (and runtimes of 0?? Can't be right)
		runTime = Integer.parseInt( s[ 1 ].split( "S" )[ 0 ].split( "M" )[ 0 ] );
		newRunTime = runTime;
	}

        //  TODO: Update this to use tD, not j, to expire runTimes (so Sim can run correctly)
	public int getRunTime( dataTimeDate tD, int j ) {
                // Do we need to reset this run time?
                //  We might if we've got a stored journey reference, and a valid <j>...
                if( journey != -1 && j != -1 )
                    //  ...and we definitely do if it's NOT this journey!
                    if( journey != j ) {
//if( DEBUG )                        
                        Log.i( TAG, "[dataPatternLink] getRunTime(): *Reset runTime* (was for journey " + journey + ", this is " + j + ")" );
                        journey = -1;
                        newRunTime = runTime;
                    }

if( DEBUG )
                if( journey != -1 )
                    Log.i( TAG, "[dataPatternLink] getRunTime(): Returning modified runTime to " + stopRefTo );
                else
                    Log.i( TAG, "[dataPatternLink] getRunTime(): Returning scheduled runTime to " + stopRefTo );

		return newRunTime;
	}

        public int getScheduledRunTime() {
                return runTime;
        }

	public void update( dataTimeDate tDNow, int j, dataSiriChange dSC, int currentAimedTime ) {
if( DEBUG )        
                Log.i( TAG, "[dataPatternLink] Updated from " + newRunTime + " to " + ( runTime + ( dSC.expected.time() - currentAimedTime ) ) + "(" + newRunTime + " + (" + dSC.expected.time() + " - " + currentAimedTime + ") )" );
                // Store the journey this modification is for
                journey = j;
if( DEBUG )
                Log.i( TAG, "[dataPatternLink] Expires with !journey " + journey );
		// And set the newRunTime
		newRunTime = newRunTime + ( dSC.expected.time() - currentAimedTime );
	}

/*        public void expire() {
if( DEBUG )        
                Log.i( TAG, "[dataPatternLink] Expiring link from " + stopRefFrom + " to " + stopRefTo );
                newRunTime = runTime;
        }*/

    public String getFrom() {
        return stopRefFrom;
    }

    public String getTo() {
        return stopRefTo;
    }
}
