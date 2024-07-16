package com.example.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;



public class TablesAdapter extends ArrayAdapter<String> implements FetchDataTask.FetchDataListener {
    public interface OnTableDeletedListener {
        void onTableDeleted(int position);
    }
    private Context context;
    private ArrayList<String> tables;
    private OnTableDeletedListener listener;

    public TablesAdapter(Context context, ArrayList<String> tables, OnTableDeletedListener listener) {
        super(context, 0, tables);
        this.context = context;
        this.tables = tables;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_table, parent, false);
        }

        String tableName = getItem(position);

        TextView tableNameTextView = convertView.findViewById(R.id.tableNameTextView);
        TextView columnsButton = convertView.findViewById(R.id.columnsTextView);
        TextView deleteTV = convertView.findViewById(R.id.deleteColumnTV);
        LinearLayout itemLayout = convertView.findViewById(R.id.itemLayout);

        tableNameTextView.setText(tableName);

        columnsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDataFromServer(config.API_GETDATA_URL);
                config.TABLENAME = tableName;
            }
        });

        deleteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.TABLENAME = tableName;
                showDeleteTableDialog(position);
            }
        });

        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.TABLENAME = tableName;
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("tableName", config.TABLENAME);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    public void fetchDataFromServer(String url) {
        new FetchDataTask(this).execute(url);
    }

    @Override
    public void onDataFetched(String result) {
        Log.d("SomeClass", "Response from server: " + result);
        Intent intent = new Intent(context, ManageColumnsActivity.class);
        intent.putExtra("tableName", config.TABLENAME);
        context.startActivity(intent);
    }

    private void showDeleteTableDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Table");
        builder.setMessage("Are you sure you want to delete this table?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTableFromServer(position);
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

    private void deleteTableFromServer(int position) {
        String url = config.API_MANAGECOLUMNS_URL;
        new DeleteTableTask(position).execute(url, config.TABLENAME);
    }

    private class DeleteTableTask extends AsyncTask<String, Void, String> {
        private int position;

        public DeleteTableTask(int position) {
            this.position = position;
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            String tableName = params[1];
            String servername = config.ADDRESS;
            String username = config.DBUSER;
            String password = config.DB_PASS;
            String dbname = config.DATABASE;

            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String postData = "action=delete_table&tablename=" + tableName +
                        "&servername=" + servername +
                        "&username=" + username +
                        "&password=" + password +
                        "&dbname=" + dbname;
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                InputStream is = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d("DeleteTableTask", "Response: " + result);
                // Notify listener
                if (listener != null) {
                    listener.onTableDeleted(position);
                }
            }
        }
    }
}
