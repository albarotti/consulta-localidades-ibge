package utfpr;

import javax.swing.SwingUtilities;
import utfpr.view.TelaLocalidades;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaLocalidades().setVisible(true));
    }
}
