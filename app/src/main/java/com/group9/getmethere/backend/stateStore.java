package com.group9.getmethere.backend;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

//ifdef android
import android.content.Context;
import android.app.Application;
//endif android

public class stateStore {

  // Logging
  private static final String TAG = "GetMeThere [stateStore] ";
  //

  public static final String suffix = ".state";

//ifdef android
  private static Context context = null;

  // Context must be set before any calls to load/save!
  public static void setContext( Context c ) {
    context = c;
  }
//endif android
    
  // Attempt to load a stored state from a file
  public static Object loadState( String filename ) {

    FileInputStream fIn;
    ObjectInputStream oIn;
    Object obj = null;

    try {
/*ifdef pc
      fIn = new FileInputStream( filename + suffix );
endif pc*/
//ifdef android
      fIn = context.openFileInput( filename + suffix );
//endif android
    }
    catch( FileNotFoundException e ) {
      Log.e( TAG, "[loadState] File " + filename + suffix + " not found - maybe it was not yet created? " + e );
      return null;
    }

    try {
      oIn = new ObjectInputStream( fIn );
    }
    catch( IOException e ) {
      Log.e( TAG, "[loadState] ERROR: Could not load object from " + filename + suffix + "! " + e );
      return null;
    }

    try {
      obj = oIn.readObject();
    }
    catch( ClassNotFoundException e ) {
      Log.e( TAG, "[loadState] ERROR: Could not find object in " + filename + suffix + "! " + e );
      return null;
    }
    catch( IOException e ) {
      Log.e( TAG, "[loadState] ERROR: Could not find object in " + filename + suffix + "! " + e );
      return null;
    }

    if( obj instanceof tndsParse ) {
      Log.i( TAG, "[loadState] Found saved instance of tndsParse" );

      try {
        fIn.close();
      }
      catch( IOException e ) {
        Log.e( TAG, "[loadState] ERROR: Could not close " + filename + suffix + "! " + e );
      }

      return obj;
    }

    return null;
  }

  // Attempt to store the state of an object to a file
  public static boolean saveState( String filename, Object obj ) {

    FileOutputStream fOut;
    ObjectOutputStream oOut;

    try {
/*ifdef pc    
      fOut = new FileOutputStream( filename + suffix );
endif pc*/
//ifdef android
      fOut = context.openFileOutput( filename + suffix, Context.MODE_PRIVATE );
//endif android
    }
    catch( FileNotFoundException e ) {
      Log.e( TAG, "[saveState] ERROR: File " + filename + suffix + " not found! " + e );
      return false;
    }

    try {
      oOut = new ObjectOutputStream( fOut );
    }
    catch( IOException e ) {
      Log.e( TAG, "[saveState] ERROR: Could not open object output stream on " + filename + suffix + "! " + e );
      return false;
    }

    try {
      oOut.writeObject( obj );
      fOut.close();
      return true;
    }
    catch( IOException e ) {
      Log.e( TAG, "[saveState] ERROR: Could not write object to " + filename + suffix + "! " + e );
    }

    return false;
  }

}
