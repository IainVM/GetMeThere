package com.group9.getmethere.backend;

import java.util.TreeMap;
import java.util.Iterator;

public class dataPatternSect {

	TreeMap < String, dataPatternLink > patternLinks;

	public dataPatternSect() {
	    patternLinks = new TreeMap < String, dataPatternLink >();
	}

	public void add( String id, dataPatternLink dPS ) {
	    patternLinks.put( id, dPS );
	}

        /* Polyline related methods */
        public double length() {
            double total = 0;
            Iterator pL = patternLinks.keySet().iterator();
            while( pL.hasNext() )
                total += patternLinks.get( pL.next() ).length();

            return total;
        }
}
