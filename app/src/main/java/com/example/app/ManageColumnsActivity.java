package com.example.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

public class ManageColumnsActivity extends AppCompatActivity {

    ListView columnsListView;
    Button addColumnButton;
    Button menuBtn;
    ArrayList<String> columnsList;
    ColumnsAdapter columnsAdapter;
    String tableName;
    JSONArray jsonArray;
    LinearLayout itemLayout;
    TextView tableNameTextView;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managecolumns);

        menuBtn = findViewById(R.id.menuButton);
        tableNameTextView = findViewById(R.id.tableNameTextView);
       // itemLayout = findViewById(R.id.itemLayout);

       // tableName = getIntent().getStringExtra("tableName");
        //tableNameTextView.setText(tableName);

        columnsListView = findViewById(R.id.columnsListView);
        addColumnButton = findViewById(R.id.addColumnButton);

        columnsList = new ArrayList<>();
        columnsAdapter = new ColumnsAdapter(this, columnsList);
        columnsListView.setAdapter(columnsAdapter);

        loadColumnsFromJsonFile(MainActivity.jsonFile);

        addColumnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddColumnDialog();
            }
        });
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManageColumnsActivity.this,MenuActivity.class));
            }
        });


    }

    private void loadColumnsFromJsonFile(File jsonFile) {
        try {
            FileInputStream fis = new FileInputStream(jsonFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            JSONArray jsonArray = new JSONArray(jsonBuilder.toString());

            if (jsonArray.length() > 0) {
                JSONObject firstObject = jsonArray.getJSONObject(0);
                Iterator<String> keys = firstObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    columnsList.add(key);
                }
                columnsAdapter.notifyDataSetChanged();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void showAddColumnDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Column");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText columnNameEditText = new EditText(this);
        columnNameEditText.setHint("Column Name");
        layout.addView(columnNameEditText);

        final Spinner columnTypeSpinner = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.column_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        columnTypeSpinner.setAdapter(adapter);
        layout.addView(columnTypeSpinner);

        builder.setView(layout);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String columnName = columnNameEditText.getText().toString().trim();
                String columnType = columnTypeSpinner.getSelectedItem().toString();
                if (!columnName.isEmpty()) {
                    // Add the column to the list and notify the adapter
                    columnsList.add(columnName);
                    columnsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ManageColumnsActivity.this, "Please enter column name", Toast.LENGTH_SHORT).show();
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

    private class ColumnsAdapter extends ArrayAdapter<String> {

        public ColumnsAdapter(Context context, ArrayList<String> columns) {
            super(context, 0, columns);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_table, parent, false);
            }

            String columnName = getItem(position);

            TextView columnNameTextView = convertView.findViewById(R.id.tableNameTextView);
            TextView deleteTextView = convertView.findViewById(R.id.columnsTextView);
            deleteTextView.setText("Delete");

            columnNameTextView.setText(columnName);

            deleteTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle delete column
                    columnsList.remove(position);
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }
}
