package com.group9.getmethere.backend;

import java.util.HashMap;

public class siriRequests {

	public HashMap < String, siriRequest > requests;

	public siriRequests() {
		requests = new HashMap < String, siriRequest >();
	}

	public void add( String id, siriRequest sR ) {
		requests.put( id, sR );
	}
}
