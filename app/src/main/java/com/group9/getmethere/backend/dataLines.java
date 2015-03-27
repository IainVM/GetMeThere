package com.group9.getmethere.backend;

import java.util.HashMap;
import java.util.Set;

public class dataLines {

    private HashMap< String, dataLine > lines;

    public dataLines() {
        lines = new HashMap < String, dataLine >();
    }

    public dataLine get( String id ) { return lines.get( id ); }

    public void add( String id, dataLine dL ) {
        lines.put( id, dL );
    }

    public Set keySet() {
        return lines.keySet();
    }
}
