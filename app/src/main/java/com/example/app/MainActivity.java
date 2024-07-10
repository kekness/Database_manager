package com.example.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button getButton;
    private Button addButton;
    private Button syncButton;
    private Button viewButton;
    private Button filterButton;
    private Button settingsButton;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private String url = config.API_GETDATA_URL;
    private EditText filterText;
    private Spinner columnSpinner;
    private boolean filter_visibility = true;
    private HashMap<String, EditText> editTextMap = new HashMap<>();
    private ArrayList<String> columnList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    private String servername = "192.168.210.116";
    private String username = "API";
    private String password = ")Xcm*.H2OHn*THJl";


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsButton = findViewById(R.id.settingsButton);
        filterText = findViewById(R.id.editTextFilter);
        getButton = findViewById(R.id.myButton);
        addButton = findViewById(R.id.myButton2);
        syncButton = findViewById(R.id.syncButton);
        viewButton = findViewById(R.id.viewButton);
        filterButton = findViewById(R.id.filterButton);
        recyclerView = findViewById(R.id.recyclerView);
        columnSpinner = findViewById(R.id.spinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        File jsonFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "data.json");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchDataTask(MainActivity.this, servername, username, password, config.DATABASE, config.TABLENAME).execute(url);
            }
        });

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray jsonArray = fun.fileToJsonArray(jsonFile);
                if (filter_visibility) {
                    String selectedColumn = columnSpinner.getSelectedItem().toString();
                    String filterValue = filterText.getText().toString();
                    jsonArray = fun.filterJsonArray(jsonArray, selectedColumn, filterValue);
                }
                myAdapter = new MyAdapter(MainActivity.this, jsonArray);
                recyclerView.setAdapter(myAdapter);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog(jsonFile);
            }
        });

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("przycisk", "dzialam");
                new SaveDataAsyncTask().execute(jsonFile); //UPLOAD DATA
                new FetchDataTask(MainActivity.this, servername, username, password, config.DATABASE, config.TABLENAME).execute(url); //DOWNLOAD DATA
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

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsDialog settingsDialog = new SettingsDialog(MainActivity.this);
                settingsDialog.showSettingsDialog();
            }
        });

        loadColumnNames(jsonFile);
    }

    public void updateRecyclerView(JSONArray jsonArray) {
        myAdapter = new MyAdapter(this, jsonArray);
        recyclerView.setAdapter(myAdapter);
    }

    private void loadColumnNames(File jsonFile) {
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
}
