package com.group9.getmethere.backend;

import java.util.HashMap;
import android.util.Log;

public class dataServices {

  // Debug
  private static boolean DEBUG = false;
  //

  // Logging
  private static final String TAG = "GetMeThere [dataServices] ";
  //

  public static final String  OUTBOUND_ID = "o";

  public static final boolean OUTBOUND    = false;
  public static final boolean INBOUND     = true;

  private HashMap < String, dataService > services;

  public dataServices() {
    services = new HashMap < String, dataService >();
  }

  // Modifies the service name (key) if the service direction is outbound
  public String getKey( String id, boolean direction ) {
    if( direction == OUTBOUND )
      return id + OUTBOUND_ID;
    return id;
  }

  public dataService get( String id, boolean direction ) { 
if( DEBUG )  
    Log.i( TAG, "[get] Getting service with name " + getKey( id, direction ) );
    return services.get( getKey( id, direction ) );
  }

  public void add( String id, dataService dS, boolean direction ) {
    services.put( getKey( id, direction ), dS );
  }

}
