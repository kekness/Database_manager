package com.example.app;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class TablesAdapter extends ArrayAdapter<String> implements FetchDataTask.FetchDataListener {

    private Context context;
    private ArrayList<String> tables;

    public TablesAdapter(Context context, ArrayList<String> tables) {
        super(context, 0, tables);
        this.context = context;
        this.tables = tables;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_table, parent, false);
        }

        String tableName = getItem(position);

        TextView tableNameTextView = convertView.findViewById(R.id.tableNameTextView);
        TextView columnsButton = convertView.findViewById(R.id.columnsTextView);
        LinearLayout itemLayout = convertView.findViewById(R.id.itemLayout);

        tableNameTextView.setText(tableName);

        columnsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDataFromServer(config.API_GETDATA_URL);
                config.TABLENAME = tableName;

            }
        });

        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle item click
                config.TABLENAME = tableName;
                context.startActivity(new Intent(context, MainActivity.class));
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
        // intent.putExtra("tableName", tableName);
        context.startActivity(intent);
    }
}
