package com.group9.getmethere.backend;

import java.io.Serializable;

import java.util.HashMap;

public class dataRouteSects implements Serializable {

	public HashMap < String, dataRouteSect > routeSects;

	public dataRouteSects() {
		routeSects = new HashMap < String, dataRouteSect >();
	}

	public void add( String id, dataRouteSect dRS ) {
		routeSects.put( id, dRS );
	}
}
