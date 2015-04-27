package com.group9.getmethere;

import java.io.Serializable;

import java.util.HashMap;

public class dataStandard implements Serializable {
	
	public String origin, destination;

	public HashMap < String, dataPattern > journeyPatterns;

	public dataStandard() {
	    journeyPatterns = new HashMap < String, dataPattern >();
	}

	public void add( String id, String destinationDisplay, String direction ) {
	    journeyPatterns.put( id, new dataPattern( destinationDisplay, direction ) );
	}

        public String journeyOrigin( String id ) {
            dataPattern dP = journeyPatterns.get( id );
            if( dP != null ) {
                if( dP.direction.equals( dP.INBOUND ) )
                    return destination;
                else
                    return origin;
            }

            return null;
        }

        public String journeyDestination( String id ) {
            dataPattern dP = journeyPatterns.get( id );
            if( dP != null ) {
                if( dP.direction.equals( dP.INBOUND ) )
                    return origin;
                else
                    return destination;
            }

            return null;
        }
}
