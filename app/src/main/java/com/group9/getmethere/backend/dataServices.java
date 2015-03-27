package com.group9.getmethere.backend;

import java.util.HashMap;

public class dataServices {

	private HashMap < String, dataService > services;

	public dataServices() {
		services = new HashMap < String, dataService >();
	}

        public dataService get( String id ) { 
                return services.get( id );
        }

	public void add( String id, dataService dS ) {
		services.put( id, dS );
	}
}
