package lexiconforge.main.UI.Dialogs;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;

public class TutorialDialog extends JDialog {

    public TutorialDialog(JFrame parent) {
        super(parent, "Tutorial", true);
        setLayout(new BorderLayout(10, 10));

        // ============ Horizontal Logo at the top ============
        JLabel logoLabel = new JLabel("", SwingConstants.CENTER);
        java.net.URL horizLogoURL = getClass().getClassLoader()
                .getResource("resources/pictures/lexiconforgehorizontallogo.png");
        if (horizLogoURL != null) {
            ImageIcon rawIcon = new ImageIcon(horizLogoURL);
            // Scale to e.g. 200x96 (SCALE_SMOOTH for better quality)
            Image scaledImage = rawIcon.getImage().getScaledInstance(200, 96, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            logoLabel.setText("LexiconForge Logo");
            logoLabel.setForeground(Color.RED);
        }
        add(logoLabel, BorderLayout.NORTH);

        // ============ Tutorial text in the center ============
        String tutorialText
                = """
                  LexiconForge is your all-in-one tool for creating, managing, and translating custom fantasy languages. Whether you want to build a simple conlang or a complex linguistic universe, LexiconForge provides an intuitive interface to get you started. This tutorial walks you through the key features step by step.
                  
                  --------------------------------------------------------------------------------------------
                  
                  1. Logging In and Registering
                  Login Screen:
                  - Enter your username and password.
                  - If you haven’t created an account yet, click Register.
                  
                  Register Screen:
                  - Create a new username and password.
                  - Once registered, return to the login screen and sign in.
                  
                  
                  2. Creating a New Language
                  Open the Language Ribbon: 
                  - At the top of the main window, click Language.
                  
                  Create New Language:
                  - A dialog appears. Enter a Language Name (e.g., “Elvish”), choose a Script Type (e.g., “Latin”), and optionally add phonetics. 
                  - Click Create.
                  - After creation, LexiconForge may restart (or you can manually close the app) to load your new language.
                  
                  Where It’s Saved: 
                  - The language’s metadata is stored as a .json file in your LexiconForge Files/Users/[yourname]/Creations/Languages folder (e.g., Elvish.json).
                  
                  
                  3. Dictionary Panel
                  Accessing: 
                  - Click the Dictionary tab.
                  
                  Create Dictionary: 
                  - Click Create Dictionary at the top.
                  - Select an IRL (In-Reality) Language (e.g., English) and an OFL (Original Fantasy Language) (e.g., Elvish).
                  - Optionally check Phonetics, Meanings, or Synonyms columns.
                  - When you click OK, a new dictionary tab appears (e.g., “EnglishToElvish”).
                  
                  IRL Wordlists:
                  - If LexiconForge finds a matching .txt wordlist for your IRL language, it automatically loads those words into the table. 
                  - If no .txt file is found, you’ll see an empty table with a warning.
                  
                  Filtering & Searching:
                  - Click a letter (A–Z) to filter by words starting with that letter.
                  - Click Numbers & Symbols to show words starting with non-letter characters.
                  - Enter a word in the Search field and press Enter to see exact matches.
                  
                  
                  4. Editing Word Information
                  Open Editor: 
                  - Click the Edit Word Information button next to your dictionary tab.
                  
                  Add Custom Word:
                  - Enter an IRL word and its OFL translation.
                  - The new row appears in your dictionary.
                  
                  Edit Word Information:
                  - Select a row in the dictionary table, click Edit Word Information.
                  - A dialog appears showing all columns (e.g., IRL Word, OFL Word, Meaning, Synonyms).
                  - Make your changes and click OK to save.
                  
                  
                  5. Importing Dictionaries
                  - Open the Language Ribbon and click Import Dictionary.
                  
                  Choose File:
                  - Supported formats: .json, .csv, .xlsx, .sqlite.
                  - A preview of the file’s content is shown.
                  
                  Import:
                  - The file is parsed into a dictionary object. Do note that it can take a while to load these files.
                  - If the dictionary references a fantasy language you don’t have, LexiconForge may prompt you to create or overwrite that language.
                  - Once imported, the dictionary is added to saved_dictionaries.json. LexiconForge restarts or reloads so you see the new dictionary in the Dictionary tab.
                  
                  
                  6. Exporting Dictionaries
                  - Open the Language Ribbon and click Export Dictionary.
                  
                  Choose Dictionary: 
                  - Select from the dropdown.
                  
                  Select Format: 
                  - CSV, JSON, SQLite, or XLSX.
                  
                  Export:
                  - LexiconForge saves the dictionary to your user’s Creations/Dictionaries folder.
                  - For JSON, it creates a single top-level JSON object containing “name,” “columns,” “customWords,” and 
                  “updatedWordInfo.”
                  - For CSV/XLSX/SQLite, it converts all rows into the respective format.
                  
                  
                  7. Translator Panel
                  Open the Translator Tab:
                  - The top portion has two dropdowns: IRL and OFL.
                  - By default, IRL is a built-in list (e.g., English, Spanish, etc.), and OFL is loaded from your Creations/Languages .json files.                  
                  - Type Your Text in the input area.
                  
                  Translate:
                  - Click Translate.
                  - LexiconForge tries to match each word in your input against the dictionary named “IRLToOFL” (e.g., “EnglishToElvish”).
                  - If a match is found, it replaces the IRL word with the OFL word, preserving uppercase or capitalized forms.
                  - Unrecognized words become [untranslated].
                  - If you typed only one word and it’s not recognized, you see “No translation available.”
                  
                  Clear / Copy:
                  - Clear empties both input and output.
                  - Copy copies the translated text to your clipboard.
                  
                  
                  8. Preferences
                  Dark Mode: 
                  - Toggle in the Preferences ribbon.
                  
                  Fonts:
                  - Change the global font. The UI restyles accordingly.
                  
                  Exit / Logout:
                  - Use the Exit ribbon to log out or close the app.
                  
                  
                  9. Best Practices & Tips
                  Naming Conventions:
                  - Keep dictionary names in the format “IRLToOFL” so the translator can automatically find them (e.g., “EnglishToElvish”).
                  
                  Custom Words:
                  - If you need IRL words not in the default .txt list, add them via Add Custom Word.
                  
                  Managing Large Dictionaries:
                  - Use letter filtering (A–Z) to find words faster.
                  - Or use the Search box for exact matches.
                  
                  Sharing:
                  If you share a dictionary with someone else, also share the fantasy language .json file located in Creations/Languages so they can import both.
                  
                  
                  10. Where Your Files Are Stored
                  Languages:
                  - In Documents/LexiconForge Files/Users/[username]/Creations/Languages, each language is a single .json (e.g., Elvish.json).
                  
                  Dictionaries:
                  - In Documents/LexiconForge Files/Users/[username]/Creations/Dictionaries, stored in saved_dictionaries.json plus any exported files.
                  
                  IRL Wordlists: 
                  -In src/resources/wordlists (or your chosen folder) if you want LexiconForge to auto-populate base IRL words.
                  
                  --------------------------------------------------------------------------------------------
                  
                  LexiconForge brings together language creation, dictionary management, and on-the-fly translation in one intuitive interface. Whether you’re just starting with a small conlang or managing a full dictionary for a novel or RPG setting, these features provide a powerful toolkit to keep everything organized.
                  
                  Feel free to experiment with custom columns (Phonetics, Meanings, Synonyms), advanced import/export formats, or the built-in translator. For feedback and further questions or troubleshooting, feel free to reach out at sg.remotionanmusicharmer@gmail.com. 
                  
                  Hope you enjoy using LexiconForge, where you forge words into worlds!
                  """;

        JTextArea tutorialArea = new JTextArea(tutorialText);
        tutorialArea.setEditable(false);
        tutorialArea.setLineWrap(true);
        tutorialArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(tutorialArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // ============ Close button at the bottom ============
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener((ActionEvent e) -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(500, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
}
