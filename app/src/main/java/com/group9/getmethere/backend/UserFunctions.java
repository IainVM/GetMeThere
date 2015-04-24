package com.group9.getmethere.backend;

import android.content.Context;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JMorris on 23/03/15.
 */


public class UserFunctions {
        private JSONParser jsonParser;
        //URL of the PHP file
        //if using localhost use IP of machine
        private static String loginURL = "http://localhost/login.php/";
        private static String registerURL = "http://localhost/login.php/";
        private static String login_tag = "login";
        private static String register_tag = "register";



        public UserFunctions() {
            jsonParser = new JSONParser();
        }

        //Function to login user

        public JSONObject loginUser(String email, String password){
            //parameters for login
            List params = new ArrayList();
            params.add(new BasicNameValuePair("tag", login_tag));
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", password));
            JSONObject json = jsonParser.getJSONFromUrl(loginURL, params);
            return json;
        }


        //Function to register user

        public JSONObject registerUser(String email, String password){
            //parameters for registration
            List params = new ArrayList();
            params.add(new BasicNameValuePair("tag", register_tag));

            params.add(new BasicNameValuePair("email", email));

            params.add(new BasicNameValuePair("password", password));
            JSONObject json = jsonParser.getJSONFromUrl(registerURL, params);
            return json;
        }

         //Function to logout user
         //Resets the temporary data stored in SQLite Database

        public boolean logoutUser(Context context){
            SQLiteHandler db = new SQLiteHandler(context);
            db.deleteUsers();
            return true;
        }
}

