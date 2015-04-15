package com.group9.getmethere.backend;

public class dataPattern {

        public static final String  INBOUND   = "inbound";
        public static final String  OUTBOUND  = "outbound";

	public String destinationDisplay, direction;

	public dataPattern( String dD, String dir ) {
		destinationDisplay = dD;
		direction = dir;
	}
}
