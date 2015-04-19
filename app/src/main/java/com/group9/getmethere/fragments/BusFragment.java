package com.group9.getmethere.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.group9.getmethere.MainActivity;
import com.group9.getmethere.R;
import com.group9.getmethere.backend.backendAPI;
import com.group9.getmethere.backend.dataTime;

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

    private String time = "-";
    private String service = bus.name;
    private boolean dir = bus.direction;

    updateLoop uL = null;

    // Log
    private static final String TAG = "GetMeThere [BusFragment] ";
    private View rootView;

    // Backend-related members
    private backendAPI bAPI = null;
    //

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
        rootView = inflater.inflate(R.layout.fragment_bus, container, false);

        Intent i = getActivity().getIntent();

        // Instantiate the backend
        bAPI = ((MainActivity)this.getActivity()).backEnd();

        bus = (backendAPI.Bus) i.getSerializableExtra("bus");

        populateInfo(rootView);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume(){
        super.onResume();

        // Instantiate the backend
        bAPI = ((MainActivity)this.getActivity()).backEnd();
        //

        // Backend related - start a thread to handle updating of the ListView
        uL = new updateLoop();
        Thread updateThread = new Thread(uL);
        updateThread.start();
        //
    }

    @Override
    public void onPause(){
        super.onPause();
        // updateLoop kill code
        Log.i( TAG, "[onDetach] Trying to kill updateLoop..." );
        if( uL != null ) {
            uL.kill();
            Log.i( TAG, "[onDetach] Success." );
        }
        else
            Log.e( TAG, "[onDetach] ERROR: Attempted to kill non-existent updateLoop!" );
        //

    }

    // Listview updating code - run as thread from within populateBuses()
    public class updateLoop implements Runnable {

        private volatile boolean active = true;

        public void kill() {
            active = false;
        }

        public void run() {
            while( active ) {
                Log.i( TAG, "[updateLoop] Running" );

                if( bAPI != null ) {
                    if (bAPI.isReady()) {
                        Log.i(TAG, "[updateLoop] Getting data for ArrayLists from backendAPI...");

                        // Iain: I'd rather be sending a <Bus> ArrayList to recAdapter since you can
                        //  simply plug the backend into it then - but otherwise, this bodge will
                        //  fill the relevant arrays
                        //TODO: change out the adapter to use Bus Array Lists
                        int numSvcs = bAPI.services();
                        dataTime busTime = bAPI.timeOfArrivalDelay( service, dir );

                        if( busTime!= null) {
                            time = Integer.toString(busTime.minutes);
                        }

                        // Debugging output
                        //Log.i( TAG, "[updateLoop] Debug: Adding service " + busNames.get( i ) );

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                updateMap(rootView);
                                populateInfo(rootView);

                            }
                        });
                        Log.i(TAG, "[updateLoop] Done!");
                    }

                    try {
                        Thread.currentThread().sleep(5000);   // THIS CONTROLS THE UPDATE FREQUENCY
                    } catch (InterruptedException e) {
                        Log.e(TAG, "[updateLoop] Interrupted Exception " + e);
                    }
                }
            }
        }
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

    public void updateMap(View rootView){
        //TODO:Method for updating map
        //service is a instance variable
        //dir is an instance variable

    }

    public void populateInfo(View rootView){

        dataTime busTime = bAPI.timeOfArrivalDelay( service, dir );

        if( busTime!= null) {
            time = Integer.toString(busTime.minutes);
        }

            TextView busName = (TextView) rootView.findViewById(R.id.busName);
            busName.setText(this.bus.name);

            TextView busTo = (TextView) rootView.findViewById(R.id.busTo);
            busTo.setText(this.bus.to);

            TextView busFrom = (TextView) rootView.findViewById(R.id.busFrom);
            busFrom.setText(this.bus.from);

            TextView busDelayTime = (TextView) rootView.findViewById(R.id.busDelayTime);
            busDelayTime.setText(time);

    }

}
