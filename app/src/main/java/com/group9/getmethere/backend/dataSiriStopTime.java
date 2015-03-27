package com.group9.getmethere.backend;

import android.util.Log;

public class dataSiriStopTime {

        // Logging
        private static final String TAG = "GetMeThere";
        //

	private dataTime dT;

	// Obtained data
	// NB: An aimed time is scheduled, and an expected time is live
	public String monitoringRef;
	public dataTimeDate aimedDepartureTD, expectedDepartureTD;
	public boolean hasAimed, hasExpected;
	//

	public dataSiriStopTime() {
		dT = new dataTime();
		aimedDepartureTD    = new dataTimeDate();
		expectedDepartureTD = new dataTimeDate();
		hasAimed    = false;
		hasExpected = false;
	}

	public void setAimed( String a ) {
		if( a != null ) {
			aimedDepartureTD = extractTime( a );
			hasAimed = aimedDepartureTD.isSet();
		}
	}

	public void setExpected( String e ) {
		if( e != null ) {
			expectedDepartureTD = extractTime( e );
			hasExpected = expectedDepartureTD.isSet();
		}
	}

        public dataTimeDate getAimed() {
            if( hasAimed )
                return aimedDepartureTD;

            return null;
        }

        public dataTimeDate getExpected() {
            if( hasExpected )
                return expectedDepartureTD;

            return null;
        }

	private dataTimeDate extractTime( String input ) {
		boolean success = false;
		dataTimeDate tD = new dataTimeDate();
		String [] date = input.split( "T" );
                // If we fail to extract the date, show an error and fail
		if( !tD.setDate( date[ 0 ] ) )
                    Log.e( TAG, "ERROR: dataSiriStopTime: Failed to extract date from '" + date[ 0 ] + "'." );
                // Otherwise, continue to process
                else {
    	        	// If we have the right number of elements to process, go ahead
        		if( date.length == 2 ) {
        			String [] times = input.split( "T" )[ 1 ].split( "\\." )[ 0 ].split( ":" );
        			// If we have the right number of elements to process, do it
        			if( times.length == 3 ) {
        				tD.setTime( dT.calcTime( Integer.parseInt( times[ 0 ] ), Integer.parseInt( times[ 1 ] ), Integer.parseInt( times [ 2 ] ) ) );
        				success = true;
        			}
        		}
                }

		// If we have the wrong number of elements or couldn't get a date, display an error
		if( !success )
			Log.e( TAG, "ERROR: dataSiriStopTime: Siri time of incorrect format (" + input + ")" );
		return tD;
	}
}
