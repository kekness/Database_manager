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

    private RecyclerView recyclerView;
    private MyAdapter myAdapter;

    private String url = config.API_GETDATA_URL;
    public static EditText editTextCode;
    public static EditText editTextName;
    public static EditText editTextDate;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextCode = findViewById(R.id.editTextCode);
        editTextDate = findViewById(R.id.editTextDate);
        editTextName = findViewById(R.id.editTextTextName);

        getButton = findViewById(R.id.myButton);
        saveButton = findViewById(R.id.myButton2);
        syncButton = findViewById(R.id.syncButton);
        viewButton = findViewById(R.id.viewButton);

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

                JSONArray jsonArray=fun.fileToJsonArray(jsonFile);
                 myAdapter = new MyAdapter(jsonArray);
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
    }

    @Override
    public void onFetchDataSuccess(JSONArray jsonArray) {
        myAdapter = new MyAdapter(jsonArray);
        recyclerView.setAdapter(myAdapter);
    }
}
