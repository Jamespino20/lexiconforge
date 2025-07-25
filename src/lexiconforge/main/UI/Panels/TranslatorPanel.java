package lexiconforge.main.UI.Panels;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import javax.swing.Timer;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lexiconforge.main.UI.Frames.MainFrame.UserSession;

/**
 * A panel that allows users to select IRL and OFL languages, type text, and get
 * a naive word-for-word translation using data from DictionaryPanel.
 */
public class TranslatorPanel extends JPanel {

    private JComboBox<String> irlDropdown, oflDropdown;
    private JTextArea inputTextArea, outputTextArea;
    private JButton translateButton, clearButton, copyOutputButton;

    // Optional: label for a short "copy to clipboard" GIF
    private JLabel copyGifLabel;

    // Reference to DictionaryPanel so we can fetch dictionary data
    private DictionaryPanel dictionaryPanel;

    public TranslatorPanel(DictionaryPanel dictionaryPanel) {
        this.dictionaryPanel = dictionaryPanel;
        setLayout(new BorderLayout(10, 10));

        Border panelBorder = BorderFactory.createLineBorder(Color.GRAY);
        TitledBorder inputBorder = BorderFactory.createTitledBorder(panelBorder, "Input");
        TitledBorder outputBorder = BorderFactory.createTitledBorder(panelBorder, "Translation");

        // Top panel: 3 rows in a GridLayout
        JPanel topPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        // 1) Language selection row
        JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        irlDropdown = new JComboBox<>(new String[]{
            "English", "Arabic", "Croatian", "Czech",
            "Danish", "Dutch", "English", "French",
            "Georgian", "Hebrew", "Italian", "Norwegian",
            "Polish", "Portuguese", "Russian", "Serbian",
            "Spanish", "Swedish", "Turkish", "Ukrainian"
        });
        oflDropdown = new JComboBox<>();
        loadUserLanguages(); // Populate the oflDropdown from Documents

        languagePanel.add(new JLabel("IRL:"));
        languagePanel.add(irlDropdown);
        languagePanel.add(new JLabel("OFL:"));
        languagePanel.add(oflDropdown);
        topPanel.add(languagePanel);

        // 2) Input area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(inputBorder);
        inputTextArea = new JTextArea(4, 30);
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputPanel.add(inputTextArea, BorderLayout.CENTER);
        topPanel.add(inputPanel);

        // 3) Output area
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(outputBorder);
        outputTextArea = new JTextArea(4, 30);
        outputTextArea.setEditable(false);
        outputTextArea.setLineWrap(true);
        outputTextArea.setWrapStyleWord(true);

        JPanel outputHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        copyOutputButton = new JButton("Copy");
        copyOutputButton.addActionListener(this::copyOutputAction);
        outputHeader.add(copyOutputButton);

        copyGifLabel = new JLabel();
        copyGifLabel.setVisible(false);
        outputHeader.add(copyGifLabel);

        outputPanel.add(outputHeader, BorderLayout.NORTH);
        outputPanel.add(outputTextArea, BorderLayout.CENTER);
        topPanel.add(outputPanel);

        // Bottom panel: translate & clear
        JPanel buttonPanel = new JPanel();
        translateButton = new JButton("Translate");
        clearButton = new JButton("Clear");

        translateButton.addActionListener(e -> translateText());
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(translateButton);
        buttonPanel.add(clearButton);

        add(topPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Load user’s fantasy languages from: C:\Users\...\Documents\LexiconForge
     * Files\Users\{username}\Creations\Languages
     */
    private void loadUserLanguages() {
        String currentUser = UserSession.getCurrentUser();
        if (currentUser == null || currentUser.isEmpty()) {
            System.err.println("Error: No user logged in.");
            return;
        }
        File userLangDir = new File(System.getProperty("user.home")
                + "/Documents/LexiconForge Files/Users/"
                + currentUser + "/Creations/Languages/"
        );

        System.out.println("TranslatorPanel: Checking for OFLs in " + userLangDir.getAbsolutePath());
        oflDropdown.removeAllItems();

        if (!userLangDir.exists() || !userLangDir.isDirectory()) {
            System.err.println("Warning: No language directory found for user " + currentUser);
            return;
        }

        File[] jsonFiles = userLangDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (jsonFiles == null || jsonFiles.length == 0) {
            System.out.println("No .json languages found for user " + currentUser);
            return;
        }
        for (File f : jsonFiles) {
            String baseName = f.getName().replace(".json", "");
            oflDropdown.addItem(baseName);
        }
        oflDropdown.revalidate();
        oflDropdown.repaint();
        System.out.println("TranslatorPanel: total OFLs found: " + oflDropdown.getItemCount());
    }

    /**
     * Splits input into tokens (alphabetic vs. non-alphabetic).
     */
    private List<String> tokenizePreservingDelimiters(String input) {
        String regex = "([a-zA-Z]+)|([^a-zA-Z]+)";
        List<String> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                tokens.add(matcher.group(2));
            }
        }
        return tokens;
    }

    private boolean isAlpha(String s) {
        return s.matches("[A-Za-z]+");
    }

    private String preserveCase(String original, String translation) {
        if (original.length() == 1) {
            // For single-letter words, always return lowercase translation.
            return translation.toLowerCase();
        } else if (original.equals(original.toUpperCase())) {
            // All uppercase
            return translation.toUpperCase();
        } else if (isCapitalized(original)) {
            // Capitalized: first letter uppercase, rest lowercase
            return translation.substring(0, 1).toUpperCase() + translation.substring(1).toLowerCase();
        } else {
            // Otherwise, return as is.
            return translation;
        }
    }

    private boolean isCapitalized(String word) {
        if (word.length() < 2) {
            return word.substring(0, 1).matches("[A-Z]");
        }
        return Character.isUpperCase(word.charAt(0))
                && word.substring(1).equals(word.substring(1).toLowerCase());
    }

    private void translateText() {
        String input = inputTextArea.getText();
        if (input.trim().isEmpty()) {
            outputTextArea.setText("");
            return;
        }

        String irl = (String) irlDropdown.getSelectedItem();   // e.g. "English"
        String ofl = (String) oflDropdown.getSelectedItem();   // e.g. "Kodicai"

        if (irl == null || ofl == null || irl.isEmpty() || ofl.isEmpty()) {
            outputTextArea.setText("Error: Missing IRL or OFL selection.");
            return;
        }

        // Build the dictionary name, e.g. "EnglishToKodicai"
        String dictName = irl + "To" + ofl;

        // Retrieve the IRL->OFL map from DictionaryPanel
        Map<String, String> translationMap = dictionaryPanel.getDictionaryMap(dictName);
        if (translationMap.isEmpty()) {
            outputTextArea.setText("No dictionary found for " + dictName);
            return;
        }

        List<String> tokens = tokenizePreservingDelimiters(input);
        StringBuilder result = new StringBuilder();
        int totalWords = 0;
        int translatedWords = 0;

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (isAlpha(token)) {
                totalWords++;
                String dictionaryKey = token.toLowerCase();
                String oflWord = translationMap.get(dictionaryKey);
                // If translation is missing, use the original token (instead of "[untranslated]")
                if (oflWord == null || oflWord.isEmpty()) {
                    result.append(token);
                } else {
                    // For single-letter tokens, adjust casing based on the next alphabetic token if possible
                    if (token.length() == 1) {
                        String styleToken = getFollowingAlphaToken(tokens, i);
                        if (styleToken != null) {
                            // If the next word is all uppercase, force uppercase; otherwise, capitalize
                            if (styleToken.equals(styleToken.toUpperCase())) {
                                oflWord = oflWord.toUpperCase();
                            } else {
                                oflWord = oflWord.substring(0, 1).toUpperCase() + oflWord.substring(1).toLowerCase();
                            }
                        } else {
                            // Default behavior if no following word found
                            oflWord = preserveCase(token, oflWord);
                        }
                    } else {
                        oflWord = preserveCase(token, oflWord);
                    }
                    result.append(oflWord);
                    translatedWords++;
                }
            } else {
                result.append(token);
            }
        }

        // If there is exactly one alphabetic token and none translated, show message:
        if (totalWords == 1 && translatedWords == 0) {
            outputTextArea.setText("No translation available.");
        } else {
            outputTextArea.setText(result.toString());
        }
    }

    /**
     * Scans forward from the given index and returns the next alphabetic token,
     * or null if not found.
     */
    private String getFollowingAlphaToken(List<String> tokens, int currentIndex) {
        for (int j = currentIndex + 1; j < tokens.size(); j++) {
            String next = tokens.get(j);
            if (isAlpha(next)) {
                return next;
            }
        }
        return null;
    }

    private void clearFields() {
        inputTextArea.setText("");
        outputTextArea.setText("");
    }

    /**
     * Copies output text to clipboard, optionally shows short animation.
     */
    private void copyOutputAction(ActionEvent e) {
        String text = outputTextArea.getText();
        if (text.isEmpty()) {
            return;
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);

        // Example: Show a quick message
        JOptionPane.showMessageDialog(this, "Copied to clipboard!");

        java.net.URL gifURL = getClass().getResource("/resources/pictures/copytoclipboard.gif");
        if (gifURL != null) {
            copyGifLabel.setIcon(new ImageIcon(gifURL));
            copyGifLabel.setVisible(true);
            Timer t = new Timer(1500, evt -> {
                copyGifLabel.setVisible(false);
                copyGifLabel.setIcon(null);
            });
            t.setRepeats(false);
            t.start();
        }
    }
}
