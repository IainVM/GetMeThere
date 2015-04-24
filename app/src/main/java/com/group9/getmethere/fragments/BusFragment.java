package com.group9.getmethere.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.group9.getmethere.MainActivity;
import com.group9.getmethere.R;
import com.group9.getmethere.backend.backendAPI;
import com.group9.getmethere.backend.dataTime;
// Map-related backend imports
import com.group9.getmethere.backend.dataTimeDate;
import com.group9.getmethere.backend.dataLine;
import com.group9.getmethere.backend.dataLines;
import com.group9.getmethere.backend.dataPoint;
import com.group9.getmethere.backend.dataStop;
import com.group9.getmethere.backend.dataPatternLink;
import com.group9.getmethere.backend.tndsParse;
import com.group9.getmethere.backend.pointCalculate;
import com.group9.getmethere.backend.dataService;
import com.group9.getmethere.backend.siriUpdate;
import com.group9.getmethere.backend.pathRequest;
import com.group9.getmethere.backend.dataPolyLine;

// Map-related imports
import com.mapquest.android.maps.AnnotationView;
import com.mapquest.android.maps.DefaultItemizedOverlay;
import com.mapquest.android.maps.DrawableOverlay;
import com.mapquest.android.maps.GeoPoint;
import com.mapquest.android.maps.ItemizedOverlay;
import com.mapquest.android.maps.LineOverlay;
import com.mapquest.android.maps.MapActivity;
import com.mapquest.android.maps.MapView;
import com.mapquest.android.maps.Overlay;
import com.mapquest.android.maps.OverlayItem;
//
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.graphics.Color;
//
import android.content.res.AssetManager;
//
import android.graphics.Paint;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
//
import java.io.IOException;
//

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.group9.getmethere.fragments.NewsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link com.group9.getmethere.fragments.NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BusFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private backendAPI.Bus bus;

    private String time = "-";
    private String serviceName = null;
    private boolean serviceDirection = false;

    updateLoop uL = null;

    // Log
    private static final String TAG = "GetMeThere [BusFragment] ";
    //

    // Debug / testing
    private static final boolean DEBUG = false;
    private static final boolean TESTING_ONLY = false;    // SET TO TRUE IF YOU WANT TO FORCE MAP TIME TO AN ACTIVE PERIOD
    //

    // Enable update system?
    //

    private ToggleButton NewsListView;
    private View rootView;

    // Backend-related members
    private boolean mapReady = false;
    private backendAPI bAPI = null;
    private tndsParse tnds;
    private dataService service;
    private int selectedJourney;
    private static final int SLEEP_PERIOD = 1000;   // ms
    private siriUpdate siri = null;
    private Thread siriThread;
    private dataTimeDate tD = new dataTimeDate();
    private pointCalculate pCalc = new pointCalculate();
    private String reversedServices[] = { "27", "27o", "61o" };   // Testing only - these are services that appear reversed
    //
    private dataPoint busPos;
    private volatile boolean trackBus = true;
    private boolean live = false;
//    private mainLoop mL = null;
//    private Thread mLThread;
    //
    AssetManager assets;
    // Strings to be used for info display inside and outside of mainLoop (I think!)
    String stopRefTo, journeyPatternRef, destinationDisplay;
    //

    // MapQuest related members
    private MapView map;
    private AnnotationView annotation;
    private DefaultItemizedOverlay poiOverlay;
    private Paint paint;
    private LineOverlay lineOverlay;
    private List lineData;
    private static final String LINE_OVERLAY = "LINE_OVERLAY";
    private static final String BUS_OVERLAY  = "BUS_OVERLAY";
    //  Map-related HashMaps
    private HashMap < String, OverlayItem > stopPOI;
    private HashMap <String, Integer> routeCodes;
    //

    public static BusFragment newInstance(int sectionNumber) {
        BusFragment fragment = new BusFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        return fragment;

    }

    public BusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_bus, container, false);

        Intent i = getActivity().getIntent();

        mapReady = false;

        // TESTING - ensure no updates can happen when testing
        if( TESTING_ONLY == true )    MainActivity.UPDATES_ENABLED = false;
        //

        // Instantiate the backend
        bAPI = ((MainActivity)this.getActivity()).backEnd();
        // Grab a handle to the AssetManager
        assets = ((MainActivity)this.getActivity()).assetsHandle();

        bus = (backendAPI.Bus) i.getSerializableExtra("bus");

        this.serviceName = bus.name;
        this.serviceDirection = bus.direction;

        populateInfo(rootView);
        eventHandle(rootView);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume(){
        super.onResume();

        // Instantiate the backend
        bAPI = ((MainActivity)this.getActivity()).backEnd();
        //

        // Backend related - start a thread to handle updating of the ListView
        uL = new updateLoop();
        Thread updateThread = new Thread(uL);
        updateThread.start();
        //
        
        Log.i( TAG, "[onResume] >>>>>>>>>> calling initMap()... <<<<<<<<<< " );
        initMap();
    }

    @Override
    public void onPause(){
        super.onPause();

        Log.i( TAG, "[onPause] >>>>>>>>>>> MAP THREAD SHOULD END! <<<<<<<<< " );

        // Stop any active update object
        if( siri != null ) {
          siri.suspend();
          siri = null;
        }

        // updateLoop kill code
        Log.i( TAG, "[onDetach] Trying to kill updateLoop..." );
        if( uL != null ) {
            uL.kill();
            Log.i( TAG, "[onDetach] Success." );
        }
        else
            Log.e( TAG, "[onDetach] ERROR: Attempted to kill non-existent updateLoop!" );
        //

    }

    // Map initialisation code
    public void initMap() {
        // Grab a handle to the tndsParse object
        tnds = bAPI.tnds;
        // Grab a service
        Log.i( TAG, "[initMap] **** GETTING SERVICE " + serviceName + " DIRECTION " + serviceDirection + " ****" );
        service = tnds.services.get( serviceName, serviceDirection );
        // This MUST be checked!
        if( service == null )
          Log.e( TAG, "[initMap] ERROR: NO SERVICE FOUND (" + serviceName + ")" );
        else {
          // MapQuest initialisation - set the zoom level, center point and enable the default zoom controls
          map = (MapView) getActivity().findViewById(R.id.map);
          map.getController().setZoom( 14 );
          map.getController().setCenter( new GeoPoint( (double) 51.4833, (double) -3.1833 ) );
          map.setBuiltInZoomControls( true );
          // Initialise the map annotation view
          annotation = new AnnotationView( map );
          // Initialise the paint object (for line drawing)
          setupPaintLines();
          // Set up a HashMap for the BusCMS route codes (hard-coded)
          routeCodes = new HashMap <String, Integer> ();
          routeCodes.put( "54", 3431 ); routeCodes.put( "54" + tnds.services.OUTBOUND_ID, 3432 );
          routeCodes.put( "27", 1161 ); routeCodes.put( "27" + tnds.services.OUTBOUND_ID, 3073 );
          routeCodes.put( "61", 1151 ); routeCodes.put( "61" + tnds.services.OUTBOUND_ID, 2988 );

          /* PolyLine Route Setup Code */
          // (Possible memory 'leak' - haven't checked, but it's possible multiple calls to this for the same service
          //  might be adding to the polyline for each Link section unnecessarily - checking this is a future TODO)
          // Get a route from BusCMS (if we have a matching routeCode)
          int j = service.NOT_FOUND;
          if( routeCodes.containsKey( tnds.services.getKey( serviceName, serviceDirection ) ) ) {
            pathRequest pR = new pathRequest();
            dataPolyLine path = pR.getPath( routeCodes.get( tnds.services.getKey( serviceName, serviceDirection ) ) );
            Iterator jSet = service.journeys.journeys.keySet().iterator();
            while( jSet.hasNext() ) {
              j = (Integer) jSet.next();
              boolean success = pR.matchToJourney( path, tnds, service, j );  // HARDWIRED FOR TESTING
            }
          }
          // Otherwise, get the first available journey time to pass to addStops()
          else {
            j = (Integer) service.journeys.journeys.keySet().iterator().next();
          }

          // Start the SIRI-SM update class in MULTI mode for this service, if necessary
          if( siri == null ) {
            siri = new siriUpdate( tnds, service, siriUpdate.MULTI );
            siriThread = new Thread( siri );
            if( MainActivity.UPDATES_ENABLED )
              siriThread.start();
          }

          // Reset any existing lines and marker
          map.removeOverlayByKey( LINE_OVERLAY );
          map.removeOverlayByKey( BUS_OVERLAY );

          // Plot stops on map
          clearStops();
          addStops( j );  // HARDWIRED FOR TESTING

          mapReady = true;
        }
    }

    // Listview updating code - run as thread from within populateBuses()
    public class updateLoop implements Runnable {

        private volatile boolean active = true;

        public void kill() {
            active = false;
        }

        public void run() {
            while( active ) {
                Log.i( TAG, "[updateLoop] Running" );

                if( bAPI != null ) {
                    if (bAPI.isReady()) {
                        Log.i(TAG, "[updateLoop] Updating data from backendAPI..." );

                        //TODO: change out the adapter to use Bus Array Lists
                        int numSvcs = bAPI.services();
                        dataTime busTime = bAPI.timeOfArrivalDelay( serviceName, serviceDirection );

                        if( busTime!= null) {
                            time = Integer.toString(busTime.minutes);
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if( mapReady ) {
                                    Log.i( TAG, "[updateLoop] updating map..." );
                                    updateMap(rootView);
                                }
                                populateInfo(rootView);

                            }
                        });
                        Log.i(TAG, "[updateLoop] Done!");
                    }

                    try {
                        Thread.currentThread().sleep( SLEEP_PERIOD );   // THIS CONTROLS THE UPDATE FREQUENCY
                    } catch (InterruptedException e) {
                        Log.e(TAG, "[updateLoop] Interrupted Exception " + e);
                    }
                }
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    public void updateMap(View rootView){
        //TODO:Method for updating map
        //serviceName is a instance variable
        //serviceDirection is an instance variable

        // Get the current time
        tD.setCurrent();

        // TESTING BODGE BLOCK: Forces map system to a time when (most) services are running
        if( TESTING_ONLY == true ) {
          MainActivity.UPDATES_ENABLED = false;
          Log.i( TAG, "[updateMap] TEST MODE ENABLED. Time forced within map code." );
          tD.setDate( "2015-04-17" );
          tD.setTime( tD.time() - (60*3) ); 
        }
        // END TESTING BODGE BLOCK

        // Select the currently active journey
        selectedJourney = service.activeJourney( tD, tD, true );
        // ACTIVATE BELOW CODE FOR VERBOSE LOGGING
        Log.i( TAG, "Time " + tD.hour() + ":" + tD.minute() + ":" + tD.second() + " | Selected journey: " + selectedJourney );

        // If we have one, check the NextBuses system, and plot it on the map
        if( selectedJourney != service.NOT_FOUND ) {

            // TESTING OUTPUT BLOCK //
            String stopRefFromTest = service.activeStopRefFrom( tD, tD, selectedJourney, true );
            String stopRefToTest   = service.activeStopRefTo(   tD, tD, selectedJourney, true );
            String stopNameFrom = tnds.stops.name( stopRefFromTest );
            String stopNameTo   = tnds.stops.name( stopRefToTest );
            float progress = service.activeLinkProgress( tD, tD, selectedJourney, true );
            int progPercent = (int) ( progress * 100 );
            Log.i( TAG, "Time " + tD.hour() + ":" + tD.minute() + ":" + tD.second() + " | Service " + serviceName + "-" + selectedJourney + " | From " + stopNameFrom + " to " + stopNameTo + " | Progress " + progPercent + "%" );
            // TESTING OUTPUT BLOCK ENDS //

            // Check for service updates
            stopRefTo          = service.activeStopRefTo( tD, tD, selectedJourney, true );
            journeyPatternRef  = service.journeys.journeys.get( selectedJourney ).journeyPatternRef;
            destinationDisplay = service.stdService.journeyPatterns.get( journeyPatternRef ).destinationDisplay;
            if( siri != null )
                siri.update( tD, tD, serviceName, destinationDisplay, selectedJourney, stopRefTo );
            // Check for active live links
            live = service.activeLink( tD, tD, selectedJourney, true ).hasLiveTime( selectedJourney );
//ifdef android		    
/*            // Perform the UI updates
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//endif android	    */
            plotAllRouteLinkProgress( tD, selectedJourney );
/*//ifdef android		    
                    // Show the status of this link's times
                    if( live == true )
                        statusIndicator.setText( "Selected service:\n[Link: *LIVE*]" );
                    else
                        statusIndicator.setText( "Selected service:\n[Link: Scheduled]" );

                    // Show origin and destination information
                    originIndicator.setText(      service.stdService.journeyOrigin( journeyPatternRef ) );
                    destinationIndicator.setText( destinationDisplay );

                    // Show next stop information
                    nextStopIndicator.setText(    tnds.stops.name( stopRefTo ) );
                    int remaining = service.timeToStopRef( tD, selectedJourney, stopRefTo, true ) - tD.time();
                    if( remaining < 0 ) remaining = 0;
                    dataTime dT = new dataTime( remaining );
                    arrivalTimeLabel.setText( "in" );
                    arrivalTimeIndicator.setText( String.format( "%02d:%02d:%02d", dT.hours, dT.minutes, dT.seconds ) );
                }
            });
//endif android	*/	    
        }
        else {
            // If we don't have a journey, turn off the update system
            //  ToDo: this call only needs to be made once - but siri handle needed if new journey activate
            siri.suspend();
/*//ifdef android		    
            // And don't show any state in the indicator
            //  Perform the UI updates
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    statusIndicator.setText( "Next " + serviceName + " service:" );
                    // Get the next service
                    int nextJourney = service.nextJourney( tD, tD, false );
                    String nextJourneyPatternRef = service.journeys.journeys.get( nextJourney ).journeyPatternRef;
                    originIndicator.setText( service.stdService.journeyOrigin( nextJourneyPatternRef ) );
                    destinationIndicator.setText( service.stdService.journeyPatterns.get( nextJourneyPatternRef ).destinationDisplay );
                    // Show next stop information
//                    String nextStopRefTo = service.activeStopRefTo( tD, tD, nextJourney, false ); 
                    nextStopIndicator.setText( tnds.stops.name( service.stopRef( nextJourney, 1 ) ) );
                    dataTime dT = new dataTime( nextJourney );
                    arrivalTimeLabel.setText( "at" );
                    arrivalTimeIndicator.setText( String.format( "%02d:%02d:%02d", dT.hours, dT.minutes, dT.seconds ) );
                }
            });*/
        }
    }

    public void populateInfo(View rootView){

        dataTime busTime = bAPI.timeOfArrivalDelay( serviceName, serviceDirection );

        if( busTime!= null) {
            time = Integer.toString(busTime.minutes);
        }

        String service = this.bus.name;
        boolean dir = this.bus.direction;
        String to = this.bus.to;
        String from = this.bus.from;

        String prevStop = bAPI.previousStop( service, dir );
        String nextStop = bAPI.nextStop( service, dir );
        dataTime arrival  = bAPI.timeOfArrival( service, dir );
        dataTime current = bAPI.dtCurrentTime();

        String timeTilNextArrive = bAPI.dataTimeDifference(arrival, current);



        TextView busName = (TextView) rootView.findViewById(R.id.busName);
        busName.setText(service);

        TextView busTo = (TextView) rootView.findViewById(R.id.busTo);
        busTo.setText(to);

        TextView busFrom = (TextView) rootView.findViewById(R.id.busFrom);
        busFrom.setText(from);

        TextView busDelayTime = (TextView) rootView.findViewById(R.id.busDelayTime);
        busDelayTime.setText(time);


        TextView busCurrentStop = (TextView) rootView.findViewById(R.id.busCurrent);
        busCurrentStop.setText(prevStop);

        TextView busNextStop = (TextView) rootView.findViewById(R.id.busNext);
        busNextStop.setText(nextStop);

        TextView busNextTime = (TextView) rootView.findViewById(R.id.busNextTime);
        busNextTime.setText(timeTilNextArrive);

    }

    /* MAP-SPECIFIC METHODS */

    // Plot stops on the map as POIs
    private void addStops( int journey ) {
        boolean viewSet = false;
        // Allocate the hashmap
        stopPOI = new HashMap < String, OverlayItem >();

        // Create the itemized overlay
        try {
            Drawable icon = Drawable.createFromStream( assets.open( "location_marker.png" ), null);
            // TEST
            BitmapDrawable bmd = new BitmapDrawable( ((BitmapDrawable) icon).getBitmap() );
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics( metrics );
            bmd.setTargetDensity( metrics );
            //
            poiOverlay = new DefaultItemizedOverlay( bmd );

            // Step through each stop for this service and journey (testing only)
            String stopKey;
            int stopNo = 1;
            do {
                stopKey = service.stopRef( journey, stopNo );
                if( stopKey != null ) {
                    dataStop stop = tnds.stops.get( stopKey );
                    stopPOI.put( stopKey, new OverlayItem( new GeoPoint( stop.getLat(), stop.getLon() ), stop.stopName, serviceName ) );
                    poiOverlay.addItem( stopPOI.get( stopKey ) );
                    // Centre the map on the first stop
                    if( stopNo == 1 ) {
                        map.getController().setCenter( new GeoPoint( stop.getLat(), stop.getLon() ) );
                        viewSet = true;
                    }

                    stopNo ++;
                }
            } while( stopKey != null );

            // Set an interaction handler
            poiOverlay.setTapListener( new tapListener() );
            // Activate the overlay
            map.getOverlays().add(poiOverlay);
        }
        catch( IOException e ) {
            Log.e( TAG, "[addStops] IOException: " + e );
        }
    }
   
    private void clearStops() {
      if( poiOverlay != null )
        poiOverlay.clear();
    }

    // POI Interaction handler
    public class tapListener implements ItemizedOverlay.OverlayTapListener {

        @Override
        public void onTap( GeoPoint pt, MapView mapView ) {
            // when tapped, show the annotation for the overlayItem
            int lastTouchedIndex = poiOverlay.getLastFocusedIndex();
            if (lastTouchedIndex > -1) {
                OverlayItem tapped = poiOverlay.getItem(lastTouchedIndex);
                annotation.showAnnotationView(tapped);
            }
        }
    }

    // Plot progress of all route links
    private void plotAllRouteLinkProgress( dataTimeDate tD, int chosenJourney ) {
        boolean success = true, draw = false;
        int link = 1, linkEnd = service.activeLinkNo( tD, tD, chosenJourney, true );

//ifdef android		
        // Remove the old layer (NECESSARY??)
        map.removeOverlayByKey( LINE_OVERLAY );
        // Reset it
        resetLines();
//endif android	

        while( link <= linkEnd && success ) {

            // Get the nth patternLink
            dataPatternLink dPL = service.patternLink( chosenJourney, link );
            // If we have one, act on it
            if( dPL != null ) {
                success = plotRouteLinkProgress( tD, chosenJourney, dPL.getFrom(), dPL.getTo() );
                if( success )
                    draw = true;
            }
            else
                success = false;

            link++;
        };

//ifdef android
        // Render the lines (and bus), if anything happened
        if( draw ) {
            renderLines();
            plotBus();
        }
//endif android	
    }

    // Plot progress of a single route link
    //  Returns false if no progress was plotted
    private boolean plotRouteLinkProgress( dataTimeDate tD, int chosenJourney, String stopFrom, String stopTo ) {
        // Find the progress between the two stops at time <time> for <chosenJourney>
        //  (using live times here, assuming map is to display all live data)
        double progress = service.linkProgress( tD, tD, chosenJourney, stopFrom, stopTo, true );

        // Only plot if we have some progress to show
        if( progress > 0f ) {
            /* POLYLINE PLOTTER */
            // Get the active pattern link
            dataPatternLink dPL = service.linkWithStopRefFrom( chosenJourney, stopFrom );
            // If we have a pattern section, and it has polyline data...
            boolean usingPolyPlotter = false;
            if( dPL != null ) {
                if( dPL.hasLine() ) {
                    // ...use it!
                    usingPolyPlotter = true;
//                    Log.i( TAG, "[plotRouteLinkProgress] Using PolyPlotter" );

                    // Get the last active polyline section index
                    int maxIndex = dPL.progressIndex( progress );
                    // Get the progress of the last active polyline section
                    double progressSect = dPL.progressLine( maxIndex, progress );
                    // REVERSED SERVICES BODGE
                    boolean dir = serviceDirection;
                    if( isReversed( serviceName, serviceDirection ) )
                      if( dir == tnds.services.INBOUND )  dir = tnds.services.OUTBOUND;
                      else                                dir = tnds.services.INBOUND;
                    // TEST BODGE
                    if( dir == tnds.services.OUTBOUND )
                      progressSect = 1 - progressSect;

                    // Step through each section of the polyline
                    for( int i = 0; i < maxIndex; i++ ) {
//                        System.out.format( "Getting %d\n", i );
                        // Get its line
                        dataLine dL = dPL.polyLineSection( i );
//ifdef android            
                        // Draw it!
                        addLine( dL );
//endif android
                    }
                    // Draw the last line using pointCalculate
                    dataLine dL = dPL.polyLineSection( maxIndex );
                    dataPoint toProgress = pCalc.calcPoint( dL.from, dL.to, progressSect );
//ifdef android
                    // TEST BODGE
                    if( dir == tnds.services.OUTBOUND )     // Without 'REVERSED SERVICES', 'dir' should be 'serviceDirection'
                      addLine( dL.to, toProgress );
                    else
                      addLine( dL.from, toProgress );
//endif android

                    // Mark the bus for plotting
                    busPos = toProgress;

                    /* POLYLINE PLOTTER END */
                }
//                else
//                    Log.i( TAG, "[plotRouteLinkProgress] Couldn't use PolyPlotter (dataPatternLink.hasLine() == false)" );
            }
//            else
//                Log.i( TAG, "[plotRouteLinkProgress] Couldn't use PolyPlotter (dataPatternLink == null)" );

            // If we weren't using the polyline plotter, use the stop-to-stop one
            if( !usingPolyPlotter ) {
//                Log.i( TAG, "[plotRouteLinkProgress] Using Straight-LinePlotter" );
                /* STRAIGHT LINE, STOP-TO-STOP PLOTTER */
                // This needs to be re-enabled, for the case where no polyline route is available
                // Set the two points from the given stopRefs
                dataPoint from = new dataPoint( tnds.stops.get( stopFrom ).getLat(),
                                                tnds.stops.get( stopFrom ).getLon() );
                dataPoint to   = new dataPoint( tnds.stops.get( stopTo   ).getLat(),
                                                tnds.stops.get( stopTo   ).getLon() );
                // Get the progress-based line end point
                dataPoint toProgress = pCalc.calcPoint( from, to, progress );

//ifdef android
                // Plot the line
                addLine(from, toProgress);
//endif android	    

//              // Plot the bus
                busPos = toProgress;
                /* STRAIGHT LINE, STOP-TO-STOP PLOTTER END */
            }

            // We made a successful plot
            return true;
        }

        // No plot was made
        return false;
    }

//ifdef android
    // Setup paint object and line overlay
    private void setupPaintLines() {
        // Set custom line style
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        // Line overlay initial setup
        resetLines();
    }

    private void resetLines() {
        // (Re)set line overlay
        lineOverlay = new LineOverlay( paint );
        lineOverlay.setKey( LINE_OVERLAY );
        // And line data list
        lineData = new ArrayList();
    }

    public void plotLines( dataLines dLs ) {
        Iterator lines = dLs.keySet().iterator();

        while( lines.hasNext() ) {
            addLine((dataLine) lines.next());
        }
    }

    // Plot the bus marker on a map
    public void plotBus() {
        try {
            Drawable icon = Drawable.createFromStream( assets.open( "bus_marker.png" ), null );
            // TEST
            BitmapDrawable bmd = new BitmapDrawable( ((BitmapDrawable) icon).getBitmap() );
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics( metrics );
            bmd.setTargetDensity( metrics );
            //
            DefaultItemizedOverlay busOverlay = new DefaultItemizedOverlay( bmd );
            busOverlay.setKey( BUS_OVERLAY );
            busOverlay.addItem( new OverlayItem( new GeoPoint( busPos.lat, busPos.lon ), serviceName, null ) );
            // Centre the map on the bus marker, if requested
            if( trackBus )
              map.getController().setCenter( new GeoPoint( busPos.lat, busPos.lon ) );

            // Activate the overlay
            map.getOverlays().add( busOverlay );
        }
        catch( IOException e ) {
            Log.e( TAG, "[plotBus] IOException: " + e );
        }
    }

    // Plot a line on a map (using a dataLine )
    public void addLine( dataLine dL ) {
        addLine(dL.from, dL.to);
    }

    // Plot a line on the map (using two dataPoints )
    public void addLine( dataPoint from, dataPoint to ) {
        // Add the points to the current lineData list
        lineData.add( new GeoPoint( from.lat, from.lon ) );
        lineData.add( new GeoPoint( to.lat,   to.lon   ) );
    }

    public void renderLines() {
        // Set lineData for the overlay
        lineOverlay.setData( lineData );
        // Plot the overlay on the map
        map.getOverlays().add( lineOverlay );
    }
//endif android

    // Testing only - checks to see if direction of this service is reversed
    private boolean isReversed( String name, boolean direction ) {
      if( name != null ) 
        for( String service: reversedServices )
          if( service.equals( tnds.services.getKey( name, direction ) ) )
            return true;
      return false;
    }

    public void eventHandle(View rootView){

        ToggleButton b = (ToggleButton) rootView.findViewById(R.id.toggleButton);

        // attach an OnClickListener
        b.setOnClickListener(new ToggleButton.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                trackBus = !trackBus;
            }
        });
    }

}
