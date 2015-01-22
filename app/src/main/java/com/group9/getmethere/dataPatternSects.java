package com.group9.getmethere;

import java.util.HashMap;

public class dataPatternSects {

	public HashMap < String, dataPatternSect > patternSects;

	public dataPatternSects() {
		patternSects = new HashMap < String, dataPatternSect >();
	}

	public void add( String id, dataPatternSect dPS ) {
		patternSects.put( id, dPS );
	}
}
