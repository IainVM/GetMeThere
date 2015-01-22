package com.group9.getmethere;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class xmlParser {

        // Logging
        private static final String TAG = "GetMeThere";
        //

	// Debug
	public static boolean DEBUG = false;
	//

	// Defines
	public static final int GET	= 1;
	public static final int ID	= 2;
	public static final int WITHIN	= 3;

	private static final String tagStart = "<";
	private static final String tagEnd   = ">";
	private static final String tagQuote = "" + '"';
	private static final String id       = " id";
	private static final String tagClose = "/";
	private static final String newLine  = "\n";
	//

	private BufferedReader inputData;
	private String input, end;
	private boolean newlineDelim, eof;

	/* Constructor */
	public xmlParser( BufferedReader iD, boolean nD ) {
		inputData = iD;
		newlineDelim = nD;

		input = "";
	}

	public String lastInput() {
		return input;
	}

	/* Reset file reader (should seek to 0 bytes) ) */
	public void reset() {
		try {
			inputData.reset();
		}
		catch( IOException e ) {
		        Log.e(TAG, "IOException (in xmlParser [reset()]): " + e);
		}
	}

	// If this method detects a possible closing tag ("/" followed by ">"), treats it as CR/LF
	private String readLine( String tag ) {
		boolean foundTag = false, done = false;
		int inVal = 0;
		String inLine = "";

		do {
			try {
				// Is BufferedInput ready for a read?
				if( inputData.ready() ) {
					inVal = inputData.read();

					// If we're working on a file with no newlines, work special cases
					if( !newlineDelim ) {
						// If we've got a '/', signal a possible 'found tag' (generic)
						//  or if we've found the sought data already (specific)
						if( inVal == (int) tagClose.charAt( 0 ) || inLine.contains( tag ) )
							foundTag = true;
						// If we've got a '>' after a possible closeTag or sought tag, we're done
						if( foundTag && inVal == (int) tagEnd.charAt( 0 ) )
							done = true;
					}

					// If we've got an end-of-line or no data, we're done
					if( inVal == (int) newLine.charAt( 0 ) || inVal == -1 )
						done = true;
					// If it's not a null, add this char to the string
					if( inVal > 0 )
						inLine += (char) inVal;
				}
				else 
					done = true;
			}
			catch( IOException e ) {
				Log.e( TAG, "IOException (in xmlParser [readLine()]): " + e );
			}

		} while( done == false );
	
		// Did we get anything?
		if( inLine == "" )
			return null;
		return inLine;
	}

	/* Return the data ON ONE LINE containing "<tag>" (or "<tag...>" if terminated = false) */
	private String getRaw( String tagSection, String tag, boolean terminated, String delimStart, String delimEnd ) {
		if( terminated )
			end = delimEnd;
		else
			end = "";

		while( !eof ) {
			if( DEBUG )
				Log.i( TAG, "[xmlParser] about to readLine().." );
			input = readLine( tag );
			if( DEBUG )
				Log.i( TAG, "[xmlParser] >: " + input );

                        if( input == null )
				eof = true;
			// End of section
			if( input == null || input.contains( tagStart + tagClose + tagSection + tagEnd ) )
				return null;
			// Found sought data
			if( input.contains( delimStart + tag + end ) )
				return input;
		}

		return input;
	}

	/* Locates the data ON ONE LINE between "<delimStart><tag><delimEnd>" and "<delimStart>"
	 * 	(or "<delimStart><tag>...<delimEnd>" if terminated == false)
	 * 	or "<delimStart>" and "<delimStart>" if <delimStart> != <delimEnd> */
	private String getContents( String tagSection, String tag, boolean terminated, String delimStart, String delimEnd ) {
		input = getRaw( tagSection, tag, terminated, tagStart, tagEnd );

		// Have we found something?
		if( input != null )
			if( delimStart != delimEnd ) {
                                String [] inputSplit = input.split( delimEnd );
                                if( inputSplit.length > 1 )
        				return inputSplit[ 1 ].split( delimStart )[ 0 ];
                        }
			else
				return input.split( delimStart )[ 1 ];
		
		// Otherwise, we ran out of input
		return null;
	}

	/* Returns the tag string on NEXT LINE between <delimStart> and " /"+<delimEnd>, or null if section ends */
	public String getTag( String section, boolean terminated ) {
		String delimStart = "<", delimEnd = ">";
		if( terminated )
			delimEnd = " /" + delimEnd;
		try{ 
			input = inputData.readLine();

			if( input == null )
				eof = true;
			// End of section check
			if( input.contains( section ) )
				return null;
			// Otherwise, return the tag
			if( input.contains( delimStart ) && input.contains( delimEnd ) )
				return input.split( delimStart )[ 1 ].split( delimEnd )[ 0 ];
		}
		catch( IOException e ) {
			eof = true;
			Log.e( TAG, "IOException (in xmlParser [getTag()]): " + e );
		}

		return null;
	}

	/* Locates the data ON ONE LINE between "<tag>" and "<" (or "<tag...>" if terminated == false) */
	public String get( String tagSection, String tag, boolean terminated ) {
		return getContents( tagSection, tag, terminated, tagStart, tagEnd );
	}

	/* Locates the data ON ONE LINE containing "<tag>" (or "<tag..>" if terminated == false)
	 * 	between "<tagQuote>" and "<tagQuote>" */
	public String getID( String tagSection, String tag ) {
		return getContents( tagSection, tag + id, false, tagQuote, tagQuote );
	}

	/* Locates the data ON ONE LINE between "<innerTag>" and "<" (or "<innerTag...>" if terminated = false)
	 * occurring after the line containing "<outerTag>" and "<" (or "<outerTag...>" if terminated = false) */
	public String getWithin( String tagSection, String outerTag, String innerTag, boolean terminated ) {
		input = getRaw( tagSection, outerTag, terminated, tagStart, tagEnd );

		// Have we found something?
		if( input != null )
			return get( tagSection, innerTag, terminated );

		// Otherwise, we ran out of input
		return null;
	}

	/* Locates the line containing "<tag>" and "<" (or "<tag...>" if terminated = false)
	 * Subsequent calls seek data AFTER this line */
	public boolean find( String section, String tag, boolean terminated ) {
		if( !eof )
			return getRaw( section, tag, terminated, tagStart, tagEnd ) != null;
		return false;
	}

	/* EOF test */
	public boolean endOfFile() {
		return eof;
	}

	/* Select method by pre-defined type */
	public String getByType( int type, String tagSection, String outerTag, String innerTag, boolean terminated ) {
		if( type == GET )
			return get( tagSection, innerTag, terminated );
		if( type == ID )
			return getID( tagSection, innerTag );
		if( type == WITHIN )
			return getWithin( tagSection, outerTag, innerTag, terminated );

		// Return NULL if type was unknown
		return null;
	}
}
