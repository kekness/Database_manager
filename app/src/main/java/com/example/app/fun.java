package com.example.app;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

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

    //convert file using filter
    public static JSONArray fileToJsonArray(File jsonFile, int filter) {
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
            JSONArray jsonArray = new JSONArray(jsonContent.toString());
            JSONArray filteredArray = new JSONArray();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getInt("status") == filter) {
                    filteredArray.put(jsonObject);
                }
            }

            return filteredArray;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    //save data from string to data.json
    public static void saveToFile(String data,String pathToFile) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(path, pathToFile);

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

    //convert json array to already existing text file
    public static void saveJsonArrayToFile(JSONArray jsonArray, File file) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonArray.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //returns array filtered by column selected in spinner
    public static JSONArray filterJsonArray(JSONArray jsonArray, String selectedColumn, String filterValue) {
        JSONArray filteredArray = new JSONArray();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (jsonObject.has(selectedColumn)) {
                    String columnValue = jsonObject.getString(selectedColumn);

                    if (columnValue != null && columnValue.contains(filterValue)) {
                        filteredArray.put(jsonObject);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filteredArray;
    }

    //converts json file to a String
    public static String readFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static void exportDataToCSV(String tableName,File jsonFile) {
        // Wczytaj dane z pliku JSON
        String jsonString = readJsonFromFile(jsonFile);
        if (jsonString == null || jsonString.isEmpty()) {
            Log.d("fjup zdziub", "No Data to export");
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            if (jsonArray.length() == 0) {
                Log.d("fjup zdziub", "No Data to export");
                return;
            }

            // Ścieżka do miejsca docelowego dla pliku CSV
            String csvFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+tableName+".csv";

            // Tworzenie obiektu BufferedWriter do zapisu do pliku
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFilePath), "UTF-8"));

            // Dodanie nagłówków kolumn (klucze z pierwszego obiektu JSON)
            JSONObject firstObject = jsonArray.getJSONObject(0);
            Iterator<String> keys = firstObject.keys();
            StringBuilder headerLine = new StringBuilder();
            while (keys.hasNext()) {
                String key = keys.next();
                headerLine.append("\"").append(key).append("\",");
            }
            headerLine.deleteCharAt(headerLine.length() - 1); // Usunięcie ostatniego przecinka
            bw.write(headerLine.toString());
            bw.newLine();

            // Zapisywanie danych do pliku CSV
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                StringBuilder csvLine = new StringBuilder();
                keys = firstObject.keys(); // Zresetuj iterator kluczy
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = jsonObject.optString(key, "");
                    csvLine.append("\"").append(value).append("\",");
                }
                csvLine.deleteCharAt(csvLine.length() - 1); // Usunięcie ostatniego przecinka
                bw.write(csvLine.toString());
                bw.newLine();
            }

            bw.flush();
            bw.close();

            Log.d("fjup zdziub", "Data exported to " + csvFilePath);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    // Metoda do odczytu danych z pliku JSON
    private static String readJsonFromFile(File jsonFile) {
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(jsonFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return jsonString.toString();
    }



    private static ArrayList<ArrayList<String>> getDataForTable(File jsonFile) {
        ArrayList<ArrayList<String>> data = new ArrayList<>();

        try {
            // Otwórz strumień do odczytu z pliku jsonFile
            FileInputStream fis = new FileInputStream(jsonFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }

            // Zamknij czytnik
            reader.close();

            // Przetwórz JSON do listy danych
            JSONArray jsonArray = new JSONArray(jsonString.toString());

            // Pobierz kolumny dla danych tabeli
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject rowObject = jsonArray.getJSONObject(i);
                Iterator<String> keys = rowObject.keys();

                ArrayList<String> row = new ArrayList<>();
                while (keys.hasNext()) {
                    String key = keys.next();
                    row.add(rowObject.getString(key));
                }
                data.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Blunder ziutek", "chodzi o parsowanie json");
        }

        return data;
    }

}
