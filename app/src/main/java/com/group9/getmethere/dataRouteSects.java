package com.group9.getmethere;

import java.util.HashMap;

public class dataRouteSects {

	public HashMap < String, dataRouteSect > routeSects;

	public dataRouteSects() {
		routeSects = new HashMap < String, dataRouteSect >();
	}

	public void add( String id, dataRouteSect dRS ) {
		routeSects.put( id, dRS );
	}
}