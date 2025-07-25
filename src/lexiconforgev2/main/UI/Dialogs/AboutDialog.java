package lexiconforge.main.UI.Dialogs;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;

public class AboutDialog extends JDialog {

    public AboutDialog(JFrame parent) {
        super(parent, "About LexiconForge", true);
        setLayout(new BorderLayout(15, 15));

        // Logo at the top
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

        // About text in the center
        String aboutHtml = "<html><body style='width:400px;'>"
            + "<h2>LexiconForge</h2>"
            + "<p>Version 2.2</p>"
            + "<p>Created by Jamespino20</p>"
            + "</body></html>";
        JLabel aboutLabel = new JLabel(aboutHtml);
        aboutLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(aboutLabel, BorderLayout.CENTER);

        // Close button at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener((ActionEvent e) -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }
}
