package com.group9.getmethere;

import android.util.Log;

import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class dataService {

    // Debug
    private static boolean DEBUG = false;
    //

    // Logging
    private static final String TAG = "GetMeThere";
    //

	// Defines
	public static final boolean FROM	= false;
	public static final boolean TO		= true;

	public static final int NOT_FOUND 	= Integer.MAX_VALUE;
	public static final float PROGRESS_ERR	= 2f;

	public static final byte NOT_DEPARTED	= 0;
	public static final byte RUNNING	= 1;
	public static final byte ARRIVED	= 2;
	public static final byte NO_JOURNEY	= 3;
	//

	/* <Service> derived */
	public String lineName, description;
	public dataStandard stdService;

	/* Other sections */
	// Route sections
	dataRouteSects routeSects;
	// Pattern sections
	dataPatternSects patternSects;
	// Dictionary of journeys
	dataJourneys journeys;

	public dataService() {
		stdService   = new dataStandard();

		routeSects   = new dataRouteSects();
		patternSects = new dataPatternSects();
		journeys     = new dataJourneys();
	}


	/* JOURNEY RELATED METHODS */

	// Returns the previously active journey time (or 0 if none found)
	public int previousJourney( dataTimeDate tDNow, dataTimeDate tDSim ) {
		boolean found = false;
		int active = activeJourney( tDNow, tDSim );
		int current, previous = 0;
	
                List < Integer > journeyTimesList = new ArrayList < Integer >( journeys.journeys.keySet() );
                Collections.sort( journeyTimesList );
                Iterator journeyTimes = journeyTimesList.iterator();

//		Iterator journeyTimes = journeys.journeys.keySet().iterator();

		while( journeyTimes.hasNext() && found != true ) {
			current = (Integer) journeyTimes.next();
			// If we found the currently active journey..
			if( current == active )
				// End the loop
				found = true;
			else
				// Otherwise, record this journey
				previous = current;
		}

		// Did we find anything?
		if( found )
			return previous;

		// If not, return NOT_FOUND
		return NOT_FOUND;
	}

	/* A return value of NOT_FOUND means NO currently active journey for this service */
	public int activeJourney( dataTimeDate tDNow, dataTimeDate tDSim ) {
		boolean found = false;
		int selected = NOT_FOUND;
		int previous = 0, current, journeyRunTime;
                dataJourney currentJourney;

                List < Integer > journeyTimesList = new ArrayList < Integer >( journeys.journeys.keySet() );
                Collections.sort( journeyTimesList );
                Iterator journeyTimes = journeyTimesList.iterator();

//		Iterator journeyTimes = journeys.journeys.keySet().iterator();

		while( journeyTimes.hasNext() && found != true ) {
			current = (Integer) journeyTimes.next();
			journeyRunTime = journeyRunTime( tDNow, current );
			// Check to see if the current journey is in scope
			if( journey( current ).hasDay( tDSim.dayByte() ) &&
                            current <= tDSim.time() && current + journeyRunTime > tDSim.time() ) {
				previous = current;
				selected = current;
				found = true;
			}
		}

		return selected;
	}

	// Returns a value denoting the run state of a given journey, or an error if the journey is not known
	public byte stateJourney( dataTimeDate tDNow, dataTimeDate tDSim, int departureTime ) {
                // Only test if we have a matching journey
		if( journeys.journeys.keySet().contains( departureTime ) ) {
                        // And only test further if it's running on this day
                        if( journey( departureTime ).hasDay( tDSim.dayByte() ) ) {
        			if( departureTime > tDSim.time() )
	        			return NOT_DEPARTED;
        			if( departureTime + journeyRunTime( tDNow, departureTime ) <= tDSim.time() )
        				return ARRIVED;
        			return RUNNING;
                        }
		}

		return NO_JOURNEY;
	}

	// Returns the journey object for a given departure time
	public dataJourney journey( int departureTime ) {
		return journeys.journeys.get( departureTime );
	}


	/* JOURNEY-PATTERN-SECTION RELATED METHODS */

	// Returns the JourneyPatternSection for the given journey
	private dataPatternSect patternSection( int departureTime ) {
		dataJourney j = journey( departureTime );
		if( j != null ) {
			String journeyPatternRef = j.journeyPatternRef;
			return patternSects.patternSects.get( journeyPatternRef );
		}
		return null;
	}

/*	// Returns the JourneyPatternTimingLink IDs for all links on the given journey
	private Set patternLinkAll( int departureTime ) {
		return patternSection( departureTime ).patternLinks.keySet();

                List < Integer > journeyTimesList = new ArrayList < Integer >( journeys.journeys.keySet() );
                Collections.sort( journeyTimesList );
                Iterator journeyTimes = journeyTimesList.iterator();


        }*/

	// Returns the nth JourneyPatternTimingLink ID in the given journey
	public String patternLinkID( int departureTime, int n ) {
		int index = 0;
		String ID = null;

                List < String > patternLinkList = new ArrayList < String >( patternSection( departureTime ).patternLinks.keySet() );
                Collections.sort( patternLinkList );
                Iterator linkID = patternLinkList.iterator();

//		Iterator linkID = patternLinkAll( departureTime ).iterator();

		while( linkID.hasNext() && index < n ) {
			ID = (String) linkID.next();
			index ++;
		}

		// Only return an ID if we found one
		if( index == n )
			return ID;
		return null;
	}

	// Returns the nth JourneyPatternTimingLink in the given journey
	public dataPatternLink patternLink( int departureTime, int n ) {
		dataPatternSect p = patternSection( departureTime );
		if( p != null ) {
			String pLinkID = patternLinkID( departureTime, n );
			if( pLinkID != null )
				return p.patternLinks.get( pLinkID );
		}
		return null;
	}

	// Returns the total run-time of a journey from origin to destination
	public int journeyRunTime( dataTimeDate tDNow, int departureTime ) {
		int total = 0;
		dataPatternSect dPS = patternSection( departureTime );

                List < String > patternLinkList = new ArrayList < String >( dPS.patternLinks.keySet() );
                Collections.sort( patternLinkList );
                Iterator linkID = patternLinkList.iterator();

//        	Iterator linkID = dPS.patternLinks.keySet().iterator();

		while( linkID.hasNext() ) {
//                        // TESTING ONLY
//                        Log.i( TAG, "[dataService] journeyRunTime(): **TEST** Using depTime " + departureTime );
//                        // TESTING END
                        // Don't send a departureTime (it would cause expiry of any updated run times
			total += dPS.patternLinks.get( linkID.next() ).getRunTime( tDNow, -1 );
		}

		return total;
	}


	/* JOURNEY PATTERN TIMING LINK RELATED METHODS */

        // Returns the nth journey pattern timing link
        public dataPatternLink linkNo( int departureTime, int n ) {

                // Find the stopRefFrom for the link whose from stop is stop n
                String stopRefFrom = stopRef( departureTime, n );
                // Then get the patternLink whose stopRefFrom contains this
                //  (If the returned value was null, it was likely a stopRefTo (and there is no link From final stop) )
                return linkWithStopRefFrom( departureTime, stopRefFrom );
        }

        // Returns the journey pattern timing link whose stopRefFrom matches the given stopRef
        public dataPatternLink linkWithStopRefFrom( int departureTime, String stopRef ) {
                boolean found = false;
                dataPatternLink dPL = null;

                dataPatternSect dPS = patternSection( departureTime );
                if( dPS != null ) {
                        Iterator linkID = dPS.patternLinks.keySet().iterator();

                        while( linkID.hasNext() && found == false ) {
                                dPL = dPS.patternLinks.get( linkID.next() );
                                if( dPL.stopRefFrom.equals( stopRef ) )
                                    found = true;
                        }
                }

                if( found )
                    return dPL;

                return null;
        }

/*        // Returns the journey pattern timing link whose stopRefTo matches the given stopRef
        public dataPatternLink linkWithStopRefTo( int departureTime, String stopRef ) {
                boolean found = false;
                dPL dataPatternLink = null;

                dataPatternSect dPS = patternSection( departureTime );
                if( dPS != null ) {
                        Iterator linkID = dPS.patternLinks.keySet().iterator();

                        while( linkID.hasNext() && found == false ) {
                                dPL = dPS.patternLinks.get( linkID.next() );
                                if( dPL.stopRefTo == stopRef )
                                    found = true;
                        }
                }

                if( found )
                    return dPL;

                return null;
        }*/

        // Returns the number of the journey pattern timing link whose stopRefTo matches the given stopRef
        public int linkNoWithStopRefTo( int departureTime, String stopRef ) {
                int linkNo = 0;
                boolean found = false;

                dataPatternSect dPS = patternSection( departureTime );
                if( dPS != null ) {

                        List < String > patternLinkList = new ArrayList < String >( dPS.patternLinks.keySet() );
                        Collections.sort( patternLinkList );
                        Iterator linkID = patternLinkList.iterator();

//                        Iterator linkID = dPS.patternLinks.keySet().iterator();

                        while( linkID.hasNext() && found == false ) {
                                dataPatternLink dPL = dPS.patternLinks.get( linkID.next() );
                                linkNo ++;
                                if( dPL.stopRefTo.equals( stopRef ) ) 
                                    found = true;
                        }
                }

                if( found )
                    return linkNo;

                return -1;
        }

	// Returns the number of the currently active journey pattern timing link for a given time and service
	public int activeLinkNo( dataTimeDate tDNow, dataTimeDate tDSim, int departureTime ) {
		int linkNo = 1;
		int timeNext, timePrevious = departureTime;

                // We assume here the programmer is only testing against the TIME field of tDSim
		dataPatternSect dPS = patternSection( departureTime );
		if( dPS != null ) {
                        List < String > patternLinkList = new ArrayList < String >( dPS.patternLinks.keySet() );
                        Collections.sort( patternLinkList );
                        Iterator linkID = patternLinkList.iterator();

//			Iterator linkID = dPS.patternLinks.keySet().iterator();

			while( linkID.hasNext() ) {
				dataPatternLink dPL = dPS.patternLinks.get( linkID.next() );
				linkNo ++;

//                                // TESTING ONLY
//                                Log.i( TAG, "[dataService] activeLinkNo(): **TEST** Using depTime " + departureTime );
//                                // TESTING END

				timeNext = timePrevious + dPL.getRunTime( tDNow, departureTime );
				// If the current time is between the previous and next stop times...
				if( timePrevious <= tDSim.time() && timeNext > tDSim.time() )
					// ... return the index number (1..n) of this link
					return linkNo;
				timePrevious = timeNext;
			}
		}

		// Otherwise, we found nothing (or no links existed!)
		return 0;
	}

	// Returns the currently active journey pattern timing link for a given time and service
	//  Otherwise, returns the LAST link (just in case it is still active!)
	public dataPatternLink activeLink( dataTimeDate tDNow, dataTimeDate tDSim, int departureTime ) {
		int timeNext, timePrevious = departureTime;
		dataPatternLink dPL = null;

                // We assume here the programmer is only testing against the TIME field of tDSim
		dataPatternSect dPS = patternSection( departureTime );
		if( dPS != null ) {
                        List < String > patternLinkList = new ArrayList < String >( dPS.patternLinks.keySet() );
                        Collections.sort( patternLinkList );
                        Iterator linkID = patternLinkList.iterator();

//			Iterator linkID = dPS.patternLinks.keySet().iterator();

			while( linkID.hasNext() ) {
				dPL = dPS.patternLinks.get( linkID.next() );

//                                // TESTING ONLY
//                                Log.i( TAG, "[dataService] activeLink(): **TEST** Using depTime " + departureTime );
//                                // TESTING END

				timeNext = timePrevious + dPL.getRunTime( tDNow, departureTime );
				// If the current time is between the previous and next stop times...
				if( timePrevious <= tDSim.time() && timeNext > tDSim.time() )
					// ... return the index number (1..n) of this link
					return dPL;
				timePrevious = timeNext;
			}
		}

		// Otherwise, return NULL for no links or the LAST ACTIVE LINK
		return dPL;
	}



	/* STOP RELATED METHODS */

	// Returns the previous StopPointRef for the active link of the given journey
	public String activeStopRefFrom( dataTimeDate tDNow, dataTimeDate tDSim, int departureTime ) {
		dataPatternLink dPL = activeLink( tDNow, tDSim, departureTime );
		if( dPL != null )
			return dPL.stopRefFrom;
		return null;
	}

	// Returns the next StopPointRef for the active link of the given journey
	public String activeStopRefTo( dataTimeDate tDNow, dataTimeDate tDSim, int departureTime ) {
		dataPatternLink dPL = activeLink( tDNow, tDSim, departureTime );
		if( dPL != null )
			return dPL.stopRefTo;
		return null;
	}

	// Returns the StopPointRef for stop n of the given journey
	// n = 1 = first stop
	public String stopRef( int departureTime, int n ) {
		String stopPointRef = null;
		
		int index = 0;
		dataPatternSect dPS = patternSection( departureTime );

                List < String > patternLinkList = new ArrayList < String >( dPS.patternLinks.keySet() );
                Collections.sort( patternLinkList );
                Iterator linkID = patternLinkList.iterator();

//		Iterator linkID = dPS.patternLinks.keySet().iterator();
		String ID = null;

		if( n <= dPS.patternLinks.size() + 1 ) {

			while( linkID.hasNext() && index < n ) {
				ID = (String) linkID.next();
				stopPointRef = dPS.patternLinks.get( ID ).stopRefFrom;
				index ++;
			}
		}
		if( n == dPS.patternLinks.size() + 1 )
			stopPointRef = dPS.patternLinks.get( ID ).stopRefTo;
			
		return stopPointRef;
	}

        // Returns the number of stops in the journey
        public int numberOfStops( int departureTime ) {
                dataPatternSect dPS = patternSection( departureTime );
                if( dPS != null )
                    return dPS.patternLinks.size() + 1;

                return -1;
        }

	// Returns the time (s) taken from departureTime to reach stop n
	// n = 1 = first stop, so always returns FROM time until last link (then TO time)
	// Returns NOT_FOUND if there is an error
	public int offsetToStopNo( dataTimeDate tDNow, int departureTime, int n ) {
		int index = 1, offset = NOT_FOUND;

		dataPatternSect dPS = patternSection( departureTime );
		if( dPS != null ) {
                        List < String > patternLinkList = new ArrayList < String >( dPS.patternLinks.keySet() );
                        Collections.sort( patternLinkList );
                        Iterator linkID = patternLinkList.iterator();

//			Iterator linkID = dPS.patternLinks.keySet().iterator();
			offset = 0;

			if( n <= dPS.patternLinks.size() + 1 ) {

				while( linkID.hasNext() && index < n ) {
					dataPatternLink dPL = dPS.patternLinks.get( linkID.next() );

//                                        // TESTING ONLY
//                                        Log.i( TAG, "[dataService] offsetToStopNo(): **TEST** Using depTime " + departureTime );
//                                        // TESTING END

					offset += dPL.getRunTime( tDNow, departureTime );
					index ++;
				}
			}
		}
		
		return offset;
	}

	// Returns the actual time stop n will be reached
	// Returns NOT_FOUND if there was an error
	private int timeToStopNo( dataTimeDate tDNow, int departureTime, int n ) {
		int time = offsetToStopNo( tDNow, departureTime, n );
		if( time != NOT_FOUND )
			return departureTime + time;
		return NOT_FOUND;
	}

	// Returns the time (s) taken from departureTime to reach / leave stop <ref>
	// Returns NOT_FOUND if there is an error
	private int offsetToStopRef( dataTimeDate tDNow, int departureTime, String stopRef ) {
		int offset = NOT_FOUND;

		// Get a pattern section for the given service
		dataPatternSect dPS = patternSection( departureTime );
		if( dPS != null ) {
                        List < String > patternLinkList = new ArrayList < String >( dPS.patternLinks.keySet() );
                        Collections.sort( patternLinkList );
                        Iterator linkID = patternLinkList.iterator();

//			Iterator linkID = dPS.patternLinks.keySet().iterator();
			offset = 0;

			// Step through the section
			while( linkID.hasNext() ) {
				// Get a link
				dataPatternLink dPL = dPS.patternLinks.get( linkID.next() );

				// Does this link start at the given stop? (Only happens at first iteration)
				if( dPL.stopRefFrom.equals( stopRef ) )
					return offset;

				offset += dPL.getRunTime( tDNow, departureTime );

				// Otherwise, does this link end at the given stop?
				if( dPL.stopRefTo.equals( stopRef ) )
					return offset;
			}

			// If we dropped through, we never found the stop!
			return NOT_FOUND;
		}
		
		// If we're here, we never found the journey!
		return NOT_FOUND;
	}

	// Returns the time (s) taken from departureTime to reach / leave stop <ref>
	// Returns NOT_FOUND if there is an error
	private int scheduledOffsetToStopRef( int departureTime, String stopRef ) {
		int offset = NOT_FOUND;

		// Get a pattern section for the given service
		dataPatternSect dPS = patternSection( departureTime );
		if( dPS != null ) {
                        List < String > patternLinkList = new ArrayList < String >( dPS.patternLinks.keySet() );
                        Collections.sort( patternLinkList );
                        Iterator linkID = patternLinkList.iterator();

//			Iterator linkID = dPS.patternLinks.keySet().iterator();
			offset = 0;

			// Step through the section
			while( linkID.hasNext() ) {
				// Get a link
				dataPatternLink dPL = dPS.patternLinks.get( linkID.next() );

				// Does this link start at the given stop? (Only happens at first iteration)
				if( dPL.stopRefFrom.equals( stopRef ) )
					return offset;

				offset += dPL.getScheduledRunTime();

				// Otherwise, does this link end at the given stop?
				if( dPL.stopRefTo.equals( stopRef ) )
					return offset;
			}

			// If we dropped through, we never found the stop!
			return NOT_FOUND;
		}
		
		// If we're here, we never found the journey!
		return NOT_FOUND;
	}



	// Returns the actual time stop <ref> will be reached
	// Returns NOT_FOUND if there was an error
	public int timeToStopRef( dataTimeDate tDNow, int departureTime, String stopRef ) {
		int time = offsetToStopRef( tDNow, departureTime, stopRef );
		if( time != NOT_FOUND )
			return departureTime + time;
		return NOT_FOUND;
	}

        // Returns the scheduled time stop <ref> will be reached
        // Returns NOT_FOUND if there was an error
        public int scheduledTimeToStopRef( int departureTime, String stopRef ) {
                int time = scheduledOffsetToStopRef( departureTime, stopRef );
                if( time != NOT_FOUND )
                        return departureTime + time;
                return NOT_FOUND;
        }

        // Returns the progress between last and next stops as a float between 0 and 1
        public float activeLinkProgress( dataTimeDate tDNow, dataTimeDate tDSim, int departureTime ) {
                String activeFrom = activeStopRefFrom( tDNow, tDSim, departureTime );
                String activeTo   = activeStopRefTo(   tDNow, tDSim, departureTime );

                return linkProgress( tDNow, tDSim, departureTime, activeFrom, activeTo );
        }

	// Returns the progress between last and next stops as a float between 0 and 1
        //  tDSim = desired calculation time (note ONLY time field is in use here - programmer checks for a valid day),
        //  tDNow = current actual time, from/to = stop refs, dept. time = journey ref
	public float linkProgress( dataTimeDate tDNow, dataTimeDate tDSim, int departureTime, String stopFrom, String stopTo ) {
		// Do we have valid from and to stop references?
		if( stopFrom != null && stopTo != null ) {
			int timeFrom = timeToStopRef( tDNow, departureTime, stopFrom );
			int timeTo   = timeToStopRef( tDNow, departureTime, stopTo   );

			// Do we have valid from and to stop times?
			if( timeFrom != NOT_FOUND && timeTo != NOT_FOUND ) {
				// Calculate the total time needed to traverse the link 
				int linkTotal = timeTo - timeFrom;
				// Calculate the current traversal time
				int linkCurr  = tDSim.time() - timeFrom;

				// Return the actual progress
				float progress = (float) linkCurr / linkTotal;
				if( progress > 1f )
					return 1;
				if( progress < 0f )
					return 0;
				return progress;
			}
		}

		// If we dropped through, there is no calculated progress
		return PROGRESS_ERR;
	}

	/* LIVE SERVICE UPDATE RELATED METHODS */

	public boolean update( dataTimeDate tDNow, dataSiriChange dSC ) {
		// Find the JourneyPatternSection that needs updating...
		//  (here, I'm assuming it's an active one for the given aimed time...)
		int journey = activeJourney( tDNow, dSC.aimed );
		// If we've got a valid journey to search, do so
		if( journey != NOT_FOUND ) {
//if( DEBUG )                
                        Log.i( TAG, "[dataService] update(): Found valid journey (" + journey + ")" );
			dataPatternSect dPS = patternSection( journey );
			// If we have a valid section, search for the given stop within it
			if( dPS != null ) {
				Iterator linkID = dPS.patternLinks.keySet().iterator();

				while( linkID.hasNext() ) {
					dataPatternLink dPL = dPS.patternLinks.get( linkID.next() );

					// If we've found the stop we need to update...
					if( dPL.stopRefTo.equals( dSC.stopRef ) ) {
                                                // ...calculate the current time to stop
//                                                int timeTo = scheduledTimeToStopRef( journey, dSC.stopRef );
                                                int timeTo = timeToStopRef( tDNow, journey, dSC.stopRef );
                                                // Use this to update (so new run-times are relative to 'live' times in use)
                                                // TESTING ONLY
                                                dPL.DEBUG = true;
                                                // END TESTING
						dPL.update( tDNow, journey, dSC, timeTo );
                                                // TESTING ONLY
                                                dPL.DEBUG = false;
                                                // END TESTING

//if( DEBUG )                                                
						Log.i( TAG, "[dataService] UPDATE SUCCESSFUL for " + dSC.stopRef );
						// Return success
						return true;
					}
				}
			}
                        else
                            Log.e( TAG, "[dataService] update(): ERROR: No matching PatternSect on update!!" );
		}
                else
if( DEBUG )                
                    Log.e( TAG, "[dataService] update(): No associated journey found" );

		// If we're here, we failed to find the pattern section or link!
		return false;
	}
}
