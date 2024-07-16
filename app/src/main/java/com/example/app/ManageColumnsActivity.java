package com.example.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ManageColumnsActivity extends AppCompatActivity {

    ListView columnsListView;
    Button addColumnButton;
    Button menuBtn;
    ArrayList<String> columnsList;
    ColumnsAdapter columnsAdapter;
    String tableName;
    TextView tableNameTextView;
    TextView tableNameEditText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managecolumns);

        menuBtn = findViewById(R.id.menuButton);
        tableNameTextView = findViewById(R.id.tableNameTextView);
        tableNameEditText=findViewById(R.id.tableNameEditText);

        columnsListView = findViewById(R.id.columnsListView);
        addColumnButton = findViewById(R.id.addColumnButton);

        columnsList = new ArrayList<>();
        columnsAdapter = new ColumnsAdapter(this, columnsList);
        columnsListView.setAdapter(columnsAdapter);

        Intent intent = getIntent();
        tableNameEditText.setText(intent.getStringExtra("tableName")+"'s columns");

        loadColumnsFromJsonFile(MenuActivity.jsonFile);

        addColumnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddColumnDialog();
            }
        });
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManageColumnsActivity.this, MenuActivity.class));
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
                String columnName = columnNameEditText.getText().toString();
                String columnType = columnTypeSpinner.getSelectedItem().toString();
                if (!columnName.isEmpty()) {
                    addColumn(tableName, columnName, columnType);
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

    private void showEditColumnDialog(String oldColumnName,int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Column");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText newColumnNameEditText = new EditText(this);
        newColumnNameEditText.setHint("New Column Name");
        layout.addView(newColumnNameEditText);

        final Spinner newColumnTypeSpinner = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.column_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newColumnTypeSpinner.setAdapter(adapter);
        layout.addView(newColumnTypeSpinner);

        builder.setView(layout);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newColumnName = newColumnNameEditText.getText().toString().trim();
                String newColumnType = newColumnTypeSpinner.getSelectedItem().toString();
                if (!newColumnName.isEmpty()) {
                    editColumn(tableName, oldColumnName, newColumnName, newColumnType);
                    columnsList.set(pos,newColumnName);
                    columnsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ManageColumnsActivity.this, "Please enter new column name", Toast.LENGTH_SHORT).show();
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

    private void showDeleteColumnDialog(final String columnName,int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Column");
        builder.setMessage("Are you sure you want to delete this column?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteColumn(tableName, columnName);
               columnsList.remove(pos);
                columnsAdapter.notifyDataSetChanged();
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
            TextView deleteTextView = convertView.findViewById(R.id.deleteColumnTV);
            TextView columnTextView=convertView.findViewById(R.id.columnsTextView);
            columnNameTextView.setText(columnName);

            // Set item click listener
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditColumnDialog(columnName,position);
                }
            });
            columnTextView.setVisibility(View.INVISIBLE);
            deleteTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteColumnDialog(columnName,position);

                }
            });

            return convertView;
        }
    }


    private class ManageColumnsTask extends AsyncTask<Map<String, String>, Void, String> {

        private String action;

        public ManageColumnsTask(String action) {
            this.action = action;
        }

        @Override
        protected String doInBackground(Map<String, String>... params) {
            Map<String, String> param = params[0];
            String result = "";

            try {
                URL url = new URL(config.API_MANAGECOLUMNS_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setDoOutput(true);

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, String> entry : param.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (key != null && value != null) {
                        if (postData.length() != 0) postData.append('&');
                        postData.append(URLEncoder.encode(key, "UTF-8"));
                        postData.append('=');
                        postData.append(URLEncoder.encode(value, "UTF-8"));
                    }
                }

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData.toString());
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
            Log.d("ManageColumnsTask", "Response from server: " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                boolean success = jsonObject.getBoolean("success");
                String message = jsonObject.getString("message");
                Toast.makeText(ManageColumnsActivity.this, message, Toast.LENGTH_LONG).show();
                if (success) {
                   // loadColumnsFromJsonFile(MenuActivity.jsonFile);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ManageColumnsActivity.this, "Error parsing server response: " + result, Toast.LENGTH_SHORT).show();
            }
        }

    }


    private void addColumn(String tableName, String columnName, String columnType) {

        Map<String, String> params = new HashMap<>();
        params.put("servername", config.ADDRESS);
        params.put("username", config.DBUSER);
        params.put("password", config.DB_PASS);
        params.put("dbname", config.DATABASE);
        params.put("action", "add_column");
        params.put("tablename", config.TABLENAME);
        params.put("column_name", columnName);
        params.put("column_type", columnType);

        new ManageColumnsTask("add_column").execute(params);
    }

    private void editColumn(String tableName, String oldColumnName, String newColumnName, String newColumnType) {

        Map<String, String> params = new HashMap<>();
        params.put("servername", config.ADDRESS);
        params.put("username", config.DBUSER);
        params.put("password", config.DB_PASS);
        params.put("dbname", config.DATABASE);
        params.put("action", "edit_column");
        params.put("tablename", config.TABLENAME);
        params.put("old_column_name", oldColumnName);
        params.put("new_column_name", newColumnName);
        params.put("new_column_type", newColumnType);

        new ManageColumnsTask("edit_column").execute(params);
    }

    private void deleteColumn(String tableName, String columnName) {

        Map<String, String> params = new HashMap<>();
        params.put("servername", config.ADDRESS);
        params.put("username", config.DBUSER);
        params.put("password", config.DB_PASS);
        params.put("dbname", config.DATABASE);
        params.put("action", "delete_column");
        params.put("tablename", config.TABLENAME);
        params.put("column_name", columnName);

        new ManageColumnsTask("delete_column").execute(params);
    }

}
