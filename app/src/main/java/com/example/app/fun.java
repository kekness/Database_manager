package com.example.app;

import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class fun {
    //save data from editboxes to file
    public static void saveData(String code, String name, String date) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(path, "data.json");

            if (!path.exists()) {
                path.mkdirs();
            }

            StringBuilder fileData = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                fileData.append(line).append("\n");
            }
            reader.close();

            JSONArray jsonArray;
            if (fileData.length() > 0) {
                jsonArray = new JSONArray(fileData.toString());
            } else {
                jsonArray = new JSONArray();
            }

            JSONObject newData = new JSONObject();
            newData.put("kod", code);
            newData.put("nazwa", name);
            newData.put("data", date);

            jsonArray.put(newData);

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(jsonArray.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //convert json file to json array
    public static JSONArray fileToJsonArray(File jsonFile) {
        StringBuilder jsonContent = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(jsonFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            return new JSONArray(jsonContent.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    //save data from string to file
    public static void saveToFile(String data) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(path, "data.json");

            if (!path.exists()) {
                path.mkdirs();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(data);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
