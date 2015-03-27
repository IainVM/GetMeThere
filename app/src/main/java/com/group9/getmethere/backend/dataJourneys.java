package com.group9.getmethere.backend;

import java.util.TreeMap;

public class dataJourneys {

	public TreeMap < Integer, dataJourney > journeys;

	public dataJourneys() {
		journeys = new TreeMap < Integer, dataJourney >();
	}

	public void add( int time, dataJourney dJ ) {
		journeys.put( time, dJ );
	}
}
