package com.example.app;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

public class SettingsDialog {

    private Context context;

    public SettingsDialog(Context context) {
        this.context = context;
    }

    public void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Settings");

        View viewInflated = LayoutInflater.from(context).inflate(R.layout.settings_dialog, null, false);

        EditText editSettingsUser = viewInflated.findViewById(R.id.editSettingsUser);
        EditText editSettingsPassword = viewInflated.findViewById(R.id.editSettingsPassword);
        EditText editSettingsIP = viewInflated.findViewById(R.id.editSettingsIP);
        EditText editSettingsDatabase = viewInflated.findViewById(R.id.editSettingsDatabase);


        builder.setView(viewInflated);

        // Setting the negative button before creating the dialog
        builder.setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.cancel());
        builder.setPositiveButton("Save",(DialogInterface,which)->{
            config.ADDRESS = editSettingsIP.getText().toString();
            config.DATABASE = editSettingsDatabase.getText().toString();
            config.DBUSER= editSettingsUser.getText().toString();
            config.DB_PASS=editSettingsPassword.getText().toString();
            MenuActivity.infoTv.setText("Tables in "+config.DATABASE);
            new FetchTablesTask().execute();
        });

        AlertDialog dialog = builder.create();

        editSettingsIP.setText(config.ADDRESS);
        editSettingsDatabase.setText(config.DATABASE);
        editSettingsUser.setText(config.DBUSER);
        editSettingsPassword.setText(config.DB_PASS);

        dialog.show();
    }
}
