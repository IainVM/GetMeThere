package com.group9.getmethere;

import android.util.Log;

public class dataTime {

        // Logging
        private static final String TAG = "GetMeThere";
        //

	private static final int MINS = 60;
	private static final int HRS  = MINS * 60;

	public int time;
	public int seconds;
	public int minutes;
	public int hours;

	public void dataTime() {
		time = 0;
		hours = 0; minutes = 0; seconds = 0;
	}

	public void dataTime( int t ) {
		time = t;
		calcHMS();
	}

	public void dataTime( int h, int m, int s ) {
		setHMS( h, m, s );
		calcTime();
	}

	public void setHMS( int h, int m, int s ) {
		hours = h; minutes = m; seconds = s;
	}

	public void setStringHMS( String sT ) {
		String [] s = sT.split( ":" );
		if( s.length < 3 )
			Log.e( TAG, "ERROR: dataTime: Time string is of incorrect format (" + sT + ")" );

		hours   = Integer.parseInt( s[ 0 ] );
		minutes = Integer.parseInt( s[ 1 ] );
		seconds = Integer.parseInt( s[ 2 ] );
	}

	private int doCalcTime() {
		return time = seconds + ( minutes * MINS ) + ( hours * HRS );
	}

	public int calcTime() {
		return doCalcTime();
	}

	public int calcTime( int h, int m, int s ) {
		setHMS( h, m, s );
		return doCalcTime();
	}

	public int getTime() {
		return time;
	}

	private void doCalcHMS() {
		seconds = time % MINS;
		minutes = ( time / MINS ) % MINS;
		hours   = time / HRS;
	}

	public void calcHMS() {
		doCalcHMS();
	}

	public void calcHMS( int t ) {
		time = t;
		doCalcHMS();
	}
}
