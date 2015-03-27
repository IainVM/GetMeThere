package com.group9.getmethere.backend;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class inputFile {

        // Logging
        private static final String TAG = "GetMeThere";
        //

	// Defines
        private static final String ENCODING = "UTF-8";
	private static final int READAHEAD_MAX	= 3000000;
	//

        private AssetManager assets;
	private String filename;
	public BufferedReader reader;
	private boolean closed;

	public inputFile( AssetManager a, String f ) {
        assets = a;
		closed = true;
		filename = f;
		open();
	}

	/* Public file status check */
	public boolean isOpen() {
		return !closed;
	}

	/* Private open method */
	private void open() {
		try {
            InputStream inputStream = assets.open( filename );
			reader = new BufferedReader( new InputStreamReader( inputStream, ENCODING ) );
			reader.mark( READAHEAD_MAX );
			closed = false;
		}
		catch( IOException e ) {
		        Log.e( TAG, "IOException (in inputFile [constructor]): " + e );
		}
	}

	/* Close then re-open file (equivalent to seek(0) ) */
	public void reset() {
		close();
		open();
	}

	/* Public close method */
	public void close() {
		try {
			reader.close();
		}
		catch( IOException e ) {
		        Log.e( TAG, "IOException (in inputFile [close()]): " + e );
		}
		closed = true;
	}
}
