package com.example.app;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

// AsyncTask to fetch data from a server and update UI
public class FetchDataTask extends AsyncTask<String, Void, String> {
    public interface FetchDataListener {
        void onDataFetched(String result);
    }

    private FetchDataListener listener;

    public FetchDataTask(FetchDataListener listener) {
        this.listener = listener;
    }

    // Background task to execute HTTP POST request
    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];
        StringBuilder result = new StringBuilder();
        try {
            // Creating URL connection
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoOutput(true);

            // Creating POST data
            String postData = "servername=" + URLEncoder.encode(config.ADDRESS, "UTF-8") +
                    "&username=" + URLEncoder.encode(config.DBUSER, "UTF-8") +
                    "&password=" + URLEncoder.encode(config.DB_PASS, "UTF-8") +
                    "&dbname=" + URLEncoder.encode(config.DATABASE, "UTF-8") +
                    "&tablename=" + URLEncoder.encode(config.TABLENAME, "UTF-8");

            // Writing POST data to output stream
            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(postData);
            writer.flush();
            writer.close();
            os.close();

            // Reading response from the server
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    // Executed after the background task completes; updates UI with server response
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d("FetchDataTask", "Response from server: " + result);

        // Save response to a file
            fun.saveToFile(result,"data.json");

            //functions from interface are done
            if (listener != null) {
                listener.onDataFetched(result);
            }

    }
}
