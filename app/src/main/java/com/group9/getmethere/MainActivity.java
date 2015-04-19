package com.group9.getmethere;

import android.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.content.res.AssetManager;

// Logging
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

// Backend imports
import com.group9.getmethere.backend.*;

// NASTY NETWORK-ACCESS TESTING BODGE IMPORT
import android.os.StrictMode;

import com.group9.getmethere.fragments.*;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        NewsFragment.OnBusSelectedListener
{

    // Log
    private static final String TAG = "GetMeThere [MainActivity] ";
    //

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;


    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Fragment fragment;

    // Backend-related members
    public backendAPI bAPI;
    private AssetManager assets;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // NASTY TEST BODGE (to allow net access from main activity - pathRequest() causes crash without this!!)
        //  Testing only - this should be resolved using proper Android protocol (TODO)
        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy( policy );


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // DJH: Set up the backend API
        assets = getAssets();
        bAPI = new backendAPI( getAssets() );

        // DJH: Start the update threads for all known services
        startUpdateThreads sUT = new startUpdateThreads();
        Thread sUTThread = new Thread( sUT );
        sUTThread.start();
        // Iain: Note you'll now need to call bAPI.checkAllUpdates() periodically from
        //  within any fragment you want updated information available for. For the list
        //  of services, for example, perhaps start a thread (see startUpdateThreads()
        //  for an example of my version of this) which calls checkAllUpdates() every few
        //  seconds whilst the view is active. checkAllUpdates() will update all the
        //  information available, and then it's just a matter of updating the view
        //  from the backend's information.

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment = LoginFragment.newInstance(0);
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        switch (position){
            case 0:
                fragment = NewsFragment.newInstance(position + 1);
                break;
            case 1:
                fragment = TicketFragment.newInstance(position + 1);
                break;
            case 2:
                fragment = MyTicketFragment.newInstance(position + 1);
                break;
            case 3:
                fragment = FavouriteFragment.newInstance(position + 1);
                break;
            case 4:
                fragment = SettingsFragment.newInstance(position + 1);
                break;
            case 5:
                fragment = LoginFragment.newInstance(position + 1);
                break;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
            case 6:
                mTitle = getString(R.string.title_section6);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Backend-related methods */
    // Return a handle to the backendAPI
    public backendAPI backEnd() {
    	return bAPI;
    }

    // Return a handle to the assets
    public AssetManager assetsHandle() {
        return assets;
    }


    @Override
    public void onBusSelected(backendAPI.Bus bus) {
        getIntent().putExtra("bus", bus);

        fragment = BusFragment.newInstance(6);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();


    }

    // Keep checking until the backend is ready, then start all the update threads
    public class startUpdateThreads implements Runnable {

        private boolean running = false;

        public void run() {
          while( !running ) {
            Log.i( TAG, "[startUpdateThreads] Running" );

            if( bAPI != null )
              if( bAPI.isReady() ) {
                Log.i( TAG, "[startUpdateThreads] Backend ready - starting update threads..." );

                bAPI.startUpdates();
                running = true;

                Log.i( TAG, "[startUpdateThreads] Done!" );
              }

            try {
              Thread.currentThread().sleep( 20000 );   // THIS CONTROLS THE CHECK FREQUENCY
            }
            catch( InterruptedException e ) {
              Log.e( TAG, "[startUpdateThreads] Interrupted Exception " + e );
            }
          }
        }
    }
    /* End of backend-related methods */
}
