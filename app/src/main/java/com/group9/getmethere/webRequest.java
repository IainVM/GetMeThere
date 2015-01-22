package com.group9.getmethere;

//ifdef android
import android.util.Base64;
//endif android
/*ifdef pc
import net.iharder.Base64;
endif pc*/
import android.util.Log;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

// Testing
import java.io.OutputStream;

public class webRequest {

        // Logging
        private static final String TAG = "GetMeThere";
        //

        // Debug
        private static boolean DEBUG = false;
        //

	// Defines
	private static final String method	= "POST";
        private static final String typeKey     = "Content-Type";
        private static final String typeValue   = "application/x-www-form-urlencoded";
	private static final String lengthKey	= "Content-Length";
	private static final String authKey	= "Authorization";
	private static final String authStart	= "Basic ";
	private static final String authSep     = ":";
	private static final String encoding	= "UTF-8";

	private static final int OK		= 200;
	//

	private URL server;
	private HttpURLConnection socket;
	private byte [] postData;
	private BufferedReader inputData;
	private String auth;

	public webRequest( String url, String username, String password ) {
		try {
if( DEBUG )
			Log.i( TAG, "[webRequest] URL: " + url );
			server = new URL( url );
			String authString = username + authSep + password;
			//ifdef android
			auth = authStart + Base64.encodeToString( authString.getBytes( encoding ), Base64.DEFAULT );
			//endif android
			/*ifdef pc
			auth = authStart + Base64.encodeBytes( authString.getBytes( encoding ) );
			endif pc*/
if( DEBUG )
			Log.i( TAG, "[webRequest] Auth: " + authKey + " = " + auth );
		}
		catch( MalformedURLException e ) {
			Log.e( TAG, "MalformedURLException: (in webRequest [constructor()] ): " + e );
		}
		catch( UnsupportedEncodingException e ) {
			Log.e( TAG, "UnsupportedEncodingException: (in webRequest [constructor()] ): " + e );
		}

		inputData = null;
	}

	// Null return value means NO data was returned!
	public BufferedReader send( String data ) {
		try{
			// Convert given data
			postData = data.getBytes( encoding );
		}
		catch( UnsupportedEncodingException e ) {
			Log.e( TAG, "UnsupportedEncodingException: (in webRequest [send()] ): " + e );
			// Try again!
			postData = data.getBytes();
		}

		// Do we have anything to send?
		if( postData.length > 0 ) {
			// Send the request
			try {
                                System.setProperty( "http.keepAlive", "false" );

				socket = (HttpURLConnection) server.openConnection();
				socket.setRequestMethod( method );
				socket.setRequestProperty( authKey, auth );
                                socket.setRequestProperty( typeKey, typeValue );
				socket.setRequestProperty( lengthKey, String.valueOf( postData.length ) );
                                socket.setRequestProperty( "Connection", "close" );
				socket.setDoOutput( true );
if( DEBUG )
				Log.i( TAG, "[webRequest] Sending: " + new String( postData ) );
				socket.getOutputStream().write( postData );

				// Did we get an OK?
				if( socket.getResponseCode() == OK ) {
					// Open a stream to get the response, and return it
					inputData = new BufferedReader( new InputStreamReader( socket.getInputStream(), encoding ) );
					return inputData;
				}
				else
					Log.e( TAG, "ERROR: webRequest: [send()]: Got code " + socket.getResponseCode() );
			}
			catch( IOException e ) {
			        Log.e( TAG, "IO/Protocol/UnsupportedEncoding Exception (in webRequest [send()] ): " + e );
			}
		}

		// If we've dropped through, nothing was sent
		return null;
	}
}
