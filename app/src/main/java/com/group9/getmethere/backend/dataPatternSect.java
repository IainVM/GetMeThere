package com.group9.getmethere.backend;

import java.util.TreeMap;

public class dataPatternSect {

	TreeMap < String, dataPatternLink > patternLinks;

	public dataPatternSect() {
		patternLinks = new TreeMap < String, dataPatternLink >();
	}

	public void add( String id, dataPatternLink dPS ) {
		patternLinks.put( id, dPS );
	}
}
