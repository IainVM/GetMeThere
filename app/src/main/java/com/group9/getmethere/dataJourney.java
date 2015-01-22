package com.group9.getmethere;

public class dataJourney {

	// defines
	public static final int MONDAY		= 1;
	public static final int TUESDAY		= 2;
	public static final int WEDNESDAY	= 4;
	public static final int THURSDAY	= 8;
	public static final int FRIDAY		= 16;
	public static final int SATURDAY	= 32;
	public static final int SUNDAY		= 64;
	public static final String [] days	= { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	//

	private byte operatingDays;
	public String journeyPatternRef;

	public dataJourney() {
		operatingDays = 0;
	}

	public void addDay( byte day ) {
		operatingDays = (byte) ( operatingDays | day );
	}

	public boolean hasDay( byte day ) {
		return ( operatingDays & day ) > 0;
	}

        // Accepts a String day from days[] and converts it to a constant <DAY>
	public byte isDay( String strDay ) {
		for( byte i = 0; i < days.length; i++ ) {
			if( strDay.equals( days[ i ] ) )
				return (byte) Math.pow( 2, i );
		}

		return -1;
	}
}
