package com.group9.getmethere.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.group9.getmethere.R;
import com.group9.getmethere.adapters.BusListAdapter;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.group9.getmethere.fragments.NewsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link com.group9.getmethere.fragments.NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    OnBusSelectedListener mCallback;

    private static final String ARG_SECTION_NUMBER = "section_number";

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
        public void onBusSelected(int i);
        public void onBusSelected(String title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        populateBuses(rootView);
        eventHandle(rootView);
        //TestFRContract.FeedEntry.TestFRDbHelper mDbHelper = new TestFRContract.FeedEntry.TestFRDbHelper(rootView.getContext());

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


    public void populateBuses(final View rootView){
        BusListAdapter recAdapter = new BusListAdapter(this.getActivity());

        // ListViews display data in a scrollable list
        ListView NewsListView = (ListView) rootView.findViewById(R.id.news_list);

        // Tells the ListView what data to use
        NewsListView.setAdapter(recAdapter);
    }

    public void eventHandle(View rootView){
        ListView theListView = (ListView) rootView.findViewById(R.id.news_list);
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String title = ((TextView) view.findViewById(R.id.busName)).getText().toString();

                mCallback.onBusSelected(title);
            }
        });
    }

}
