package lexiconforge.main.UI.Frames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import lexiconforge.utils.Database.UserDatabase;
import lexiconforge.utils.SettingsProperties.ConfigManager;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckBox;
    public static String currentUser;

    public LoginFrame() {
        // Force dark mode
        ConfigManager.setProperty("dark_mode", "true");
        UserDatabase.initializeDatabase();

        setTitle("LexiconForge - Login");
        setSize(550, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Load icon from resources
        java.net.URL iconURL = getClass().getClassLoader()
                .getResource("resources/pictures/lexiconforgeicon.png");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            setIconImage(icon.getImage());
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

        // Use GridBagLayout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        // =============== ROW 0: HEADER (Welcome + Logo) ===============
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, gbc);

        // Reset for subsequent rows
        gbc.gridwidth = 1;

        // =============== ROW 1: USERNAME LABEL & FIELD ===============
        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        add(usernameField, gbc);

        // =============== ROW 2: PASSWORD LABEL & FIELD ===============
        gbc.gridy = 2;
        gbc.gridx = 0;
        add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        add(passwordField, gbc);

        // =============== ROW 3: FORGOT PASSWORD? + SHOW PASSWORD ===============
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel forgotPasswordLabel = new JLabel("<HTML><U>Forgot Password?</U></HTML>");
        forgotPasswordLabel.setForeground(Color.WHITE);
        forgotPasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openResetPasswordDialog();
            }
        });
        add(forgotPasswordLabel, gbc);

        gbc.gridx = 1;
        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('*');
            }
        });
        add(showPasswordCheckBox, gbc);

        // =============== ROW 4: LOGIN & REGISTER BUTTONS ===============
        gbc.gridy = 4;
        gbc.gridx = 0;
        JButton loginButton = new JButton("Login");
        add(loginButton, gbc);

        gbc.gridx = 1;
        JButton registerButton = new JButton("Register");
        add(registerButton, gbc);

        // Listeners
        loginButton.addActionListener(e -> authenticateUser());
        registerButton.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        // Press Enter to log in
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    authenticateUser();
                }
            }
        };
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
        loginButton.addKeyListener(enterKeyListener);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome to");
        welcomeLabel.setFont(new Font("Brush Script MT", Font.PLAIN, 22));
        welcomeLabel.setForeground(new Color(218, 165, 32));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(welcomeLabel);

        JLabel logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        java.net.URL horizLogoURL = getClass().getClassLoader()
                .getResource("resources/pictures/lexiconforgehorizontallogo.png");
        if (horizLogoURL != null) {
            ImageIcon rawIcon = new ImageIcon(horizLogoURL);
            Image scaledImage = rawIcon.getImage()
                    .getScaledInstance(200, 96, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            logoLabel.setText("LexiconForge Logo");
            logoLabel.setForeground(Color.RED);
        }
        headerPanel.add(logoLabel);

        return headerPanel;
    }

    private void authenticateUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Hardcoded admin
        if (username.equals("admin") && password.equals("admin123")) {
            currentUser = username;
            JOptionPane.showMessageDialog(this, "Login Successful!");
            dispose();
            new MainFrame(currentUser, null);
            return;
        }

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please input your credentials",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (UserDatabase.validateUser(username, password)) {
            currentUser = username;
            JOptionPane.showMessageDialog(this, "Login Successful!");
            dispose();
            new MainFrame(currentUser, null);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Username or password is incorrect",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openResetPasswordDialog() {
        new ResetPasswordDialog(this).setVisible(true);
    }

    // ===================== RESET PASSWORD DIALOG (Dynamic Q combos) =====================
    private class ResetPasswordDialog extends JDialog {

        private JTextField userField;
        private JComboBox<String> q1Dropdown, q2Dropdown, q3Dropdown;
        private JTextField a1Field, a2Field, a3Field;
        private JPasswordField newPassField;

        private final List<String> allQuestions;
        private boolean isSyncing = false;

        public ResetPasswordDialog(JFrame parent) {
            super(parent, "Reset Password", true);
            setSize(420, 380);
            setLocationRelativeTo(parent);
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;

            allQuestions = Arrays.asList(
                    "What is your favorite color?",
                    "What is your birth city?",
                    "What is your pet's name?",
                    "Who is your childhood hero?",
                    "What is your mother's maiden name?"
            );

            // Username
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(new JLabel("Username:"), gbc);

            gbc.gridx = 1;
            userField = new JTextField(12);
            add(userField, gbc);

            // Q1
            gbc.gridy = 1;
            gbc.gridx = 0;
            add(new JLabel("Q1:"), gbc);
            gbc.gridx = 1;
            q1Dropdown = new JComboBox<>(allQuestions.toArray(new String[0]));
            add(q1Dropdown, gbc);

            // A1
            gbc.gridy = 2;
            gbc.gridx = 0;
            add(new JLabel("Answer1:"), gbc);
            gbc.gridx = 1;
            a1Field = new JTextField(12);
            add(a1Field, gbc);

            // Q2
            gbc.gridy = 3;
            gbc.gridx = 0;
            add(new JLabel("Q2:"), gbc);
            gbc.gridx = 1;
            q2Dropdown = new JComboBox<>(allQuestions.toArray(new String[0]));
            add(q2Dropdown, gbc);

            // A2
            gbc.gridy = 4;
            gbc.gridx = 0;
            add(new JLabel("Answer2:"), gbc);
            gbc.gridx = 1;
            a2Field = new JTextField(12);
            add(a2Field, gbc);

            // Q3
            gbc.gridy = 5;
            gbc.gridx = 0;
            add(new JLabel("Q3:"), gbc);
            gbc.gridx = 1;
            q3Dropdown = new JComboBox<>(allQuestions.toArray(new String[0]));
            add(q3Dropdown, gbc);

            // A3
            gbc.gridy = 6;
            gbc.gridx = 0;
            add(new JLabel("Answer3:"), gbc);
            gbc.gridx = 1;
            a3Field = new JTextField(12);
            add(a3Field, gbc);

            // New password
            gbc.gridy = 7;
            gbc.gridx = 0;
            add(new JLabel("New Password:"), gbc);
            gbc.gridx = 1;
            newPassField = new JPasswordField(12);
            add(newPassField, gbc);

            // Buttons
            gbc.gridy = 8;
            gbc.gridx = 0;
            JButton resetBtn = new JButton("Reset");
            add(resetBtn, gbc);

            gbc.gridx = 1;
            JButton cancelBtn = new JButton("Cancel");
            add(cancelBtn, gbc);

            // Add dynamic Q combos
            q1Dropdown.addActionListener(e -> syncDropdowns(q1Dropdown));
            q2Dropdown.addActionListener(e -> syncDropdowns(q2Dropdown));
            q3Dropdown.addActionListener(e -> syncDropdowns(q3Dropdown));

            resetBtn.addActionListener(e -> doReset());
            cancelBtn.addActionListener(e -> dispose());
        }

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

                q1Dropdown.setSelectedItem(sel1);
                q2Dropdown.setSelectedItem(sel2);
                q3Dropdown.setSelectedItem(sel3);
            } finally {
                isSyncing = false;
            }
        }

        private void rebuildModel(JComboBox<String> combo, String excludeA, String excludeB) {
            String currentSelection = (String) combo.getSelectedItem();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

            for (String q : allQuestions) {
                if (!q.equals(excludeA) && !q.equals(excludeB) || q.equals(currentSelection)) {
                    model.addElement(q);
                }
            }
            combo.setModel(model);
        }

        private void doReset() {
            String uname = userField.getText().trim();
            String question1 = (String) q1Dropdown.getSelectedItem();
            String answer1 = a1Field.getText().trim();
            String question2 = (String) q2Dropdown.getSelectedItem();
            String answer2 = a2Field.getText().trim();
            String question3 = (String) q3Dropdown.getSelectedItem();
            String answer3 = a3Field.getText().trim();
            String newPass = new String(newPassField.getPassword());

            if (uname.isEmpty() || newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username & new password required!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = UserDatabase.resetPassword(
                    uname, question1, answer1, question2, answer2, question3, answer3, newPass
            );
            if (success) {
                JOptionPane.showMessageDialog(this, "Password reset successful!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Verification failed or user not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
