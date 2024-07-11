package com.example.app;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SaveDataAsyncTask extends AsyncTask<Object, Void, String> {

    private String datatable;
    private String databasename;
    private String servername;

    public SaveDataAsyncTask() {
        this.datatable = config.TABLENAME;
        this.databasename = config.DATABASE;
        this.servername = config.ADDRESS;
    }

    @Override
    protected String doInBackground(Object... objects) {
        if (objects.length == 0) {
            return null;
        }

        File jsonFile = (File) objects[0];

        try {
            URL url = new URL(config.API_INSERTDATA_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Prepare the JSON data and additional parameters
            String json = fun.readFile(jsonFile);
            String postData = "servername=" + servername +
                    "&username=" + config.DBUSER +
                    "&password=" + config.DB_PASS +
                    "&dbname=" + databasename +
                    "&tablename=" + datatable +
                    "&json=" + json;

            // Send the POST data
            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
            writer.write(postData);
            writer.flush();
            writer.close();

            // Read the server response
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();

            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            // Handle server response here (if needed)
            Log.d("SaveDataAsyncTask", "Server response: " + result);
        } else {
            // Handle error here
            Log.e("SaveDataAsyncTask", "Error during data save");
        }
    }


}
