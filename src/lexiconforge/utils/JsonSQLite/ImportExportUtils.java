package lexiconforge.utils.JsonSQLite;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;

import java.util.Map;

public class ImportExportUtils {

    private static final String DEFAULT_PATH = "dictionaries/";

    public static void exportDictionary(Map<String, String> dictionary, String filename) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Map.Entry<String, String> entry : dictionary.entrySet()) {
                JSONObject wordObj = new JSONObject();
                wordObj.put("IRL", entry.getKey());
                wordObj.put("OFL", entry.getValue());
                jsonArray.put(wordObj);
            }

            File file = new File(DEFAULT_PATH + filename + ".json");
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonArray.toString(4));
            }

            JOptionPane.showMessageDialog(null, "Dictionary exported successfully!", "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Export failed!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static Map<String, String> importDictionary(String filename) {
        Map<String, String> dictionary = new HashMap<>();

        try {
            File file = new File(DEFAULT_PATH + filename);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(null, "File not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return dictionary;
            }

            StringBuilder jsonText = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonText.append(line);
                }
            }

            JSONArray jsonArray = new JSONArray(jsonText.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                dictionary.put(obj.getString("IRL"), obj.getString("OFL"));
            }

            JOptionPane.showMessageDialog(null, "Dictionary imported successfully!", "Import Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Import failed!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return dictionary;
    }
}
