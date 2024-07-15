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

public class MainActivity extends AppCompatActivity implements FetchDataTask.FetchDataListener {

    private Button getButton;
    private Button addButton;
    private Button syncButton;
    private Button viewButton;
    private Button filterButton;
    private Button menuButton;
    private String url = config.API_GETDATA_URL;
    private EditText filterText;
    private Spinner columnSpinner;
    private boolean filter_visibility = true;
    private TableLayout tableLayout;
    private ArrayList<String> columnList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private TextView editTableName;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuButton = findViewById(R.id.menuButton);
        filterText = findViewById(R.id.editTextFilter);
        getButton = findViewById(R.id.myButton);
        addButton = findViewById(R.id.myButton2);
        syncButton = findViewById(R.id.syncButton);
        viewButton = findViewById(R.id.viewButton);
        filterButton = findViewById(R.id.filterButton);
        columnSpinner = findViewById(R.id.spinner);
        tableLayout=findViewById(R.id.tableLayout);
        editTableName=findViewById(R.id.editTableName);


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
                JSONArray jsonArray = fun.fileToJsonArray(MenuActivity.jsonFile);
                if (filter_visibility) {
                    String selectedColumn = columnSpinner.getSelectedItem().toString();
                    String filterValue = filterText.getText().toString();
                    jsonArray = fun.filterJsonArray(jsonArray, selectedColumn, filterValue);
                }
                updateTableLayout(jsonArray);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog(MenuActivity.jsonFile);
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

        // add actuall data
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
                        showEditDialog(jsonObject, position,jsonArray);
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
                    textView.setGravity(Gravity.CENTER); // Wycentrowanie tekstu

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
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                    String key = it.next();
                    if (!key.equals("id")) {
                        columnList.add(key);
                    }
                }
                spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, columnList);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                columnSpinner.setAdapter(spinnerAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void showEditDialog(JSONObject jsonObject, int position,JSONArray jsonArray) {
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
                    // Aktualizacja danych w jsonObject na podstawie wprowadzonych wartości
                    for (Map.Entry<String, EditText> entry : editTextMap.entrySet()) {
                        String key = entry.getKey();
                        String newValue = entry.getValue().getText().toString();
                        jsonObject.put(key, newValue);
                    }

                    // Zaktualizowanie jsonArray pod odpowiednim indeksem
                    jsonArray.put(position, jsonObject);

                    // Odświeżenie tabeli po edycji
                    updateTableLayout(jsonArray);

                    // Zapisanie zmian do pliku JSON
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

    public void fetchDataFromServer(String url) {
        new FetchDataTask(this).execute(url);
    }

    @Override
    public void onDataFetched(String result) {
        Log.d("SomeClass", "Response from server: " + result);
        updateTableLayout(fun.fileToJsonArray(MenuActivity.jsonFile));
        loadColumnNames(MenuActivity.jsonFile);
    }

}




