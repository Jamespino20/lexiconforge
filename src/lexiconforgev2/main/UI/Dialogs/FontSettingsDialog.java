package lexiconforgev2.main.UI.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Properties;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;

public class FontSettingsDialog extends JDialog {

    private JComboBox<String> fontDropdown;
    private JLabel previewLabel;
    private JButton applyButton, resetButton;
    private Properties properties;
    private ActionListener applyActionListener;
    private ActionListener resetActionListener;
    private static final String PROPERTIES_FILE = "font_settings.properties";
    private static final String DEFAULT_FONT = "Times New Roman";

    private static final String[] AVAILABLE_FONTS = {
        "Garamond", "Baskerville", "Times New Roman", "Georgia", "Sabon",
        "Century Schoolbook", "Montserrat", "Helvetica", "Comfortaa"
    };

    private JFrame parentFrame;
    private JRibbon ribbon;

    public FontSettingsDialog(JFrame parent, JRibbon ribbon) {
        super(parent, "Font Settings", true);
        this.ribbon = ribbon;
        this.parentFrame = parent;
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Load font preferences
        properties = new Properties();
        loadFontPreference();

        // Dropdown (Left Side)
        fontDropdown = new JComboBox<>(AVAILABLE_FONTS);
        fontDropdown.setSelectedItem(properties.getProperty("selectedFont", DEFAULT_FONT));
        fontDropdown.addActionListener(e -> updatePreviewFont());

        // Preview Panel (Right Side)
        previewLabel = new JLabel("The quick brown fox jumps over the lazy dog.");
        previewLabel.setFont(new Font(fontDropdown.getSelectedItem().toString(), Font.PLAIN, 16));

        // Buttons
        applyButton = new JButton("Apply");
        resetButton = new JButton("Reset to Default");

        applyActionListener = e -> applyFont();
        resetActionListener = e -> resetToDefault();

        applyButton.addActionListener(applyActionListener);
        resetButton.addActionListener(resetActionListener);

        // Layout Setup
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        controlPanel.add(new JLabel("Select Font:"), BorderLayout.NORTH);
        controlPanel.add(fontDropdown, BorderLayout.CENTER);

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
        previewPanel.add(previewLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(applyButton);
        buttonPanel.add(resetButton);

        add(controlPanel, BorderLayout.WEST);
        add(previewPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void updatePreviewFont() {
        String selectedFont = (String) fontDropdown.getSelectedItem();
        previewLabel.setFont(new Font(selectedFont, Font.PLAIN, 16));
    }

    private void applyFont() {
        disableButtons();  // Prevents additional clicks during execution
        String selectedFont = (String) fontDropdown.getSelectedItem();
        saveFontPreference(selectedFont);
        applyFontToAllComponents(parentFrame, selectedFont);

        JOptionPane.showMessageDialog(this,
                "Font has been applied: " + selectedFont,
                "Font Applied",
                JOptionPane.INFORMATION_MESSAGE);

        refreshUI();
        enableButtons();  // Re-enable after execution
    }

    private void resetToDefault() {
        disableButtons();
        fontDropdown.setSelectedItem(DEFAULT_FONT);
        previewLabel.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 16));
        saveFontPreference(DEFAULT_FONT);
        applyFontToAllComponents(parentFrame, DEFAULT_FONT);

        JOptionPane.showMessageDialog(this,
                "Font has been reset to default: " + DEFAULT_FONT,
                "Reset to Default",
                JOptionPane.INFORMATION_MESSAGE);

        refreshUI();
        enableButtons();
    }

    private void disableButtons() {
        applyButton.setEnabled(false);
        resetButton.setEnabled(false);
    }

    private void enableButtons() {
        applyButton.setEnabled(true);
        resetButton.setEnabled(true);
    }

    private void loadFontPreference() {
        File file = new File(PROPERTIES_FILE);
        if (!file.exists()) {
            saveFontPreference(DEFAULT_FONT);
            return;
        }

        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFontPreference(String fontName) {
        properties.setProperty("selectedFont", fontName);
        try (FileOutputStream output = new FileOutputStream(PROPERTIES_FILE)) {
            properties.store(output, "Font Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyFontToAllComponents(Container container, String fontName) {
        Font newFont = new Font(fontName, Font.PLAIN, 14);
        applyFontRecursively(container, newFont);
    }

    private void applyFontRecursively(Container container, Font font) {
        for (Component component : container.getComponents()) {
            component.setFont(font);
            if (component instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) component;
                if (scrollPane.getVerticalScrollBar() != null) {
                    scrollPane.getVerticalScrollBar().updateUI();
                }
                if (scrollPane.getHorizontalScrollBar() != null) {
                    scrollPane.getHorizontalScrollBar().updateUI();
                }
            }
            if (component instanceof Container) {
                applyFontRecursively((Container) component, font);
            }
        }
    }

    @Override
    public void dispose() {
        System.out.println("Disposing FontSettingsDialog...");
        super.dispose();
        System.out.println("Disposed successfully.");
    }

    private void refreshUI() {
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.updateComponentTreeUI(parentFrame);
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
            refreshFlamingoRibbon();
        });
    }

    private void refreshFlamingoRibbon() {
        SwingUtilities.invokeLater(() -> {
            if (ribbon != null) {
                try {
                    Thread.sleep(50);  // Slight delay before revalidating
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                try {
                    ribbon.revalidate();
                    ribbon.repaint();
                } catch (NullPointerException e) {
                    System.err.println("Ribbon update failed: " + e.getMessage());
                }
            }
        });
    }
}
