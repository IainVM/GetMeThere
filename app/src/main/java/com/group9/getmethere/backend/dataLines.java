package com.group9.getmethere.backend;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

public class dataLines {

    private HashMap< String, dataLine > lines;

    public dataLines() {
        lines = new HashMap < String, dataLine >();
    }

    public dataLine get( String id ) {
        return lines.get( id );
    }

    public void add( String id, dataLine dL ) {
        lines.put( id, dL );
    }

    public Set keySet() {
        return lines.keySet();
    }

    public int size() {
        return lines.size();
    }

    public double length() {
        double total = 0;
        Iterator l = lines.values().iterator();
        while( l.hasNext() ) {
            total += ( (dataLine) l.next() ).length();
//            total += l.next().getValue().length();
//            total += lines.get( String.valueOf( l.next() ) ).length();
        }
        return total;
    }
}
