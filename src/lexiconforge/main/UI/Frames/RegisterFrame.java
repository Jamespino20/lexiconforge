package lexiconforge.main.UI.Frames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import lexiconforge.utils.Database.UserDatabase;
import lexiconforge.utils.SettingsProperties.ConfigManager;

public class RegisterFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField, confirmPasswordField;
    private JCheckBox showPasswordCheckbox;

    private JComboBox<String> q1Dropdown, q2Dropdown, q3Dropdown;
    private JTextField a1Field, a2Field, a3Field;

    // For dynamic question removal
    private final List<String> allQuestions;
    private boolean isSyncing = false; // A small guard to avoid infinite loops

    public RegisterFrame() {
        // Force dark mode
        ConfigManager.setProperty("dark_mode", "true");
        UserDatabase.initializeDatabase();

        setTitle("Register - LexiconForge");
        setSize(500, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Load icon
        java.net.URL iconURL = getClass().getClassLoader()
                .getResource("resources/pictures/lexiconforgeicon.png");
        if (iconURL != null) {
            setIconImage(new ImageIcon(iconURL).getImage());
        }

        // Immediately apply dark theme
        try {
            UIManager.setLookAndFeel(
                    new org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel()
            );
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Common questions
        String[] commonQuestionsArray = {
            "What is your favorite color?",
            "What is your birth city?",
            "What is your pet's name?",
            "Who is your childhood hero?",
            "What is your mother's maiden name?"
        };
        // Store them in a List so we can filter easily
        allQuestions = new ArrayList<>(Arrays.asList(commonQuestionsArray));

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // =============== ROW 0: Header ===============
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel registerLabel = new JLabel("Register to access");
        registerLabel.setFont(new Font("Brush Script MT", Font.PLAIN, 28));
        registerLabel.setForeground(new Color(218, 165, 32));
        registerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(registerLabel);

        JLabel logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        java.net.URL horizLogoURL = getClass().getClassLoader()
                .getResource("resources/pictures/lexiconforgehorizontallogo.png");
        if (horizLogoURL != null) {
            ImageIcon rawIcon = new ImageIcon(horizLogoURL);
            Image scaledImage = rawIcon.getImage().getScaledInstance(200, 96, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            logoLabel.setText("LexiconForge Logo");
            logoLabel.setForeground(Color.RED);
        }
        headerPanel.add(logoLabel);

        add(headerPanel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // =============== ROW 1: Username ===============
        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        add(usernameField, gbc);

        // =============== ROW 2: Password ===============
        gbc.gridy = 2;
        gbc.gridx = 0;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        add(passwordField, gbc);

        // =============== ROW 3: Confirm Password ===============
        gbc.gridy = 3;
        gbc.gridx = 0;
        add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(15);
        add(confirmPasswordField, gbc);

        // =============== ROW 4: Show Password Checkbox ===============
        gbc.gridy = 4;
        gbc.gridx = 1;
        showPasswordCheckbox = new JCheckBox("Show Password");
        add(showPasswordCheckbox, gbc);

        // =============== ROW 5..10: Q & A ===============
        gbc.gridy = 5;
        gbc.gridx = 0;
        add(new JLabel("Question 1:"), gbc);
        gbc.gridx = 1;
        q1Dropdown = new JComboBox<>(allQuestions.toArray(new String[0]));
        add(q1Dropdown, gbc);

        gbc.gridy = 6;
        gbc.gridx = 0;
        add(new JLabel("Answer 1:"), gbc);
        gbc.gridx = 1;
        a1Field = new JTextField(15);
        add(a1Field, gbc);

        gbc.gridy = 7;
        gbc.gridx = 0;
        add(new JLabel("Question 2:"), gbc);
        gbc.gridx = 1;
        q2Dropdown = new JComboBox<>(allQuestions.toArray(new String[0]));
        add(q2Dropdown, gbc);

        gbc.gridy = 8;
        gbc.gridx = 0;
        add(new JLabel("Answer 2:"), gbc);
        gbc.gridx = 1;
        a2Field = new JTextField(15);
        add(a2Field, gbc);

        gbc.gridy = 9;
        gbc.gridx = 0;
        add(new JLabel("Question 3:"), gbc);
        gbc.gridx = 1;
        q3Dropdown = new JComboBox<>(allQuestions.toArray(new String[0]));
        add(q3Dropdown, gbc);

        gbc.gridy = 10;
        gbc.gridx = 0;
        add(new JLabel("Answer 3:"), gbc);
        gbc.gridx = 1;
        a3Field = new JTextField(15);
        add(a3Field, gbc);

        // =============== ROW 11: Register & Back Buttons ===============
        gbc.gridy = 11;
        gbc.gridx = 0;
        JButton registerButton = new JButton("Register");
        add(registerButton, gbc);

        gbc.gridx = 1;
        JButton backButton = new JButton("Back");
        add(backButton, gbc);

        // Show/hide password
        showPasswordCheckbox.addActionListener(e -> {
            boolean show = showPasswordCheckbox.isSelected();
            passwordField.setEchoChar(show ? '\0' : '•');
            confirmPasswordField.setEchoChar(show ? '\0' : '•');
        });

        // Add dynamic removal for q1, q2, q3
        q1Dropdown.addActionListener(e -> syncDropdowns(q1Dropdown));
        q2Dropdown.addActionListener(e -> syncDropdowns(q2Dropdown));
        q3Dropdown.addActionListener(e -> syncDropdowns(q3Dropdown));

        registerButton.addActionListener(e -> registerUser());
        backButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    /**
     * Called whenever one of the three question dropdowns changes; it rebuilds
     * the other dropdowns so that the same question is not repeated.
     */
    private void syncDropdowns(JComboBox<String> changedCombo) {
        if (isSyncing) {
            return;
        }
        isSyncing = true;
        try {
            String sel1 = (String) q1Dropdown.getSelectedItem();
            String sel2 = (String) q2Dropdown.getSelectedItem();
            String sel3 = (String) q3Dropdown.getSelectedItem();

            rebuildModel(q1Dropdown, sel2, sel3);
            rebuildModel(q2Dropdown, sel1, sel3);
            rebuildModel(q3Dropdown, sel1, sel2);

            // Reselect the user’s current choices
            q1Dropdown.setSelectedItem(sel1);
            q2Dropdown.setSelectedItem(sel2);
            q3Dropdown.setSelectedItem(sel3);
        } finally {
            isSyncing = false;
        }
    }

    /**
     * Rebuilds the model for the given combo, excluding the two questions that
     * are already used in the other combos (excludeA and excludeB).
     */
    private void rebuildModel(JComboBox<String> combo, String excludeA, String excludeB) {
        String currentSelection = (String) combo.getSelectedItem();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

        for (String q : allQuestions) {
            // Add the question if it is not one of the excludes
            // or if it equals the combo’s current selection
            if ((!q.equals(excludeA) && !q.equals(excludeB)) || q.equals(currentSelection)) {
                model.addElement(q);
            }
        }
        combo.setModel(model);
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{6,}$")) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 6 characters, contain 1 uppercase letter & 1 number!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String q1 = (String) q1Dropdown.getSelectedItem();
        String a1 = a1Field.getText().trim();
        String q2 = (String) q2Dropdown.getSelectedItem();
        String a2 = a2Field.getText().trim();
        String q3 = (String) q3Dropdown.getSelectedItem();
        String a3 = a3Field.getText().trim();

        if (a1.isEmpty() || a2.isEmpty() || a3.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill out all verification answers!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = UserDatabase.registerUser(
                username, password, q1, a1, q2, a2, q3, a3
        );
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Registration successful!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Username already exists!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
