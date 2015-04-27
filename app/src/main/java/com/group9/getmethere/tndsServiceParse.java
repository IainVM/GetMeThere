package com.group9.getmethere;

import android.content.res.AssetManager;
import android.util.Log;

public class tndsServiceParse {

        // Logging
        private static final String TAG = "GetMeThere [tndsServiceParse] ";
        //

	// Testing
	static final boolean SHOW_DATA	= false;
	static final boolean DEBUG	= false;
	//
	
	// Defines
	static final String xmlTag = "TransXChange";
	//

	private xmlParser parser;
	String input;

	// Gathered data
	private dataStops stops;
	private dataRouteSects routeSects;
	private dataPatternSects patternSects;
	private dataService service;
	private dataJourneys journeys;
	//

	public tndsServiceParse( AssetManager assets, String f, dataStops dSt, dataService dSe ) {
		stops = dSt;
		routeSects = dSe.routeSects;
		patternSects = dSe.patternSects;
		service = dSe;
		journeys = dSe.journeys;
		inputFile iF = new inputFile( assets, f );
		parser = new xmlParser( iF.reader, true );

		// If we have a TransXChange file, extract data from it
		if( parser.find( xmlTag, xmlTag, false ) ) {
if( DEBUG == true )
			Log.i( TAG, "[constructor] Parsing for service data..." );
			getService();
			parser.reset();

			getStops();
			getRouteSects();
			getPatternSects();
			getJourneys();
		}
	}

	private void getStops() {
		String section = "StopPoints";

		if( parser.find( xmlTag, section, true ) ) {
			// We've found the StopPoints tag - data follows

			String StopPointRef, CommonName;
			boolean endSection = parser.endOfFile();

			// Step through the file
			while( !endSection ) {
				// Look for a stop point reference
				StopPointRef = parser.getWithin( section, "AnnotatedStopPointRef", "StopPointRef", true );
				// Only get name and store if we found one
				if( StopPointRef != null ) {
					// Only store this stop point if we don't yet know it
					if( stops.get( StopPointRef ) == null ) {
						CommonName   = parser.get( section, "CommonName", true );
						dataStop stop = new dataStop( CommonName, service.lineName );
if( DEBUG == true )
						Log.i( TAG, "[getStops] Adding stop " + StopPointRef );
						stops.put( StopPointRef, stop );
					}
				}
				else
					endSection = true;

				// Only check for EOF if endSection has not already been signalled
				if( !endSection )
					endSection = parser.endOfFile();
			}
		}
		if( SHOW_DATA )	/* TESTING */
			Log.i( TAG, "\n[getStops] Stops: " + stops );
	}

	private void getRouteSects() {
		String section = "RouteSections";

		if( parser.find( xmlTag, section, true ) ) {
			// We've found the RouteSections tag - data follows
			
			String subSection = "RouteSection";
			String routeSectID = parser.getID( section, subSection );
			// Only continue if we've found a RouteSection
			if( routeSectID != null ) {

				dataRouteSect routeSect = new dataRouteSect();
				boolean endSection = parser.endOfFile();

				// Step through the section
				while( !endSection ) {
					// Look for RouteLinks
					String subSubSection = "RouteLink";
					String routeLinkID = parser.getID( subSection, subSubSection );

					// Only get further data and store if we've found an ID
					if( routeLinkID != null ) {
						// Create a new link
						dataRouteLink routeLink = new dataRouteLink();

						// Look for both StopPointRefs
						routeLink.stopRefFrom = parser.getWithin( subSubSection, "From", "StopPointRef", true );
						routeLink.stopRefTo   = parser.getWithin( subSubSection, "To",   "StopPointRef", true );
						// Look for Direction
						routeLink.direction   = parser.get( subSubSection, "Direction", true );

						// Store data
						routeSect.add( routeLinkID, routeLink );
if( DEBUG == true )
						Log.i( TAG, "[getRouteSects] Adding routeSect " + routeLinkID );
					}
					// Otherwise, end the section
					else
						endSection = true;

					if( endSection == false )
						endSection = parser.endOfFile();
				}
				if( SHOW_DATA ) /* TESTING */
					Log.i( TAG, "\n[getRouteSects] RouteSect: " + routeSect.routeLinks );

				// Store the routeSect data
				routeSects.add( routeSectID, routeSect );
			}
		}
		if( SHOW_DATA ) /* TESTING */
			Log.i( TAG, "\n[getRouteSects] RouteSects: " + routeSects.routeSects );
	}

	// This code is (was!) almost identical to getRouteSects() - abstract?
	private void getPatternSects() {
		String section = "JourneyPatternSections";

		if( parser.find( xmlTag, section, true ) ) {
			// We've found the JourneyPatternSections tag - data follows
			
			String subSection = "JourneyPatternSection";
			boolean endSection = parser.endOfFile();

			// Step through each JourneyPatternSection
			while( !endSection ) {

				String patternSectID = parser.getID( section, subSection );
				// Only continue if we've found a JourneyPatternSection
				if( patternSectID != null ) {
					dataPatternSect patternSect = new dataPatternSect();

					// Step through the section
					while( !endSection ) {
						// Look for JourneyPatternTimingLinks
						String subSubSection = "JourneyPatternTimingLink";
						String patternLinkID = parser.getID( subSection, subSubSection );

						// Only get further data and store if we've found an ID
						if( patternLinkID != null ) {
							// Create a new link
							dataPatternLink patternLink = new dataPatternLink();

							// Get From data
							patternLink.stopRefFrom      = parser.getWithin( subSubSection, "From", "StopPointRef", true );
							patternLink.timingStatusFrom = parser.get( subSubSection, "TimingStatus", true );
							// Get To data
							patternLink.stopRefTo        = parser.getWithin( subSubSection, "To",   "StopPointRef", true );
							patternLink.timingStatusTo   = parser.get( subSubSection, "TimingStatus", true );
							// Look for RouteLinkRef
							patternLink.routeLinkRef = parser.get( subSubSection, "RouteLinkRef", true );
							// Look for Direction
							patternLink.direction    = parser.get( subSubSection, "Direction", true );
							// Look for RunTime
							patternLink.setRunTime(    parser.get( subSubSection, "RunTime", true ) );

							// Store data
if( DEBUG == true )
							Log.i( TAG, "[getPatternSects] Adding PatternSect " + patternLinkID );
							patternSect.add( patternLinkID, patternLink );
						}
						// Otherwise, end the section
						else
							endSection = true;

						if( endSection == false )
							endSection = parser.endOfFile();
					}

					// Store the routeSect data
					patternSects.add( patternSectID, patternSect );

					endSection = parser.endOfFile();
				}
				// Otherwise, we've finished the section
				else
					endSection = true;

				if( endSection == false )
					endSection = parser.endOfFile();
			}
		}
		if( SHOW_DATA ) /* TESTING */
			Log.i( TAG, "\n[getPatternSects] PatternSects: " + patternSects.patternSects );
	}

	private void getService() {
		String section = "Services";
		String subSection = "StandardService";

		if( parser.find( xmlTag, section, true ) ) {
if( DEBUG == true )
			Log.i( TAG, "[getService] Found SERVICES." );
			// We've found the Services tag - data follows

			// Look for the Line Name
			service.lineName = parser.get( section, "LineName", true );
			// Look for the Description
			service.description = parser.get( section, "Description", true );

			// Look for a Standard Service section, and handle it
			if( parser.find( section, subSection, true ) )
				getStandardService( subSection, service );
		}
		if( SHOW_DATA ) /* TESTING */
			Log.i( TAG, "\n[getService] Service: " + service.lineName );
	}

	private void getStandardService( String section, dataService service ) {
		String subSection = "JourneyPattern";

		// Look for Origin string
		service.stdService.origin = parser.get( section, "Origin", true );
		// Look for the Destination string
		service.stdService.destination = parser.get( section, "Destination", true );

		boolean endSection = parser.endOfFile();

		while( !endSection ) {

			String id, destinationDisplay, direction;
			id = parser.getID( section, subSection );
			// Only get further tags and store if we found an ID
			if( id != null ) {
				destinationDisplay = parser.get( subSection, "DestinationDisplay", true );
				direction = parser.get( subSection, "Direction", true );
if( DEBUG == true )
				Log.i( TAG, "[getStandardService] Adding standard service " + id );
				service.stdService.add( id, destinationDisplay, direction );
			}
			else
				endSection = true;

			// Check for EOF
			if( endSection == false )
				endSection = parser.endOfFile();
		}
		if( SHOW_DATA ) { /* TESTING */
			Log.i( TAG, "\n[getStandardService] stdService: " + service.stdService.origin );
			Log.i( TAG, "\n[getStandardService] Pattern: " + service.stdService.journeyPatterns );
		}
	}

	private void getJourneys() {
		String section = "VehicleJourneys";
		String subSection = "VehicleJourney";
		// dataTime used for converting String time to integer time value
		dataTime dT = new dataTime();

		if( parser.find( xmlTag, section, true ) ) {
			// We've found the VehicleJourneys tag - data follows

			boolean endSection = parser.endOfFile();

			while( !endSection ) {
				// Look for a VehicleJourney section
				if( parser.find( section, subSection, true ) ) {
					// We've found a VehicleJourney section - data follows

					String subSubSection = "RegularDayType";
					dataJourney journey = new dataJourney();

					if( parser.find( subSection, subSubSection, true ) ) {
						// We've found a RegularDayType section - data follows

						while( !endSection ) {
							// Get a tag...
							String tag = parser.getTag( subSubSection, false );
							// ...check if it's the end of the section (or file!)
							if( tag == null )
								endSection = true;
							// Otherwise, check if it's a day
							else {
								byte day = journey.isDay( tag.split( " /" )[ 0 ] );
								if( day != -1 )
									// We've got a day! Add it to the journey
									journey.addDay( day );
							}

							if( endSection == false )
								endSection = parser.endOfFile();
						}
					}

					// Look for JourneyPatternRef
					journey.journeyPatternRef = parser.get( subSection, "JourneyPatternRef", true );
					// Look for Departure Time, and convert
					dT.setStringHMS( parser.get( subSection, "DepartureTime", true ) );

					// Add this journey to the list
					journeys.add( dT.calcTime(), journey );	
if( DEBUG == true )
					Log.i( TAG, "[getJourneys] Adding journey " + dT.calcTime() );

					if( SHOW_DATA ) /* TESTING */
						Log.i( TAG, "[getJourneys] Journey: " + journey.journeyPatternRef );

					endSection = parser.endOfFile();
//					}
				}
				else
					endSection = true;

				// Only check EOF if subSection end has not been signalled
				if( endSection == false )
					endSection = parser.endOfFile();
			}
		}
		if( SHOW_DATA ) /* TESTING */		
			Log.i( TAG, "[getJourneys] Journeys: " + journeys.journeys.keySet() );
	}
}
