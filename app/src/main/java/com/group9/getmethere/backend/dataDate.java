package com.group9.getmethere.backend;

import android.util.Log;

public class dataDate {

    // Logging
    private static final String TAG = "GetMeThere";
    //

	public int day, month, year;
	private boolean isSet;

	public dataDate() {
		day = 0; month = 0; year = 0;
		isSet = false;
	}

	public dataDate( String date ) {
		setDate( date );
	}

	public void setDate( int d, int m, int y ) {
		day   = d;
		month = m;
		year  = y;
		isSet = true;
	}

	public boolean setDate( String date ) {
		String [] elements = date.split( "-" );
		// If we have the right number of elements to process, do it
		if( elements.length == 3 ) {
			year  = Integer.parseInt( elements[ 0 ] );
			month = Integer.parseInt( elements[ 1 ] );
			day   = Integer.parseInt( elements[ 2 ] );
			isSet = true;

			// Success
			return true;
		}
		// If we have the wrong number of elements, display an error
		else
			Log.e( TAG, "ERROR: dataTimeDate: given Date of incorrect format (" + date + ")" );

		// If we dropped through, there was an error
		return false;
	}

	public boolean set() {
		return isSet;
	}
}
