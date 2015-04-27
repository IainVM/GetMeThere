package com.group9.getmethere;

import java.io.Serializable;

public class dataPattern implements Serializable {

        public static final String  INBOUND   = "inbound";
        public static final String  OUTBOUND  = "outbound";

	public String destinationDisplay, direction;

	public dataPattern( String dD, String dir ) {
		destinationDisplay = dD;
		direction = dir;
	}
}
