package com.example.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements FetchDataTask.FetchDataListener, ExecuteSqlTask.ExecuteSqlListener {

    private Button getButton;
    private Button addButton;
    private Button syncButton;
    private Button viewButton;
    private Button filterButton;
    private Button menuButton;
    private String url = config.API_GETDATA_URL;
    private EditText filterText;
    private Spinner columnSpinner;
    private boolean filter_visibility = false;
    private TableLayout tableLayout;
    private ArrayList<String> columnList = new ArrayList<>();
    private ArrayList<String> queryList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private TextView editTableName;
    private EditText sql_query_ET;
    private TextView records_number_TV;
    private Button executeButton;
    public File sqlFile;
    public String tableshown;
    private Spinner sqlSpinner;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executeButton = findViewById(R.id.sql_execute_button);
        menuButton = findViewById(R.id.menuButton);
        filterText = findViewById(R.id.editTextFilter);
        getButton = findViewById(R.id.myButton);
        addButton = findViewById(R.id.myButton2);
        syncButton = findViewById(R.id.syncButton);
        viewButton = findViewById(R.id.viewButton);
        filterButton = findViewById(R.id.filterButton);
        columnSpinner = findViewById(R.id.spinner);
        tableLayout = findViewById(R.id.tableLayout);
        editTableName = findViewById(R.id.editTableName);
        sql_query_ET = findViewById(R.id.sql_querry_ET);
        records_number_TV = findViewById(R.id.number_of_records_TV);
        sqlSpinner = findViewById(R.id.sqlhistorySpinner);

        columnSpinner.setVisibility(View.INVISIBLE);
        filterText.setVisibility(View.INVISIBLE);

        sqlFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sql.json");

        Intent intent = getIntent();
        editTableName.setText(intent.getStringExtra("tableName"));

        //button functions
        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDataFromServer(url);
            }
        });

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray jsonArray;
                if (tableshown == "data.json")
                    jsonArray = fun.fileToJsonArray(MenuActivity.jsonFile);
                else
                    jsonArray = fun.fileToJsonArray(sqlFile);
                if (filter_visibility) {
                    String selectedColumn = columnSpinner.getSelectedItem().toString();
                    String filterValue = filterText.getText().toString();
                    jsonArray = fun.filterJsonArray(jsonArray, selectedColumn, filterValue);
                }
                records_number_TV.setText("Found " + jsonArray.length() + " records");
                updateTableLayout(jsonArray);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog(MenuActivity.jsonFile);

            }
        });

        executeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeSqlQuery(sql_query_ET.getText().toString());
                loadSqlSpinner(sql_query_ET.getText().toString());
            }
        });

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("przycisk", "dzialam");
                new SaveDataAsyncTask().execute(MenuActivity.jsonFile); //UPLOAD DATA
                fetchDataFromServer(url); //DOWNLOAD DATA
            }
        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterText.isShown()) {
                    filterButton.setText("filter: OFF");
                    filterText.setVisibility(View.GONE);
                    columnSpinner.setVisibility(View.GONE);
                    filter_visibility = false;
                } else {
                    filterButton.setText("filter: ON");
                    filterText.setVisibility(View.VISIBLE);
                    columnSpinner.setVisibility(View.VISIBLE);
                    filter_visibility = true;
                }
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MenuActivity.class));
            }
        });



        // Set up the listener for sqlSpinner
        sqlSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedQuery = (String) parent.getItemAtPosition(position);
                sql_query_ET.setText(selectedQuery);
             //   executeSqlQuery(sql_query_ET.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        fetchDataFromServer(url);
    }

    public void updateTableLayout(JSONArray jsonArray) {
        tableLayout.removeAllViews();

        //add headers to table
        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        //get headers
        try {
            if (jsonArray.length() > 0) {
                JSONObject firstObject = jsonArray.getJSONObject(0);
                Iterator<String> keys = firstObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();

                    TextView headerTextView = new TextView(this);
                    headerTextView.setText(key);
                    headerTextView.setPadding(10, 10, 10, 10);
                    headerTextView.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    headerTextView.setBackgroundColor(Color.LTGRAY);
                    headerTextView.setTextColor(Color.BLACK);
                    headerTextView.setGravity(Gravity.CENTER);

                    headerRow.addView(headerTextView);
                }
                tableLayout.addView(headerRow);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // add actual data
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));

                // make rows clickable
                final int position = i;
                tableRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditDialog(jsonObject, position, jsonArray);
                    }
                });

                Iterator<String> it = jsonObject.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    String value = jsonObject.optString(key);

                    TextView textView = new TextView(this);
                    textView.setText(value);
                    textView.setPadding(10, 10, 10, 10);
                    textView.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    textView.setGravity(Gravity.CENTER); // Center the text

                    tableRow.addView(textView);
                }

                tableLayout.addView(tableRow);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //loads column names to filter spinner
    public void loadColumnNames(File jsonFile) {
        try {
            JSONArray jsonArray = fun.fileToJsonArray(jsonFile);
            if (jsonArray.length() > 0) {
                columnList.clear();
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                    String key = it.next();
                    columnList.add(key);
                }
                spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, columnList);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                columnSpinner.setAdapter(spinnerAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //loads String to sqlSpinner
    public void loadSqlSpinner(String sqlquery) {
        queryList.add(sqlquery);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, queryList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sqlSpinner.setAdapter(spinnerAdapter);
    }

    private void showAddDialog(File jsonFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Record");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add, (LinearLayout) findViewById(R.id.dynamicContainer), false);
        LinearLayout dynamicContainer = viewInflated.findViewById(R.id.dynamicContainer);

        HashMap<String, EditText> dialogEditTextMap = new HashMap<>();

        try {
            JSONArray jsonArray = fun.fileToJsonArray(jsonFile);
            if (jsonArray.length() > 0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                    String key = it.next();

                    // Skip adding 'id' field in the dialog
                    if (key.equals("id")) {
                        continue;
                    }

                    EditText editText = new EditText(this);
                    editText.setHint("Enter " + key);
                    dynamicContainer.addView(editText);

                    dialogEditTextMap.put(key, editText);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        builder.setView(viewInflated);

        builder.setPositiveButton("Add", (dialog, which) -> {
            try {
                JSONObject newJsonObject = new JSONObject();
                for (Map.Entry<String, EditText> entry : dialogEditTextMap.entrySet()) {
                    newJsonObject.put(entry.getKey(), entry.getValue().getText().toString());
                }

                // Remove 'id' from the new record data
                newJsonObject.remove("id");

                JSONArray jsonArray = fun.fileToJsonArray(jsonFile);
                jsonArray.put(newJsonObject);
                fun.saveJsonArrayToFile(jsonArray, jsonFile);

                dialog.dismiss();
                updateTableLayout(jsonArray);
            } catch (Exception e) {
                e.printStackTrace();
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

    private void showEditDialog(JSONObject jsonObject, int position, JSONArray jsonArray) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Record");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final HashMap<String, EditText> editTextMap = new HashMap<>();

        try {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = jsonObject.getString(key);

                TextView textView = new TextView(this);
                textView.setText(key);
                layout.addView(textView);

                EditText editText = new EditText(this);
                editText.setText(value);
                layout.addView(editText);

                editTextMap.put(key, editText);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        builder.setView(layout);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    // Update the data in jsonObject based on the entered values
                    for (Map.Entry<String, EditText> entry : editTextMap.entrySet()) {
                        String key = entry.getKey();
                        String newValue = entry.getValue().getText().toString();
                        jsonObject.put(key, newValue);
                    }

                    // Update jsonArray at the appropriate index
                    jsonArray.put(position, jsonObject);

                    // Refresh the table after editing
                    updateTableLayout(jsonArray);

                    // Save changes to JSON file
                    File jsonFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "data.json");
                    fun.saveJsonArrayToFile(jsonArray, jsonFile);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
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

    private void executeSqlQuery(String query) {
        new ExecuteSqlTask(this).execute(query);
    }

    public void fetchDataFromServer(String url) {
        new FetchDataTask(this).execute(url);
    }


    //interfaces to execute certain functions after response from server

    //interface after fetching data from server
    @Override
    public void onDataFetched(String result) {
        tableshown = "data.json";
        Log.d("SomeClass", "Response from server: " + result);
        updateTableLayout(fun.fileToJsonArray(MenuActivity.jsonFile));
        loadColumnNames(MenuActivity.jsonFile);
        records_number_TV.setText("Found " + fun.fileToJsonArray(MenuActivity.jsonFile).length() + " records");
    }

    //interface after sql query executed
    @Override
    public void onSqlExecuted(String result) {
        tableshown = "sql.json";
        updateTableLayout(fun.fileToJsonArray(sqlFile));
        loadColumnNames(sqlFile);
        records_number_TV.setText("Found " + fun.fileToJsonArray(sqlFile).length() + " records");
    }
}
