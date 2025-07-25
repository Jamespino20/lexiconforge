package lexiconforge.main.UI.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import lexiconforge.main.UI.Frames.MainFrame;
import lexiconforge.main.UI.Frames.MainFrame.UserSession;
import org.json.JSONObject;
import lexiconforge.main.UI.Panels.TranslatorPanel;

public class CreateLanguageDialog extends JDialog {

    private JTextField languageNameField;
    private JComboBox<String> scriptTypeDropdown;
    private JTextField phoneticsField;
    private JButton createButton, cancelButton;
    private TranslatorPanel translatorPanel; // Not used for dynamic updates, just stored reference
    private boolean skipRestart = false;

    // Existing constructor
    public CreateLanguageDialog(JFrame parent, TranslatorPanel translatorPanel, boolean skipRestart) {
        super(parent, "Create New Language", true);
        this.translatorPanel = translatorPanel;
        initUI(null);
        this.skipRestart = skipRestart;
    }

    // New constructor that can prefill the language name
    public CreateLanguageDialog(JFrame parent, TranslatorPanel translatorPanel,
            String prefilledLanguageName, boolean skipRestart) {
        super(parent, "Create New Language", true);
        this.translatorPanel = translatorPanel;
        this.skipRestart = skipRestart;
        initUI(prefilledLanguageName);
    }

    private void initUI(String prefilledLanguageName) {
        setLayout(new BorderLayout(10, 10));

        String username = UserSession.getCurrentUser();
        if (username == null || username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No user is logged in. Please log in first.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        inputPanel.add(new JLabel("Language Name:"));
        languageNameField = new JTextField();
        if (prefilledLanguageName != null && !prefilledLanguageName.isEmpty()) {
            languageNameField.setText(prefilledLanguageName);
        }
        inputPanel.add(languageNameField);

        inputPanel.add(new JLabel("Script Type:"));
        scriptTypeDropdown = new JComboBox<>(new String[]{
            "Latin", "Greek", "Cyrillic", "Korean", "Chinese",
            "Devnagari", "Thai", "Tamil", "Kana", "Arabic", "Hebrew"
        });
        inputPanel.add(scriptTypeDropdown);

        inputPanel.add(new JLabel("Phonetics:"));
        phoneticsField = new JTextField();
        inputPanel.add(phoneticsField);

        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        createButton = new JButton("Create");
        cancelButton = new JButton("Cancel");

        createButton.addActionListener(this::handleCreateLanguage);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    private void handleCreateLanguage(ActionEvent e) {
        String languageName = languageNameField.getText().trim();
        String scriptType = (String) scriptTypeDropdown.getSelectedItem();
        String phonetics = phoneticsField.getText().trim();

        if (languageName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Language name cannot be empty!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String currentUser = UserSession.getCurrentUser();
        if (currentUser == null || currentUser.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No user is logged in. Please log in first.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        File userLanguagesDir = new File(System.getProperty("user.home")
                + "/Documents/LexiconForge Files/Users/"
                + currentUser + "/Creations/Languages/"
        );

        if (!userLanguagesDir.exists() && !userLanguagesDir.mkdirs()) {
            JOptionPane.showMessageDialog(this,
                    "Failed to create language directory.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Save language metadata in a JSON file
        File languageDataFile = new File(userLanguagesDir, languageName + ".json");
        JSONObject langData = new JSONObject();
        langData.put("name", languageName);
        langData.put("scriptType", scriptType);
        langData.put("phonetics", phonetics);
        System.out.println("📝 Saving file at: " + languageDataFile.getAbsolutePath());

        try (FileWriter writer = new FileWriter(languageDataFile)) {
            writer.write(langData.toString(4));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving language data.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Language created successfully!\n\nPress OK to refresh LexiconForge.",
                "Language Created",
                JOptionPane.INFORMATION_MESSAGE);

        if (!skipRestart) {
            JFrame owner = (JFrame) getOwner();
            if (owner instanceof MainFrame) {
                ((MainFrame) owner).restartApp();
            }
        }
        dispose();
    }
}
