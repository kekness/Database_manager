package com.example.app;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


//THIS CLASS INSERTS THE JSON FILE TO THE DATABASE
public class SaveDataAsyncTask extends AsyncTask<File, Void, String> {

    @Override
    protected String doInBackground(File... files) {
        if (files.length == 0) {
            return null;
        }

        File jsonFile = files[0];

        try {
            URL url = new URL(config.API_INSERTDATA_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);

            // Read json.data from the file
            String json = readFile(jsonFile);

            urlConnection.getOutputStream().write(json.getBytes());

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

    private String readFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
