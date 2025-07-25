package lexiconforge.main.UI.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import lexiconforge.main.UI.Frames.MainFrame;
import lexiconforge.main.UI.Frames.MainFrame.UserSession;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import lexiconforge.main.UI.Panels.DictionaryPanel;

public class ImportDictionaryDialog extends JDialog {

    private JTextArea previewArea;
    private File selectedFile;
    private DictionaryPanel dictionaryPanel; // Reference to DictionaryPanel

    public ImportDictionaryDialog(JFrame parent, DictionaryPanel dictionaryPanel) {
        super(parent, "Import Dictionary", true);
        this.dictionaryPanel = dictionaryPanel;
        setLayout(new BorderLayout());

        // File Selection Button
        JButton browseButton = new JButton("Select File");
        browseButton.addActionListener(e -> selectFile());

        // Preview Area
        previewArea = new JTextArea(10, 40);
        previewArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(previewArea);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        JButton importButton = new JButton("Import");
        importButton.addActionListener(e -> importDictionary());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(importButton);
        buttonPanel.add(cancelButton);

        add(browseButton, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(500, 300);
        setLocationRelativeTo(parent);
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.home") + "/Downloads"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".json")
                    || f.getName().endsWith(".sqlite")
                    || f.getName().endsWith(".xlsx")
                    || f.getName().endsWith(".csv");
            }

            @Override
            public String getDescription() {
                return "Supported Files (*.json, *.sqlite, *.xlsx, *.csv)";
            }
        });

        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            previewFileContents();
        }
    }

    private void previewFileContents() {
        if (selectedFile == null) {
            return;
        }

        String fileName = selectedFile.getName().toLowerCase();

        if (fileName.endsWith(".json")) {
            try (FileReader reader = new FileReader(selectedFile)) {
                JSONTokener tokener = new JSONTokener(reader);
                JSONObject jsonObject = new JSONObject(tokener);
                previewArea.setText(jsonObject.toString(4));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error reading JSON file!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (fileName.endsWith(".csv")) {
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                StringBuilder previewText = new StringBuilder();
                String line;
                int rowCount = 0;
                while ((line = reader.readLine()) != null && rowCount < 10) {
                    previewText.append(line).append("\n");
                    rowCount++;
                }
                previewArea.setText(previewText.toString());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error reading CSV file!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (fileName.endsWith(".xlsx")) {
            try (FileInputStream fis = new FileInputStream(selectedFile);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                StringBuilder previewText = new StringBuilder();
                int rowCount = 0;
                for (Row row : sheet) {
                    if (rowCount >= 10) {
                        break;
                    }
                    for (Cell cell : row) {
                        previewText.append(cell.toString()).append("\t");
                    }
                    previewText.append("\n");
                    rowCount++;
                }
                previewArea.setText(previewText.toString());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error reading Excel file!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (fileName.endsWith(".sqlite")) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + selectedFile.getAbsolutePath());
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' LIMIT 1;")) {
                if (rs.next()) {
                    String tableName = rs.getString("name");
                    try (ResultSet tableData = stmt.executeQuery("SELECT * FROM " + tableName + " LIMIT 10;")) {
                        ResultSetMetaData metaData = tableData.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        StringBuilder previewText = new StringBuilder();
                        for (int i = 1; i <= columnCount; i++) {
                            previewText.append(metaData.getColumnName(i)).append("\t");
                        }
                        previewText.append("\n");
                        while (tableData.next()) {
                            for (int i = 1; i <= columnCount; i++) {
                                previewText.append(tableData.getString(i)).append("\t");
                            }
                            previewText.append("\n");
                        }
                        previewArea.setText(previewText.toString());
                    }
                } else {
                    previewArea.setText("No tables found in database.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error reading database file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            previewArea.setText("Preview not available for this file type.");
        }
    }

    private void importDictionary() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a file first!",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fileName = selectedFile.getName().toLowerCase();
        JSONObject importedDict = null;
        try {
            if (fileName.endsWith(".json")) {
                importedDict = importFromJSON();
            } else if (fileName.endsWith(".csv")) {
                importedDict = importFromCSV();
            } else if (fileName.endsWith(".xlsx")) {
                importedDict = importFromXLSX();
            } else if (fileName.endsWith(".sqlite")) {
                importedDict = importFromSQLite();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Unsupported file type.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Import failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (importedDict == null) {
            JOptionPane.showMessageDialog(this,
                "Failed to parse the imported file.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Extract dictionary name and parse the OFL portion
        String dictName = importedDict.getString("name"); // e.g. "EnglishToKodicai"
        String ofl = parseOFL(dictName);                  // e.g. "Kodicai"

        // If we can't parse an OFL, prompt user
        if (ofl == null || ofl.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "We couldn't detect the language name from '" + dictName
                    + "'. Do you want to create a new language manually?",
                    "Missing OFL",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                // Let the user create a new language with an arbitrary name
                CreateLanguageDialog cld = new CreateLanguageDialog((JFrame) getOwner(),
                    null, ofl, true);
                cld.setVisible(true);
            }
            // Proceed anyway, but no local language is guaranteed
        } else {
            // Check if user has this language
            String currentUser = UserSession.getCurrentUser();
            File oflFolder = new File(System.getProperty("user.home")
                + "/Documents/LexiconForge Files/Users/"
                + currentUser + "/Creations/Languages/" + ofl);
            File oflFile = new File(oflFolder, ofl + ".json");

            if (!oflFolder.exists()) {
                // user doesn't have this language folder
                int choice = JOptionPane.showConfirmDialog(this,
                        "This dictionary references a new language '" + ofl
                        + "'. Create it now?",
                        "New Language",
                        JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    CreateLanguageDialog cld = new CreateLanguageDialog((JFrame) getOwner(),
                        null, ofl, true);
                    cld.setVisible(true);
                }
            } else {
                // folder exists, check .json
                if (oflFile.exists()) {
                    // Prompt user for Overwrite or Cancel
                    Object[] options = {"Overwrite", "Cancel"};
                    int choice = JOptionPane.showOptionDialog(this,
                            "Language '" + ofl + "' already exists. Overwrite or Cancel?",
                            "Language Conflict",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);
                    if (choice == 0) {
                        // Overwrite
                        oflFolder.mkdirs();
                        CreateLanguageDialog cld = new CreateLanguageDialog((JFrame) getOwner(),
                            null, ofl, true);
                        cld.setVisible(true);
                    } else {
                        return;
                    }
                } else {
                    // folder but no .json
                    CreateLanguageDialog cld = new CreateLanguageDialog((JFrame) getOwner(),
                        null, ofl, true);
                    cld.setVisible(true);
                }
            }
        }

        // Append imported dictionary to saved_dictionaries.json
        String currentUser = UserSession.getCurrentUser();
        File saveFile = new File(System.getProperty("user.home")
            + "\\Documents\\LexiconForge Files\\Users\\"
            + currentUser + "\\Creations\\Dictionaries\\saved_dictionaries.json");

        // Ensure the parent folder exists
        File parentDir = saveFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        JSONArray savedDicts;
        try {
            if (saveFile.exists()) {
                try (FileReader sr = new FileReader(saveFile)) {
                    JSONTokener st = new JSONTokener(sr);
                    savedDicts = new JSONArray(st);
                }
            } else {
                savedDicts = new JSONArray();
            }
            savedDicts.put(importedDict);

            try (FileWriter fw = new FileWriter(saveFile)) {
                fw.write(savedDicts.toString(4));
            }
            JOptionPane.showMessageDialog(this,
                "Dictionary imported successfully! LexiconForge will restart after importing.",
                "Success", JOptionPane.INFORMATION_MESSAGE);

            JFrame owner = (JFrame) getOwner();
            if (owner instanceof MainFrame) {
                ((MainFrame) owner).restartApp();
            }
            dispose();

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error saving imported dictionary: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String parseOFL(String dictName) {
        if (!dictName.contains("To")) {
            return null;
        }
        int idx = dictName.indexOf("To") + 2;
        if (idx >= dictName.length()) {
            return null;
        }
        return dictName.substring(idx).trim();
    }

    // ---------- Import Methods ----------

    private JSONObject importFromJSON() throws IOException {
        try (FileReader reader = new FileReader(selectedFile)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);
            if (!jsonObject.has("name") || !jsonObject.has("columns")) {
                throw new IOException("Imported JSON is missing required dictionary information.");
            }
            return jsonObject;
        }
    }

    private JSONObject importFromCSV() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty.");
            }
            String[] headers = headerLine.split(",");
            int colCount = headers.length;

            String[] columnHeaders = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                columnHeaders[i] = headers[i].trim();
            }
            JSONObject updatedWordInfoJSON = new JSONObject();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split(",");
                if (rowData.length < 1) {
                    continue;
                }
                String[] row = new String[colCount];
                for (int i = 0; i < colCount; i++) {
                    if (i < rowData.length) {
                        row[i] = rowData[i].trim();
                    } else {
                        row[i] = "";
                    }
                }
                String irlKey = row[0].toLowerCase();
                updatedWordInfoJSON.put(irlKey, new JSONArray(row));
            }

            JSONObject dict = new JSONObject();
            String name = selectedFile.getName();
            if (name.contains(".")) {
                name = name.substring(0, name.lastIndexOf('.'));
            }
            dict.put("name", name);
            dict.put("columns", new JSONArray(columnHeaders));
            dict.put("customWords", new JSONArray());
            dict.put("updatedWordInfo", updatedWordInfoJSON);

            return dict;
        }
    }

    private JSONObject importFromXLSX() throws IOException {
        try (FileInputStream fis = new FileInputStream(selectedFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IOException("Excel file has no header row.");
            }

            int colCount = headerRow.getLastCellNum();
            String[] columnHeaders = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                Cell cell = headerRow.getCell(i);
                columnHeaders[i] = (cell != null) ? cell.toString().trim() : "";
            }
            JSONObject updatedWordInfoJSON = new JSONObject();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                String[] rowData = new String[colCount];
                for (int c = 0; c < colCount; c++) {
                    Cell cell = row.getCell(c);
                    rowData[c] = (cell != null) ? cell.toString().trim() : "";
                }
                String irlKey = rowData[0].toLowerCase();
                updatedWordInfoJSON.put(irlKey, new JSONArray(rowData));
            }

            JSONObject dict = new JSONObject();
            String name = selectedFile.getName();
            if (name.contains(".")) {
                name = name.substring(0, name.lastIndexOf('.'));
            }
            dict.put("name", name);
            dict.put("columns", new JSONArray(columnHeaders));
            dict.put("customWords", new JSONArray());
            dict.put("updatedWordInfo", updatedWordInfoJSON);

            return dict;
        }
    }

    private JSONObject importFromSQLite() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + selectedFile.getAbsolutePath());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' LIMIT 1;")) {

            if (!rs.next()) {
                throw new SQLException("No tables found in the SQLite database.");
            }
            String tableName = rs.getString("name");

            String query = "SELECT * FROM " + tableName + " LIMIT 10;";
            try (ResultSet tableData = stmt.executeQuery(query)) {
                ResultSetMetaData metaData = tableData.getMetaData();
                int columnCount = metaData.getColumnCount();

                String[] columnHeaders = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    columnHeaders[i - 1] = metaData.getColumnName(i);
                }
                JSONObject updatedWordInfoJSON = new JSONObject();

                while (tableData.next()) {
                    String[] rowData = new String[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        String val = tableData.getString(i);
                        rowData[i - 1] = (val == null) ? "" : val.trim();
                    }
                    String irlKey = rowData[0].toLowerCase();
                    updatedWordInfoJSON.put(irlKey, new JSONArray(rowData));
                }

                JSONObject dict = new JSONObject();
                String name = selectedFile.getName();
                if (name.contains(".")) {
                    name = name.substring(0, name.lastIndexOf('.'));
                }
                dict.put("name", name);
                dict.put("columns", new JSONArray(columnHeaders));
                dict.put("customWords", new JSONArray());
                dict.put("updatedWordInfo", updatedWordInfoJSON);
                return dict;
            }
        }
    }
}
