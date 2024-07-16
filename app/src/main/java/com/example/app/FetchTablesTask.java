package com.example.app;

import static com.example.app.MenuActivity.tablesAdapter;
import static com.example.app.MenuActivity.tablesList;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.app.MenuActivity;
import com.example.app.config;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class FetchTablesTask extends AsyncTask<Void, Void, String> {

    @Override
    protected String doInBackground(Void... params) {
        String result = "";
        try {
            //connection
            URL url = new URL(config.API_GETTABLES_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoOutput(true);

            //create data to POST to API
            String postData = "servername=" + URLEncoder.encode(config.ADDRESS, "UTF-8") +
                    "&username=" + URLEncoder.encode(config.DBUSER, "UTF-8") +
                    "&password=" + URLEncoder.encode(config.DB_PASS, "UTF-8") +
                    "&dbname=" + URLEncoder.encode(config.DATABASE, "UTF-8");

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(postData);
            writer.flush();
            writer.close();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            result = response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d("FetchTablesTask", "Response from server: " + result);
        try {
            JSONArray jsonArray = new JSONArray(result);
            tablesList.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                tablesList.add(jsonArray.getString(i));
            }
            tablesAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MenuActivity.context, "Error parsing server response", Toast.LENGTH_SHORT).show();
        }
    }
}