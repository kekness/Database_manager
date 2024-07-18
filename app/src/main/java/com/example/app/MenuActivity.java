package com.example.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity implements TablesAdapter.OnTableDeletedListener,FetchDataTask.FetchDataListener{

    public static Context context;
    Button createTable_Button;
    Button settings_Button;
    Button logout_Button;
    Button exportButton;
    Button importButton;
    ListView tablesListView;
    static TextView infoTv;
    static TablesAdapter tablesAdapter;
    static ArrayList<String> tablesList;
    public static File jsonFile;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        context = this;

        importButton=findViewById(R.id.import_button);
        logout_Button = findViewById(R.id.logoutButton);
        createTable_Button = findViewById(R.id.createTableButton);
        settings_Button = findViewById(R.id.settingsButton);
        tablesListView = findViewById(R.id.tablesListView);
        infoTv=findViewById(R.id.info_menu_TV);
        exportButton=findViewById(R.id.export_button);

        tablesList = new ArrayList<>();
        tablesAdapter = new TablesAdapter(this, tablesList,this);
        tablesListView.setAdapter(tablesAdapter);

        infoTv.setText("Tables in "+config.DATABASE);
        //get actual data from saved file
        jsonFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "data.json");

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        createTable_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateTableDialog();
            }
        });

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
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
                startActivity(new Intent(MenuActivity.this, LoginActivity.class));
            }
        });

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               config.TABLENAME="t2";

              fetchDataFromServer();
            }
        });

        new FetchTablesTask().execute();


    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 3);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            showImportTableDialog(uri);
            assert uri != null;
            Log.d("nw czyddziala",uri.toString());
        }
    }
    private void showImportTableDialog(Uri uri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Import Table");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_importtable, null);
        builder.setView(dialogView);

        final EditText tableNameEditText = dialogView.findViewById(R.id.tableNameEditText);

        builder.setPositiveButton("Import", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tableName = tableNameEditText.getText().toString();
                if (!tableName.isEmpty()) {
                    ArrayList<Map<String, String>> columns = parseCSVAndGetColumns(uri);
                    for (int i = 0; i < columns.size(); i++) {
                        Log.d("Columns", "Row " + i + ": " + columns.get(i).toString());
                    }
                    if (columns != null && !columns.isEmpty()) {
                        new CreateTableTask().execute(tableName, columns);
                    } else {
                        Toast.makeText(MenuActivity.this, "Error parsing CSV file", Toast.LENGTH_SHORT).show();
                    }
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

        builder.show();
    }


    private ArrayList<Map<String, String>> parseCSVAndGetColumns(Uri uri) {
        ArrayList<Map<String, String>> columns = new ArrayList<>();

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Read the first line to get column headers
            String line = reader.readLine();
            if (line != null) {
                // Split line by comma, ignoring commas inside quotes
                String[] headers = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                // Remove surrounding quotes and trim headers
                for (int i = 0; i < headers.length; i++) {
                    headers[i] = headers[i].replaceAll("^\"|\"$", "").trim();
                }

                // Assume headers are names of columns, adjust for your CSV format if needed
                for (String header : headers) {
                    // Check if the header is "id" and skip adding it to columns
                    if (!header.equalsIgnoreCase("id")) {
                        Map<String, String> column = new HashMap<>();
                        column.put("name", header);
                        column.put("type", "TEXT"); // Assuming default type is TEXT
                        columns.add(column);
                    }
                }
            }

            reader.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return columns;
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
                for (int i = 0; i < columns.size(); i++)
                    Log.d("Columns2", "Row " + i + ": " + columns.get(i).toString());
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
    @Override
    public void onTableDeleted(int position) {
        tablesList.remove(position);
        tablesAdapter.notifyDataSetChanged();
    }
    public void fetchDataFromServer()
    {
        new FetchDataTask(this).execute(config.API_GETDATA_URL);
    }
    @Override
    public void onDataFetched(String result) {
        Log.d("SomeClass", "Response from server: " + result);
        fun.exportDataToCSV(config.TABLENAME,jsonFile);
        Toast.makeText(MenuActivity.this,"Table exported to /Download/"+config.TABLENAME+".csv",Toast.LENGTH_SHORT).show();
    }
}
