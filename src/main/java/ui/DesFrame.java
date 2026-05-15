package ui;

import des.DesService;
import des.EncodingFormat;
import des.EncodingUtils;
import des.InputFormat;
import file.FileService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;

public class DesFrame extends JFrame {
    private static final String WORKSPACE_CARD = "workspace";
    private static final String KEY_INFO_CARD = "keyInfo";

    private static final Color PAGE_BACKGROUND = new Color(245, 247, 250);
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color SIDEBAR_BACKGROUND = new Color(235, 240, 246);
    private static final Color PRIMARY_COLOR = new Color(32, 111, 89);
    private static final Color PRIMARY_DARK = new Color(23, 82, 66);
    private static final Color BORDER_COLOR = new Color(218, 225, 233);
    private static final Color ERROR_COLOR = new Color(170, 35, 35);
    private static final Color SUCCESS_COLOR = new Color(25, 105, 65);
    private static final Color TEXT_MUTED = new Color(86, 96, 110);

    private final DesService desService;
    private final FileService fileService;
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);
    private final JLabel statusLabel = new JLabel("Sẵn sàng");
    private final JTextField keyField = new JTextField();
    private final JTextArea inputArea = new JTextArea();
    private final JTextArea outputArea = new JTextArea();
    private final JTextArea keyInfoArea = new JTextArea();
    private final JComboBox<InputFormat> inputFormatCombo = new JComboBox<>(InputFormat.values());
    private final JComboBox<EncodingFormat> outputFormatCombo = new JComboBox<>(EncodingFormat.values());

    public DesFrame() {
        this(new DesService(), new FileService());
    }

    DesFrame(DesService desService, FileService fileService) {
        super("DES Studio");
        this.desService = desService;
        this.fileService = fileService;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setLocationByPlatform(true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PAGE_BACKGROUND);

        add(buildBody(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        configureInputs();
        pack();
    }

    private JSplitPane buildBody() {
        contentPanel.setBackground(PAGE_BACKGROUND);
        contentPanel.add(buildWorkspacePanel(), WORKSPACE_CARD);
        contentPanel.add(buildKeyInfoPanel(), KEY_INFO_CARD);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildNavigationPanel(), contentPanel);
        splitPane.setResizeWeight(0);
        splitPane.setDividerLocation(220);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerSize(1);
        return splitPane;
    }

    private JPanel buildNavigationPanel() {
        JPanel navigation = new JPanel();
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));
        navigation.setBackground(SIDEBAR_BACKGROUND);
        navigation.setBorder(BorderFactory.createEmptyBorder(24, 16, 16, 16));
        navigation.setPreferredSize(new Dimension(220, 0));

        JLabel title = new JLabel("DES Studio");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        JLabel subtitle = new JLabel("Bảo mật chính xác");
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 26, 0));

        JButton workspaceButton = navigationButton("Mã hóa/Giải mã");
        JButton keyInfoButton = navigationButton("Thông tin khóa");

        workspaceButton.addActionListener(event -> showCard(WORKSPACE_CARD, "Mã hóa/Giải mã"));
        keyInfoButton.addActionListener(event -> {
            refreshKeyInfo();
            showCard(KEY_INFO_CARD, "Thông tin khóa");
        });

        navigation.add(title);
        navigation.add(subtitle);
        navigation.add(workspaceButton);
        navigation.add(Box.createVerticalStrut(8));
        navigation.add(keyInfoButton);
        navigation.add(Box.createVerticalGlue());
        return navigation;
    }

    private JButton navigationButton(String text) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setBackground(PANEL_BACKGROUND);
        button.setFocusPainted(false);
        return button;
    }

    private JPanel buildWorkspacePanel() {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setBackground(PAGE_BACKGROUND);
        workspace.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));

        addWorkspacePanel(workspace, buildSecretKeyPanel(), 0, 0, 3, 0, new Insets(0, 0, 18, 0));
        addWorkspacePanel(workspace, buildInputPanel(), 0, 1, 1, 1, new Insets(0, 0, 0, 16));
        addWorkspacePanel(workspace, buildActionPanel(), 1, 1, 1, 1, new Insets(0, 0, 0, 16));
        addWorkspacePanel(workspace, buildOutputPanel(), 2, 1, 1, 1, new Insets(0, 0, 0, 0));
        return workspace;
    }

    private JPanel buildSecretKeyPanel() {
        JPanel panel = cardPanel("Khóa bí mật");
        panel.setLayout(new BorderLayout(10, 10));

        keyField.setToolTipText("16 ký tự Hex, tương đương 8 byte");
        keyField.setFont(Font.decode(Font.MONOSPACED));

        JButton generateButton = neutralButton("Tạo ngẫu nhiên");
        JButton loadKeyButton = neutralButton("Tải khóa");
        JButton saveKeyButton = neutralButton("Lưu khóa");
        JButton clearButton = neutralButton("Xóa dữ liệu");

        generateButton.addActionListener(event -> generateRandomKey());
        loadKeyButton.addActionListener(event -> loadKeyFile());
        saveKeyButton.addActionListener(event -> saveKeyFile());
        clearButton.addActionListener(event -> clearData());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(generateButton);
        buttons.add(loadKeyButton);
        buttons.add(saveKeyButton);
        buttons.add(clearButton);

        panel.add(keyField, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildInputPanel() {
        JPanel panel = cardPanel("Dữ liệu vào");
        panel.setLayout(new BorderLayout(10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setOpaque(false);
        topBar.add(new JLabel("Định dạng"));
        topBar.add(inputFormatCombo);

        JButton loadButton = neutralButton("Tải file");
        loadButton.addActionListener(event -> loadInputFile());
        topBar.add(loadButton);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(150, 0));

        JButton encryptButton = primaryButton("Mã hóa");
        JButton decryptButton = outlineButton("Giải mã");
        encryptButton.addActionListener(event -> encrypt());
        decryptButton.addActionListener(event -> decrypt());

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        encryptButton.setAlignmentX(CENTER_ALIGNMENT);
        decryptButton.setAlignmentX(CENTER_ALIGNMENT);
        actions.add(encryptButton);
        actions.add(Box.createVerticalStrut(12));
        actions.add(decryptButton);

        panel.add(actions);
        return panel;
    }

    private JPanel buildOutputPanel() {
        JPanel panel = cardPanel("Kết quả");
        panel.setLayout(new BorderLayout(10, 10));

        outputArea.setEditable(false);
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setOpaque(false);
        topBar.add(new JLabel("Định dạng"));
        topBar.add(outputFormatCombo);

        JButton copyButton = neutralButton("Sao chép");
        JButton saveButton = neutralButton("Lưu file");
        copyButton.addActionListener(event -> copyOutput());
        saveButton.addActionListener(event -> saveOutputFile());
        topBar.add(copyButton);
        topBar.add(saveButton);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildKeyInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(PAGE_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));

        JPanel card = cardPanel("Thông tin khóa");
        card.setLayout(new BorderLayout(10, 10));
        keyInfoArea.setEditable(false);
        keyInfoArea.setFont(Font.decode(Font.MONOSPACED));
        keyInfoArea.setText(buildKeyInfoText());
        card.add(new JScrollPane(keyInfoArea), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel cardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(title),
                        BorderFactory.createEmptyBorder(14, 14, 14, 14))));
        return panel;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(128, 42));
        button.setMaximumSize(new Dimension(128, 42));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private JButton outlineButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(128, 42));
        button.setMaximumSize(new Dimension(128, 42));
        button.setBackground(PANEL_BACKGROUND);
        button.setForeground(PRIMARY_DARK);
        button.setFocusPainted(false);
        return button;
    }

    private JButton neutralButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        return button;
    }

    private void addWorkspacePanel(JPanel target, JPanel panel, int x, int y, int width,
                                   double weightY, Insets insets) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.weightx = width == 1 && x == 1 ? 0 : 1;
        constraints.weighty = weightY;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = insets;
        target.add(panel, constraints);
    }

    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(PAGE_BACKGROUND);
        statusBar.setBorder(BorderFactory.createEmptyBorder(8, 20, 12, 20));
        statusLabel.setForeground(Color.DARK_GRAY);
        statusBar.add(statusLabel, BorderLayout.WEST);
        return statusBar;
    }

    private void configureInputs() {
        outputFormatCombo.setSelectedItem(EncodingFormat.BASE64);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        keyInfoArea.setLineWrap(false);
        keyField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshKeyInfo();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshKeyInfo();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshKeyInfo();
            }
        });
    }

    private void generateRandomKey() {
        try {
            keyField.setText(desService.generateRandomKeyHex());
            showSuccess("Đã tạo khóa DES ngẫu nhiên.");
        } catch (RuntimeException exception) {
            showError("Không thể tạo khóa DES. Vui lòng thử lại.", exception);
        }
    }

    private void encrypt() {
        if (!validateInput("mã hóa")) {
            return;
        }
        if (!validateKey()) {
            return;
        }
        if (!validateSelectedInputFormat()) {
            return;
        }

        try {
            String result = desService.encrypt(
                    inputArea.getText(),
                    selectedInputFormat(),
                    keyField.getText(),
                    selectedOutputFormat());
            outputArea.setText(result);
            showSuccess("Mã hóa thành công.");
        } catch (RuntimeException exception) {
            showError("Mã hóa thất bại. Vui lòng kiểm tra dữ liệu đầu vào.", exception);
        }
    }

    private void decrypt() {
        if (!validateInput("giải mã")) {
            return;
        }
        if (!validateKey()) {
            return;
        }
        if (!validateSelectedInputFormat()) {
            return;
        }

        try {
            String result = desService.decrypt(
                    inputArea.getText(),
                    selectedInputFormat(),
                    keyField.getText(),
                    selectedOutputFormat());
            outputArea.setText(result);
            showSuccess("Giải mã thành công.");
        } catch (RuntimeException exception) {
            showError("Giải mã thất bại. Vui lòng kiểm tra khóa hoặc dữ liệu đầu vào.", exception);
        }
    }

    private boolean validateInput(String actionName) {
        if (inputArea.getText() == null || inputArea.getText().isBlank()) {
            showError("Vui lòng nhập dữ liệu cần " + actionName + ".");
            return false;
        }
        return true;
    }

    private boolean validateKey() {
        String key = normalizedKey();
        if (key.isBlank()) {
            showError("Vui lòng tạo hoặc nhập khóa DES trước.");
            return false;
        }
        if (key.length() != 16) {
            showError("Khóa DES phải có đúng 16 ký tự Hex.");
            return false;
        }
        if (!isHex(key)) {
            showError("Khóa DES chỉ được chứa ký tự Hex.");
            return false;
        }
        return true;
    }

    private boolean validateSelectedInputFormat() {
        InputFormat format = selectedInputFormat();
        String input = inputArea.getText();
        try {
            if (format == InputFormat.HEX) {
                EncodingUtils.decodeHex(removeWhitespace(input));
            } else if (format == InputFormat.BASE64) {
                EncodingUtils.decodeBase64(removeWhitespace(input));
            }
            return true;
        } catch (IllegalArgumentException exception) {
            if (format == InputFormat.HEX) {
                showError("Dữ liệu đầu vào không đúng định dạng Hex.", exception);
            } else {
                showError("Dữ liệu đầu vào không đúng định dạng Base64.", exception);
            }
            return false;
        }
    }

    private void loadInputFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(loadFileFilter());
        int choice = chooser.showOpenDialog(this);
        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        try {
            inputArea.setText(fileService.readSupportedFile(selectedFile.toPath()));
            showSuccess("Đã tải file " + selectedFile.getName() + ".");
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage(), exception);
        } catch (IOException exception) {
            showError("Không thể đọc file. Vui lòng kiểm tra lại file đã chọn.", exception);
        }
    }

    private void saveOutputFile() {
        if (outputArea.getText() == null || outputArea.getText().isBlank()) {
            showError("Kết quả đang rỗng, không có dữ liệu để lưu.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(saveFileFilter());
        int choice = chooser.showSaveDialog(this);
        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        try {
            fileService.writeText(selectedFile.toPath(), outputArea.getText());
            showSuccess("Đã lưu file " + selectedFile.getName() + ".");
        } catch (IOException exception) {
            showError("Không thể ghi file. Vui lòng chọn vị trí khác.", exception);
        }
    }

    private void loadKeyFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("File khóa (*.key, *.txt)", "key", "txt"));
        int choice = chooser.showOpenDialog(this);
        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        try {
            String key = fileService.readText(selectedFile.toPath()).replaceAll("\\s+", "").toUpperCase();
            keyField.setText(key);
            if (validateKey()) {
                showSuccess("Đã tải khóa từ " + selectedFile.getName() + ".");
            }
        } catch (IOException exception) {
            showError("Không thể đọc file khóa.", exception);
        }
    }

    private void saveKeyFile() {
        if (!validateKey()) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("File khóa (*.key, *.txt)", "key", "txt"));
        int choice = chooser.showSaveDialog(this);
        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        try {
            fileService.writeText(selectedFile.toPath(), normalizedKey());
            showSuccess("Đã lưu khóa vào " + selectedFile.getName() + ".");
        } catch (IOException exception) {
            showError("Không thể ghi file khóa. Vui lòng chọn vị trí khác.", exception);
        }
    }

    private void copyOutput() {
        if (outputArea.getText() == null || outputArea.getText().isBlank()) {
            showError("Kết quả đang rỗng, không có dữ liệu để sao chép.");
            return;
        }

        try {
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(outputArea.getText()), null);
            showSuccess("Đã sao chép kết quả.");
        } catch (RuntimeException exception) {
            showError("Không thể sao chép kết quả.", exception);
        }
    }

    private void clearData() {
        inputArea.setText("");
        outputArea.setText("");
        showStatus("Đã xóa dữ liệu vào và kết quả.");
    }

    private void refreshKeyInfo() {
        keyInfoArea.setText(buildKeyInfoText());
        keyInfoArea.setCaretPosition(0);
    }

    private String buildKeyInfoText() {
        String key = normalizedKey();
        StringBuilder builder = new StringBuilder();
        builder.append("Khóa hiện tại: ").append(key.isBlank() ? "(chưa nhập)" : key).append(System.lineSeparator());
        builder.append("Độ dài khóa: ").append(key.length()).append("/16 ký tự Hex").append(System.lineSeparator());
        builder.append("Định dạng: Hex").append(System.lineSeparator());

        if (key.isBlank()) {
            builder.append("Trạng thái hợp lệ: Chưa có khóa").append(System.lineSeparator());
            builder.append("Xem trước nhị phân: -").append(System.lineSeparator());
        } else if (key.length() == 16 && isHex(key)) {
            byte[] keyBytes = EncodingUtils.decodeDesKeyHex(key);
            builder.append("Trạng thái hợp lệ: Hợp lệ").append(System.lineSeparator());
            builder.append("Xem trước nhị phân: ").append(binaryPreview(keyBytes)).append(System.lineSeparator());
        } else {
            builder.append("Trạng thái hợp lệ: Không hợp lệ").append(System.lineSeparator());
            builder.append("Xem trước nhị phân: -").append(System.lineSeparator());
        }

        builder.append("Kích thước khối: 64 bit").append(System.lineSeparator());
        builder.append("Độ dài khóa hiệu dụng: 56 bit").append(System.lineSeparator());
        builder.append("Số vòng: 16").append(System.lineSeparator());
        return builder.toString();
    }

    private String binaryPreview(byte[] keyBytes) {
        StringBuilder builder = new StringBuilder();
        int previewBytes = Math.min(4, keyBytes.length);
        for (int index = 0; index < previewBytes; index++) {
            if (index > 0) {
                builder.append(' ');
            }
            String binary = Integer.toBinaryString(keyBytes[index] & 0xff);
            builder.append("0".repeat(8 - binary.length())).append(binary);
        }
        builder.append(" ...");
        return builder.toString();
    }

    private FileNameExtensionFilter loadFileFilter() {
        return new FileNameExtensionFilter(
                "File được hỗ trợ (*.txt, *.csv, *.json, *.xml, *.docx, *.pdf)",
                fileService.supportedLoadExtensions());
    }

    private FileNameExtensionFilter saveFileFilter() {
        return new FileNameExtensionFilter(
                "File văn bản (*.txt, *.csv, *.json, *.xml)",
                fileService.supportedSaveExtensions());
    }

    private InputFormat selectedInputFormat() {
        return (InputFormat) inputFormatCombo.getSelectedItem();
    }

    private EncodingFormat selectedOutputFormat() {
        return (EncodingFormat) outputFormatCombo.getSelectedItem();
    }

    private String normalizedKey() {
        String key = keyField.getText();
        if (key == null) {
            return "";
        }
        return removeWhitespace(key).toUpperCase();
    }

    private String removeWhitespace(String value) {
        return value.replaceAll("\\s+", "");
    }

    private boolean isHex(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (Character.digit(value.charAt(index), 16) == -1) {
                return false;
            }
        }
        return true;
    }

    private void showCard(String cardName, String label) {
        contentLayout.show(contentPanel, cardName);
        showStatus(label);
    }

    private void showStatus(String message) {
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setText(message);
    }

    private void showSuccess(String message) {
        statusLabel.setForeground(SUCCESS_COLOR);
        statusLabel.setText(message);
    }

    private void showError(String message) {
        showError(message, null);
    }

    private void showError(String message, Exception exception) {
        statusLabel.setForeground(ERROR_COLOR);
        statusLabel.setText(message);
        if (exception != null) {
            exception.printStackTrace(System.err);
        }
        JOptionPane.showMessageDialog(this, message, "DES Studio", JOptionPane.ERROR_MESSAGE);
    }
}
