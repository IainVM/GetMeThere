package com.group9.getmethere.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.group9.getmethere.MainActivity;
import com.group9.getmethere.R;
import com.group9.getmethere.adapters.BusListAdapter;

// Backend-related imports
import com.group9.getmethere.backend.backendAPI;
import android.util.Log;
import java.util.ArrayList;
//

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.group9.getmethere.fragments.NewsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link com.group9.getmethere.fragments.NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragment extends Fragment {

    // Log
    private static final String TAG = "GetMeThere [NewsFragment] ";

    OnBusSelectedListener mCallback;

    private static final String ARG_SECTION_NUMBER = "section_number";

    // Backend-related members
    private backendAPI bAPI = null;
    updateLoop uL = null;
    public ProgressBar spinner;
    //

    private ListView NewsListView;
    public ArrayList<backendAPI.Bus> busses = new ArrayList<backendAPI.Bus>();
    private View rootView = null;
    //

    public static NewsFragment newInstance(int sectionNumber) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        return fragment;

    }

    public NewsFragment() {
        // Required empty public constructor
    }

    public interface OnBusSelectedListener {
        public void onBusSelected(backendAPI.Bus bus);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_news, container, false);

        spinner = (ProgressBar)rootView.findViewById(R.id.progressBar);

        populateBuses(rootView);
        eventHandle(rootView);

        return rootView;
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnBusSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnBusSelectedListener");
        }
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onPause(){
        super.onPause();
        // updateLoop kill code
        // Iain: does the updateLoop need to be instantiated in onAttach? Or is onCreate
        //  the right place for it? I'm unsure :)
        Log.i( TAG, "[onDetach] Trying to kill updateLoop..." );
        if( uL != null ) {
            uL.kill();
            Log.i( TAG, "[onDetach] Success." );
        }
        else
            Log.e( TAG, "[onDetach] ERROR: Attempted to kill non-existent updateLoop!" );
        //

    }

    @Override
    public void onDetach() {
        super.onDetach();
        OnFragmentInteractionListener mListener = null;
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


    public void populateBuses(final View rootView){

        BusListAdapter recAdapter = new BusListAdapter(this.getActivity(), busses, bAPI);

        // ListViews display data in a scrollable list
        NewsListView = (ListView) rootView.findViewById(R.id.news_list);

        // Tells the ListView what data to use
        NewsListView.setAdapter(recAdapter);

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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                spinner.setVisibility(View.GONE);

                            }
                        });
                        Log.i(TAG, "[updateLoop] Getting data for ArrayLists from backendAPI...");

                        // Iain: I'd rather be sending a <Bus> ArrayList to recAdapter since you can
                        //  simply plug the backend into it then - but otherwise, this bodge will
                        //  fill the relevant arrays
                        //TODO: change out the adapter to use Bus Array Lists
                        int numSvcs = bAPI.services();
                        busses = bAPI.busses;

                        // Debugging output
                        //Log.i( TAG, "[updateLoop] Debug: Adding service " + busNames.get( i ) );

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                populateBuses(rootView);

                            }
                        });

                        Log.i(TAG, "[updateLoop] Done!");
                    }else{
                        spinner.setVisibility(View.VISIBLE);
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


    public void eventHandle(View rootView){

        NewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                backendAPI.Bus bus = (backendAPI.Bus) adapterView.getAdapter().getItem(i);

                mCallback.onBusSelected(bus);
            }
        });
    }

}
