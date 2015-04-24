package com.group9.getmethere.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
// DJH
import java.util.ArrayList;
import java.util.List;
//

import com.group9.getmethere.R;
import com.group9.getmethere.backend.backendAPI;
import com.group9.getmethere.backend.dataTime;

public class BusListAdapter extends ArrayAdapter<backendAPI.Bus> {
    private final Context context;
    // Iain: I updated these from arrays to ArrayLists so they can be
    //  dynamically altered. I'd prefer to have a single <Bus>
    //  ArrayList here, but couldn't seem to pass that into the
    //  constructor - maybe <?> would work? I've not yet tried that.
    private ArrayList<backendAPI.Bus> busses;
    private backendAPI api;


    public BusListAdapter(Context context, ArrayList<backendAPI.Bus> busses, backendAPI api) {
        super(context, R.layout.bus_list_item, busses);
        this.context = context;
        this.busses = busses;
        this.api = api;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        String time = "-";
        String service = busses.get(position).name;
        boolean dir = busses.get(position).direction;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.bus_empty, parent, false);


        //TODO:Remove comments when Live stuff is sorted
        if(api.isActive(service, dir)) {

            rowView = inflater.inflate(R.layout.bus_list_item, parent, false);

            dataTime busTime = api.timeOfArrivalDelay( service, dir );

            if (busTime != null) {
                time = Integer.toString(busTime.minutes);
            }
            TextView textView = (TextView) rowView.findViewById(R.id.busName);
            textView.setText(service);

            textView = (TextView) rowView.findViewById(R.id.busTime);
            textView.setText(time);

            textView = (TextView) rowView.findViewById(R.id.busDirection);
            if(dir){
                textView.setText("Inbound");
            }else {
                textView.setText("Outbound");
            }
        }
        return rowView;
    }
}
