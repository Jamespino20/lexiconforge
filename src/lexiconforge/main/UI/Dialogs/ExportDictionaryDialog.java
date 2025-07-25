package lexiconforge.main.UI.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.sql.*;
import java.util.List;
import lexiconforge.main.UI.Panels.DictionaryPanel;
import lexiconforge.main.UI.Frames.MainFrame.UserSession;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.Desktop;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExportDictionaryDialog extends JDialog {

    private DictionaryPanel dictionaryPanel;
    private JComboBox<String> dictionaryComboBox;
    private JRadioButton csvRadio;
    private JRadioButton jsonRadio;
    private JRadioButton sqliteRadio;
    private JRadioButton xlsxRadio;
    private JButton exportButton;
    private JButton cancelButton;

    public ExportDictionaryDialog(Frame owner, DictionaryPanel dictionaryPanel) {
        super(owner, "Export Dictionary", true);
        this.dictionaryPanel = dictionaryPanel;
        initComponents();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Top panel: Dictionary selection
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Select Dictionary:"));
        dictionaryComboBox = new JComboBox<>();
        // Populate using public method from DictionaryPanel.
        List<String> dictNames = dictionaryPanel.getDictionaryNames();
        for (String name : dictNames) {
            dictionaryComboBox.addItem(name);
        }
        topPanel.add(dictionaryComboBox);
        add(topPanel, BorderLayout.NORTH);

        // Center panel: Export format selection
        JPanel formatPanel = new JPanel(new FlowLayout());
        formatPanel.setBorder(BorderFactory.createTitledBorder("Export Format"));
        csvRadio = new JRadioButton("Spreadsheet (CSV)");
        jsonRadio = new JRadioButton("JSON");
        sqliteRadio = new JRadioButton("SQLite");
        xlsxRadio = new JRadioButton("Excel (.xlsx)");
        ButtonGroup group = new ButtonGroup();
        group.add(csvRadio);
        group.add(jsonRadio);
        group.add(sqliteRadio);
        group.add(xlsxRadio);
        // Set default selection
        csvRadio.setSelected(true);
        formatPanel.add(csvRadio);
        formatPanel.add(jsonRadio);
        formatPanel.add(sqliteRadio);
        formatPanel.add(xlsxRadio);
        add(formatPanel, BorderLayout.CENTER);

        // Bottom panel: Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exportButton = new JButton("Export");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(exportButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        exportButton.addActionListener((ActionEvent e) -> exportDictionary());
        cancelButton.addActionListener((ActionEvent e) -> dispose());
    }

    private void exportDictionary() {
        String dictName = (String) dictionaryComboBox.getSelectedItem();
        if (dictName == null || dictName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a dictionary to export.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Retrieve data from DictionaryPanel
        List<String[]> data = dictionaryPanel.getDictionaryData(dictName);
        String[] columns = dictionaryPanel.getDictionaryColumns(dictName);

        if (csvRadio.isSelected()) {
            exportAsCSV(dictName, columns, data);
        } else if (jsonRadio.isSelected()) {
            exportAsJSON(dictName, columns, data);
        } else if (sqliteRadio.isSelected()) {
            exportAsSQLite(dictName, columns, data);
        } else if (xlsxRadio.isSelected()) {
            exportAsXLSX(dictName, columns, data);
        }
    }

    // Builds the export file path in Documents
    // e.g. C:\Users\...\Documents\LexiconForge Files\Users\{currentUser}\Creations\Dictionaries\{dictName}\{dictName}.ext
    private File getExportFile(String dictName, String extension) {
        String safeDictName = dictName.replaceAll("[\\\\/]", "_");
        String currentUser = UserSession.getCurrentUser();

        String filePath = System.getProperty("user.home") + File.separator
                + "Documents" + File.separator
                + "LexiconForge Files" + File.separator
                + "Users" + File.separator
                + currentUser + File.separator
                + "Creations" + File.separator
                + "Dictionaries" + File.separator
                + safeDictName;
        File dir = new File(filePath);
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("Failed to create directories: " + filePath);
        }
        return new File(dir, safeDictName + extension);
    }

    private void exportAsCSV(String dictName, String[] columns, List<String[]> data) {
        File file = getExportFile(dictName, ".csv");
        try (PrintWriter pw = new PrintWriter(file)) {
            // Write header
            pw.println(String.join(",", columns));
            // Write data rows
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    // Escape quotes
                    row[i] = "\"" + row[i].replace("\"", "\"\"") + "\"";
                }
                pw.println(String.join(",", row));
            }
            pw.flush();
            JOptionPane.showMessageDialog(this,
                    "Dictionary exported successfully as CSV to: " + file.getAbsolutePath());
            Desktop.getDesktop().open(file.getParentFile());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportAsJSON(String dictName, String[] columns, List<String[]> data) {
        File file = getExportFile(dictName, ".json");
        try (PrintWriter pw = new PrintWriter(file)) {
            JSONObject dictObject = new JSONObject();
            dictObject.put("name", dictName);
            dictObject.put("columns", new JSONArray(columns));
            dictObject.put("customWords", new JSONArray());

            JSONObject updatedWordInfo = new JSONObject();
            for (String[] row : data) {
                if (row.length > 0) {
                    String irlKey = row[0].toLowerCase();
                    updatedWordInfo.put(irlKey, new JSONArray(row));
                }
            }
            dictObject.put("updatedWordInfo", updatedWordInfo);

            pw.println(dictObject.toString(4));
            pw.flush();

            JOptionPane.showMessageDialog(this,
                    "Dictionary exported successfully as JSON to: " + file.getAbsolutePath());
            Desktop.getDesktop().open(file.getParentFile());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportAsSQLite(String dictName, String[] columns, List<String[]> data) {
        File file = getExportFile(dictName, ".sqlite");
        String url = "jdbc:sqlite:" + file.getAbsolutePath();
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                String tableName = dictName.replaceAll("[^a-zA-Z0-9_]", "_");
                StringBuilder createTable = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                        .append(tableName).append(" (");
                for (int i = 0; i < columns.length; i++) {
                    createTable.append(columns[i].replaceAll("\\s+", "_")).append(" TEXT");
                    if (i < columns.length - 1) {
                        createTable.append(", ");
                    }
                }
                createTable.append(");");
                Statement stmt = conn.createStatement();
                stmt.execute(createTable.toString());

                conn.setAutoCommit(false);

                StringBuilder placeholders = new StringBuilder();
                for (int i = 0; i < columns.length; i++) {
                    placeholders.append("?");
                    if (i < columns.length - 1) {
                        placeholders.append(", ");
                    }
                }
                String insertSQL = "INSERT INTO " + tableName
                        + " VALUES (" + placeholders.toString() + ")";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                for (String[] row : data) {
                    for (int i = 0; i < row.length; i++) {
                        pstmt.setString(i + 1, row[i]);
                    }
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit();
                conn.setAutoCommit(true);

                JOptionPane.showMessageDialog(this,
                        "Dictionary exported successfully as SQLite DB to: " + file.getAbsolutePath());
                Desktop.getDesktop().open(file.getParentFile());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportAsXLSX(String dictName, String[] columns, List<String[]> data) {
        File file = getExportFile(dictName, ".xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            String safeSheetName = WorkbookUtil.createSafeSheetName(dictName);
            Sheet sheet = workbook.createSheet(safeSheetName);

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Data rows
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                String[] rowData = data.get(i);
                for (int j = 0; j < rowData.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(rowData[j]);
                }
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            JOptionPane.showMessageDialog(this,
                    "Dictionary exported successfully as XLSX to: " + file.getAbsolutePath());
            Desktop.getDesktop().open(file.getParentFile());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
