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
//    private final backendAPI api;

    public BusListAdapter(Context context) {
        super(context, R.layout.bus_list_item);
        this.context = context;
//        this.api = new backendAPI(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.bus_list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.busName);
//        textView.setText(api.name(position));
        textView.setText("name");
        textView = (TextView) rowView.findViewById(R.id.busName);
//        textView.setText(api.to(position));
        textView.setText("to");
        textView = (TextView) rowView.findViewById(R.id.busTo);
//        textView.setText(api.from(position));
        textView.setText("to");
        textView = (TextView) rowView.findViewById(R.id.busFrom);

        return rowView;
    }

}
