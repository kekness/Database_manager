package com.example.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class TablesAdapter extends ArrayAdapter<String> {

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
        Button columnsButton = convertView.findViewById(R.id.columnsButton);
        LinearLayout itemLayout = convertView.findViewById(R.id.itemLayout);

        tableNameTextView.setText(tableName);

        columnsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle columns button click
               // Intent intent = new Intent(context, ManageColumnsActivity.class);
                //intent.putExtra("tableName", tableName);
                //context.startActivity(intent);
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
}
