package com.group9.getmethere;

import java.io.BufferedReader;
import android.util.Log;

public class cmsRequest {

    // Debug
    private static boolean DEBUG = false;
    //

    // Logging
    private static final String TAG = "GetMeThere [cmsRequest] ";
    //

    // Defines
    public static final String  url         = "http://www.buscms.com/api/rest/ent/route-routemap.aspx";

    public static final String  routeID     = "routeid=";
    public static final String  format      = "format=json";
    public static final String  separator   = "&";
    //

    private webRequest request;
    public BufferedReader inputData;

    public cmsRequest() {
        request = new webRequest( url );
    }

    public boolean send( int rID ) {
        String requestStr = routeID + rID + separator + format;
        inputData = request.send( requestStr );
        if( inputData != null ) {
if( DEBUG )        
            Log.i( TAG, "[send] Got data!\n" );
            return true;
        }

if( DEBUG )
        Log.i( TAG, "[send] Nothing returned.\n" );
        return false;
    }
}
