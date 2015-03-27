package com.group9.getmethere.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.group9.getmethere.R;
import com.group9.getmethere.backend.backendAPI;



public class BusListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] busNames;
    private final String[] busTos;
    private final String[] busFroms;


    public BusListAdapter(Context context, String[] busNames, String[] busFroms, String[] busTos) {
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
        textView.setText(busNames[position]);
        textView = (TextView) rowView.findViewById(R.id.busTo);
        textView.setText(busTos[position]);
        textView = (TextView) rowView.findViewById(R.id.busFrom);
        textView.setText(busFroms[position]);

        return rowView;
    }

}
