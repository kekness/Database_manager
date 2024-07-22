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

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.net.Uri;

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
    public static File importFile;
    public String exportname;

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
        importFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "import.json");

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
                showExportDialog();
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
            convertCSVFromUriToJson(uri);
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
                        config.TABLENAME=tableName;
                        new SaveDataAsyncTask().execute(importFile);
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

    private void showExportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export Table");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_exporttable, null);
        builder.setView(dialogView);

        Spinner tablesSpinner = dialogView.findViewById(R.id.spinnerTables);
        EditText exportName = dialogView.findViewById(R.id.tableNameEditText);

        // Create an ArrayAdapter using the list of tables
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tablesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tablesSpinner.setAdapter(adapter);

        builder.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedTable = tablesSpinner.getSelectedItem().toString();
                exportname = exportName.getText().toString();

                if (!exportname.isEmpty()) {
                    config.TABLENAME = selectedTable;
                    fetchDataFromServer();
                } else {
                    Toast.makeText(MenuActivity.this, "Please enter export name", Toast.LENGTH_SHORT).show();
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

    private ArrayList<Map<String, String>> parseCSVAndGetColumns(Uri uri) {
        ArrayList<Map<String, String>> columns = new ArrayList<>();

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            // Read the first line to determine the separator
            String firstLine = bufferedReader.readLine();
            char separator = fun.detectSeparator(firstLine);

            // Close and reopen the InputStream to reset it
            bufferedReader.close();
            streamReader.close();
            inputStream.close();

            inputStream = getContentResolver().openInputStream(uri);
            streamReader = new InputStreamReader(inputStream);
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(separator)
                    .withIgnoreQuotations(false)
                    .build();

            CSVReader csvReader = new CSVReaderBuilder(streamReader)
                    .withCSVParser(parser)
                    .build();

            // Re-read the first line to get column headers
            String[] headers = csvReader.readNext();
            if (headers != null) {
                // Remove surrounding quotes and trim headers
                for (int i = 0; i < headers.length; i++) {
                    headers[i] = headers[i].replaceAll("^\"|\"$", "").trim();
                }

                // Assume headers are names of columns, adjust for your CSV format if needed
                for (String header : headers) {
                    // Check if the header is "id" or "index" and skip adding it to columns
                    if (!header.equalsIgnoreCase("index") && !header.equalsIgnoreCase("id")) {
                        Map<String, String> column = new HashMap<>();
                        column.put("name", header);
                        column.put("type", "TEXT"); // Assuming default type is TEXT
                        columns.add(column);
                    }
                }
            }

            csvReader.close();
            streamReader.close();
            inputStream.close();

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }

        return columns;
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
                    String columnName = column.get("name").replace(" ", "_"); // Replace spaces with underscores

                    // Add underscore if column name starts with a number
                    if (columnName.matches("^\\d.*")) {
                        columnName = "_" + columnName;
                    }

                    Log.d("ColumnName", "Original: " + column.get("name") + " | Modified: " + columnName);
                    columnDefinitions.append(columnName).append(" ").append(column.get("type")).append(",");
                }

                // Log column definitions before removing the last comma
                Log.d("ColumnDefinitions", columnDefinitions.toString());

                if (columnDefinitions.length() > 0) {
                    columnDefinitions.setLength(columnDefinitions.length() - 1); // Remove last comma
                }

                String postData = "servername=" + URLEncoder.encode(config.ADDRESS, "UTF-8") +
                        "&username=" + URLEncoder.encode(config.DBUSER, "UTF-8") +
                        "&password=" + URLEncoder.encode(config.DB_PASS, "UTF-8") +
                        "&dbname=" + URLEncoder.encode(config.DATABASE, "UTF-8") +
                        "&tablename=" + URLEncoder.encode(tableName, "UTF-8") +
                        "&columns=" + URLEncoder.encode(columnDefinitions.toString(), "UTF-8");

                Log.d("PostData", postData); // Log the post data to see what is being sent

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

                // Log the server response
                Log.d("ServerResponse", result);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("CreateTableTask", "Error: " + e.getMessage());
                result = e.getMessage();
            }
            return result;
        }

        // Interfaces executed after the data is downloaded from server
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


    private void convertCSVFromUriToJson(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            // Convert CSV to JSON
            String jsonString = convertCSVToJson(reader);

            // Save JSON to file in the public Documents directory
            File jsonFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "import.json");
            fun.saveJsonToFile(jsonString, jsonFile);

            Log.d("CSVtoJSON", "JSON file saved at: " + jsonFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Convert CSV data from BufferedReader to JSON string
// Convert CSV data from BufferedReader to JSON string
    private String convertCSVToJson(BufferedReader reader) throws IOException {
        JSONArray jsonArray = new JSONArray();
        String firstLine = reader.readLine();
        if (firstLine == null) {
            return jsonArray.toString(); // Empty CSV
        }

        // Detect separator
        char separator = fun.detectSeparator(firstLine);
        String[] headers = firstLine.split(String.valueOf(separator));

        // Initialize OpenCSV CSVReader
        try (CSVReader csvReader = new CSVReaderBuilder(reader)
                .withSkipLines(0) // Read all lines, including the header
                .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                .build()) {

            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                if (nextLine.length != headers.length) {
                    Log.d("CSVtoJSON", "Skipping malformed line: " + Arrays.toString(nextLine));
                    continue;
                }

                JSONObject jsonObject = new JSONObject();
                for (int i = 0; i < headers.length; i++) {
                    // Clean and adjust header name
                    String header = headers[i].replaceAll("^\"|\"$", "").trim();

                    // Add underscore if header starts with a number
                    if (header.matches("^\\d.*")) {
                        header = "_" + header;
                    }

                    // Replace spaces with underscores
                    header = header.replace(" ", "_");

                    // Trim and assign value
                    String value = nextLine[i].replaceAll("^\"|\"$", "").trim();

                    // Exclude columns with names "id" and "index" from the JSON object
                    if (!header.equalsIgnoreCase("id") && !header.equalsIgnoreCase("index")) {
                        jsonObject.put(header, value);
                    }
                }
                jsonArray.put(jsonObject);
            }

        } catch (CsvValidationException | JSONException e) {
            e.printStackTrace();
        }

        return jsonArray.toString();
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
        fun.exportDataToCSV(config.TABLENAME,jsonFile,exportname);
        Toast.makeText(MenuActivity.this,"Table exported to /Download/"+exportname+".csv",Toast.LENGTH_SHORT).show();
    }
}
