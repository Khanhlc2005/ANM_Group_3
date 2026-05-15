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
        UIManager.put("FileChooser.openButtonText", "Mở");
        UIManager.put("FileChooser.saveButtonText", "Lưu");
        UIManager.put("FileChooser.cancelButtonText", "Hủy");
        UIManager.put("FileChooser.fileNameLabelText", "Tên file:");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Loại file:");
        UIManager.put("FileChooser.lookInLabelText", "Thư mục:");
        UIManager.put("FileChooser.saveInLabelText", "Lưu tại:");
        UIManager.put("FileChooser.acceptAllFileFilterText", "Tất cả file");
        UIManager.put("FileChooser.openDialogTitleText", "Mở file");
        UIManager.put("FileChooser.saveDialogTitleText", "Lưu file");
    }
}
