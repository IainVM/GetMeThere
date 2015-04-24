package com.group9.getmethere.backend;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Set;

public class dataStops implements Serializable {

	private HashMap < String, dataStop > stops;
	
	public dataStops() {
		stops = new HashMap < String, dataStop >();
	}

	public void put( String id, dataStop stop ) {
		stops.put( id, stop );
	}

	public dataStop get( String id ) {
		return stops.get( id );
	}

        public Set keySet() {
                return stops.keySet();
        }

	// Returns the name of a given stop
	public String name( String id ) {
		if( id != null )
			return stops.get( id ).stopName;
		return null;
	}
}
