package com.example.app;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ExecuteSqlTask extends AsyncTask<String, Void, String> {
    public interface ExecuteSqlListener {
        void onSqlExecuted(String result);
    }

    private ExecuteSqlListener listener;

    public ExecuteSqlTask(ExecuteSqlListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String query = params[0];
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(config.API_EXECUTE_SQL_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoOutput(true);

            String postData = "servername=" + URLEncoder.encode(config.ADDRESS, "UTF-8") +
                    "&username=" + URLEncoder.encode(config.DBUSER, "UTF-8") +
                    "&password=" + URLEncoder.encode(config.DB_PASS, "UTF-8") +
                    "&dbname=" + URLEncoder.encode(config.DATABASE, "UTF-8") +
                    "&query=" + URLEncoder.encode(query, "UTF-8");

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
        Log.d("ExecuteSqlTask", "Response from server: " + result);
        fun.saveToFile(result);
        if (listener != null) {
            listener.onSqlExecuted(result);
        }
    }
}
