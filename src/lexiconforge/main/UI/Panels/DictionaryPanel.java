package lexiconforge.main.UI.Panels;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lexiconforge.main.UI.Frames.MainFrame.UserSession;
import org.json.JSONArray;
import org.json.JSONObject;

public class DictionaryPanel extends JPanel {

    private static final String WORDS_DIR = "/resources/wordlists/";

    // The saved_dictionaries.json path in Documents:
    private String getSaveFilePath() {
        String currentUser = UserSession.getCurrentUser();
        return System.getProperty("user.home")
                + "\\Documents\\LexiconForge Files\\Users\\"
                + currentUser + "\\Creations\\Dictionaries\\saved_dictionaries.json";
    }

    private JTabbedPane dictionaryTabs;
    private JPanel dictionaryEditorPanel;
    private Map<String, JTable> dictionaryTables;
    private Map<String, String[]> dictionaryColumns;

    private Map<String, Map<Character, List<String>>> dictionaryWordMap = new HashMap<>();
    private Map<String, List<String>> dictionaryNumberOrSymbolWords = new HashMap<>();
    private Map<String, List<String>> customWords = new HashMap<>();
    private Map<String, Map<String, String[]>> updatedWordInfo = new HashMap<>();

    public DictionaryPanel() {
        setLayout(new BorderLayout());

        dictionaryTabs = new JTabbedPane();
        dictionaryTables = new HashMap<>();
        dictionaryColumns = new HashMap<>();

        JButton createDictionaryBtn = new JButton("Create Dictionary");
        createDictionaryBtn.addActionListener(this::createDictionary);

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(createDictionaryBtn);

        add(topPanel, BorderLayout.NORTH);
        add(dictionaryTabs, BorderLayout.CENTER);

        loadDictionaries(); // Load existing dictionaries on startup
    }

    // A custom table model that disallows editing
    private class NonEditableTableModel extends DefaultTableModel {

        public NonEditableTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    // A custom cell renderer for multiline "Meaning" or synonyms
    private class TextAreaRenderer extends JTextArea implements TableCellRenderer {

        public TextAreaRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            setSize(table.getColumnModel().getColumn(column).getWidth(), Short.MAX_VALUE);
            int preferredHeight = getPreferredSize().height;
            if (table.getRowHeight(row) != preferredHeight) {
                table.setRowHeight(row, preferredHeight);
            }
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            return this;
        }
    }

    private String[] getRowDataForWord(String dictionaryName, String irlWord) {
        String[] columns = dictionaryColumns.get(dictionaryName);
        Map<String, String[]> updates = updatedWordInfo.get(dictionaryName);
        if (updates != null && updates.containsKey(irlWord.toLowerCase())) {
            return updates.get(irlWord.toLowerCase());
        } else {
            String[] rowData = new String[columns.length];
            rowData[0] = irlWord;
            for (int i = 1; i < columns.length; i++) {
                rowData[i] = "";
            }
            return rowData;
        }
    }

    private String[] getRowDataForCustomWord(String dictionaryName, String irlWord, String oflTranslation) {
        String[] columns = dictionaryColumns.get(dictionaryName);
        Map<String, String[]> updates = updatedWordInfo.get(dictionaryName);
        if (updates != null && updates.containsKey(irlWord.toLowerCase())) {
            return updates.get(irlWord.toLowerCase());
        } else {
            String[] rowData = new String[columns.length];
            rowData[0] = irlWord;
            if (columns.length > 1) {
                rowData[1] = oflTranslation;
            }
            for (int i = 2; i < columns.length; i++) {
                rowData[i] = "";
            }
            return rowData;
        }
    }

    private String generateUniqueDictionaryName(String baseName, String[] newColumns) {
        if (!dictionaryTables.containsKey(baseName)) {
            return baseName;
        }
        // If dictionary with baseName exists, check if columns match
        if (Arrays.equals(dictionaryColumns.get(baseName), newColumns)) {
            return null; // exact duplicate
        }
        int count = 1;
        String candidate;
        do {
            candidate = baseName + " (" + count + ")";
            count++;
        } while (dictionaryTables.containsKey(candidate));
        return candidate;
    }

    private void createDictionary(ActionEvent e) {
        JPanel panel = new JPanel(new GridLayout(5, 2));

        JComboBox<String> irlDropdown = new JComboBox<>(new String[]{
            "English", "Arabic", "Croatian", "Czech",
            "Danish", "Dutch", "French", "Georgian",
            "Hebrew", "Italian", "Norwegian", "Polish",
            "Portuguese", "Russian", "Serbian", "Spanish",
            "Swedish", "Turkish", "Ukrainian"
        });
        JComboBox<String> oflDropdown = new JComboBox<>();

        updateOFLDropdown(oflDropdown); // fetch existing user-languages

        JCheckBox phoneticsBox = new JCheckBox("Include Phonetics");
        JCheckBox meaningsBox = new JCheckBox("Include Meanings");
        JCheckBox synonymsBox = new JCheckBox("Include Synonyms");

        panel.add(new JLabel("Select In-Reality Language:"));
        panel.add(irlDropdown);
        panel.add(new JLabel("Select Original Fantasy Language:"));
        panel.add(oflDropdown);
        panel.add(phoneticsBox);
        panel.add(meaningsBox);
        panel.add(synonymsBox);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Create Dictionary", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String irl = (String) irlDropdown.getSelectedItem();
            String ofl = (String) oflDropdown.getSelectedItem();

            if (ofl == null || ofl.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please select an Original Fantasy Language!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String baseName = irl + "To" + ofl;
            String[] newColumns = getSelectedColumns(phoneticsBox, meaningsBox, synonymsBox);
            String dictionaryName;
            if (dictionaryTables.containsKey(baseName)) {
                if (Arrays.equals(dictionaryColumns.get(baseName), newColumns)) {
                    JOptionPane.showMessageDialog(this,
                            "Dictionary already exists!",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    dictionaryName = generateUniqueDictionaryName(baseName, newColumns);
                    if (dictionaryName == null) {
                        JOptionPane.showMessageDialog(this,
                                "Dictionary already exists!",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            } else {
                dictionaryName = baseName;
            }

            dictionaryColumns.put(dictionaryName, newColumns);
            addDictionaryTab(dictionaryName, irl);
            saveDictionaries();
        }
    }

    // Lists all .json files in the user's Languages folder
    private void updateOFLDropdown(JComboBox<String> oflDropdown) {
        oflDropdown.removeAllItems();
        String currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No user logged in; can't update OFL dropdown.");
            return;
        }

        File userLanguagesDir = new File(System.getProperty("user.home")
                + "/Documents/LexiconForge Files/Users/"
                + currentUser + "/Creations/Languages/"
        );
        System.out.println("Checking OFLs in: " + userLanguagesDir.getAbsolutePath());
        if (!userLanguagesDir.exists() || !userLanguagesDir.isDirectory()) {
            System.out.println("❌ Directory does not exist: " + userLanguagesDir.getAbsolutePath());
            return;
        }

        File[] jsonFiles = userLanguagesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (jsonFiles == null || jsonFiles.length == 0) {
            System.out.println("🚨 No valid OFLs found!");
            return;
        }
        for (File file : jsonFiles) {
            String baseName = file.getName().replace(".json", "");
            oflDropdown.addItem(baseName);
            System.out.println("✅ Added language: " + baseName);
        }
        if (oflDropdown.getItemCount() == 0) {
            System.out.println("🚨 No valid OFLs found!");
        }
    }

    private String[] getSelectedColumns(JCheckBox phonetics,
            JCheckBox meanings,
            JCheckBox synonyms) {
        List<String> columns = new ArrayList<>(Arrays.asList("IRL Word", "OFL Word"));
        if (phonetics.isSelected()) {
            columns.add("Phonetics");
        }
        if (meanings.isSelected()) {
            columns.add("Meaning");
        }
        if (synonyms.isSelected()) {
            columns.add("Synonyms");
        }
        return columns.toArray(new String[0]);
    }

    private void addDictionaryTab(String dictionaryName, String irlLanguage) {
        JPanel tabPanel = new JPanel(new BorderLayout());

        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        editButton.addActionListener(e -> openDictionaryEditor(dictionaryName, deleteButton));
        deleteButton.addActionListener(e -> removeDictionary(dictionaryName));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        tabPanel.add(buttonPanel, BorderLayout.NORTH);

        String[] columns = dictionaryColumns.get(dictionaryName);
        NonEditableTableModel model = new NonEditableTableModel(columns, 0);
        JTable table = new JTable(model);
        dictionaryTables.put(dictionaryName, table);

        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equalsIgnoreCase("Meaning")) {
                table.getColumnModel().getColumn(i).setCellRenderer(new TextAreaRenderer());
            }
        }

        loadIRLWords(dictionaryName, irlLanguage, model);
        tabPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        dictionaryTabs.addTab(dictionaryName, tabPanel);

        if (irlLanguage.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No wordlist found for your IRL Language. Using empty list.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openDictionaryEditor(String dictionaryName, JButton deleteButton) {
        if (dictionaryEditorPanel != null) {
            remove(dictionaryEditorPanel);
        }

        dictionaryEditorPanel = new JPanel(new BorderLayout());
        dictionaryEditorPanel.setBorder(new TitledBorder(dictionaryName));

        JPanel editorTopPanel = new JPanel();
        editorTopPanel.setLayout(new BoxLayout(editorTopPanel, BoxLayout.X_AXIS));
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            remove(dictionaryEditorPanel);
            revalidate();
            repaint();
            if (deleteButton != null) {
                deleteButton.setVisible(true);
            }
        });
        JButton addCustomWordBtn = new JButton("Add Custom Word");
        addCustomWordBtn.addActionListener(e -> addCustomWord(dictionaryName));
        JButton editWordInfoBtn = new JButton("Edit Word Information");
        editWordInfoBtn.addActionListener(e -> editWordInformation(dictionaryName));

        editorTopPanel.add(backButton);
        editorTopPanel.add(Box.createHorizontalStrut(10));
        editorTopPanel.add(addCustomWordBtn);
        editorTopPanel.add(Box.createHorizontalStrut(10));
        editorTopPanel.add(editWordInfoBtn);
        editorTopPanel.add(Box.createHorizontalGlue());

        JLabel searchLabel = new JLabel("Search: ");
        JTextField searchField = new JTextField(15);
        searchField.setMaximumSize(new Dimension(150, 25));
        searchField.addActionListener(e -> searchWord(dictionaryName, searchField.getText().trim()));
        editorTopPanel.add(searchLabel);
        editorTopPanel.add(searchField);

        dictionaryEditorPanel.add(editorTopPanel, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new BorderLayout());
        JPanel aToZPanel = new JPanel(new GridLayout(2, 13));
        for (char c = 'A'; c <= 'Z'; c++) {
            final char letter = c;
            JButton letterBtn = new JButton(String.valueOf(c));
            letterBtn.addActionListener(e -> filterWordsByLetter(dictionaryName, letter));
            aToZPanel.add(letterBtn);
        }
        filterPanel.add(aToZPanel, BorderLayout.CENTER);

        JPanel numSymPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton numSymBtn = new JButton("Numbers & Symbols");
        numSymBtn.addActionListener(e -> filterNumbersAndSymbols(dictionaryName));
        numSymPanel.add(numSymBtn);
        filterPanel.add(numSymPanel, BorderLayout.SOUTH);

        dictionaryEditorPanel.add(filterPanel, BorderLayout.CENTER);

        JTable table = dictionaryTables.get(dictionaryName);
        dictionaryEditorPanel.add(new JScrollPane(table), BorderLayout.SOUTH);
        add(dictionaryEditorPanel, BorderLayout.EAST);
        revalidate();
        repaint();
    }

    private void editWordInformation(String dictionaryName) {
        JTable table = dictionaryTables.get(dictionaryName);
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a word to edit.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        NonEditableTableModel model = (NonEditableTableModel) table.getModel();
        String[] columns = dictionaryColumns.get(dictionaryName);
        JComponent[] fields = new JComponent[columns.length - 1];
        JPanel panel = new JPanel(new GridLayout(columns.length - 1, 2));

        for (int i = 1; i < columns.length; i++) {
            panel.add(new JLabel(columns[i] + ":"));
            String currentValue = (String) model.getValueAt(selectedRow, i);
            if (columns[i].equalsIgnoreCase("Meaning")) {
                JTextArea textArea = new JTextArea(currentValue);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setRows(3);
                JScrollPane scrollPane = new JScrollPane(textArea);
                fields[i - 1] = scrollPane;
                panel.add(scrollPane);
            } else {
                JTextField textField = new JTextField(currentValue);
                fields[i - 1] = textField;
                panel.add(textField);
            }
        }
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Edit Word Information", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            for (int i = 1; i < columns.length; i++) {
                String newText;
                if (fields[i - 1] instanceof JScrollPane) {
                    JTextArea ta = (JTextArea) ((JScrollPane) fields[i - 1]).getViewport().getView();
                    newText = ta.getText();
                } else {
                    newText = ((JTextField) fields[i - 1]).getText();
                }
                model.setValueAt(newText, selectedRow, i);
            }
            String irlWord = ((String) model.getValueAt(selectedRow, 0)).toLowerCase();
            String[] newRow = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                newRow[i] = (String) model.getValueAt(selectedRow, i);
            }
            Map<String, String[]> dictUpdates = updatedWordInfo.get(dictionaryName);
            if (dictUpdates == null) {
                dictUpdates = new HashMap<>();
                updatedWordInfo.put(dictionaryName, dictUpdates);
            }
            dictUpdates.put(irlWord, newRow);
            saveDictionaries();
        }
    }

    private void addCustomWord(String dictionaryName) {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("IRL Word:"));
        JTextField irlField = new JTextField();
        panel.add(irlField);
        panel.add(new JLabel("OFL Translation:"));
        JTextField oflField = new JTextField();
        panel.add(oflField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Add Custom Word", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String irlWord = irlField.getText().trim();
            String oflWord = oflField.getText().trim();
            if (irlWord.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "IRL Word cannot be empty!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Map<Character, List<String>> baseMap = dictionaryWordMap.get(dictionaryName);
            if (baseMap != null) {
                List<String> wordsForLetter = baseMap.get(Character.toUpperCase(irlWord.charAt(0)));
                if (wordsForLetter != null) {
                    for (String baseWord : wordsForLetter) {
                        if (baseWord.equalsIgnoreCase(irlWord)) {
                            JOptionPane.showMessageDialog(this,
                                    "This word already exists in the base word list!",
                                    "Warning", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }
                }
            }
            List<String> customList = customWords.get(dictionaryName);
            if (customList != null) {
                for (String customEntry : customList) {
                    String[] parts = customEntry.split(" → ");
                    if (parts.length > 0 && parts[0].equalsIgnoreCase(irlWord)) {
                        JOptionPane.showMessageDialog(this,
                                "This custom word already exists!",
                                "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }
            String customEntry = irlWord + " → " + oflWord;
            customWords.computeIfAbsent(dictionaryName, k -> new ArrayList<>()).add(customEntry);
            NonEditableTableModel model = (NonEditableTableModel) dictionaryTables.get(dictionaryName).getModel();
            model.addRow(getRowDataForCustomWord(dictionaryName, irlWord, oflWord));
            saveDictionaries();
        }
    }

    private void removeDictionary(String dictionaryName) {
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Delete " + dictionaryName + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            int index = dictionaryTabs.indexOfTab(dictionaryName);
            if (index != -1) {
                dictionaryTabs.remove(index);
                dictionaryTables.remove(dictionaryName);
                dictionaryColumns.remove(dictionaryName);
                customWords.remove(dictionaryName);
                updatedWordInfo.remove(dictionaryName);
                saveDictionaries();
            }
        }
    }

    private void filterWordsByLetter(String dictionaryName, char letter) {
        System.out.println("Filtering " + dictionaryName + " for " + letter);
        NonEditableTableModel model = (NonEditableTableModel) dictionaryTables.get(dictionaryName).getModel();
        model.setRowCount(0);

        Map<Character, List<String>> letterMap = dictionaryWordMap.get(dictionaryName);
        if (letterMap != null) {
            List<String> words = letterMap.get(Character.toUpperCase(letter));
            if (words != null) {
                for (String word : words) {
                    model.addRow(getRowDataForWord(dictionaryName, word));
                }
            }
        }
        List<String> customList = customWords.get(dictionaryName);
        if (customList != null) {
            for (String customEntry : customList) {
                String[] parts = customEntry.split(" → ");
                if (parts.length > 0 && !parts[0].isEmpty()
                        && Character.toUpperCase(parts[0].charAt(0)) == Character.toUpperCase(letter)) {
                    String oflTranslation = parts.length > 1 ? parts[1] : "";
                    model.addRow(getRowDataForCustomWord(dictionaryName, parts[0], oflTranslation));
                }
            }
        }
    }

    private void filterNumbersAndSymbols(String dictionaryName) {
        System.out.println("Filtering " + dictionaryName + " for Numbers & Symbols");
        NonEditableTableModel model = (NonEditableTableModel) dictionaryTables.get(dictionaryName).getModel();
        model.setRowCount(0);

        List<String> numSymWords = dictionaryNumberOrSymbolWords.get(dictionaryName);
        if (numSymWords != null) {
            for (String word : numSymWords) {
                model.addRow(getRowDataForWord(dictionaryName, word));
            }
        }
        List<String> customList = customWords.get(dictionaryName);
        if (customList != null) {
            for (String customEntry : customList) {
                String[] parts = customEntry.split(" → ");
                if (parts.length > 0 && !parts[0].isEmpty()
                        && !Character.isLetter(parts[0].charAt(0))) {
                    String oflTranslation = parts.length > 1 ? parts[1] : "";
                    model.addRow(getRowDataForCustomWord(dictionaryName, parts[0], oflTranslation));
                }
            }
        }
    }

    private void searchWord(String dictionaryName, String searchTerm) {
        System.out.println("Searching in " + dictionaryName + " for: " + searchTerm);
        NonEditableTableModel model = (NonEditableTableModel) dictionaryTables.get(dictionaryName).getModel();
        model.setRowCount(0);

        // Retrieve all rows (this includes base words and custom words)
        List<String[]> allRows = getDictionaryData(dictionaryName);
        for (String[] row : allRows) {
            if (row.length >= 2
                    && (row[0].equalsIgnoreCase(searchTerm) || row[1].equalsIgnoreCase(searchTerm))) {
                model.addRow(row);
            }
        }
    }

    private void saveDictionaries() {
        JSONArray dictionaries = new JSONArray();
        for (String name : dictionaryTables.keySet()) {
            JSONObject dict = new JSONObject();
            dict.put("name", name);
            dict.put("columns", new JSONArray(dictionaryColumns.get(name)));

            List<String> custom = customWords.get(name);
            dict.put("customWords", custom != null ? new JSONArray(custom) : new JSONArray());

            Map<String, String[]> updates = updatedWordInfo.get(name);
            JSONObject updatesObj = new JSONObject();
            if (updates != null) {
                for (Map.Entry<String, String[]> entry : updates.entrySet()) {
                    updatesObj.put(entry.getKey(), new JSONArray(entry.getValue()));
                }
            }
            dict.put("updatedWordInfo", updatesObj);
            dictionaries.put(dict);
        }

        File saveFile = new File(getSaveFilePath());
        File parentDir = saveFile.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            System.err.println("Failed to create dictionary directory: " + parentDir.getAbsolutePath());
        }

        try (FileWriter writer = new FileWriter(saveFile)) {
            writer.write(dictionaries.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDictionaries() {
        File file = new File(getSaveFilePath());
        if (!file.exists()) {
            return;
        }
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            JSONArray dictionaries = new JSONArray(content);
            for (int i = 0; i < dictionaries.length(); i++) {
                JSONObject dict = dictionaries.getJSONObject(i);
                String name = dict.getString("name");
                String[] columns = dict.getJSONArray("columns").toList().toArray(new String[0]);
                dictionaryColumns.put(name, columns);

                JSONArray customArray = dict.optJSONArray("customWords");
                if (customArray != null) {
                    List<String> customList = new ArrayList<>();
                    for (int j = 0; j < customArray.length(); j++) {
                        customList.add(customArray.getString(j));
                    }
                    customWords.put(name, customList);
                }

                JSONObject updatesObj = dict.optJSONObject("updatedWordInfo");
                if (updatesObj != null) {
                    Map<String, String[]> updatesMap = new HashMap<>();
                    for (String key : updatesObj.keySet()) {
                        JSONArray arr = updatesObj.getJSONArray(key);
                        String[] rowData = new String[arr.length()];
                        for (int j = 0; j < arr.length(); j++) {
                            rowData[j] = arr.get(j).toString();
                        }
                        updatesMap.put(key, rowData);
                    }
                    updatedWordInfo.put(name, updatesMap);
                }

                String irlLanguage = "";
                if (name.contains("To")) {
                    irlLanguage = name.split("To")[0];
                } else if (name.contains(" → ")) {
                    irlLanguage = name.split(" → ")[0];
                } else {
                    irlLanguage = name;
                }
                addDictionaryTab(name, irlLanguage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadDictionaries() {
        dictionaryTabs.removeAll();
        dictionaryTables.clear();
        dictionaryColumns.clear();
        dictionaryWordMap.clear();
        dictionaryNumberOrSymbolWords.clear();
        customWords.clear();
        updatedWordInfo.clear();
        loadDictionaries();
        revalidate();
        repaint();
    }

    /**
     * Reads IRL words from the local text file in
     * /resources/wordlists/{irlLanguage}.txt If not found, user sees a warning
     * and gets an empty list.
     */
    private void loadIRLWords(String dictionaryName, String irlLanguage, NonEditableTableModel model) {
        if (irlLanguage == null || irlLanguage.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No IRL language specified for " + dictionaryName + ". Using empty list.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1) Use launch4j.exedir to get the folder where your EXE is.
        //    If not found, default to "."
        String baseDir = System.getProperty("launch4j.exedir", ".");

        // 2) Construct the path to resources/wordlists/english.txt (for example)
        //    so it becomes something like:
        //    C:\Program Files (x86)\LexiconForge\resources\wordlists\english.txt
        String wordlistPath = "resources/wordlists/" + irlLanguage.toLowerCase() + ".txt";
        File wordFile = new File(baseDir, wordlistPath);

        if (!wordFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "No wordlist found for '" + irlLanguage + "' (" + dictionaryName + "). Using empty list.\n"
                    + "Expected location:\n" + wordFile.getAbsolutePath(),
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3) Read the file
        Map<Character, List<String>> letterMap = new HashMap<>();
        List<String> numSymWords = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(wordFile))) {
            String word;
            while ((word = br.readLine()) != null) {
                if (word.isEmpty()) {
                    continue;
                }

                char firstChar = word.charAt(0);
                if (Character.isLetter(firstChar)) {
                    letterMap
                            .computeIfAbsent(Character.toUpperCase(firstChar), k -> new ArrayList<>())
                            .add(word);
                } else {
                    numSymWords.add(word);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error reading wordlist for " + irlLanguage + ": " + e.getMessage()
                    + "\nUsing empty list.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 4) Store in dictionaryWordMap for filtering
        dictionaryWordMap.put(dictionaryName, letterMap);
        dictionaryNumberOrSymbolWords.put(dictionaryName, numSymWords);

        // 5) Populate the table
        for (char c = 'A'; c <= 'Z'; c++) {
            List<String> words = letterMap.get(c);
            if (words != null) {
                for (String w : words) {
                    model.addRow(getRowDataForWord(dictionaryName, w));
                }
            }
        }
        for (String w : numSymWords) {
            model.addRow(getRowDataForWord(dictionaryName, w));
        }
    }

    // Return list of dictionary names
    public List<String> getDictionaryNames() {
        return new ArrayList<>(dictionaryColumns.keySet());
    }

    // Return columns
    public String[] getDictionaryColumns(String dictName) {
        return dictionaryColumns.get(dictName);
    }

    /**
     * Returns a map of (IRL word -> OFL word) for the specified dictionary
     * (assuming columns[0] is IRL Word, columns[1] is OFL Word).
     */
    public Map<String, String> getDictionaryMap(String dictName) {
        Map<String, String> translationMap = new HashMap<>();
        List<String[]> allRows = getDictionaryData(dictName);
        for (String[] row : allRows) {
            if (row.length >= 2) {
                String irl = (row[0] == null) ? "" : row[0].trim();
                String ofl = (row[1] == null) ? "" : row[1].trim();
                if (!irl.isEmpty()) {
                    translationMap.put(irl.toLowerCase(), ofl);
                }
            }
        }
        return translationMap;
    }

    /**
     * Returns all dictionary rows as a list of String[]. Combines base words
     * (dictionaryWordMap + dictionaryNumberOrSymbolWords) plus custom words.
     */
    public List<String[]> getDictionaryData(String dictName) {
        List<String[]> data = new ArrayList<>();
        Map<Character, List<String>> letterMap = dictionaryWordMap.get(dictName);
        if (letterMap != null) {
            for (char c = 'A'; c <= 'Z'; c++) {
                List<String> words = letterMap.get(c);
                if (words != null) {
                    for (String w : words) {
                        data.add(getRowDataForWord(dictName, w));
                    }
                }
            }
        }
        List<String> numSymWords = dictionaryNumberOrSymbolWords.get(dictName);
        if (numSymWords != null) {
            for (String w : numSymWords) {
                data.add(getRowDataForWord(dictName, w));
            }
        }
        List<String> customList = customWords.get(dictName);
        if (customList != null) {
            for (String entry : customList) {
                String[] parts = entry.split(" → ");
                String ofl = (parts.length > 1) ? parts[1] : "";
                data.add(getRowDataForCustomWord(dictName, parts[0], ofl));
            }
        }
        return data;
    }
}

/* through	reynai
world	mulri
protected	steapi
sei	am
and	ert
i	mi
hello	yisha*/
