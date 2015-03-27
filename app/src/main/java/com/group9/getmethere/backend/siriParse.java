package com.group9.getmethere.backend;

import android.util.Log;

import java.util.HashMap;
import java.io.BufferedReader;
import java.util.Iterator;

public class siriParse {

        // Debug
        private static boolean DEBUG = false;
        //

        // Logging
        private static final String TAG = "GetMeThere";
        //

        // Defines
        public final static String siriTag 	        = "ServiceDelivery";
        public final static int ERROR 		= Integer.MAX_VALUE;
        public final static int AIMEDTIME_TOLERANCE = 300;  // +/- tolerance for Aimed stop time, in seconds
        //
	
        private xmlParser parser;

        private HashMap < String, dataSiriStop > siriStops;

	public siriParse( BufferedReader iD ) {
		// Create a map to hold the list of Monitored Stop Visit responses
		siriStops = new HashMap < String, dataSiriStop >();

		parser = new xmlParser( iD, true );

		// If we have a Siri file, extract data from it
		if( parser.find( siriTag, siriTag, false ) )
			getStops();
	}

	private void getStops() {
		String section = "MonitoredStopVisit";
		boolean endSection = parser.endOfFile();
		
		// Step through the file
		while( !endSection ) {

			if( parser.find( siriTag, section, true ) ) {
				// We've found a MonitoredStopVisit section - data follows
				dataSiriStopTime siriStopTime = new dataSiriStopTime();

				siriStopTime.monitoringRef     = parser.get( section, "MonitoringRef", true );
				String publishedLineName = parser.get( section, "PublishedLineName", true );
                                if( publishedLineName != null ) {
if( DEBUG )
        				Log.i( TAG, "[siriParse] Got name match for service " + publishedLineName );
				
        				// Look for MonitoredCall section
	        			String subSection = "MonitoredCall";
        				if( parser.find( section, subSection, true ) ) {
	        				// We've found a MonitoredCall section - data follows
if( DEBUG )
		        			Log.i( TAG, "[siriParse] Seeking AimedDepartureTime..." );
        					String aimed = parser.get( subSection, "AimedDepartureTime", true );
        					if( aimed != null ) {
                                                    siriStopTime.setAimed(aimed);
if( DEBUG )
                                                    Log.i( TAG, "[siriParse] Got AIMED: " + aimed );
                                                }
		        			String expected = parser.get( subSection, "ExpectedDepartureTime", true );
				        	// Do we have an expected time to store?
        					if( expected != null ) {
	        					siriStopTime.setExpected( expected );
if( DEBUG )
                                                        Log.i( TAG, "[siriParse] Got EXPECTED: " + expected );
        					}

	        				// Extract a key from the aimed time
		        			String timeKey = siriStopTime.aimedDepartureTD.getShortTimeStamp();
if( DEBUG )
			        		Log.i( TAG, "[siriParse] Storing under " + timeKey );
				        	dataSiriStop siriStop = siriStops.get( publishedLineName );
        					// STORE SST inside SS[ timeKey, SST ], then SS inside stops[ servName, SS ]
					
	        				// Do we need to create a new dataSiriStop?
        					if( siriStop == null )
        						siriStop = new dataSiriStop();

	        				// So now we definitely have a dataSiriStop object to manipulate
        					siriStop.add( timeKey, siriStopTime );
        					// Put the new / existing entry into the map of objects for this service
        					siriStops.put( publishedLineName, siriStop );
	        			}
        				// NOTHING is stored if MonitoredStopVisit DIDN'T have an aimed departure time
                                }
                                // If the stop name we received was null, signal end of section
                                else
                                    endSection = true;
			}
			else 
				endSection = true;

			// Only check for EOF if endSection has not already been signalled
			if( !endSection )
				endSection = parser.endOfFile();
		}
	}

        public dataSiriStopTime getStopTimeNear( String serviceName, dataTimeDate tD ) {
                // Get the stop objects map for this service
                dataSiriStop siriStop = siriStops.get( serviceName );

                // If it contains anything...
                if( siriStop != null ) {
                    // ...step through each to see if it's a close match
                    Iterator serviceStops = siriStop.stopTimes.keySet().iterator();

                    while( serviceStops.hasNext() ) {
                        dataSiriStopTime dSST = siriStop.stopTimes.get( serviceStops.next() );
                        dataTimeDate tDAimed = dSST.getAimed();
                        if( tDAimed == null ) {
if( DEBUG )                        
                            Log.e( TAG, "[siriParse] ERROR: Aimed dTD is NULL" );
                        }
                        else {
                            if( tDAimed.isWithin( tD, AIMEDTIME_TOLERANCE ) ) {
if( DEBUG )
                                Log.i( TAG, "[siriParse] Match found: retrieved " + tDAimed.getTimeStamp() + " ~ scheduled " + tD.getTimeStamp() );
                                return dSST;
                            }                        
                        }
                    }
                }

                // Otherwise, return null
                return null;
        }

	public dataSiriStopTime getStopTime( String serviceName, String shortTimeStamp ) {
		// Get the stop objects map for this service
		dataSiriStop siriStop = siriStops.get( serviceName );

		// If it contains anything...
		if( siriStop != null )
			// ...get the stop object for the given timestamp, and return it
			return siriStop.stopTimes.get( shortTimeStamp );

		// Otherwise, return null
		return null;
	}

	public dataSiriChange getSiriChange( dataSiriStopTime dSST ) {
		// Do we have an entry for this service and timestamp?
		if( dSST != null ) {
			dataSiriChange dSC = new dataSiriChange( dSST.monitoringRef, dSST.aimedDepartureTD, dSST.expectedDepartureTD );
			return dSC;
		}

		// Otherwise, return an error condition
		return null;
	}
}
