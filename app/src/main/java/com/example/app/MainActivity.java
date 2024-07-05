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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements FetchDataTask.FetchDataListener {

    private Button getButton;
    private Button addButton;
    private Button syncButton;
    private Button viewButton;
    private Button filterButton;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private String url = config.API_GETDATA_URL;
    private EditText filterText;
    private boolean filter_visibility = true;
    private HashMap<String, EditText> editTextMap = new HashMap<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filterText = findViewById(R.id.editTextFilter);
        getButton = findViewById(R.id.myButton);
        addButton = findViewById(R.id.myButton2);
        syncButton = findViewById(R.id.syncButton);
        viewButton = findViewById(R.id.viewButton);
        filterButton = findViewById(R.id.filterButton);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        File jsonFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "data.json");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchDataTask(MainActivity.this).execute(url);
            }
        });

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray jsonArray;
                if (filter_visibility)
                    jsonArray = fun.fileToJsonArray(jsonFile, Integer.parseInt(filterText.getText().toString())); //second argument is just number from filter textbox
                else
                    jsonArray = fun.fileToJsonArray(jsonFile);
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
                new SaveDataAsyncTask().execute(jsonFile);//UPLOAD DATA
                new FetchDataTask(MainActivity.this).execute(url);//DOWNLOAD DATA
            }
        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterText.isShown()) {
                    filterButton.setText("filter: OFF");
                    filterText.setVisibility(View.GONE);
                    filter_visibility = false;
                } else {
                    filterButton.setText("filter: ON");
                    filterText.setVisibility(View.VISIBLE);
                    filter_visibility = true;
                }
            }
        });
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

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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


    @Override
    public void onFetchDataSuccess(JSONArray jsonArray) {
        myAdapter = new MyAdapter(this, jsonArray);
        recyclerView.setAdapter(myAdapter);
    }
}
