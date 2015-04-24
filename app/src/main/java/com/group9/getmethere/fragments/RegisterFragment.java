package com.group9.getmethere.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.group9.getmethere.MainActivity;
import com.group9.getmethere.backend.SQLiteHandler;
import com.group9.getmethere.backend.UserFunctions;
import com.group9.getmethere.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class RegisterFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private EditText emailInput;
    private EditText passwordInput;
    private Button registerBtn;

    public static BusFragment newInstance(int sectionNumber) {
        BusFragment fragment = new BusFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        return fragment;

    }

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_news, container, false);



        getActivity().getActionBar().hide();
        getActivity().setContentView(R.layout.fragment_register);
        emailInput = (EditText) rootView.findViewById(R.id.email);
        passwordInput = (EditText) rootView.findViewById(R.id.password);
        registerBtn = (Button) rootView.findViewById(R.id.register);


        return rootView;
    }

    public void onButtonClick(View v){
        if ( ( !emailInput.getText().toString().equals("")) && ( !passwordInput.getText().toString().equals("")) ) {

                NetAsync(v);
        }
        else {

            Toast.makeText(getActivity().getApplicationContext(),
                    "One or more fields are empty", Toast.LENGTH_SHORT).show();
        }

    }

    public void onButtonClickLogin(View v){
        Intent myIntent = new Intent(v.getContext(), MainActivity.class);
        startActivity(myIntent);
        getActivity().finish();
    }

    private class NetCheck extends AsyncTask <String, Void, Boolean> {

        private ProgressDialog nDialog;
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(getActivity().getBaseContext());
            nDialog.setMessage("Loading..");
            nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }
        @Override
        protected Boolean doInBackground(String... args){

            //check for working internet connection

            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                } catch (MalformedURLException e1) {

                    e1.printStackTrace();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean connect){
            if(connect == true){
                nDialog.dismiss();
                new ProcessRegister().execute();
            }
            else{
                nDialog.dismiss();
                Toast.makeText(getActivity().getApplicationContext(),
                        "Error connecting to the network", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ProcessRegister extends AsyncTask <String, Void, JSONObject> {

        private ProgressDialog pDialog;
        private String email;
        private String password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            email = emailInput.getText().toString();

            password = passwordInput.getText().toString();
            pDialog = new ProgressDialog(getActivity().getBaseContext());
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Registering ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            UserFunctions userFunction = new UserFunctions();
            JSONObject json = userFunction.registerUser(email, password);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

             //Check for successful register.

            try {
                if (json.getString("success") != null) {

                    String success = json.getString("success");
                    String fail = json.getString("error");
                    if (Integer.parseInt(success) == 1) {
                        pDialog.setTitle("Getting Data");
                        pDialog.setMessage("Loading Info");

                        SQLiteHandler db = new SQLiteHandler(getActivity().getApplicationContext());
                        JSONObject json_user = json.getJSONObject("user");

                        db.addUser(json_user.getString("email"));

                        //load home page
                        Intent registered = new Intent(getActivity().getApplicationContext(), MainActivity.class);

                        pDialog.dismiss();
                        startActivity(registered);
                        getActivity().finish();
                    } else if (Integer.parseInt(fail) == 2) {
                        pDialog.dismiss();
                        Toast.makeText(getActivity().getApplicationContext(),
                                "The information you entered already exists", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    pDialog.dismiss();
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Error occurred while registering", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void NetAsync(View view){
        new NetCheck().execute();
    }
}
