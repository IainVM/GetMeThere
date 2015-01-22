package com.group9.getmethere;

import java.util.HashMap;

// A single SIRI data object for a stop can contain many entries for the same service
public class dataSiriStop {

	public HashMap < String, dataSiriStopTime > stopTimes;

	public dataSiriStop() {
		stopTimes = new HashMap < String, dataSiriStopTime >();
	}

	public void add( String id, dataSiriStopTime dSST ) {
		stopTimes.put( id, dSST );
	}
}
