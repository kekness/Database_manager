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

public class FetchDataTask extends AsyncTask<String, Void, String> {

    private String servername;
    private String username;
    private String password;
    private String dbname;
    private String tablename;
    private MainActivity mainActivity;

    public FetchDataTask(MainActivity mainActivity, String servername, String username, String password, String dbname, String tablename) {
        this.mainActivity = mainActivity;
        this.servername = servername;
        this.username = username;
        this.password = password;
        this.dbname = dbname;
        this.tablename = tablename;
    }

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoOutput(true);

            String postData = "servername=" + URLEncoder.encode(servername, "UTF-8") +
                    "&username=" + URLEncoder.encode(username, "UTF-8") +
                    "&password=" + URLEncoder.encode(password, "UTF-8") +
                    "&dbname=" + URLEncoder.encode(dbname, "UTF-8") +
                    "&tablename=" + URLEncoder.encode(tablename, "UTF-8");

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(postData);
            writer.flush();
            writer.close();
            os.close();

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

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d("FetchDataTask", "Response from server: " + result);
        try {
            JSONArray jsonArray = new JSONArray(result);
            fun.saveToFile(result);
            mainActivity.updateTableLayout(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
