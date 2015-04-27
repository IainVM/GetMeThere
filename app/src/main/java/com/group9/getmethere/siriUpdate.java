package com.group9.getmethere;

import android.util.Log;

public class siriUpdate implements Runnable {

        // Debug
        private static boolean DEBUG = false;
        //

        // Logging
        private static final String TAG = "GetMeThere [siriUpdate] ";
        //

        // Defines
        public static final boolean   SINGLE  = false;
        public static final boolean   MULTI   = true;

	private dataTime 	dT;
	private tndsParse   	tnds;
	private dataService 	service;
	private siriRequests 	requests;

	// Thread variables
	dataTimeDate tDNow;
	dataTimeDate tDSim;
	String serviceName, destinationDisplay;
	int activeJourney;
        int linkNo, linkNoMin, linkNoMax;
	String stopRef;
	boolean ready, end, found, searchType;
	//

	public siriUpdate( tndsParse t, dataService dS, boolean sT ) {
		dT = new dataTime();
		tnds = t;
		service = dS;
                searchType = sT;
		requests = new siriRequests();
                linkNoMin = 1;
                linkNoMax = 1;
		end = false;
		suspend();
	}

	public void update( dataTimeDate tDN, dataTimeDate tDS, String sN, String dD, int aJ, String sR ) {
		tDNow = tDN;
		tDSim = tDS;
		serviceName = sN;
                destinationDisplay = dD;
		activeJourney = aJ;
                stopRef = sR;
                linkNoMax = service.linkNoWithStopRefTo( activeJourney, stopRef ) + 1;
		ready = true;
	}

	public void terminate() {
		end = true;
	}

	public void suspend() {
		ready = false;
	}

        // For debug output only
        private void showRunTimes() {
            String output = "Runtimes: ";
            String stopRef = service.activeStopRefTo( tDNow, tDSim, activeJourney, true );
            int linkNoMax = service.linkNoWithStopRefTo( activeJourney, stopRef );

            for( int linkNo = 1; linkNo <= linkNoMax; linkNo++ ) {
                dataPatternLink dPL = service.patternLink( activeJourney, linkNo );
                output += dPL.getRunTime( tDNow, activeJourney ) + ",";
            }

            Log.i( TAG, "[showRunTimes] TESTING-> " + output );
        }

	public void run() {
		while( !end ) {
			// Only work if we have the necessary variables set already
			if( ready ) {
				// Only check if we have an active journey (DO WE NEED THIS? DEPENDS WHERE CALLED FROM **)
				if( activeJourney != service.NOT_FOUND ) {

if( DEBUG )
                                        showRunTimes();

                                        // Check to make sure we're not on another simulation, if so reset the linkNoMin
                                        if( linkNoMin > linkNoMax ) {
if( DEBUG )                                            
                                            Log.i( TAG, "[run] Reset linkNoMin to 1" );
                                            linkNoMin = 1;
                                        }

                                        found = false;
                                        // Limit the search to the (anticipated) current stop, if requested
                                        if( searchType == SINGLE )
                                          linkNo = linkNoMax;
                                        else
                                          linkNo = linkNoMin;

                                        while( found == false && linkNo <= linkNoMax && ready == true ) {
if( DEBUG )
                                                Log.i( TAG, "[run] Checking link " + linkNo + " of " + linkNoMax );
                                                if( check( serviceName, destinationDisplay, activeJourney, service.stopRef( activeJourney, linkNo ) ) ) {
if( DEBUG )                                                                
                                                        Log.i( TAG, "[run] Link check successfully returned update" );
                                                        linkNoMin = linkNo;
                                                        // If we have a successful update, finish here on this iteration
                                                        found = true;
                                                       
                                                }

                                                linkNo ++;
                                        }

if( DEBUG )
                                        showRunTimes();
				}
			}
			// Sleep the thread for a while
			try {
				Thread.currentThread().sleep( ( siriRequest.requestFreq / 3 ) * 1000 );
			}
			catch( InterruptedException e ) {
				Log.e( TAG, "[run] ERROR: Interrupted exception " + e );
			}
		}

		Log.e( TAG, "[run] *** WARNING: SIRI UPDATE THREAD ENDED! ***" );
	}

	private boolean check( String serviceName, String destinationDisplay, int activeJourney, String stopRef ) {
if( DEBUG )
                Log.i( TAG, "[check] Checking " + tnds.stops.name( stopRef ) );

		siriRequest request = requests.requests.get( stopRef );

		// Do we need to make a new request?
		if( request == null ) {
			// Set up the new request to the NextBuses server
			request = new siriRequest( stopRef );
			requests.add( stopRef, request );
		}

		// Make the request - did we get any response?
		if( request.send( tDSim ) ) {
        		Log.i( TAG, "[check] Checked " + tnds.stops.name( stopRef ) );

			// We did, so parse it
			siriParse parser = new siriParse( request.inputData );
			// OK, did we get anything nearby time-wise for our service?
if( DEBUG )                        
                        Log.i( TAG, "[check] Seeking a SIRI update near time " + getScheduledShortTimeStamp( activeJourney, stopRef ) );
                        dataSiriStopTime dSST = parser.getStopTimeNear( serviceName, destinationDisplay, getScheduledTimeDate( activeJourney, stopRef ) );
                        if( dSST != null ) {
if( DEBUG )                        
                                Log.i( TAG, "[check] Found a SIRI update near time " + getScheduledShortTimeStamp( activeJourney, stopRef ) );
                                // And did we have an expected time?
                                if( dSST.hasExpected )
        			{
//if( DEBUG )                                
                                        Log.i( TAG, "[check] Got expected time " + dSST.expectedDepartureTD.getTimeStamp() );
		        		// We did, so act on this!
                                        if( service.update( tDNow, parser.getSiriChange( dSST ) ) == true )
	        			{
if( DEBUG )                                        
                                                Log.i( TAG, "[check] returning success (true)" );
			        		return true;

          				}
	        			else
		        			// Warn if the update failed
			        		Log.e( TAG, "[check] ERROR: service update FAILED!" );
        			}
        			// We didn't get anything - show this for debugging
        			else {
	        			if( dSST.hasAimed ) 
                                            Log.i( TAG, "[check] No expected time found for this stop" );
		        		// Warn if we haven't got ANY time for this stop!
                                        else
				        	Log.e( TAG, "[check] WARNING: NO AIMED DATA FOR STOP " + tnds.stops.name( stopRef ) + "!" );
        			}
                        }
                        else {
                            Log.i( TAG, "[check] No matching updates found." );
                        }
		}

		// We didn't update anything - so return false
		return false;
	}

        private dataTimeDate getScheduledTimeDate( int activeJourney, String stopRef ) {
                int stopTime = service.scheduledTimeToStopRef( activeJourney, stopRef );

                if( stopTime != service.NOT_FOUND ) {
                        dataTimeDate tDTemp = new dataTimeDate();
                        tDTemp.setCurrent();
                        tDTemp.setTime( stopTime );
                        return tDTemp;
                }
            
                return null;
        }

	private String getScheduledShortTimeStamp( int activeJourney, String stopRef ) {
	        return getScheduledTimeDate( activeJourney, stopRef ).getShortTimeStamp();
	}
}
