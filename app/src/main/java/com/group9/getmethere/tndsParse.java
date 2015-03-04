package com.group9.getmethere;

import android.content.res.AssetManager;

public class tndsParse {

	public dataStops 	stops		= new dataStops();
	public dataServices 	services 	= new dataServices();

	public void parse( AssetManager assets, String filename ) {
		dataService service = new dataService();
		tndsServiceParse tndsService = new tndsServiceParse( assets, filename, stops, service );
		services.add( service.lineName, service );
	}
}
