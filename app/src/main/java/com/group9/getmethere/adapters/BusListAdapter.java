package com.group9.getmethere.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
// DJH
import java.util.ArrayList;
//

import com.group9.getmethere.R;
import com.group9.getmethere.backend.backendAPI;



public class BusListAdapter extends ArrayAdapter<String> {
    private final Context context;
    // Iain: I updated these from arrays to ArrayLists so they can be
    //  dynamically altered. I'd prefer to have a single <Bus>
    //  ArrayList here, but couldn't seem to pass that into the
    //  constructor - maybe <?> would work? I've not yet tried that.
    private ArrayList <String> busNames;
    private ArrayList <String> busTos;
    private ArrayList <String> busFroms;


    public BusListAdapter(Context context, ArrayList <String> busNames, ArrayList <String> busFroms, ArrayList <String> busTos ) {
        super(context, R.layout.bus_list_item, busNames);
        this.context = context;
        this.busNames = busNames;
        this.busFroms = busFroms;
        this.busTos = busTos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.bus_list_item, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.busName);
        textView.setText(busNames.get( position ));
        textView = (TextView) rowView.findViewById(R.id.busTo);
        textView.setText(busTos.get( position ));
        textView = (TextView) rowView.findViewById(R.id.busFrom);
        textView.setText(busFroms.get( position ));

        return rowView;
    }
}
