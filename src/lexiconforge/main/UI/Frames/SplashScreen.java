package lexiconforge.main.UI.Frames;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {

    public SplashScreen() {
        // Load the image
        java.net.URL imageURL = getClass().getClassLoader().getResource("resources/pictures/lexiconforgelogo.png");
        if (imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL);
            JLabel splashLabel = new JLabel(icon);
            splashLabel.setOpaque(false);
            getContentPane().setBackground(new Color(0, 0, 0, 0));
            getContentPane().add(splashLabel, BorderLayout.CENTER);
        } else {
            System.err.println("Error: Image not found!");
            JLabel errorLabel = new JLabel("LexiconForge");
            getContentPane().add(errorLabel, BorderLayout.CENTER);
        }

        pack();
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        // Load and set the application icon
        java.net.URL iconURL = getClass().getClassLoader().getResource("resources/pictures/lexiconforgeicon.png");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            setIconImage(icon.getImage());
        } else {
            System.err.println("Error: Icon not found!");
        }

        setVisible(true);

        // Timer to dispose splash screen and open login
        Timer timer = new Timer(3000, e -> {
            ((Timer) e.getSource()).stop();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });
        timer.setRepeats(false);
        timer.start();
    }

    public static void main(String[] args) {
        new SplashScreen();
    }
}
