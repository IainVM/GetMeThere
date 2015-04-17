package com.group9.getmethere.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.group9.getmethere.R;
import com.group9.getmethere.backend.backendAPI;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.group9.getmethere.fragments.NewsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link com.group9.getmethere.fragments.NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BusFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private backendAPI.Bus bus;

    public static BusFragment newInstance(int sectionNumber) {
        BusFragment fragment = new BusFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        return fragment;

    }

    public BusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bus, container, false);

        Intent i = getActivity().getIntent();

        bus = (backendAPI.Bus) i.getSerializableExtra("bus");

        populateInfo(rootView);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }


    public void populateInfo(View rootView){

        //TODO:Logic to display this bus numbers info using the API
        TextView busName = (TextView) rootView.findViewById(R.id.busName);
        busName.setText(this.bus.name);

        TextView busTo = (TextView) rootView.findViewById(R.id.busTo);
        busTo.setText(this.bus.to);

        TextView busFrom = (TextView) rootView.findViewById(R.id.busFrom);
        busFrom.setText(this.bus.from);

    }

}
