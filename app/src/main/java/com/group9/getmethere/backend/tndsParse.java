package com.group9.getmethere.backend;

import android.content.res.AssetManager;

public class tndsParse {

  public static final String  SUFFIX = ".xml";

  public dataStops 	stops		= new dataStops();
  public dataServices 	services 	= new dataServices();

  public void parse( AssetManager assets, String filename ) {
    // Inbound addition
    dataService serviceInbound = new dataService();
    tndsServiceParse tndsService = new tndsServiceParse( assets, filename + SUFFIX, stops, serviceInbound );
    services.add( serviceInbound.lineName, serviceInbound, services.INBOUND );
    // Outbound addition
    dataService serviceOutbound = new dataService();
    tndsService = new tndsServiceParse( assets, filename + services.OUTBOUND_ID + SUFFIX, stops, serviceOutbound );
    services.add( serviceOutbound.lineName, serviceOutbound, services.OUTBOUND );
  }

}
