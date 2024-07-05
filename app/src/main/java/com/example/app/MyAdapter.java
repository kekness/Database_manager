package com.example.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private JSONArray jsonArray;
    private Context context;

    public MyAdapter(Context context, JSONArray jsonArray) {
        this.context = context;
        this.jsonArray = jsonArray;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        try {
            JSONObject jsonObject = jsonArray.getJSONObject(position);
            holder.textViewCode.setText(jsonObject.getString("kod"));
            holder.textViewName.setText(jsonObject.getString("nazwa"));
            holder.textViewDate.setText(jsonObject.getString("data"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return jsonArray.length();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCode, textViewName, textViewDate;
        Button editButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCode = itemView.findViewById(R.id.textViewCode);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            editButton = itemView.findViewById(R.id.editButton);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(position);
                            showEditDialog(jsonObject, position);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }


        //edytowanie
        private void showEditDialog(JSONObject jsonObject, int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Edit Record");

            View viewInflated = LayoutInflater.from(context).inflate(R.layout.edit_dialog, null, false);

            final EditText inputStatus = viewInflated.findViewById(R.id.editTextStatus);
            final EditText inputCode = viewInflated.findViewById(R.id.editTextCode);
            final EditText inputName = viewInflated.findViewById(R.id.editTextName);
            final EditText inputDate = viewInflated.findViewById(R.id.editTextDate);

            try {
                inputCode.setText(jsonObject.getString("kod"));
                inputName.setText(jsonObject.getString("nazwa"));
                inputDate.setText(jsonObject.getString("data"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            builder.setView(viewInflated);

            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    try {
                        jsonObject.put("kod", inputCode.getText().toString());
                        jsonObject.put("nazwa", inputName.getText().toString());
                        jsonObject.put("data", inputDate.getText().toString());
                        jsonObject.put("status", inputStatus.getText().toString());
                        jsonArray.put(position, jsonObject);
                        notifyItemChanged(position);

                        // Save the updated JSONArray to the file
                        File jsonFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "data.json");
                        fun.saveJsonArrayToFile(jsonArray, jsonFile);

                    } catch (JSONException e) {
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

    }

}
