package com.group9.getmethere.backend;

import java.util.HashMap;

public class dataRouteSect {

	HashMap < String, dataRouteLink > routeLinks;

	public dataRouteSect() {
		routeLinks = new HashMap < String, dataRouteLink >();
	}

	public void add( String id, dataRouteLink dRS ) {
		routeLinks.put( id, dRS );
	}
}
