package com.example.app;

import android.content.Context;
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

        EditText editSettingsIP = viewInflated.findViewById(R.id.editSettingsIP);
        EditText editSettingsDatabase = viewInflated.findViewById(R.id.editSettingsDatabase);
        EditText editSettingsTablename = viewInflated.findViewById(R.id.editSettingsTablename);
        Button buttonSave = viewInflated.findViewById(R.id.saveSettingsButton);

        builder.setView(viewInflated);

        // Setting the negative button before creating the dialog
        builder.setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.cancel());

        AlertDialog dialog = builder.create();

        editSettingsIP.setText(config.ADDRESS);
        editSettingsDatabase.setText(config.DATABASE);
        editSettingsTablename.setText(config.TABLENAME);

        buttonSave.setOnClickListener(v -> {
            config.ADDRESS = editSettingsIP.getText().toString();
            config.DATABASE = editSettingsDatabase.getText().toString();
            config.TABLENAME = editSettingsTablename.getText().toString();

            dialog.dismiss();
        });

        dialog.show();
    }
}
