package com.group9.getmethere;

//ifdef android
import android.app.Activity;
import android.content.Context;
//endif android
import android.content.res.AssetManager;
//ifdef android
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.StrictMode;   // NASTY TEST BODGE
import android.os.AsyncTask;
import android.util.DisplayMetrics;
//endif android
import android.util.Log;
//ifdef android
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.View;
import android.graphics.PorterDuff;
//endif android

//ifdef android
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
//endif android

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity //ifdef android
			extends MapActivity //endif android
					{

    // Debug
    private static boolean DEBUG = false;
    //

    // Logging
    private static final String TAG = "GetMeThere [MainActivity] ";
    //

    // Enable update system?
    private static final boolean UPDATES_ENABLED  = true;
    //

    // Activity / Mapquest related fields
    private AssetManager assets;
//ifdef android
    private MapView map;
    private AnnotationView annotation;
    private DefaultItemizedOverlay poiOverlay;
    private Paint paint;
    private LineOverlay lineOverlay;
    private List lineData;
    private static final String LINE_OVERLAY = "LINE_OVERLAY";
    private static final String BUS_OVERLAY  = "BUS_OVERLAY";
    //

    // Map stops related fields
    HashMap < String, OverlayItem > stopPOI;
    //

    // Context
    private static Context mContext = null;

    // TextViews, Spinners and ToggleButtons
    TextView statusIndicator;
    Spinner spinnerService, spinnerDirection;
    TextView originIndicator;
    TextView destinationIndicator;
    TextView nextStopIndicator;
    TextView arrivalTimeLabel;
    TextView arrivalTimeIndicator;
    ToggleButton bustrackToggle;
    //

    // Views
    View mainView;
    //
//endif android


    //  Back-end related fields
    private static final int SLEEP_PERIOD = 1000;   // ms
    private tndsParse tnds = new tndsParse();
    private String serviceName = null;
    private boolean serviceDirection = false;
    public  String serviceNames[] = { "10", "27", "54", "61" };
    private String reversedServices[] = { "27", "27o", "61o" };   // Testing only - these are services that appear reversed
    private HashMap <String, Integer> routeCodes;
    private String directionNames[] = { "inbound", "outbound" };
    private int selectedJourney;
    private dataService service;
    private dataTimeDate tD = new dataTimeDate();
    private pointCalculate pCalc = new pointCalculate();
    //
    private dataPoint busPos;
    private String busName;
    private final int MARKER_OFFSET = 12;
    //
    private siriUpdate siri = null;
    private Thread siriThread;
    private mainLoop mL;
    private Thread mLThread;
    private boolean live = false;
    private volatile boolean ready = false;
    private volatile boolean trackBus = true;

    String stopRefTo, journeyPatternRef, destinationDisplay;
    //

    // Nasty runOnUiThread message global
    private String message;
    //

//ifdef android
    @Override
//endif android
    public void onCreate( //ifdef android
    			Bundle savedInstanceState //endif android
						) {
//ifdef android
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainView = (View) findViewById( R.id.mainView );

        // Get assets handle
        assets = getAssets();

        // Set the zoom level, center point and enable the default zoom controls
        map = (MapView) findViewById(R.id.map);
        map.getController().setZoom( 14 );
        map.getController().setCenter( new GeoPoint( (double) 51.4833, (double) -3.1833 ) );
        map.setBuiltInZoomControls( true );
        // Initialise the map annotation view
        annotation = new AnnotationView( map );
        // Initialise the paint object (for line drawing)
        setupPaintLines();

        // Get activity's TextView handle(s)
        //  Labels
        TextView originLabel      = (TextView) findViewById( R.id.originLabel );
        originLabel.setText( "Origin:" );
        TextView destinationLabel = (TextView) findViewById( R.id.destinationLabel );
        destinationLabel.setText( "Destination:" );
        TextView nextStopLabel    = (TextView) findViewById( R.id.nextStopLabel );
        nextStopLabel.setText( "Next stop:" );
        arrivalTimeLabel = (TextView) findViewById( R.id.arrivalTimeLabel );
        arrivalTimeLabel.setText( "  " );
        //  Status indicators
        statusIndicator       = (TextView) findViewById( R.id.statusIndicator );
        originIndicator       = (TextView) findViewById( R.id.originIndicator );
        destinationIndicator  = (TextView) findViewById( R.id.destinationIndicator );
        nextStopIndicator     = (TextView) findViewById( R.id.nextStopIndicator );
        arrivalTimeIndicator  = (TextView) findViewById( R.id.arrivalTimeIndicator );

        // Get Spinner handles and set callbacks
        //  Service name spinner
        spinnerService = (Spinner) findViewById( R.id.servicesSpinner );
        spinnerService.getBackground().setColorFilter( getResources().getColor( android.R.color.holo_blue_light ), PorterDuff.Mode.SRC_ATOP );
        ArrayAdapter <String> adapterService = new ArrayAdapter <String> ( this, R.layout.map_spinner, serviceNames );
        adapterService.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        spinnerService.setAdapter( adapterService );
        spinnerListener sListener = new spinnerListener();
        spinnerService.setOnItemSelectedListener( (OnItemSelectedListener) sListener );
        //  Direction spinner
        spinnerDirection = (Spinner) findViewById( R.id.directionSpinner );
        spinnerDirection.getBackground().setColorFilter( getResources().getColor( android.R.color.holo_blue_light ), PorterDuff.Mode.SRC_ATOP );
        ArrayAdapter <String> adapterDirection = new ArrayAdapter <String> ( this, R.layout.map_spinner, directionNames );
        adapterDirection.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        spinnerDirection.setAdapter( adapterDirection );
        spinnerDirection.setOnItemSelectedListener( (OnItemSelectedListener) sListener );

        // Get ToggleButton handles and set callbacks
        bustrackToggle = (ToggleButton) findViewById( R.id.bustrackToggle );
        bustrackToggle.setChecked( trackBus );
//endif android

//ifdef android
        // NASTY TEST BODGE (to allow net access from main activity)
        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy( policy );
//endif android

        // Set up a HashMap for the BusCMS route codes (hard-coded)
        routeCodes = new HashMap <String, Integer> ();
        routeCodes.put( "54", 3431 ); routeCodes.put( "54" + tnds.services.OUTBOUND_ID, 3432 );
        routeCodes.put( "27", 1161 ); routeCodes.put( "27" + tnds.services.OUTBOUND_ID, 3073 );
        routeCodes.put( "61", 1151 ); routeCodes.put( "61" + tnds.services.OUTBOUND_ID, 2988 );

        // CALL PARSERS

//ifdef android
        new initialise().execute();
//endif android

/*ifdef pc
        // Try to load stored state
        Log.i( TAG, "[initialise] Searching for stored TNDS..." );
        Object obj = stateStore.loadState( "tnds" );
        if( obj != null ) {
          tnds = (tndsParse) obj;
          Log.i( TAG, "[initialise] Stored TNDS found and loaded!" );
        }
        else {
          Log.i( TAG, "[initialise] No stored TNDS found. Constructing..." );

          for( int i = 0; i < serviceNames.length; i++ ) {
            Log.i( TAG, "[initialise] Adding inbound/outbound for service " + serviceNames[ i ] );
            tnds.parse( assets, serviceNames[ i ] );
          }

          Log.i( TAG, "[initialise] Adding NaPTAN data" );
          naptanParse naptan = new naptanParse( assets, tnds.stops, "NaPTAN_571-541.xml" );

          // Attempt to store state
          if( stateStore.saveState( "tnds", ( Object ) tnds ) )
            Log.i( TAG, "[initialise] TNDS state stored." );
          else
            Log.e( TAG, "[initialise] Could not store TNDS!" );
        }
endif pc*/

        Log.i( TAG, "[initialise] Done setting up service. Initialising..." );
        // Main loop setup
        mL = new mainLoop();

/*ifdef pc
        serviceName = "61";                         // HARDWIRED FOR TESTING
        serviceDirection = tnds.services.INBOUND;   // HARDWIRED FOR TESTING
        if( initMap() )
          mL.activate();
endif pc*/
    }

//ifdef android
    public void onDestroy() {
        Log.i( TAG, "[onDestroy] Stopping map thread" );

        // Stop the map update thread
        mL.cancel();

        super.onDestroy();
    }

    public class initialise extends AsyncTask <Void, Void, Void> {

      protected Void doInBackground( Void... params ) {
        // BEGIN SERVICE DATA SETUP
        // Try to load stored state
        Log.i( TAG, "[initialise] Searching for stored TNDS..." );
        stateStore.setContext( mContext );
        message = "Please wait.\nLoading setup...";
        showMessage();
        Object obj = stateStore.loadState( "tnds" );
        if( obj != null ) {
          tnds = (tndsParse) obj;
          Log.i( TAG, "[initialise] Stored TNDS found and loaded!" );
        }
        else {
          Log.i( TAG, "[initialise] No stored TNDS found. Constructing..." );

          // Load data for all known services
          for( int i = 0; i < serviceNames.length; i++ ) {
            Log.i( TAG, "[initialise] Adding inbound/outbound for service " + serviceNames[ i ] );
            message = "Please wait.\nLoading " + serviceNames[ i ] + "...";
            showMessage();
            tnds.parse( assets, serviceNames[ i ] );
          }

          // Fetch stop geolocation data
          Log.i( TAG, "[initialise] Loading NaPTAN data" );
          message = "Please wait.\nLoading stops...";
          showMessage();
          naptanParse naptan = new naptanParse( assets, tnds.stops, "NaPTAN_571-541.xml" );

          // Attempt to store state
          message = "Please wait.\nStoring setup...";
          showMessage();
          if( stateStore.saveState( "tnds", ( Object ) tnds ) )
            Log.i( TAG, "[initialise] TNDS state stored." );
          else
            Log.e( TAG, "[initialise] Could not store TNDS!" );
        }

        return null;
      }

      private void showMessage() {
        runOnUiThread( new Runnable() {
          @Override
          public void run() {
            statusIndicator.setText( message );
            mainView.invalidate();
          }
        } );
      }

      protected void onProgressUpdate( Void params ) {
      }

      protected void onPostExecute( Void params ) {
        ready = true;
        message = "Loaded.";
        showMessage();
        // Start the map using defaults
        final int defaultService = 2;
        final boolean defaultDirection = tnds.services.INBOUND;
        serviceName = serviceNames[ defaultService ];
        serviceDirection = defaultDirection;
        trackBus = true;
        // Setup widgets
        runOnUiThread( new Runnable() {
          @Override
          public void run() {
            spinnerService.setSelection( defaultService );
            int dir = 0;
            if( defaultDirection == tnds.services.OUTBOUND )
              dir = 1;
            spinnerDirection.setSelection( dir );
            bustrackToggle.setChecked( trackBus );
          }
        } );
        //
        start();
      }
    }
//endif android

//            // TEST ONLY
//            boolean first = true;
//            int lastLink = 3;
//            dataPatternLink dPL = null;
//            for( int link = 1; link < lastLink; link ++ ) {
//              dPL = service.patternLink( j, link );
//              System.out.format( "Link %d\n", link );
//              if( dPL != null ) {
////                if( first ) {
////                  dataPoint from = tnds.stops.get( dPL.stopRefFrom ).location;
////                  System.out.format( "%f, %f\n", from.lat, from.lon );
////                  first = false;
////                }
//                //
//                if( dPL.hasLine() ) {
//                  int sections = dPL.polyLineSize();
//                  for( int i = 0; i < sections; i++ ) {
//                    dataPoint from = dPL.polyLineSection( i ).from;
//                    dataPoint to = dPL.polyLineSection( i ).to;
//                    System.out.format( "%f, %f\n", from.lat, from.lon );
//                    System.out.format( "%f, %f\n", to.lat, to.lon );
//                  }
//                }
////                else {
////                  dataPoint to   = tnds.stops.get( dPL.stopRefTo   ).location;
////                  System.out.format( "%f, %f\n", to.lat, to.lon );
////                }
////                dataPoint to   = tnds.stops.get( dPL.stopRefTo   ).location;
////                System.out.format( "%f, %f\n", to.lat, to.lon );
////              }
//            }
//            // END TEST
//    }


    // INITIALIZE THE MAP
    private boolean initMap() {
      // Get the new service
      service = tnds.services.get( serviceName, serviceDirection );
      busName = serviceName;
      // This MUST be checked!
      if( service == null ) {
          Log.e( TAG, "[initMap] ERROR: NO SERVICE FOUND (" + serviceName + ")" );
          return false;
      }

      // Get a route from BusCMS (if we have a matching routeCode)
      int j = service.NOT_FOUND;
      if( routeCodes.containsKey( tnds.services.getKey( serviceName, serviceDirection ) ) ) {
        pathRequest pR = new pathRequest();
//        System.out.format( "Getting polyline using %s\n", routeCodes.get( tnds.services.getKey( serviceName, serviceDirection ) ) );
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

      // Start the SIRI-SM update class
      siri = new siriUpdate( tnds, service, siriUpdate.MULTI );
      siriThread = new Thread( siri );
      if( UPDATES_ENABLED )
        siriThread.start();

//ifdef android
      // Reset any existing lines and marker
      map.removeOverlayByKey( LINE_OVERLAY );
      map.removeOverlayByKey( BUS_OVERLAY );

      // Plot stops on map
      clearStops();
      addStops( j );  // HARDWIRED FOR TESTING
//endif android	    

      // Start main loop
      mLThread = new Thread( mL );
      Log.i( TAG, "[initMap] Starting main thread..." );
      mLThread.start();

      // Signal that we're up and running
      return true;
    }
    // END INITIALIZE MAP


    // Thread containing main loop, which updates the map
    private class mainLoop implements Runnable {

        private volatile boolean running = false;

        public void run() {
            // MAIN LOOP //
            while( running ) {
                // Get the current time
                tD.setCurrent();
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
                    siri.update( tD, tD, serviceName, destinationDisplay, selectedJourney, stopRefTo );
                    // Check for active live links
                    live = service.activeLink( tD, tD, selectedJourney, true ).hasLiveTime( selectedJourney );
//ifdef android		    
                    // Perform the UI updates
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//endif android		    
                            plotAllRouteLinkProgress( tD, selectedJourney );
//ifdef android		    
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
//endif android		    
                }
                else {
                    // If we don't have a journey, turn off the update system
                    //  ToDo: this call only needs to be made once - but siri handle needed if new journey activate
                    siri.suspend();
//ifdef android		    
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
//                            String nextStopRefTo = service.activeStopRefTo( tD, tD, nextJourney, false ); 
                            nextStopIndicator.setText( tnds.stops.name( service.stopRef( nextJourney, 1 ) ) );
                            dataTime dT = new dataTime( nextJourney );
                            arrivalTimeLabel.setText( "at" );
                            arrivalTimeIndicator.setText( String.format( "%02d:%02d:%02d", dT.hours, dT.minutes, dT.seconds ) );
                        }
                    });
//endif android		    
                }

                try {
                      // Pause for SLEEP_PERIOD before next map update
                    Thread.currentThread().sleep( SLEEP_PERIOD );
                }
                catch( InterruptedException e ) {
                    Log.e( TAG, "[mainLoop] ERROR: Interrupted exception " + e );
                }
            }
        }

        public void cancel() {
          running = false;
          // Disable any active siri updates
          if( siri != null )
            siri.suspend();
if( DEBUG )            
          Log.i( TAG, "[mainLoop] Thread inactive. Cleanup code here?" );
        }

        public void activate() {
          running = true;
if( DEBUG )
          Log.i( TAG, "[mainLoop] Thread now active." );
        }
    }

//ifdef android
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
            getWindowManager().getDefaultDisplay().getMetrics( metrics );
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

//            // Step through each known stop
//            Iterator stopKeys = tnds.stops.keySet().iterator();
//            while( stopKeys.hasNext() ) {
//                String id = stopKeys.next().toString();
//                dataStop stop = tnds.stops.get( id );
//                stopPOI.put( id, new OverlayItem( new GeoPoint( stop.getLat(), stop.getLon() ), stop.stopName, serviceName ) );
//                poiOverlay.addItem( stopPOI.get( id ) );
//                // Centre the map on the first stop
//                if( !viewSet ) {
//                    map.getController().setCenter( new GeoPoint( stop.getLat(), stop.getLon() ) );
//                    viewSet = true;
//                }
//            }

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
    
    // Spinner Interaction handler
    public class spinnerListener implements OnItemSelectedListener {
    
      public void onItemSelected( AdapterView <?> parent, View view, int pos, long id ) {
          // Note this is called automatically when spinner is initialised
          if( ready ) {
            mL.cancel();
            // Get service name
            if( parent == spinnerService )
              serviceName = (String) parent.getItemAtPosition( pos );
            // Get service direction
            else if( parent == spinnerDirection ) {
              String directionName = (String) parent.getItemAtPosition( pos );
              if( directionName.equals( directionNames[ 0 ] ) )
                serviceDirection = tnds.services.INBOUND;
              else
                serviceDirection = tnds.services.OUTBOUND;
            }

            Log.i( TAG, "[spinnerListener] Set service " + serviceName + " (inbound? " + serviceDirection + ")" );

            start();
          }
          else
              Log.i( TAG, "[spinnerListener] System not yet ready." );
      }

      public void onNothingSelected( AdapterView <?> parent ) {
          // Another interface callback
//          Log.i( TAG, "Spinner: nothing selected." );
      }
    }

    // ToggleButton Interaction handler
    public void onToggleClicked( View view ) {
      trackBus = ( (ToggleButton) view).isChecked();
    }

    public void start() {
      if( serviceName != null ) {
        if( initMap() )
          mL.activate();
        else
          Log.e( TAG, "[start] ERROR: Could not initMap()" );
      }
    }

//endif android

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
            getWindowManager().getDefaultDisplay().getMetrics( metrics );
            bmd.setTargetDensity( metrics );
            //
            DefaultItemizedOverlay busOverlay = new DefaultItemizedOverlay( bmd );
            busOverlay.setKey( BUS_OVERLAY );
            busOverlay.addItem( new OverlayItem( new GeoPoint( busPos.lat, busPos.lon ), busName, null ) );
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

    // return false since no route is being displayed
    @Override
    public boolean isRouteDisplayed() {
        return false;
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

/*
    // Return this context's handle
    public static Context getContext() {
      return mContext;
    }*/

/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/

}
