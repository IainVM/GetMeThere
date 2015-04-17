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

public class BusListAdapter extends ArrayAdapter<backendAPI.Bus> {
    private final Context context;
    // Iain: I updated these from arrays to ArrayLists so they can be
    //  dynamically altered. I'd prefer to have a single <Bus>
    //  ArrayList here, but couldn't seem to pass that into the
    //  constructor - maybe <?> would work? I've not yet tried that.
    private ArrayList<backendAPI.Bus> busses;

    public BusListAdapter(Context context, ArrayList<backendAPI.Bus> busses) {
        super(context, R.layout.bus_list_item, busses);
        this.context = context;
        this.busses = busses;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.bus_list_item, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.busName);
        textView.setText(busses.get(position).name);
        textView = (TextView) rowView.findViewById(R.id.busTo);
        textView.setText(busses.get( position ).to);
        textView = (TextView) rowView.findViewById(R.id.busFrom);
        textView.setText(busses.get( position ).from);

        return rowView;
    }
}
