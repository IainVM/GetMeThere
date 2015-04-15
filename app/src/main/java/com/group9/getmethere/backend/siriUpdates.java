package com.group9.getmethere.backend;

import java.util.HashMap;
import android.util.Log;

public class siriUpdates {

  // Debug
  private static boolean DEBUG = false;
  //

  // Logging
  private static final String TAG = "GetMeThere [siriUpdates] ";
  //

  private HashMap < Boolean, siriUpdate > updates;

  public siriUpdates() {
    updates = new HashMap < Boolean, siriUpdate > ();
  }

  public void add( boolean direction, siriUpdate sU ) {
if( DEBUG )
    Log.i( TAG, "[add] Adding siriUpdate direction " + direction );
    updates.put( direction, sU );
  }

  public siriUpdate get( boolean direction ) {
if( DEBUG )
    Log.i( TAG, "[get] Returning siriUpdate (if existing) for direction " + direction );
    return updates.get( direction );
  }

}
