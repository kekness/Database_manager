package com.example.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;

import java.io.File;

public class MainActivity extends AppCompatActivity implements FetchDataTask.FetchDataListener {

    private Button getButton;
    private Button saveButton;
    private Button syncButton;
    private Button viewButton;
    private Button filterButton;

    private RecyclerView recyclerView;
    private MyAdapter myAdapter;

    private String url = config.API_GETDATA_URL;
    public static EditText editTextCode;
    public static EditText editTextName;
    public static EditText editTextDate;
    public EditText filterText;
    public static boolean filter_visibility = true;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextCode = findViewById(R.id.editTextCode);
        editTextDate = findViewById(R.id.editTextDate);
        editTextName = findViewById(R.id.editTextTextName);
        filterText = findViewById(R.id.editTextFilter);

        getButton = findViewById(R.id.myButton);
        saveButton = findViewById(R.id.myButton2);
        syncButton = findViewById(R.id.syncButton);
        viewButton = findViewById(R.id.viewButton);
        filterButton = findViewById(R.id.filterButton);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        File jsonFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "data.json");
       // new SaveDataAsyncTask().execute(jsonFile);


        // Request write permissions
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
                if(filter_visibility)
                jsonArray=fun.fileToJsonArray(jsonFile,Integer.parseInt(filterText.getText().toString())); //second argument is just number from filter textbox
                else
                    jsonArray=fun.fileToJsonArray(jsonFile);
                 myAdapter = new MyAdapter(MainActivity.this,jsonArray);
                 recyclerView.setAdapter(myAdapter);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                         fun.saveData(
                        editTextCode.getText().toString(),
                        editTextName.getText().toString(),
                        editTextDate.getText().toString()
                );

                Log.d("przycisk2", "dzialam :D");
            //    new SaveDataAsyncTask().execute(jsonFile); //UPLOAD DATA
            //    new FetchDataTask(MainActivity.this).execute(url);//DOWNLOAD DATA

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
                if(filterText.isShown()) {
                    filterButton.setText("filter: OFF");
                    filterText.setVisibility(View.GONE);
                    filter_visibility=false;
                }
                else {
                    filterButton.setText("filter: ON");
                    filterText.setVisibility(View.VISIBLE);
                    filter_visibility=true;
                }
            }
        });
    }

    @Override
    public void onFetchDataSuccess(JSONArray jsonArray) {
        myAdapter = new MyAdapter(this,jsonArray);
        recyclerView.setAdapter(myAdapter);
    }
}
