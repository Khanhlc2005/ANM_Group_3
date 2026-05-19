package ui;

import des.DesService;
import des.DesBlockTrace;
import des.EncodingFormat;
import des.EncodingUtils;
import des.DesProcessResult;
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
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class DesFrame extends JFrame {
    private static final String WORKSPACE_CARD = "workspace";
    private static final String KEY_INFO_CARD = "keyInfo";
    private static final String PROCESS_CARD = "process";

    private static final Color PAGE_BACKGROUND = new Color(245, 247, 250);
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color SIDEBAR_BACKGROUND = new Color(235, 240, 246);
    private static final Color ACTIVE_NAV_BACKGROUND = new Color(213, 234, 228);
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
    private final JButton workspaceButton = navigationButton("Mã hóa/Giải mã");
    private final JButton keyInfoButton = navigationButton("Thông tin khóa");
    private final JButton processButton = navigationButton("DES Process");
    private final JLabel statusLabel = new JLabel("Sẵn sàng");
    private final JTextField keyField = new JTextField();
    private final JTextArea inputArea = new JTextArea();
    private final JTextArea outputArea = new JTextArea();
    private final JTextArea outputPreviewArea = new JTextArea();
    private final JTextArea keyInfoArea = new JTextArea();
    private final JTextArea processArea = new JTextArea();
    private final JLabel keyStatusLabel = new JLabel();
    private final JLabel inputCounterLabel = new JLabel();
    private final JLabel outputCounterLabel = new JLabel();
    private final JComboBox<InputFormat> inputFormatCombo = new JComboBox<>(InputFormat.values());
    private final JComboBox<EncodingFormat> outputFormatCombo = new JComboBox<>(EncodingFormat.values());
    private DesProcessResult lastProcessResult;

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
        contentPanel.add(buildProcessPanel(), PROCESS_CARD);

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

        workspaceButton.addActionListener(event -> showCard(WORKSPACE_CARD, "Mã hóa/Giải mã"));
        keyInfoButton.addActionListener(event -> {
            refreshKeyInfo();
            showCard(KEY_INFO_CARD, "Thông tin khóa");
        });
        processButton.addActionListener(event -> {
            refreshProcessInfo();
            showCard(PROCESS_CARD, "DES Process");
        });
        setActiveNavigation(WORKSPACE_CARD);

        navigation.add(title);
        navigation.add(subtitle);
        navigation.add(workspaceButton);
        navigation.add(Box.createVerticalStrut(8));
        navigation.add(keyInfoButton);
        navigation.add(Box.createVerticalStrut(8));
        navigation.add(processButton);
        navigation.add(Box.createVerticalGlue());
        return navigation;
    }

    private JButton navigationButton(String text) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setBackground(PANEL_BACKGROUND);
        button.setForeground(Color.DARK_GRAY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        return button;
    }

    private JPanel buildWorkspacePanel() {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setBackground(PAGE_BACKGROUND);
        workspace.setBorder(BorderFactory.createEmptyBorder(24, 24, 20, 24));

        addWorkspacePanel(workspace, buildSecretKeyPanel(), 0, 0, 3, 1, 0, new Insets(0, 0, 18, 0));
        addWorkspacePanel(workspace, buildInputPanel(), 0, 1, 1, 2, 1, new Insets(0, 0, 0, 16));
        addWorkspacePanel(workspace, buildActionPanel(), 1, 1, 1, 1, 0.35, new Insets(0, 0, 16, 16));
        addWorkspacePanel(workspace, buildOutputPanel(), 2, 1, 1, 1, 0.55, new Insets(0, 0, 16, 0));
        addWorkspacePanel(workspace, buildOutputPreviewPanel(), 1, 2, 2, 1, 0.45, new Insets(0, 0, 0, 0));
        return workspace;
    }

    private JPanel buildSecretKeyPanel() {
        JPanel panel = dashboardCard("Secret Key", "DES key must be exactly 16 Hex characters.");
        JPanel content = transparentPanel(new BorderLayout(12, 10));

        keyField.setToolTipText("16 ký tự Hex, tương đương 8 byte");
        keyField.setFont(Font.decode(Font.MONOSPACED));
        keyStatusLabel.setForeground(TEXT_MUTED);

        JPanel keyInputPanel = new JPanel(new BorderLayout(0, 6));
        keyInputPanel.setOpaque(false);
        keyInputPanel.add(keyField, BorderLayout.CENTER);
        keyInputPanel.add(keyStatusLabel, BorderLayout.SOUTH);

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

        content.add(keyInputPanel, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.EAST);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildInputPanel() {
        JPanel panel = dashboardCard("Input Data", "Plaintext, Hex, or Base64 data for DES.");
        JPanel content = transparentPanel(new BorderLayout(10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setOpaque(false);
        topBar.add(new JLabel("Định dạng"));
        topBar.add(inputFormatCombo);

        JButton loadButton = neutralButton("Tải file");
        loadButton.addActionListener(event -> loadInputFile());
        topBar.add(loadButton);

        content.add(topBar, BorderLayout.NORTH);
        content.add(buildTextAreaWithCounter(inputArea, inputCounterLabel), BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTextAreaWithCounter(JTextArea textArea, JLabel counterLabel) {
        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.setOpaque(false);
        counterLabel.setForeground(TEXT_MUTED);
        content.add(new JScrollPane(textArea), BorderLayout.CENTER);
        content.add(counterLabel, BorderLayout.SOUTH);
        return content;
    }

    private JPanel buildActionPanel() {
        JPanel panel = dashboardCard("Action", "Run DES with the selected formats.");
        panel.setPreferredSize(new Dimension(170, 0));

        JButton encryptButton = primaryButton("Mã hóa DES");
        JButton decryptButton = outlineButton("Giải mã DES");
        encryptButton.addActionListener(event -> encrypt());
        decryptButton.addActionListener(event -> decrypt());

        JPanel actions = transparentPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        encryptButton.setAlignmentX(CENTER_ALIGNMENT);
        decryptButton.setAlignmentX(CENTER_ALIGNMENT);
        actions.add(encryptButton);
        actions.add(Box.createVerticalStrut(12));
        actions.add(decryptButton);

        panel.add(actions, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildOutputPanel() {
        JPanel panel = dashboardCard("Output Data", "Ciphertext or decrypted bytes encoded as selected.");
        JPanel content = transparentPanel(new BorderLayout(10, 10));

        outputArea.setEditable(false);
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setOpaque(false);
        topBar.add(new JLabel("Định dạng"));
        topBar.add(outputFormatCombo);

        JButton copyButton = neutralButton("Sao chép");
        JButton saveButton = neutralButton("Lưu file");
        JButton useOutputAsInputButton = neutralButton("Dùng output làm input");
        copyButton.addActionListener(event -> copyOutput());
        saveButton.addActionListener(event -> saveOutputFile());
        useOutputAsInputButton.addActionListener(event -> useOutputAsInput());
        topBar.add(copyButton);
        topBar.add(saveButton);
        topBar.add(useOutputAsInputButton);

        content.add(topBar, BorderLayout.NORTH);
        content.add(buildTextAreaWithCounter(outputArea, outputCounterLabel), BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildOutputPreviewPanel() {
        JPanel panel = dashboardCard("Output Text Preview", "Readable plaintext appears here after successful decrypt.");
        outputPreviewArea.setEditable(false);
        panel.add(new JScrollPane(outputPreviewArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildKeyInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(PAGE_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));

        JPanel card = dashboardCard("Thông tin khóa", "DES key schedule and round-key details.");
        keyInfoArea.setEditable(false);
        keyInfoArea.setFont(Font.decode(Font.MONOSPACED));
        keyInfoArea.setText(buildKeyInfoText());
        card.add(new JScrollPane(keyInfoArea), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildProcessPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(PAGE_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));

        JPanel card = dashboardCard("DES Process", "Latest successful DES encrypt or decrypt process.");
        processArea.setEditable(false);
        processArea.setFont(Font.decode(Font.MONOSPACED));
        processArea.setText(buildProcessText());
        card.add(new JScrollPane(processArea), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel dashboardCard(String title, String description) {
        JPanel panel = new RoundedCardPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.add(cardHeader(title, description), BorderLayout.NORTH);
        return panel;
    }

    private JPanel cardHeader(String title, String description) {
        JPanel header = transparentPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(Color.DARK_GRAY);
        header.add(titleLabel);

        if (description != null && !description.isBlank()) {
            JLabel descriptionLabel = new JLabel(description);
            descriptionLabel.setForeground(TEXT_MUTED);
            descriptionLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
            header.add(descriptionLabel);
        }

        return header;
    }

    private JPanel transparentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private JPanel transparentPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private static class RoundedCardPanel extends JPanel {
        private static final int ARC = 18;

        RoundedCardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(PANEL_BACKGROUND);
            graphics2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            graphics2D.setColor(BORDER_COLOR);
            graphics2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(140, 42));
        button.setMaximumSize(new Dimension(140, 42));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return button;
    }

    private JButton outlineButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(140, 42));
        button.setMaximumSize(new Dimension(140, 42));
        button.setBackground(PANEL_BACKGROUND);
        button.setForeground(PRIMARY_DARK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR));
        return button;
    }

    private JButton neutralButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(248, 250, 252));
        button.setForeground(Color.DARK_GRAY);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        return button;
    }

    private void addWorkspacePanel(JPanel target, JPanel panel, int x, int y, int width, int height,
                                   double weightY, Insets insets) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.gridheight = height;
        constraints.weightx = x == 1 && width == 1 ? 0.2 : 1;
        constraints.weighty = weightY;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = insets;
        target.add(panel, constraints);
    }

    private JPanel buildStatusBar() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 20, 12, 20));
        JPanel statusBar = new RoundedCardPanel();
        statusBar.setLayout(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        statusLabel.setForeground(Color.DARK_GRAY);
        statusBar.add(statusLabel, BorderLayout.WEST);
        wrapper.add(statusBar, BorderLayout.CENTER);
        return wrapper;
    }

    private void configureInputs() {
        ((AbstractDocument) keyField.getDocument()).setDocumentFilter(new HexKeyDocumentFilter());
        outputFormatCombo.setSelectedItem(EncodingFormat.BASE64);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputPreviewArea.setLineWrap(true);
        outputPreviewArea.setWrapStyleWord(true);
        keyInfoArea.setLineWrap(false);
        inputCounterLabel.setText(buildCounterText(inputArea.getText(), selectedInputFormat()));
        outputCounterLabel.setText(buildCounterText(outputArea.getText(), selectedOutputFormat()));
        inputArea.getDocument().addDocumentListener(counterListener(this::refreshInputCounter));
        outputArea.getDocument().addDocumentListener(counterListener(this::refreshOutputCounter));
        inputFormatCombo.addActionListener(event -> refreshInputCounter());
        outputFormatCombo.addActionListener(event -> refreshOutputCounter());
        refreshKeyStatus();
        keyField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshKeyInfo();
                refreshKeyStatus();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshKeyInfo();
                refreshKeyStatus();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshKeyInfo();
                refreshKeyStatus();
            }
        });
    }

    private static class HexKeyDocumentFilter extends DocumentFilter {
        private static final int MAX_KEY_LENGTH = 16;

        @Override
        public void insertString(FilterBypass bypass, int offset, String string, AttributeSet attributes)
                throws BadLocationException {
            replace(bypass, offset, 0, string, attributes);
        }

        @Override
        public void replace(FilterBypass bypass, int offset, int length, String text, AttributeSet attributes)
                throws BadLocationException {
            String filteredText = filterHex(text);
            int availableLength = MAX_KEY_LENGTH - (bypass.getDocument().getLength() - length);
            if (filteredText.isEmpty() || availableLength <= 0) {
                return;
            }
            if (filteredText.length() > availableLength) {
                filteredText = filteredText.substring(0, availableLength);
            }
            super.replace(bypass, offset, length, filteredText, attributes);
        }

        private String filterHex(String text) {
            if (text == null || text.isEmpty()) {
                return "";
            }
            StringBuilder builder = new StringBuilder(text.length());
            for (int index = 0; index < text.length(); index++) {
                char character = Character.toUpperCase(text.charAt(index));
                if (Character.digit(character, 16) != -1) {
                    builder.append(character);
                }
            }
            return builder.toString();
        }
    }

    private DocumentListener counterListener(Runnable refreshAction) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshAction.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshAction.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshAction.run();
            }
        };
    }

    private void refreshInputCounter() {
        inputCounterLabel.setText(buildCounterText(inputArea.getText(), selectedInputFormat()));
    }

    private void refreshOutputCounter() {
        outputCounterLabel.setText(buildCounterText(outputArea.getText(), selectedOutputFormat()));
    }

    private void refreshKeyStatus() {
        String key = normalizedKey();
        if (key.isEmpty()) {
            keyStatusLabel.setForeground(TEXT_MUTED);
            keyStatusLabel.setText("Chưa nhập khóa");
        } else if (key.length() < 16) {
            keyStatusLabel.setForeground(TEXT_MUTED);
            keyStatusLabel.setText("Thiếu " + (16 - key.length()) + " ký tự");
        } else if (key.length() == 16 && isHex(key)) {
            keyStatusLabel.setForeground(SUCCESS_COLOR);
            keyStatusLabel.setText("Khóa hợp lệ");
        } else {
            keyStatusLabel.setForeground(ERROR_COLOR);
            keyStatusLabel.setText("Khóa không hợp lệ");
        }
    }

    private String buildCounterText(String value, InputFormat format) {
        return buildCounterText(value, format == InputFormat.HEX);
    }

    private String buildCounterText(String value, EncodingFormat format) {
        return buildCounterText(value, format == EncodingFormat.HEX);
    }

    private String buildCounterText(String value, boolean hexFormat) {
        String text = value == null ? "" : value;
        String counterText = "Ký tự: " + text.length()
                + " | Byte UTF-8: " + text.getBytes(StandardCharsets.UTF_8).length;
        if (!hexFormat) {
            return counterText;
        }

        String hex = removeWhitespace(text);
        if (hex.isEmpty()) {
            return counterText + " | Hex: 0 ký tự = 0 byte";
        }
        if ((hex.length() % 2) != 0 || !isHex(hex)) {
            return counterText + " | Hex không hợp lệ";
        }
        return counterText + " | Hex: " + hex.length() + " ký tự = " + (hex.length() / 2) + " byte";
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
            DesProcessResult processResult = desService.encryptWithProcess(
                    inputArea.getText(),
                    selectedInputFormat(),
                    keyField.getText(),
                    selectedOutputFormat());
            lastProcessResult = processResult;
            refreshProcessInfo();
            String result = processResult.outputText();
            outputArea.setText(result);
            outputPreviewArea.setText("Ciphertext là dữ liệu đã mã hóa, không nên đọc trực tiếp như văn bản.");
            showSuccess("Mã hóa thành công. Output Text Preview đã cập nhật.");
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
            DesProcessResult processResult = desService.decryptWithProcess(
                    inputArea.getText(),
                    selectedInputFormat(),
                    keyField.getText(),
                    selectedOutputFormat());
            lastProcessResult = processResult;
            refreshProcessInfo();
            String result = processResult.outputText();
            byte[] plainBytes = desService.decryptToPlainBytes(
                    inputArea.getText(),
                    selectedInputFormat(),
                    keyField.getText());
            outputArea.setText(result);
            outputPreviewArea.setText(buildPlainTextPreview(plainBytes));
            showSuccess("Giải mã thành công. Output Text Preview đã cập nhật.");
        } catch (RuntimeException exception) {
            showError("Giải mã thất bại. Vui lòng kiểm tra khóa hoặc dữ liệu đầu vào.", exception);
        }
    }

    private String buildPlainTextPreview(byte[] plainBytes) {
        try {
            String text = StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(plainBytes))
                    .toString();
            if (!isSafePreviewText(text)) {
                return "Plaintext đã giải mã thành công nhưng chứa ký tự không phù hợp để hiển thị dạng văn bản.";
            }
            return text;
        } catch (CharacterCodingException exception) {
            return "Plaintext đã giải mã thành công nhưng không thể hiển thị an toàn dạng văn bản UTF-8.";
        }
    }

    private boolean isSafePreviewText(String text) {
        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            if (Character.isISOControl(codePoint)
                    && codePoint != '\n'
                    && codePoint != '\r'
                    && codePoint != '\t') {
                return false;
            }
            offset += Character.charCount(codePoint);
        }
        return true;
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

    private void useOutputAsInput() {
        String output = outputArea.getText();
        if (output == null || output.isBlank()) {
            showStatus("Chưa có dữ liệu output để chuyển sang input.");
            return;
        }

        inputArea.setText(output);
        inputFormatCombo.setSelectedItem(inputFormatFor(selectedOutputFormat()));
        outputPreviewArea.setText("");
        refreshInputCounter();
        refreshOutputCounter();
        showStatus("Đã chuyển output sang input.");
    }

    private InputFormat inputFormatFor(EncodingFormat outputFormat) {
        return switch (outputFormat) {
            case BASE64 -> InputFormat.BASE64;
            case HEX -> InputFormat.HEX;
        };
    }

    private void clearData() {
        inputArea.setText("");
        outputArea.setText("");
        outputPreviewArea.setText("");
        showStatus("Đã xóa dữ liệu vào, kết quả và Output Text Preview.");
    }

    private void refreshKeyInfo() {
        keyInfoArea.setText(buildKeyInfoText());
        keyInfoArea.setCaretPosition(0);
    }

    private void refreshProcessInfo() {
        processArea.setText(buildProcessText());
        processArea.setCaretPosition(0);
    }

    private String buildProcessText() {
        if (lastProcessResult == null) {
            return "Chưa có dữ liệu xử lý. Hãy mã hóa hoặc giải mã DES trước.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Chế độ: ").append(lastProcessResult.mode()).append(System.lineSeparator());
        builder.append("Input format: ").append(lastProcessResult.inputFormat()).append(System.lineSeparator());
        builder.append("Output format: ").append(lastProcessResult.outputFormat()).append(System.lineSeparator());
        builder.append("Số block DES: ").append(lastProcessResult.blockCount()).append(System.lineSeparator());
        builder.append("Kích thước trước padding: ")
                .append(lastProcessResult.beforePaddingBytes()).append(" byte").append(System.lineSeparator());
        builder.append("Kích thước sau padding: ")
                .append(lastProcessResult.afterPaddingBytes()).append(" byte").append(System.lineSeparator());
        builder.append("Output ").append(lastProcessResult.outputFormat()).append(":")
                .append(System.lineSeparator()).append(lastProcessResult.outputText()).append(System.lineSeparator());

        builder.append(System.lineSeparator());
        builder.append(String.format("%-6s | %-18s | %-18s%n", "Block", "Input Hex", "Output Hex"));
        builder.append("-------+--------------------+-------------------").append(System.lineSeparator());
        for (DesBlockTrace block : lastProcessResult.blocks()) {
            builder.append(String.format("%-6d | %-18s | %-18s%n",
                    block.blockNumber(),
                    block.inputHex(),
                    block.outputHex()));
        }
        return builder.toString();
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
        setActiveNavigation(cardName);
        showStatus(label);
    }

    private void setActiveNavigation(String cardName) {
        styleNavigationButton(workspaceButton, WORKSPACE_CARD.equals(cardName));
        styleNavigationButton(keyInfoButton, KEY_INFO_CARD.equals(cardName));
        styleNavigationButton(processButton, PROCESS_CARD.equals(cardName));
    }

    private void styleNavigationButton(JButton button, boolean active) {
        button.setBackground(active ? ACTIVE_NAV_BACKGROUND : PANEL_BACKGROUND);
        button.setForeground(active ? PRIMARY_DARK : Color.DARK_GRAY);
        button.setFont(button.getFont().deriveFont(active ? Font.BOLD : Font.PLAIN));
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
