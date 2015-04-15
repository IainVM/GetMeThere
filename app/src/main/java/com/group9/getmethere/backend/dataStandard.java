package com.group9.getmethere.backend;

import java.util.HashMap;

public class dataStandard {
	
	public String origin, destination;

	HashMap < String, dataPattern > journeyPatterns;

	public dataStandard() {
		journeyPatterns = new HashMap < String, dataPattern >();
	}

	public void add( String id, String destinationDisplay, String direction ) {
		journeyPatterns.put( id, new dataPattern( destinationDisplay, direction ) );
	}
}
