package com.group9.getmethere.backend;

import android.util.Log;

import android.content.res.AssetManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Iterator;

//ifdef android
import android.os.AsyncTask;
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

    public class Bus {
        public String name;
        public String from;
        public String to;
        public boolean direction;  // Necessary so this bus can be referenced (i.e. 54 is both outbound AND inbound!)

        public Bus( String n, String f, String t, boolean d ) {
            name = n; from = f; to = t; direction = d;
        }
    }

    String serviceNames[] = { "10", "27", "54", "61" };

    private AssetManager assets;
    private tndsParse tnds = new tndsParse();
    private dataTimeDate tD = new dataTimeDate();

    public ArrayList <Bus> busses;
    public final boolean INBOUND   = tnds.services.INBOUND;
    public final boolean OUTBOUND  = tnds.services.OUTBOUND;

    // Constructor: parses the TNDS and stores the data
    public backendAPI( AssetManager aM ) {
        updates = new HashMap < String, siriUpdates > ();
        updateThreads = new HashMap < Integer, Thread > ();
        assets = aM;

        // Create an empty busses array
        busses = new ArrayList <Bus> ();

        // CALL PARSERS

//ifdef android
        new initialise().execute();
//endif android

/*ifdef pc
        // Parse all known services
        for( int i = 0; i < serviceNames.length; i++ )
            tnds.parse( assets, serviceNames[ i ] );

        // Parse NaPTAN data
        naptanParse naptan = new naptanParse( assets, tnds.stops, "NaPTAN_571-541.xml" );

        // Fill the busses array
        for( int i = 0; i < serviceNames.length; i++ ) {
            // Inbound services
            dataService sI = tnds.services.get( serviceNames[ i ], INBOUND );
            Bus busI = new Bus( sI.lineName, sI.stdService.origin, sI.stdService.destination, INBOUND );
            busses.add( busI );
            // Outbound services
            dataService sO = tnds.services.get( serviceNames[ i ], OUTBOUND );
            Bus busO = new Bus( sO.lineName, sO.stdService.origin, sO.stdService.destination, OUTBOUND );
            busses.add( busO );
        }

        ready = true;
endif pc*/
    }

//ifdef android
    public class initialise extends AsyncTask <Void, Void, Void> {

      protected Void doInBackground( Void... params ) {
        // BEGIN SERVICE DATA SETUP
        Log.i( TAG, "[initialise] Beginning services setup." );

        // Parse all known services
        for( int i = 0; i < serviceNames.length; i++ ) {
          Log.i( TAG, "[initialise] Adding inbound/outbound for service " + serviceNames[ i ] );
          tnds.parse( assets, serviceNames[ i ] );
        }

        // Fetch stop geolocation data
        Log.i( TAG, "[initialise] Loading NaPTAN data" );
        naptanParse naptan = new naptanParse( assets, tnds.stops, "NaPTAN_571-541.xml" );

        // Fill the busses array
        Log.i( TAG, "[initialise] Filling busses array with obtained data" );
        for( int i = 0; i < serviceNames.length; i++ ) {
            // Inbound services
            dataService sI = tnds.services.get( serviceNames[ i ], INBOUND );
            Bus busI = new Bus( sI.lineName, sI.stdService.origin, sI.stdService.destination, INBOUND );
            busses.add( busI );
            // Outbound services
            dataService sO = tnds.services.get( serviceNames[ i ], OUTBOUND );
            Bus busO = new Bus( sO.lineName, sO.stdService.origin, sO.stdService.destination, OUTBOUND );
            busses.add( busO );
        }

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
        tD.setCurrent();
        return tD.hour() + ":" + tD.minute() + ":" + tD.second();
    }

    // Returns the current time as a dataTime object
    public dataTime dtCurrentTime() {
        tD.setCurrent();
        return new dataTime( tD.time() );
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
            return dT.hours + ":" + dT.minutes + ":" + dT.seconds;

        return null;
    }

    // Returns a String representation of the scheduled time of arrival at the next stop
    public String strTimeOfArrival( String serviceName, boolean serviceDirection ) {
        dataTime dT = timeOfArrival( serviceName, serviceDirection );
        if( dT != null )
            return dT.hours + ":" + dT.minutes + ":" + dT.seconds;

        return null;
    }

    // Returns a String representation of the delay of time of arrival to the next stop
    public String strTimeOfArrivalDelay( String serviceName, boolean serviceDirection ) {
        dataTime dT = timeOfArrivalDelay( serviceName, serviceDirection );
        if( dT != null )
            return dT.hours + ":" + dT.minutes + ":" + dT.seconds;

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
