package app;

import com.formdev.flatlaf.FlatLightLaf;
import ui.DesFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setupLookAndFeel();
            new DesFrame().setVisible(true);
        });
    }

    static void setupLookAndFeel() {
        FlatLightLaf.setup();
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
    }
}
