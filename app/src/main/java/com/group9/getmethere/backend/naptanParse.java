package com.group9.getmethere.backend;

import android.content.res.AssetManager;

public class naptanParse {

	// Defines
	private static String section   = "NaPTAN";
	private static String stopSects = "StopPoints";
	private static String stopSect  = "StopPoint";

	private static String atcoCode  = "AtcoCode";
	private static String longitude = "Longitude";
	private static String latitude  = "Latitude";
	//

	boolean endSection;
	String filename;
        AssetManager assets;
	dataStops stops;

	xmlParser parser;

	public naptanParse( AssetManager a, dataStops dS, String f ) {
        assets = a;
		stops = dS;
		filename = f;
		endSection = false;
		parseNaptan();
	}

	private void parseNaptan() {
		inputFile input   = new inputFile( assets, filename );
		parser = new xmlParser( input.reader, true );

		// Find start tag
		if( parser.find( section, section, false ) ) {
			// We've got a NaPTAN XML file, so seek out the StopPoints section
			if( parser.find( section, stopSects, false ) ) 
				// We've got a StopPoints section
				getStops();
		}
		input.close();
	}

	private void getStops() {
		// Step through each StopPoint in turn...
		while( !endSection ) {
			// Seek out a StopPoint
			if( parser.find( section, stopSect, false ) ) {
				// We've got a StopPoint, so grab AtcoCode
				String atco = parser.get( stopSect, atcoCode, true );
				// Do we have something to store?
				if( atco != null ) {
					// Grab Longitude
					double lon = Double.parseDouble( parser.get( stopSect, longitude, true ) );
					// Grab Latitude
					double lat = Double.parseDouble( parser.get( stopSect, latitude, true ) );

					// Store stop data, if relevant
					dataStop stop = stops.get( atco );
					if( stop != null )
                                                stop.setLocation( lat, lon );
				}
			}
			// Otherwise, we don't have any useful data here
			else
				endSection = true;

			// Only check for EOF if endSection not yet signalled
			if( !endSection )
				endSection = parser.endOfFile();
		}
	}
}
