package lexiconforge.main.UI.Dialogs;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ExitDialog {

    public static int showExitDialog(JFrame parent) {
        Object[] options = {"Logout", "Exit", "Cancel"};
        return JOptionPane.showOptionDialog(parent,
                "Do you want to logout or exit the application?",
                "Confirm Exit",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[2]);
    }
}
