package com.group9.getmethere.backend;

import android.util.Log;

import java.io.BufferedReader;

public class siriRequest {

        // Debug
        private static boolean DEBUG = false;
        //

        // Logging
        private static final String TAG = "GetMeThere";
        //

	// Defines
	public static final String urlStart	= "http://";
	public static final String urlSeparator	= ":";
	public static final String urlAt	= "@";
	public static final String urlEnd	= "nextbus.mxdata.co.uk/nextbuses/1.0/1";
	public static final String encoding	= "UTF-8";

	public static final int requestFreq	= 15;	// seconds
	//

	// Account details
	public static final String username	= "TravelineAPI288";
	public static final String password	= "nRBA93Az";
	//

	// Request tags
	public static final String xmlHeader	= "<?xml version=`1.0` encoding=`UTF-8` standalone=`yes`?>\n".replace( '`', '"' );
	public static final String siriStart	= "<Siri version=`1.0` xmlns=`http://www.siri.org.uk/`>\n".replace( '`', '"' );
	public static final String servReqStart	= "<ServiceRequest>\n";
	public static final String timeStart	= "<RequestTimestamp>";
	public static final String timeClose	= "</RequestTimestamp>\n";
	public static final String reqIDStart	= "<RequestorRef>";
	public static final String reqIDClose	= "</RequestorRef>\n";
	public static final String stopReqStart	= "<StopMonitoringRequest version=`1.0`>\n".replace( '`', '"' );
	public static final String msgIDStart	= "<MessageIdentifier>";
	public static final String msgIDClose	= "</MessageIdentifier>\n";
	public static final String monIDStart	= "<MonitoringRef>";
	public static final String monIDClose	= "</MonitoringRef>\n";
	public static final String stopReqClose	= "</StopMonitoringRequest>\n";
	public static final String servReqClose	= "</ServiceRequest>\n";
	public static final String siriClose	= "</Siri>\n";
	//
	
	private String stopRef;
	private int reqNo;
	private dataTimeDate tD;
	private webRequest request;
	private int requestLast;
	public BufferedReader inputData;

	public siriRequest( String sR ) {
		inputData = null;
		stopRef = sR;
		reqNo = 0;
		tD = new dataTimeDate();
		request = new webRequest( urlStart + username + urlSeparator + password + urlAt + urlEnd, username, password );

	}

	// Not necessary to call this externally, if timeout() check remains in send()
	public boolean timeout( int time ) {
		return time > requestLast + requestFreq;
	}

        // Only using <time> field of tD here
	public boolean send( dataTimeDate tD ) {
		// Only perform this request if timeout has elapsed
		if( timeout( tD.time() ) )
		{
                        dataTimeDate tDCurrent = new dataTimeDate();
                        tDCurrent.setCurrent();
			String timeStamp = tDCurrent.getTimeStamp();
			String requestStr = "";
			reqNo ++;

			requestStr += xmlHeader + siriStart + servReqStart;
			requestStr += timeStart + timeStamp + timeClose;
			requestStr += reqIDStart + username + reqIDClose;
			requestStr += stopReqStart + timeStart + timeStamp + timeClose;
			requestStr += msgIDStart + reqNo + msgIDClose;
			requestStr += monIDStart + stopRef + monIDClose + stopReqClose;
			requestStr += servReqClose + siriClose;


if( DEBUG )
			Log.i( TAG, "[siriRequest] * Live fetch for stopRef " + stopRef + " *" );
			inputData = request.send( requestStr );
			if( inputData != null ) {
				// Only mark the request time if request was successful
				requestLast = tD.time();
if( DEBUG )
				Log.i( TAG, "[siriRequest] Data was returned by NextBuses." );
				return true;
			}
			else
				// Debug showing NO data was returned!
				Log.e( TAG, "ERROR: [siriRequest] *NO* data returned by NextBuses!!" );
		}
		else
			// Debug showing timeout not yet active
if( DEBUG )
			Log.i( TAG, "[siriRequest] ...No request (timeout not yet occurred)." );

		// If we're here, method failed or timeout hasn't yet occurred
		return false;
	}
}
