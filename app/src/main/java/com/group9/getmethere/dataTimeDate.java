package com.group9.getmethere;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import android.util.Log;

public class dataTimeDate {

    // Defines
    private static final int TD_ERROR = -2;

    // Logging
    private static final String TAG = "GetMeThere";
    //

    private Calendar cal;
    private boolean set;
    // INTERNAL USE ONLY
    private int year, month, day;
    private dataTime dT;
    //


    // Constructors / setters
    public dataTimeDate() {
        setup();
    }

    public dataTimeDate( String d, int t ) {
        setup();
    	setTimeDate( d, t );
    }

    public void setup() {
    	cal = Calendar.getInstance();
        set = false;
        dT  = new dataTime();
    }

    public void setTimeDate( String d, int t ) {
	if( convertStringToDate( d ) ) {
            dT.calcHMS( t );
            cal.set( year, month - 1, day, dT.hours, dT.minutes, dT.seconds );
            set = true;
        }
        else
            // Otherwise, there was an error
            set = false;
    }

    public void setTime( int t ) {
        dT.calcHMS( t );
        cal.set( cal.HOUR_OF_DAY, dT.hours );
        cal.set( cal.MINUTE, dT.minutes );
        cal.set( cal.SECOND, dT.seconds );
        // Careful - date may not be set yet!
        set = true;
    }

    public boolean setDate( String date ) {
        // Only set if we're successful
        if( convertStringToDate( date ) ) {
            cal.set( cal.YEAR, year );
            cal.set( cal.MONTH, month - 1 );
            cal.set( cal.DAY_OF_MONTH, day );
            // Careful - time may not be set yet!
            set = true;
            return true;
        }

        // We say set is false because this set failed
        set = false;
        return false;
    }

    private boolean convertStringToDate( String date ) {
        String [] elements = date.split( "-" );
        // If we have the right number of elements to process, do it
        if( elements.length == 3 ) {
            year  = Integer.parseInt( elements[ 0 ] );
            month = Integer.parseInt( elements[ 1 ] );
            day   = Integer.parseInt( elements[ 2 ] );

            // Success
            return true;
        }
        // If we have the wrong number of elements, display an error
        else
            Log.e( TAG, "ERROR: dataTimeDate: given Date of incorrect format (" + date + ")" );

        // If we dropped through, there was an error
        return false;
    }


    public void setCurrent() {
        cal = Calendar.getInstance();
        set = true;
    }


    // Return Calendar instance - for use with compareTo()
    public Calendar getCal() {
        return cal;
    }


    // Boolean getters
    public boolean isSet() {
        return set;
    }


    // Integer value getters
    public int time() {
        if( set ) {
            dT.calcTime( hour(), minute(), second() );
            return dT.time;
        }
        
        return 0;
    }

    public int day() {
        if( set )
            return cal.get( cal.DAY_OF_MONTH );

        return 0;
    }

    public int month() {
        if( set )
            return cal.get( cal.MONTH ) + 1;

        return 0;
    }

    public int year() {
        if( set )
            return cal.get( cal.YEAR );

        return 0;
    }

    public int hour() {
        if( set )
            return cal.get( cal.HOUR_OF_DAY );

        return 0;
    }

    public int minute() {
        if( set )
            return cal.get( cal.MINUTE );

        return 0;
    }

    public int second() {
        if( set )
            return cal.get( cal.SECOND );

        return 0;
    }


    // Byte getters
    public byte dayByte() {
        int d = cal.get( cal.DAY_OF_WEEK );
        if( d == cal.MONDAY )           return dataJourney.MONDAY;
        else if( d == cal.TUESDAY )     return dataJourney.TUESDAY;
        else if( d == cal.WEDNESDAY )   return dataJourney.WEDNESDAY;
        else if( d == cal.THURSDAY )    return dataJourney.THURSDAY;
        else if( d == cal.FRIDAY )      return dataJourney.FRIDAY;
        else if( d == cal.SATURDAY )    return dataJourney.SATURDAY;
        else if( d == cal.SUNDAY )      return dataJourney.SUNDAY;

        return 0;
    }


    // String getters
    public String getTimeStamp() {
	SimpleDateFormat timeStamp = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" );
	timeStamp.setTimeZone( cal.getTimeZone() );

        return timeStamp.format( cal.getTime() );
    }

    public String getShortTimeStamp() {
	String str = String.format( "%d-%d-%dT%02d:%02d", year(), month(), day(), hour(), minute() );
	return str;
    }



    // Manipulators
    public void addHour() {
        if( set )
            cal.add( cal.HOUR, 1 );
    }

    public void addMinutes( int minutes ) {
        if( set )
            cal.add( cal.MINUTE, minutes );
    }


    // Comparitors
    public boolean isBefore( dataTimeDate tD ) {
        return compareCal( tD.getCal() ) == 1;
    }

    public boolean isEqualTo( dataTimeDate tD ) {
        return compareCal( tD.getCal() ) == 0;
    }

    public boolean isAfter( dataTimeDate tD ) {
        return compareCal( tD.getCal() ) == -1;
    }

    // If THIS calendar is within +/- <seconds> of calendar <tD>
    public boolean isWithin( dataTimeDate tD, int seconds ) {
        if( year() == tD.year() && month() == tD.month() && day() == tD.day() &&
            time() <= tD.time() + seconds && time() >= tD.time() - seconds )
            return true;

        return false;
    }

    private int compareCal( Calendar otherCal ) {
        try {
            return cal.compareTo( otherCal );
        }
        catch( NullPointerException e ) {
            Log.e( TAG, "[dataTimeDate] ERROR: Null given instead of Calendar object." );
        }
        catch( IllegalArgumentException e ) {
            Log.e( TAG, "[dataTimeDate] ERROR: Invalid time value given within Calendar object." );
        }

        return TD_ERROR;
    }
}

