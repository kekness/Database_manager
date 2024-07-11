package com.example.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    public static Context context;
    Button createTable_Button;
    Button settings_Button;
    ListView tablesListView;
    static ArrayAdapter<String> tablesAdapter;
    static ArrayList<String> tablesList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        context=this;

        createTable_Button = findViewById(R.id.createTableButton);
        settings_Button=findViewById(R.id.settingsButton);
        tablesListView = findViewById(R.id.tablesListView);

        tablesList = new ArrayList<>();
        tablesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tablesList);
        tablesListView.setAdapter(tablesAdapter);

        createTable_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateTableDialog();
            }
        });

        settings_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsDialog settingsDialog = new SettingsDialog(MenuActivity.this);
                settingsDialog.showSettingsDialog();
            }
        });

        tablesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedTable = tablesList.get(position);
                config.TABLENAME=clickedTable;
                startActivity(new Intent(MenuActivity.this, MainActivity.class));
            }
        });

        //get tables from database to listview
        new FetchTablesTask().execute();
    }

    private void showCreateTableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Table");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_addtable, null);
        builder.setView(dialogView);

        final EditText tableNameEditText = dialogView.findViewById(R.id.tableNameEditText);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tableName = tableNameEditText.getText().toString().trim();
                if (!tableName.isEmpty()) {
                    new CreateTableTask().execute(tableName);
                } else {
                    Toast.makeText(MenuActivity.this, "Please enter table name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class CreateTableTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String tableName = params[0];
            String result = "";
            try {
                URL url = new URL(config.API_CREATETABLE_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setDoOutput(true);

                String postData = "servername=" + URLEncoder.encode(config.ADDRESS, "UTF-8") +
                        "&username=" + URLEncoder.encode(config.DBUSER, "UTF-8") +
                        "&password=" + URLEncoder.encode(config.DB_PASS, "UTF-8") +
                        "&dbname=" + URLEncoder.encode(config.DATABASE, "UTF-8") +
                        "&tablename=" + URLEncoder.encode(tableName, "UTF-8");

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
            Log.d("CreateTableTask", "Response from server: " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                boolean success = jsonObject.getBoolean("success");
                String message = jsonObject.getString("message");
                Toast.makeText(MenuActivity.this, message, Toast.LENGTH_LONG).show();
                if (success) {
                    new FetchTablesTask().execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MenuActivity.this, "Error parsing server response", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
