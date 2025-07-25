# LexiconForge README

## Overview

LexiconForge is a Java-based multi-class graphical user interface (GUI) application designed to create, manage, and translate dictionaries for constructed languages (conlangs) or ciphers. It is built to support language development, allowing users to input and export words, manage phonetics, and customize translation dictionaries between different languages. 

This tool is ideal for language enthusiasts, writers, or developers who are working with fictional languages or ciphers and need a streamlined interface to manage their linguistic creations.

---

## Table of Contents

- [Key Features](#key-features)
- [Installation](#installation)
- [Usage](#usage)
- [Known Limitations](#known-limitations)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)
- [Credits](#credits)

---

## Key Features

- Language & Dictionary Management:
  - Create, Import, and Export Dictionaries for various languages.
  - Organize dictionaries with easy-to-manage word lists, including phonetics and meanings.
  - Export dictionaries in multiple formats: JSON, SQLite, and Spreadsheet.

- Translator:
  - Translate between languages while preserving casing.
  - Supports over 20 languages including English, Spanish, French, German, etc.
  
- Custom Dictionaries:
  - Add words unique to a dictionary without affecting the base language wordlist.
  - Edit word information including translations, meanings, phonetics, and synonyms.
  - Apply A-Z and number/symbol-based word filtering for efficient word lookup.

- User-Specific Storage:
  - Each user has their own personalized language and dictionary storage.
  - Preferences such as theme, font, and language settings are saved per user.
  
- Wordlist Integration:
  - Pre-load word lists for various languages (IRLs) during dictionary creation.
  - Link word lists directly to the dictionaries in the DictionaryPanel.
  
- Export Options:
  - Export dictionaries into user-friendly formats for use in other applications or for sharing with others.

---

## Installation

1. Pre-requisites:
   - Java 8 or higher.
   - A Java IDE (e.g., IntelliJ IDEA, Eclipse) or the JDK installed on your system.


3. Build the Project:
   If you're using an IDE, you can import the project directly and build it from there. Otherwise, you can use Ant to build the project:

   java -jar "LexiconForge.jar"

5. Run the Application:
   After the build completes successfully, you can run LexiconForge with the following command:
   java -jar LexiconForge.jar

   If using an IDE, simply run the main class (`SplashScreen`).

---

## Usage

### Splash Screen and Login
- Upon starting LexiconForge, you’ll be greeted by a splash screen, followed by the login page.
- If you're a new user, click "Register" to create an account.
- The login page includes an option to show/hide the password, along with error handling for incorrect login attempts.

### Main Interface

1. Ribbon:
   The application uses a Microsoft Office-inspired ribbon for easy navigation:
   - Language Tab: Manage languages, create new ones, import/export dictionaries, change preferences, and access help and exit options.
   - Translator Tab: Translate text between languages, with options to select and work with stored IRLs and OFLs.
   - Dictionary Tab: Manage dictionaries, view word lists, edit words, and export dictionaries.

2. Dictionary Panel:
   - Create and manage dictionaries with a set of word lists.
   - Link wordlists to IRL languages when creating new dictionaries.
   - Apply A-Z filtering to view words in a given letter range, including a dedicated "Numbers and Symbols" category.
   - Add new words to a dictionary, ensuring that these words don’t affect the base wordlist.

3. Translation Panel:
   - Easily translate text between two selected languages, with live previews of the translation.
   - Maintain text casing where applicable.
   
4. Word Editing:
   - Add new words to dictionaries or edit existing entries via a simple dialog interface.
   - Modify word meanings, translations, phonetics, and synonyms.
   - Confirm changes before returning to the main dictionary panel.

### File Export/Import

- Exporting Dictionaries:
1. Navigate to the Export Dictionary button in the Ribbon.
2. Select the dictionary to export.
3. Choose the format: JSON, SQLite, or Spreadsheet.
4. Select the destination folder in your file system. The dictionary will be saved in a folder structure that maintains user-specific data.

- Importing Dictionaries:
1. Navigate to the Import Dictionary button in the Ribbon.
2. Browse and select a dictionary file.
3. The application will import the dictionary into the user’s language storage.

### User Preferences

LexiconForge saves individual user preferences, including:
-*Theme: Switch between light and dark themes.
- Font Preferences: Customize font size and style to suit your needs.
- Stored Languages and Dictionaries: The application remembers your active languages and dictionaries for quick access in future sessions.

---

## Known Issues & Limitations

- Scrollbar Issues: There may be temporary glitches with scrolling in some panels, particularly in the **Translator Panel**.
- Dialog Box Glitch: Occasionally, dialog boxes may open multiple times if triggered repeatedly; this is being addressed in the next patch.
- Names-Word Mixup: Names which are also common words like "Hope", "Mark", "Grace", "Bill", and so on do not have restrictions to being translated. Implementation of name lists, NERs, or context-aware translations are being decided on.

---

## Roadmap

- Implement enhanced word search functionality for faster lookups.
- Expand the dictionary import/export functionality to support additional formats.
- Finalize the integration of wordlists for all supported languages.
- Fix minor bugs in the user interface related to text field errors and scrolling behavior.

---

## Contributing

Contributions are welcome! Please:
- Open issues for bugs/feature requests.
- Fork and submit pull requests for improvements.
- Ensure code follows existing style and is well-tested.

---

## License

LexiconForge is licensed under the Apache 2.0 License. See the LICENSE file for more information.

---

## Credits
kkrypt0nn for the wordlists: https://github.com/kkrypt0nn/wordlists/tree/main/wordlists/languages

Substance jar file: https://jar-download.com/artifacts/com.github.insubstantial/substance-flamingo/7.2.1/source-code

Flamingo jar file by kirill-grouchnikov: https://github.com/kirill-grouchnikov/radiance/blob/sunshine/archive/flamingo/flamingo-5.3.00.jar

Trident jar file by kirill-grouchnikov: https://github.com/kirill-grouchnikov/radiance/blob/sunshine/archive/trident/trident-1.5.00.jar
