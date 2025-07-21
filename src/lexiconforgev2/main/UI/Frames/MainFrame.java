package lexiconforgev2.main.UI.Frames;

import lexiconforgev2.main.UI.Dialogs.*;
import lexiconforgev2.main.UI.Panels.DictionaryPanel;
import lexiconforgev2.main.UI.Panels.TranslatorPanel;
import lexiconforgev2.utils.SettingsProperties.ConfigManager;
import lexiconforgev2.utils.Database.UserDatabase;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.icon.EmptyResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.*;
import org.pushingpixels.substance.api.skin.*;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private JRibbon ribbon;
    private Font currentFont;
    private String currentUser;
    private TranslatorPanel translatorPanel;
    private DictionaryPanel dictionaryPanel;

    public MainFrame(String username, String userLanguages) {
        ConfigManager.setProperty("dark_mode", "true");
        String loggedInUser = (username == null) ? LoginFrame.currentUser : username;
        UserSession.setCurrentUser(loggedInUser);

        System.out.println("MainFrame initialized for: " + loggedInUser);

        UserDatabase.initializeDatabase();

        setTitle("LexiconForge");
        setSize(965, 800);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        applyFont(ConfigManager.getFontSetting());

        try {
            UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int choice = ExitDialog.showExitDialog(MainFrame.this);
                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                    new LoginFrame().setVisible(true);
                } else if (choice == JOptionPane.NO_OPTION) {
                    System.exit(0);
                }
            }
        });

        java.net.URL iconURL = getClass().getClassLoader()
                .getResource("resources/pictures/lexiconforgeicon.png");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            setIconImage(icon.getImage());
        }

        setLocationRelativeTo(null);

        ribbon = new JRibbon();
        ribbon.addTask(new RibbonTask("Language", createLanguageBand()));
        ribbon.addTask(new RibbonTask("Preferences", createPreferencesBand()));
        ribbon.addTask(new RibbonTask("Help", createHelpBand()));
        ribbon.addTask(new RibbonTask("Account", createAccountBand()));
        ribbon.addTask(new RibbonTask("Exit", createExitBand()));

        dictionaryPanel = new DictionaryPanel();

        tabbedPane = new JTabbedPane();
        translatorPanel = new TranslatorPanel(dictionaryPanel);
        tabbedPane.addTab("Translator", translatorPanel);
        tabbedPane.addTab("Dictionary", dictionaryPanel);

        getContentPane().add(ribbon, BorderLayout.NORTH);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        setVisible(true);
    }

    public class UserSession {

        private static String currentUser;

        public static void setCurrentUser(String username) {
            currentUser = username;
        }

        public static String getCurrentUser() {
            return currentUser;
        }
    }

    private JRibbonBand createLanguageBand() {
        JRibbonBand band = new JRibbonBand("Language", new EmptyResizableIcon(32));
        band.addCommandButton(
                createRibbonButton("Create New Language", "createnewlanguage", e -> createNewLanguage()),
                RibbonElementPriority.TOP
        );
        band.addCommandButton(
                createRibbonButton("Import Dictionary", "importdictionary", e -> importDictionary()),
                RibbonElementPriority.TOP
        );
        band.addCommandButton(
                createRibbonButton("Export Dictionary", "exportdictionary", e -> exportDictionary()),
                RibbonElementPriority.TOP
        );
        setBandResizePolicies(band);
        return band;
    }

    private JRibbonBand createPreferencesBand() {
        JRibbonBand band = new JRibbonBand("Preferences", new EmptyResizableIcon(32));
        band.addCommandButton(
                createRibbonButton("Dark Mode", "themeicon", e -> SwingUtilities.invokeLater(this::toggleDarkMode)),
                RibbonElementPriority.TOP
        );
        band.addCommandButton(
                createRibbonButton("Fonts", "changefont", e -> changeFont()),
                RibbonElementPriority.TOP
        );
        setBandResizePolicies(band);
        return band;
    }

    private JRibbonBand createHelpBand() {
        JRibbonBand band = new JRibbonBand("Help", new EmptyResizableIcon(32));
        band.addCommandButton(
                createRibbonButton("Tutorial", "tutorialicon", e -> showTutorial()),
                RibbonElementPriority.TOP
        );
        band.addCommandButton(
                createRibbonButton("About", "abouticon", e -> showAbout()),
                RibbonElementPriority.TOP
        );
        setBandResizePolicies(band);
        return band;
    }

    private JRibbonBand createAccountBand() {
        JRibbonBand band = new JRibbonBand("Account", new EmptyResizableIcon(32));
        // Example: "Delete Account" button
        band.addCommandButton(
                createRibbonButton("Delete Account", "accounticon", e -> deleteAccount()),
                RibbonElementPriority.TOP
        );
        setBandResizePolicies(band);
        return band;
    }

    private JRibbonBand createExitBand() {
        JRibbonBand band = new JRibbonBand("Exit", new EmptyResizableIcon(32));
        band.addCommandButton(
                createRibbonButton("Logout", "logouticon", e -> logout()),
                RibbonElementPriority.TOP
        );
        band.addCommandButton(
                createRibbonButton("Exit", "exiticon", e -> exitApp()),
                RibbonElementPriority.TOP
        );
        setBandResizePolicies(band);
        return band;
    }

    private void setBandResizePolicies(JRibbonBand band) {
        List<RibbonBandResizePolicy> policies
                = new ArrayList<>(CoreRibbonResizePolicies.getCorePoliciesNone(band));
        policies.removeIf(policy
                -> policy.getClass().getSimpleName().equals("IconRibbonBandResizePolicy"));
        band.setResizePolicies(policies);
    }

    private JCommandButton createRibbonButton(String title, String iconBaseName, ActionListener action) {
        ResizableIcon icon = getThemedIcon(iconBaseName);
        JCommandButton button = new JCommandButton(title, icon);
        button.setCommandButtonKind(JCommandButton.CommandButtonKind.ACTION_ONLY);
        button.addActionListener(action);
        return button;
    }

    private ResizableIcon getThemedIcon(String iconBaseName) {
        boolean isDarkMode = Boolean.parseBoolean(ConfigManager.getProperty("dark_mode"));
        String folder = isDarkMode ? "dark" : "light";
        String iconPath = "/resources/pictures/" + folder + "/"
                + iconBaseName + (isDarkMode ? "dark.png" : "light.png");

        java.net.URL iconURL = getClass().getResource(iconPath);
        if (iconURL == null) {
            System.err.println("Icon not found: " + iconPath);
            return new EmptyResizableIcon(32);
        }
        ImageIcon imgIcon = new ImageIcon(iconURL);
        return ImageWrapperResizableIcon.getIcon(imgIcon.getImage(),
                new Dimension(32, 32));
    }

    // ========== Ribbon button actions ==========
    private void createNewLanguage() {
        String username = UserSession.getCurrentUser();
        if (username == null || username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No user is logged in. Please log in first.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CreateLanguageDialog cld
                = new CreateLanguageDialog(this, translatorPanel, false);
        cld.setVisible(true);
    }

    private void importDictionary() {
        new ImportDictionaryDialog(this, dictionaryPanel).setVisible(true);
    }

    private void exportDictionary() {
        new ExportDictionaryDialog(this, dictionaryPanel).setVisible(true);
    }

    private void toggleDarkMode() {
        boolean isDarkMode = Boolean.parseBoolean(ConfigManager.getProperty("dark_mode"));
        isDarkMode = !isDarkMode;
        ConfigManager.setProperty("dark_mode", String.valueOf(isDarkMode));
        applyTheme(isDarkMode);
    }

    private void applyTheme(boolean isDark) {
        try {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(isDark
                            ? new SubstanceGraphiteLookAndFeel()
                            : new SubstanceBusinessLookAndFeel());
                } catch (UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                SwingUtilities.updateComponentTreeUI(this);
                revalidate();
                repaint();
                reinitRibbon();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reinitRibbon() {
        getContentPane().remove(ribbon);
        ribbon = new JRibbon();
        ribbon.addTask(new RibbonTask("Language", createLanguageBand()));
        ribbon.addTask(new RibbonTask("Preferences", createPreferencesBand()));
        ribbon.addTask(new RibbonTask("Help", createHelpBand()));
        ribbon.addTask(new RibbonTask("Account", createAccountBand()));
        ribbon.addTask(new RibbonTask("Exit", createExitBand()));
        getContentPane().add(ribbon, BorderLayout.NORTH);
        revalidate();
        repaint();
    }

    private void changeFont() {
        FontSettingsDialog fontDialog = new FontSettingsDialog(this, ribbon);
        fontDialog.setVisible(true);
    }

    private void applyFont(String fontName) {
        currentFont = new Font(fontName, Font.PLAIN, 14);
        SwingUtilities.invokeLater(() -> {
            updateFontRecursive(this);
            if (ribbon != null) {
                ribbon.revalidate();
                ribbon.repaint();
            }
            SwingUtilities.updateComponentTreeUI(this);
        });
    }

    private void updateFontRecursive(Component component) {
        if (component == null) {
            return;
        }
        component.setFont(currentFont);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                updateFontRecursive(child);
            }
        }
        component.repaint();
    }

    public void setNewFont(String fontName) {
        ConfigManager.setFontSetting(fontName);
        applyFont(fontName);
    }

    private void showTutorial() {
        new TutorialDialog(this).setVisible(true);
    }

    private void showAbout() {
        new AboutDialog(this).setVisible(true);
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    public void restartApp() {
        String username = UserSession.getCurrentUser();
        SwingUtilities.invokeLater(() -> {
            dispose();
            new MainFrame(username, null).setVisible(true);
        });
    }

    private void exitApp() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?",
                "Confirm Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    // ========== "Account" tab feature: delete account ==========
    private void deleteAccount() {
        String user = UserSession.getCurrentUser();
        if (user == null || user.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No user is logged in.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to DELETE your account? This cannot be undone.",
                "Delete Account", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // Remove from DB
            UserDatabase.deleteUser(user);
            // Remove from file system
            UserDatabase.deleteUserFolder(user);
            JOptionPane.showMessageDialog(this,
                    "Account deleted successfully. Goodbye!",
                    "Account Deleted", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
