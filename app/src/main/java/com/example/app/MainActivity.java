package com.example.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
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
    private TableLayout tableLayout;
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
        //recyclerView = findViewById(R.id.recyclerView);
        columnSpinner = findViewById(R.id.spinner);
        tableLayout=findViewById(R.id.tableLayout);

        //recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                updateTableLayout(jsonArray);
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

    public void updateTableLayout(JSONArray jsonArray) {
        tableLayout.removeAllViews();

        // Dodaj wiersz nagłówków kolumn
        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        // Pobierz nazwy kolumn z pierwszego obiektu JSON (założenie, że wszystkie obiekty mają te same klucze)
        try {
            if (jsonArray.length() > 0) {
                JSONObject firstObject = jsonArray.getJSONObject(0);
                Iterator<String> keys = firstObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();

                    // Utwórz TextView dla nagłówka kolumny
                    TextView headerTextView = new TextView(this);
                    headerTextView.setText(key);
                    headerTextView.setPadding(10, 10, 10, 10);
                    headerTextView.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    headerTextView.setBackgroundColor(Color.LTGRAY); // Tło nagłówka
                    headerTextView.setTextColor(Color.BLACK); // Kolor tekstu nagłówka
                    headerTextView.setGravity(Gravity.CENTER); // Wycentrowanie tekstu

                    headerRow.addView(headerTextView);
                }
                // Dodaj wiersz nagłówków do tabeli
                tableLayout.addView(headerRow);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Dodaj rzeczywiste dane do tabeli
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));

                // Dodaj onClickListener do wiersza
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

                // Dodaj wiersz danych do tabeli
                tableLayout.addView(tableRow);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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


}




