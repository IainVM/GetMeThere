package com.group9.getmethere;

//ifdef android
import android.app.Activity;
//endif android
import android.content.res.AssetManager;
//ifdef android
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
//endif android
import android.util.Log;
//ifdef android
import android.view.Menu;
import android.view.MenuItem;
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

    // Logging
    private static final String TAG = "GetMeThere";
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
//endif android


    //  Back-end related fields
    private static final int SLEEP_PERIOD = 1000;   // ms
    private tndsParse tnds = new tndsParse();
    private String serviceName;
    public  String serviceNames[] = { "1", "15", "28A", "35", "54", "57", "58", "65", "M1", "M2" };
    private int selectedJourney;
    private dataService service;
    private dataTimeDate tD = new dataTimeDate();
    private pointCalculate pCalc = new pointCalculate();
    //
    private dataPoint busPos;
    private String busName;
    private final int MARKER_OFFSET = 12;
    //
    private siriUpdate siri;
    private Thread siriThread;
    //

//ifdef android
    @Override
//endif android
    public void onCreate( //ifdef android
    			Bundle savedInstanceState //endif android
						) {
//ifdef android
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the zoom level, center point and enable the default zoom controls
        map = (MapView) findViewById(R.id.map);
        map.getController().setZoom( 14 );
        map.getController().setCenter( new GeoPoint( (double) 51.4833, (double) -3.1833 ) );
        map.setBuiltInZoomControls( true );
        // Initialise the map annotation view
        annotation = new AnnotationView( map );
        // Initialise the paint object (for line drawing)
        setupPaintLines();

	// Get assets handle
        assets = getAssets();
//endif android

        // BEGIN SERVICE DATA SETUP
        Log.i( TAG, "Done map setup. Beginning services setup." );
        // Service & stops setup
        serviceName = "54";
        busName = serviceName;
        /* Bodged addition of other services! ** FIX ME */
        for( int i = 0; i < serviceNames.length; i++ )
            tnds.parse( assets, serviceNames[ i ] + ".xml" );
        // TEST ONLY
        testTNDS();
        /* End of bodge */
        naptanParse naptan = new naptanParse( assets, tnds.stops, "NaPTAN_571-541.xml" );
        Log.i( TAG, "Done setting up service. Initialising..." );
        service = tnds.services.get( serviceName );
        // This MUST be checked!
        if( service == null )
            Log.e( TAG, "ERROR: NO SERVICE FOUND (" + serviceName + ")" );
        else {
            // Start the SIRI-SM update class
            siri = new siriUpdate( tnds, service );
            siriThread = new Thread( siri );
            // TEST ONLY
//            siriThread.start();

//ifdef android
            // Plot stops on map
            addStops();
//endif android	    

            // Main loop setup
            mainLoop mL = new mainLoop();
            Thread mLThread = new Thread( mL );
            Log.i( TAG, "Starting main thread..." );
            mLThread.start();
        }
    }

    // TEST ONLY
    private void testTNDS() {
        for( int i = 0; i < serviceNames.length; i++ ) {
            dataService s = tnds.services.get( serviceNames[ i ] );
            // This MUST be checked!
            if( s == null )
                Log.e( TAG, "ERROR: NO SERVICE FOUND (" + serviceNames[ i ] + ")" );
            else {
                Log.i( TAG, "Service " + serviceNames[ i ] + ": from " + s.stdService.origin + " to " + s.stdService.destination );
            }
        }
    }
    // END OF TEST

    private class mainLoop implements Runnable {

        public void run() {
            // MAIN LOOP //
            while( true ) {
                // Get the current time
                tD.setCurrent();
                // Select the currently active journey
                selectedJourney = service.activeJourney( tD, tD, true );
                // ACTIVATE BELOW CODE FOR VERBOSE LOGGING
                Log.i( TAG, "Time " + tD.hour() + ":" + tD.minute() + ":" + tD.second() + " | Selected journey: " + selectedJourney );
                // TEST ONLY - JOURNEY 2
                String serviceName2 = "M1";
                dataService service2 = tnds.services.get( serviceName2 );
                int selectedJourney2 = service2.activeJourney( tD, tD, false );
                Log.i( TAG, "2: Time " + tD.hour() + ":" + tD.minute() + ":" + tD.second() + " | Selected journey: " + selectedJourney2 );
                if( selectedJourney2 != service2.NOT_FOUND ) {
                    // TESTING OUTPUT BLOCK //
                    String stopRefFromTest2 = service2.activeStopRefFrom( tD, tD, selectedJourney2, false );
                    String stopRefToTest2   = service2.activeStopRefTo(   tD, tD, selectedJourney2, false );
                    String stopNameFrom2 = tnds.stops.name( stopRefFromTest2 );
                    String stopNameTo2   = tnds.stops.name( stopRefToTest2 );
                    float progress2 = service2.activeLinkProgress( tD, tD, selectedJourney2, false );
                    int progPercent2 = (int) ( progress2 * 100 );
                    Log.i( TAG, "2: Time " + tD.hour() + ":" + tD.minute() + ":" + tD.second() + " | Service " + serviceName2 + "-" + selectedJourney2 + " | From " + stopNameFrom2 + " to " + stopNameTo2 + " | Progress " + progPercent2 + "%" );
                    // TESTING OUTPUT BLOCK ENDS //

                    // Check for service updates
                    String stopRefTo2 = service2.activeStopRefTo( tD, tD, selectedJourney2, false );    // Change to TRUE for live!!
                    // TEST ONLY - no update yet!
//                    siri.update2( tD, tD, serviceName2, selectedJourney2, stopRefTo2 );
//ifdef android		    
//                  // CANNOT YET RUN HERE - because called pARLP() only handles one, hard-wired journey!
//                    // Perform the UI updates
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            plotAllRouteLinkProgress( tD, selectedJourney );
//                        }
//                    });
//endif android		    
                }
                // TEST ONLY - no update yet!
//                else
//                    // If we don't have a journey, turn off the update system
//                    siri.suspend();
//
//                }
                // END TEST - JOURNEY 2

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
                    String stopRefTo = service.activeStopRefTo( tD, tD, selectedJourney, true );
                    siri.update( tD, tD, serviceName, selectedJourney, stopRefTo );
//ifdef android		    
                    // Perform the UI updates
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            plotAllRouteLinkProgress( tD, selectedJourney );
                        }
                    });
//endif android		    
                }
                else
                    // If we don't have a journey, turn off the update system
                    siri.suspend();

                try {
                    Thread.currentThread().sleep( SLEEP_PERIOD );
                }
                catch( InterruptedException e ) {
                    Log.e( TAG, "ERROR: [MAIN LOOP]: Interrupted exception " + e );
                }
            }
        }
    }

//ifdef android
    // Plot stops on the map as POIs
    private void addStops() {
        boolean viewSet = false;
        // Allocate the hashmap
        stopPOI = new HashMap < String, OverlayItem >();

        // Create the itemized overlay
        try {
            Drawable icon = Drawable.createFromStream( assets.open( "location_marker.png" ), null);
            poiOverlay = new DefaultItemizedOverlay(icon);

            // Step through each known stop
            Iterator stopKeys = tnds.stops.keySet().iterator();
            while( stopKeys.hasNext() ) {
                String id = stopKeys.next().toString();
                dataStop stop = tnds.stops.get( id );
                stopPOI.put( id, new OverlayItem( new GeoPoint( stop.getLat(), stop.getLon() ), stop.stopName, serviceName ) );
                poiOverlay.addItem( stopPOI.get( id ) );
                // Centre the map on the first stop
                if( !viewSet ) {
                    map.getController().setCenter( new GeoPoint( stop.getLat(), stop.getLon() ) );
                    viewSet = true;
                }
            }

            // Set an interaction handler
            poiOverlay.setTapListener( new tapListener() );
            // Activate the overlay
            map.getOverlays().add(poiOverlay);
        }
        catch( IOException e ) {
            Log.e( TAG, "IOException (in MainActivity [addStops()]): " + e );
        }
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
//endif android

    // Plot progress of all route links
    private void plotAllRouteLinkProgress( dataTimeDate tD, int chosenJourney ) {
        boolean success = true, initial = true;
        int linkNo = 1;

        do {
            // Get the nth patternLink
            dataPatternLink dPL = service.patternLink( chosenJourney, linkNo );
            // If we have one
            if( dPL != null ) {
                // Clear the layer, if this is our first plot
                if( initial ) {
//ifdef android		
                    // Remove the old layer (NECESSARY??)
                    map.removeOverlayByKey( LINE_OVERLAY );
                    // Reset it
                    resetLines();
//endif android		    
                    initial = false;
                }
                success = plotRouteLinkProgress( tD, chosenJourney, dPL.getFrom(), dPL.getTo() );
                linkNo ++;
            }
            // Otherwise, signal end of loop
            else {
                success = false;
                initial = true;
            }
        } while( success == true );

//ifdef android
        // Render the lines (and bus), if anything happened
        if( !initial ) {
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

//            // Plot the bus
              busPos = toProgress;

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
            DefaultItemizedOverlay busOverlay = new DefaultItemizedOverlay( icon );
            busOverlay.setKey( BUS_OVERLAY );
            busOverlay.addItem( new OverlayItem( new GeoPoint( busPos.lat, busPos.lon ), busName, null ) );
            // Centre the map on the bus marker
            map.getController().setCenter( new GeoPoint( busPos.lat, busPos.lon ) );

            // Activate the overlay
            map.getOverlays().add( busOverlay );
        }
        catch( IOException e ) {
            Log.e( TAG, "IOException (in MainActivity [plotBus()]): " + e );
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
