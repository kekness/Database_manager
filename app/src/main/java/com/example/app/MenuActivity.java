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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    public static Context context;
    Button createTable_Button;
    Button settings_Button;
    Button logout_Button;
    ListView tablesListView;
    static ArrayAdapter<String> tablesAdapter;
    static ArrayList<String> tablesList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        context = this;

        logout_Button=findViewById(R.id.logoutButton);
        createTable_Button = findViewById(R.id.createTableButton);
        settings_Button = findViewById(R.id.settingsButton);
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

        logout_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this,LoginActivity.class));
            }
        });

        tablesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedTable = tablesList.get(position);
                config.TABLENAME = clickedTable;
                startActivity(new Intent(MenuActivity.this, MainActivity.class));
            }
        });

        // get tables from database to listview
        new FetchTablesTask().execute();
    }

    private void showCreateTableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Table");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_addtable, null);
        builder.setView(dialogView);

        final EditText tableNameEditText = dialogView.findViewById(R.id.tableNameEditText);
        final EditText columnCountEditText = dialogView.findViewById(R.id.columnCountEditText);

        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tableName = tableNameEditText.getText().toString();
                String columnCountStr = columnCountEditText.getText().toString();
                if (!tableName.isEmpty() && !columnCountStr.isEmpty()) {
                    int columnCount = Integer.parseInt(columnCountStr);
                    showDefineColumnsDialog(tableName, columnCount);
                } else {
                    Toast.makeText(MenuActivity.this, "Please enter table name and column count", Toast.LENGTH_SHORT).show();
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

    private void showDefineColumnsDialog(String tableName, int columnCount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Define Columns");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_definecolumns, null);
        builder.setView(dialogView);

        LinearLayout columnsContainer = dialogView.findViewById(R.id.columnsContainer);
        ArrayList<EditText> columnNames = new ArrayList<>();
        ArrayList<Spinner> columnTypes = new ArrayList<>();

        for (int i = 0; i < columnCount; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            EditText columnNameEditText = new EditText(this);
            columnNameEditText.setHint("Column Name " + (i + 1));
            columnNames.add(columnNameEditText);
            row.addView(columnNameEditText);

            Spinner columnTypeSpinner = new Spinner(this);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.column_types, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            columnTypeSpinner.setAdapter(adapter);
            columnTypes.add(columnTypeSpinner);
            row.addView(columnTypeSpinner);

            columnsContainer.addView(row);
        }

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ArrayList<Map<String, String>> columns = new ArrayList<>();
                for (int i = 0; i < columnCount; i++) {
                    Map<String, String> column = new HashMap<>();
                    column.put("name", columnNames.get(i).getText().toString().trim());
                    column.put("type", columnTypes.get(i).getSelectedItem().toString());
                    columns.add(column);
                }
                new CreateTableTask().execute(tableName, columns);
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

    private class CreateTableTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            String tableName = (String) params[0];
            ArrayList<Map<String, String>> columns = (ArrayList<Map<String, String>>) params[1];
            String result = "";
            try {
                URL url = new URL(config.API_CREATETABLE_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setDoOutput(true);

                StringBuilder columnDefinitions = new StringBuilder();
                for (Map<String, String> column : columns) {
                    columnDefinitions.append(column.get("name")).append(" ").append(column.get("type")).append(",");
                }
                columnDefinitions.setLength(columnDefinitions.length() - 1); // Remove last comma

                String postData = "servername=" + URLEncoder.encode(config.ADDRESS, "UTF-8") +
                        "&username=" + URLEncoder.encode(config.DBUSER, "UTF-8") +
                        "&password=" + URLEncoder.encode(config.DB_PASS, "UTF-8") +
                        "&dbname=" + URLEncoder.encode(config.DATABASE, "UTF-8") +
                        "&tablename=" + URLEncoder.encode(tableName, "UTF-8") +
                        "&columns=" + URLEncoder.encode(columnDefinitions.toString(), "UTF-8");

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
