package com.group9.getmethere.backend;

import android.util.Log;

import android.content.res.AssetManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Iterator;

//ifdef android
import android.content.Context;
import android.os.AsyncTask;
import java.io.Serializable;
//endif android

public class backendAPI {

    // Debug
    private static boolean DEBUG = false;
    //

    // Logging
    private static final String TAG = "GetMeThere [backendAPI] ";
    //

    // Control flags
    private static final boolean UPDATES_ENABLED  = true;
    private volatile boolean ready = false;
    //

    // HashMap used to store all active update instances
    HashMap < String, siriUpdates > updates;
    // HashMap used to store all active update threads
    HashMap < Integer, Thread > updateThreads;

    public class Bus 
//ifdef android
        implements Serializable
//endif android
    {
        public String name;
        public String from;
        public String to;
        public boolean direction;  // Necessary so this bus can be referenced (i.e. 54 is both outbound AND inbound!)

        public Bus( String n, String f, String t, boolean d ) {
            name = n; from = f; to = t; direction = d;
        }
    }

    String serviceNames[] = { "10", "27", "54", "61" };

    public class TimetableEntry
//ifdef android
        implements Serializable
//endif android
    {
        public String stopName;
        public String stopTime;

        public TimetableEntry( String sN, String sT ) {
            stopName = sN; stopTime = sT;
        }
    }


//ifdef android
    private Context context;
//endif android
    private AssetManager assets;
    public tndsParse tnds = new tndsParse();
    private dataTimeDate tD = new dataTimeDate();

    public ArrayList <Bus> busses;
    public final boolean INBOUND   = tnds.services.INBOUND;
    public final boolean OUTBOUND  = tnds.services.OUTBOUND;

    // Constructor: parses the TNDS and stores the data
    public backendAPI(
//ifdef android
                      Context c,
//endif android
                                AssetManager aM ) {
        updates = new HashMap < String, siriUpdates > ();
        updateThreads = new HashMap < Integer, Thread > ();
//ifdef android
        context = c;
//endif android
        assets = aM;

        // Create an empty busses array
        busses = new ArrayList <Bus> ();

        // CALL PARSERS

//ifdef android
        new initialise().execute();
//endif android

/*ifdef pc
        loadData();
        ready = true;
endif pc*/
    }

    public void loadData() {
      // BEGIN SERVICE DATA SETUP
      Log.i( TAG, "[loadData] Searching for stored TNDS..." );
//ifdef android
      stateStore.setContext( context );
//endif android
      Object obj = stateStore.loadState( "tnds" );
      if( obj != null ) {
        tnds = (tndsParse) obj;
        Log.i( TAG, "[loadData] Stored TNDS found and loaded!" );
      }
      else {
        Log.i( TAG, "[loadData] No stored TNDS found. Constructing..." );

        // Parse all known services
        for( int i = 0; i < serviceNames.length; i++ ) {
          Log.i( TAG, "[loadData] Adding inbound/outbound for service " + serviceNames[ i ] );
          tnds.parse( assets, serviceNames[ i ] );
        }

        // Fetch stop geolocation data
        Log.i( TAG, "[loadData] Loading NaPTAN data" );
        naptanParse naptan = new naptanParse( assets, tnds.stops, "NaPTAN_571-541.xml" );
        
        // Attempt to store state
        if( stateStore.saveState( "tnds", ( Object ) tnds ) )
          Log.i( TAG, "[loadData] TNDS state stored." );
        else
          Log.e( TAG, "[loadData] Could not store TNDS!" );
      }

      // Fill the busses array
      Log.i( TAG, "[loadData] Filling busses array with obtained data" );
      tD.setCurrent();
      for( int i = 0; i < serviceNames.length; i++ ) {
          // Inbound services
          dataService sI = tnds.services.get( serviceNames[ i ], INBOUND );
          int nextJourneyI = sI.nextJourney( tD, tD, false );
          String nextJourneyPatternRefI = sI.journeys.journeys.get( nextJourneyI ).journeyPatternRef;
          String originI = sI.stdService.journeyOrigin( nextJourneyPatternRefI );
//          String destinationI = sI.stdService.journeyPatterns.get( nextJourneyPatternRefI ).destinationDisplay;
          String destinationI = sI.stdService.journeyDestination( nextJourneyPatternRefI );
          Bus busI = new Bus( sI.lineName, originI, destinationI, INBOUND );
          busses.add( busI );
          // Outbound services
          dataService sO = tnds.services.get( serviceNames[ i ], OUTBOUND );
          int nextJourneyO = sO.nextJourney( tD, tD, false );
          String nextJourneyPatternRefO = sO.journeys.journeys.get( nextJourneyO ).journeyPatternRef;
          String originO = sO.stdService.journeyOrigin( nextJourneyPatternRefO );
//          String destinationO = sO.stdService.journeyPatterns.get( nextJourneyPatternRefO ).destinationDisplay;
          String destinationO = sO.stdService.journeyDestination( nextJourneyPatternRefO );
          Bus busO = new Bus( sO.lineName, originO, destinationO, OUTBOUND );
          busses.add( busO );
      }
    }

//ifdef android
    public class initialise extends AsyncTask <Void, Void, Void> {

      protected Void doInBackground( Void... params ) {
        loadData();

        return null;
      }

      protected void onProgressUpdate( Void params ) {
      }

      protected void onPostExecute( Void params ) {
        Log.i( TAG, "[initialise] Ready" );
        ready = true;
      }
    }
//endif android

    // IMPORTANT - use this to check whether the backendAPI is ready for use (i.e. all data is loaded)
    public boolean isReady() {
        return ready;
    }

    // Returns the current time as a string
    public String currentTime() {
        return dataTimeToString( dtCurrentTime() );
    }

    // Returns the current time as a dataTime object
    public dataTime dtCurrentTime() {
        tD.setCurrent();
        return new dataTime( tD.time() );
    }

    // UTILITY: Converts a dataTime into a String
    public String dataTimeToString( dataTime dT ) {
        return String.format( "%02d:%02d:%02d", dT.hours, dT.minutes, dT.seconds );
    }

    // UTILITY: Finds difference between two dataTimes
    public String dataTimeDifference(dataTime dt1, dataTime dt2){
        return dataTimeToString(new dataTime(dt1.time - dt2.time));
    }

    // Returns the total number of known services
    public int services() {
        return busses.size();
    }

    // Returns the name of service no. n from the busses array
    public String name( int n ) {
        // Range check
        if( n >= 0 && n < busses.size() ) {
            Bus bus = busses.get( n );
            return bus.name;
        }

        return null;
    }

    // Returns the origin of service no. n from the busses array
    public String from( int n ) {
        // Range check
        if( n >= 0 && n < busses.size() ) {
            Bus bus = busses.get( n );
            return bus.from;
        }

        return null;
    }

    // Returns the destination of service no. n from the busses array
    public String to( int n ) {
        // Range check
        if( n >= 0 && n < busses.size() ) {
            Bus bus = busses.get( n );
            return bus.to;
        }

        return null;
    }

    // Returns the direction of service no. n from the busses array
    public boolean direction( int n ) {
        // Range check
        if( n >= 0 && n < busses.size() ) {
            Bus bus = busses.get( n );
            return bus.direction;
        }

        return false;
    }

    // Tells you whether a given service has an active journey at the moment
    //  (i.e. we can / can't get data about it's current position and progress)
    public boolean isActive( String serviceName, boolean serviceDirection ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null ) {
            if( service.activeJourney( tD, tD, true ) != service.NOT_FOUND )
                return true;
        }

        return false;
    }

    // Returns the name of the previous stop for service named <serviceName> (no live data, currently)
    public String previousStop( String serviceName, boolean serviceDirection ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null ) {
            // Which journey is currently in progress?
            int journey = service.activeJourney( tD, tD, true );
            if( journey != service.NOT_FOUND ) {
                return tnds.stops.name( service.activeStopRefFrom( tD, tD, journey, true ) );
            }
        }

        return null;
    }

    // Returns the name of the next stop for service named <serviceName> (no live data, currently)
    public String nextStop( String serviceName, boolean serviceDirection ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null ) {
            // Which journey is currently in progress?
            int journey = service.activeJourney( tD, tD, true );
            if( journey != service.NOT_FOUND ) {
                return tnds.stops.name( service.activeStopRefTo( tD, tD, journey, true ) );
            }
        }

        return null;
    }

    // Returns the progress between the two stops for service named <serviceName>, as a float between 0 and 1
    //  (no live data, currently)
    // Returns -1 if an error occurred
    public float progressBetweenStops( String serviceName, boolean serviceDirection ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null ) {
            // Which journey is currently in progress?
            int journey = service.activeJourney( tD, tD, true );
            if( journey != service.NOT_FOUND ) {
                return service.activeLinkProgress( tD, tD, journey, true );
            }
        }

        return -1;
    }

    // Returns a String representation of the scheduled time of arrival at the next stop
    public String strScheduledTimeOfArrival( String serviceName, boolean serviceDirection ) {
        dataTime dT = scheduledTimeOfArrival( serviceName, serviceDirection );
        if( dT != null )
            return dataTimeToString( dT );

        return null;
    }

    // Returns a String representation of the scheduled time of arrival at the next stop
    public String strTimeOfArrival( String serviceName, boolean serviceDirection ) {
        dataTime dT = timeOfArrival( serviceName, serviceDirection );
        if( dT != null )
            return dataTimeToString( dT );

        return null;
    }

    // Returns a String representation of the delay of time of arrival to the next stop
    public String strTimeOfArrivalDelay( String serviceName, boolean serviceDirection ) {
        dataTime dT = timeOfArrivalDelay( serviceName, serviceDirection );
        if( dT != null )
            return dataTimeToString( dT );

        return null;
    }

    // Returns the scheduled time of arrival at the next stop
    public dataTime scheduledTimeOfArrival( String serviceName, boolean serviceDirection ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null ) {
            // Which journey is currently in progress?
            int journey = service.activeJourney( tD, tD, false );
            if( journey != service.NOT_FOUND ) {
                return new dataTime( service.scheduledTimeToStopRef( journey, service.activeStopRefTo( tD, tD, journey, false ) ) );
            }
        }

        return null;
    }

    // Returns the actual time of arrival at the next stop (or scheduled if not known)
    public dataTime timeOfArrival( String serviceName, boolean serviceDirection ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null ) {
            // Which journey is currently in progress?
            int journey = service.activeJourney( tD, tD, true );
            if( journey != service.NOT_FOUND ) {
                tD.setCurrent();
                return new dataTime( service.timeToStopRef( tD, journey, service.activeStopRefTo( tD, tD, journey, true ), false ) );
            }
        }

        return null;
    }

    // Returns the delay of time of arrival to the next stop
    public dataTime timeOfArrivalDelay( String serviceName, boolean serviceDirection ) {
        dataTime scheduled = scheduledTimeOfArrival( serviceName, serviceDirection );
        dataTime live      = timeOfArrival( serviceName, serviceDirection );
        if( scheduled != null && live != null )
            return new dataTime( scheduled.time - live.time );

        return null;
    }

    // Returns true if the current service has been delayed to its next stop
    public boolean isDelayed( String serviceName, boolean serviceDirection ) {
        dataTime delay = timeOfArrivalDelay( serviceName, serviceDirection );
        if( delay != null )
            if( delay.time > 0 )
                return true;

        return false;
    }

    // Returns the start time of the next journey - should be used if isActive() == false
    public dataTime timeOfNextJourney( String serviceName, boolean serviceDirection ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null )
            // Return the next journey time
            return new dataTime( service.nextJourney( tD, tD, false ) );

        return null;
    }

    // Returns a string version of the start time of the next journey - use if isActive() == false, as above
    public String strTimeOfNextJourney( String serviceName, boolean serviceDirection ) {
        return dataTimeToString( timeOfNextJourney( serviceName, serviceDirection ) );
    }

    // Returns the destination of the next journey - should be used if isActive() == false
    public String nextDestination( String serviceName, boolean serviceDirection ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null ) {
            int next = service.nextJourney( tD, tD, false );
            String nextJourneyPatternRef = service.journeys.journeys.get( next ).journeyPatternRef;
            return service.stdService.journeyPatterns.get( nextJourneyPatternRef ).destinationDisplay;
        }

        return null;
    }

    // Returns the first stop of next journey - should be used if isActive() == false
    public String firstStopOfNextJourney( String serviceName, boolean serviceDirection ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null )
            return tnds.stops.name( service.stopRef( service.nextJourney( tD, tD, false ), 1 ) );

        return null;
    }

    // Returns an ArrayList containing every journey time (as a String) for a given service
    public ArrayList <String> journeyTimes( String serviceName, boolean serviceDirection ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null ) {
            ArrayList <String> times = new ArrayList <String> ();

            Iterator journeys = service.journeys.journeys.keySet().iterator();
            while( journeys.hasNext() ) {
                dataTime dT = new dataTime( (int) journeys.next() );
                times.add( dataTimeToString( dT ) );
            }
            return times;
        }

        return null;
    }

    // Private method for converting a string time (from journeyTimes) to an integer key
    private int strJourneyTimeToInt( String serviceName, boolean serviceDirection, String timeStr ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null ) {
            Iterator journeys = service.journeys.journeys.keySet().iterator();
            while( journeys.hasNext() ) {
                int timeInt = (int) journeys.next();
                dataTime dT = new dataTime( timeInt );
                if( dataTimeToString( dT ).equals( timeStr ) )
                  return timeInt;
            }
        }

        return 0;
    }

    // Returns the total number of timetable entries (stops) for a given service and time
    public int timetableEntries( String serviceName, boolean serviceDirection, String timeStr ) {
        return timetableEntriesInt( serviceName, serviceDirection, strJourneyTimeToInt( serviceName, serviceDirection, timeStr ) );
    }

    // Private method for getting the total number of timetable entries (stops) for a given service and integer journey time
    public int timetableEntriesInt( String serviceName, boolean serviceDirection, int journey ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null && journey != 0 ) {
            return service.numberOfStops( journey );
        }
        
        return 0;
    }



    // Returns timetable entry <entryNo> for a given service and time
    // First stop is entryNo = 0
    public TimetableEntry getTimetableEntry( String serviceName, boolean serviceDirection, String timeStr, int entryNo ) {
        return getTimetableEntryInt( serviceName, serviceDirection, strJourneyTimeToInt( serviceName, serviceDirection, timeStr ), entryNo );
    }

    // Private method for getting a timetable entry using next journey integer time
    private TimetableEntry getTimetableEntryInt( String serviceName, boolean serviceDirection, int time, int entryNo ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null ) {
            if( entryNo >= 0 && entryNo < service.numberOfStops( time ) ) {
                String stopRef  = service.stopRef( time, entryNo + 1 );
                String stopName = tnds.stops.name( stopRef );
                dataTime sT     = new dataTime( service.timeToStopRef( tD, time, stopRef, false ) );
                return new TimetableEntry( stopName, dataTimeToString( sT ) );
            }
        }

        return null;
    }

    // Returns all timetable entries for a given service and time
    public ArrayList <TimetableEntry> getTimetable( String serviceName, boolean serviceDirection, String timeStr ) {
        return getTimetableInt( serviceName, serviceDirection, strJourneyTimeToInt( serviceName, serviceDirection, timeStr ) );
    }

    // Private method for getting a timetable using next journey integer time
    private ArrayList <TimetableEntry> getTimetableInt( String serviceName, boolean serviceDirection, int time ) {
        int entries = timetableEntriesInt( serviceName, serviceDirection, time );
        ArrayList <TimetableEntry> timetable = new ArrayList <TimetableEntry> ();
        for( int i = 0; i < entries; i++ )
          timetable.add( getTimetableEntryInt( serviceName, serviceDirection, time, i ) );

        return timetable;
    }

    // Returns the timetable entries for the next instance of a given service
    public ArrayList <TimetableEntry> getNextTimetable( String serviceName, boolean serviceDirection ) {
        // Get the current time
        tD.setCurrent();
        // Get the service instance
        dataService service = tnds.services.get( serviceName, serviceDirection );
        // Does the service exist?
        if( service != null )
            return getTimetableInt( serviceName, serviceDirection, service.nextJourney( tD, tD, false ) );

        return null;
    }


    /* UPDATE RELATED METHODS */

    // Instantiate and start a single siriUpdate thread
    private void instantiateUpdateThread( String serviceName, boolean serviceDirection ) {
        // First, create a siriUpdate instance

        // Only use the single update method, so that we can avoid unnecessary Traveline API hits
        siriUpdate sU = new siriUpdate( tnds, tnds.services.get( serviceName, serviceDirection ), siriUpdate.SINGLE );
        // Do we have an existing siriUpdates instance?
        siriUpdates sUs = updates.get( serviceName );
        // If not, create one
        if( sUs == null )
            sUs = new siriUpdates();
        // So now, we have a valid siriUpdates handle
        sUs.add( serviceDirection, sU );
        // Update the updates HashMap
        updates.put( serviceName, sUs );
if( DEBUG )        
        Log.i( TAG, "[instantiateUpdateThread] Created siriUpdate instance " + sU );

        // Secondly, create a thread for this siriUpdate instance

        Thread sUThread = new Thread( sU );
        // Start the thread
        sUThread.start();
        // Store a reference to it in the threads HashMap (using a numeric reference, for this work in progress)
        updateThreads.put( updateThreads.size(), sUThread );
if( DEBUG )
        Log.i( TAG, "[instantiateUpdateThread] Started siriUpdate thread " + sUThread );
    }

    // Instantiate and start all siriUpdate threads
    public void startUpdates() {
        // Instantiate update instances for all known service
        for( int i = 0; i < serviceNames.length; i++ ) {
if( DEBUG )
            Log.i( TAG, "[startUpdates] Adding inbound/outbound update threads for service " + serviceNames[ i ] );
            instantiateUpdateThread( serviceNames[ i ], INBOUND );
            instantiateUpdateThread( serviceNames[ i ], OUTBOUND );
        }
    }

    // Performs a single update check for all instantiated siriUpdate objects
    public void checkAllUpdates() {
        // Step through all siriUpdate instances
        Iterator updateServices = updates.keySet().iterator();
        while( updateServices.hasNext() ) {
            String serviceName = (String) updateServices.next();
            siriUpdates sUs = updates.get( serviceName );
            if( sUs != null ) {
if( DEBUG )
                Log.i( TAG, "[checkAllUpdates] Got inbound and/or outbound for service " + serviceName );
                checkUpdate( sUs, serviceName, INBOUND );
                checkUpdate( sUs, serviceName, OUTBOUND );
            }
            else {
if( DEBUG )            
                Log.i( TAG, "[checkAllUpdates] No registered siriUpdates for service " + serviceName );
            }
        }
if( DEBUG )
        Log.i( TAG, "[checkAllUpdates] Done." );
    }

    private void checkUpdate( siriUpdates sUs, String serviceName, boolean serviceDirection ) {
        siriUpdate sU = sUs.get( serviceDirection );
        // If we have a siriUpdate, work with it
        if( sU != null ) {
if( DEBUG )
            Log.i( TAG, "[checkUpdate] Checking service " + serviceName + " (inbound? " + serviceDirection + ")..." );

            dataService service = tnds.services.get( serviceName, serviceDirection );
            tD.setCurrent();
            int activeJourney = service.activeJourney( tD, tD, true );
            // If we have an active journey, check for updates to it
            if( activeJourney != service.NOT_FOUND ) {
if( DEBUG )
                Log.i( TAG, "[checkUpdate] Active service found for " + serviceName + " (inbound? " + serviceDirection + ") - checking..." );

                String stopRefTo          = service.activeStopRefTo( tD, tD, activeJourney, true );
                String journeyPatternRef  = service.journeys.journeys.get( activeJourney ).journeyPatternRef;
                String destinationDisplay = service.stdService.journeyPatterns.get( journeyPatternRef ).destinationDisplay;
                // Now we have the information we need to perform the update check, do so
                sU.update( tD, tD, serviceName, destinationDisplay, activeJourney, stopRefTo );
            }
            // If not, do nothing
            else {
if( DEBUG )
                Log.i( TAG, "[checkUpdate] No active service for " + serviceName + " (inbound? " + serviceDirection + ")..." );
            }
        }
        // If not, do nothing
        else {
if( DEBUG )        
            Log.i( TAG, "[checkUpdate] No siriUpdate for service " + serviceName + " (inbound? " + serviceDirection + ")..." );
        }
    }

    public boolean hasLiveTimes( String serviceName, boolean serviceDirection ) {
        dataService service = tnds.services.get( serviceName, serviceDirection );
        if( service != null ) {
if( DEBUG )        
            Log.i( TAG, "[hasLiveTime] Checking service " + serviceName + " (inbound? " + serviceDirection + ") for any live times..." );
            tD.setCurrent();
            int activeJourney = service.activeJourney( tD, tD, true );
            if( activeJourney != service.NOT_FOUND ) {
                // Step through each link and check it
                int linkNo = 1;
                dataPatternLink dPL;
                do {
                    dPL = service.linkNo( activeJourney, linkNo );
                    if( dPL != null ) {
                        // If we have a live time, we can signal this and quit the method
                        if( dPL.hasLiveTime( activeJourney ) ) {
if( DEBUG )
                            Log.i( TAG, "[hasLiveTime] Live times found for " + serviceName + " (inbound? " + serviceDirection + ")" );
                            return true;
                        }
                    }
                    linkNo ++;
                } while( dPL != null );

                // If we're here, no live time was found
if( DEBUG )
                Log.i( TAG, "[hasLiveTime] No live times found for " + serviceName + " (inbound? " + serviceDirection + ")" );
            }
            else {
if( DEBUG )
                Log.i( TAG, "[hasLiveTime] No active service found for " + serviceName + " (inbound? " + serviceDirection + ")" );
            }
        }
        else {
if( DEBUG )        
            Log.i( TAG, "[hasLiveTime] No match for service " + serviceName + " (inbound? " + serviceDirection + ") found" );
        }

        // If we're here, no live times are currently available
        return false;
    }
}
