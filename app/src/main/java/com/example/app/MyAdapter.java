package com.example.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private JSONArray jsonArray;

    public MyAdapter(JSONArray jsonArray) {
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

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCode, textViewName, textViewDate;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCode = itemView.findViewById(R.id.textViewCode);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }
    }
}
