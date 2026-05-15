package ui;

import des.DesService;
import des.EncodingFormat;
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
    private static final Color PAGE_BACKGROUND = new Color(246, 248, 251);
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color SIDEBAR_BACKGROUND = new Color(232, 237, 244);
    private static final Color ERROR_COLOR = new Color(170, 35, 35);
    private static final Color SUCCESS_COLOR = new Color(25, 105, 65);

    private final DesService desService;
    private final FileService fileService;
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);
    private final JLabel statusLabel = new JLabel("Ready");
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
        setMinimumSize(new Dimension(1080, 720));
        setLocationByPlatform(true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PAGE_BACKGROUND);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        configureTextAreas();
        pack();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PAGE_BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));

        JLabel title = new JLabel("DES Studio");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        JLabel subtitle = new JLabel("Manual DES workspace");
        subtitle.setForeground(new Color(80, 88, 100));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(subtitle);
        header.add(text, BorderLayout.WEST);

        return header;
    }

    private JSplitPane buildBody() {
        contentPanel.setBackground(PAGE_BACKGROUND);
        contentPanel.add(buildWorkspacePanel(), WORKSPACE_CARD);
        contentPanel.add(buildKeyInfoPanel(), KEY_INFO_CARD);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildNavigationPanel(), contentPanel);
        splitPane.setResizeWeight(0);
        splitPane.setDividerLocation(190);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        return splitPane;
    }

    private JPanel buildNavigationPanel() {
        JPanel navigation = new JPanel();
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));
        navigation.setBackground(SIDEBAR_BACKGROUND);
        navigation.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
        navigation.setPreferredSize(new Dimension(190, 0));

        JButton workspaceButton = navigationButton("Workspace");
        JButton keyInfoButton = navigationButton("Key Info");

        workspaceButton.addActionListener(event -> showCard(WORKSPACE_CARD, "Workspace"));
        keyInfoButton.addActionListener(event -> {
            refreshKeyInfo();
            showCard(KEY_INFO_CARD, "Key Info");
        });

        navigation.add(workspaceButton);
        navigation.add(Box.createVerticalStrut(8));
        navigation.add(keyInfoButton);
        navigation.add(Box.createVerticalGlue());
        return navigation;
    }

    private JButton navigationButton(String text) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return button;
    }

    private JPanel buildWorkspacePanel() {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setBackground(PAGE_BACKGROUND);
        workspace.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        addWorkspacePanel(workspace, buildSecretKeyPanel(), 0, 0, 2, 0);
        addWorkspacePanel(workspace, buildInputPanel(), 0, 1, 1, 1);
        addWorkspacePanel(workspace, buildOutputPanel(), 1, 1, 1, 1);
        addWorkspacePanel(workspace, buildActionPanel(), 0, 2, 2, 0);
        return workspace;
    }

    private JPanel buildSecretKeyPanel() {
        JPanel panel = cardPanel("Secret Key");
        panel.setLayout(new BorderLayout(8, 8));

        keyField.setToolTipText("16 hex characters, 8 bytes");
        keyField.setFont(Font.decode(Font.MONOSPACED));
        JButton generateButton = new JButton("Generate Random");
        JButton loadKeyButton = new JButton("Load Key");
        JButton saveKeyButton = new JButton("Save Key");
        JButton clearButton = new JButton("Clear Data");

        generateButton.addActionListener(event -> safelyRun("Generate random key", () -> {
            keyField.setText(desService.generateRandomKeyHex());
            refreshKeyInfo();
            showSuccess("Random DES key generated.");
        }));
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
        JPanel panel = cardPanel("Input");
        panel.setLayout(new BorderLayout(8, 8));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setOpaque(false);
        topBar.add(new JLabel("Format"));
        topBar.add(inputFormatCombo);

        JButton loadButton = new JButton("Load File");
        loadButton.addActionListener(event -> loadInputFile());
        topBar.add(loadButton);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildOutputPanel() {
        JPanel panel = cardPanel("Output");
        panel.setLayout(new BorderLayout(8, 8));

        outputArea.setEditable(false);
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setOpaque(false);
        topBar.add(new JLabel("Format"));
        topBar.add(outputFormatCombo);

        JButton copyButton = new JButton("Copy");
        JButton saveButton = new JButton("Save File");
        copyButton.addActionListener(event -> safelyRun("Copy output", this::copyOutput));
        saveButton.addActionListener(event -> saveOutputFile());
        topBar.add(copyButton);
        topBar.add(saveButton);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);

        JButton encryptButton = new JButton("Encrypt");
        JButton decryptButton = new JButton("Decrypt");
        encryptButton.addActionListener(event -> encrypt());
        decryptButton.addActionListener(event -> decrypt());

        panel.add(encryptButton);
        panel.add(decryptButton);
        return panel;
    }

    private JPanel buildKeyInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(PAGE_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        JPanel card = cardPanel("Key Info");
        card.setLayout(new BorderLayout());
        keyInfoArea.setEditable(false);
        keyInfoArea.setFont(Font.decode(Font.MONOSPACED));
        keyInfoArea.setText("Generate or enter a valid key to inspect round keys.");
        card.add(new JScrollPane(keyInfoArea), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel cardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        return panel;
    }

    private void addWorkspacePanel(JPanel target, JPanel panel, int x, int y, int width, double weightY) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.weightx = 1;
        constraints.weighty = weightY;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 12, x == 0 && width == 1 ? 12 : 0);
        target.add(panel, constraints);
    }

    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(PAGE_BACKGROUND);
        statusBar.setBorder(BorderFactory.createEmptyBorder(8, 16, 10, 16));
        statusBar.add(statusLabel, BorderLayout.WEST);
        return statusBar;
    }

    private void configureTextAreas() {
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

    private void encrypt() {
        safelyRun("Encrypt", () -> {
            String result = desService.encrypt(
                    inputArea.getText(),
                    (InputFormat) inputFormatCombo.getSelectedItem(),
                    keyField.getText(),
                    (EncodingFormat) outputFormatCombo.getSelectedItem());
            outputArea.setText(result);
            refreshKeyInfo();
            showSuccess("Encryption complete.");
        });
    }

    private void decrypt() {
        safelyRun("Decrypt", () -> {
            String result = desService.decrypt(
                    inputArea.getText(),
                    (InputFormat) inputFormatCombo.getSelectedItem(),
                    keyField.getText(),
                    (EncodingFormat) outputFormatCombo.getSelectedItem());
            outputArea.setText(result);
            refreshKeyInfo();
            showSuccess("Decryption complete.");
        });
    }

    private void loadInputFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt", "text", "csv", "log"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = chooser.getSelectedFile();
        safelyRun("Load file", () -> {
            try {
                inputArea.setText(fileService.readText(selectedFile.toPath()));
                showSuccess("Loaded " + selectedFile.getName() + ".");
            } catch (IOException exception) {
                throw new IllegalArgumentException("Could not load file: " + exception.getMessage(), exception);
            }
        });
    }

    private void saveOutputFile() {
        if (outputArea.getText().isBlank()) {
            showError("Output is empty.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt", "text"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = chooser.getSelectedFile();
        safelyRun("Save file", () -> {
            try {
                fileService.writeText(selectedFile.toPath(), outputArea.getText());
                showSuccess("Saved " + selectedFile.getName() + ".");
            } catch (IOException exception) {
                throw new IllegalArgumentException("Could not save file: " + exception.getMessage(), exception);
            }
        });
    }

    private void loadKeyFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Key files", "key", "txt"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = chooser.getSelectedFile();
        safelyRun("Load key", () -> {
            try {
                String key = fileService.readText(selectedFile.toPath()).replaceAll("\\s+", "").toUpperCase();
                desService.describeKey(key);
                keyField.setText(key);
                refreshKeyInfo();
                showSuccess("Loaded key from " + selectedFile.getName() + ".");
            } catch (IOException exception) {
                throw new IllegalArgumentException("Could not load key: " + exception.getMessage(), exception);
            }
        });
    }

    private void saveKeyFile() {
        String key = keyField.getText();
        safelyRun("Save key", () -> {
            desService.describeKey(key);
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Key files", "key", "txt"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File selectedFile = chooser.getSelectedFile();
            try {
                fileService.writeText(selectedFile.toPath(), key.replaceAll("\\s+", "").toUpperCase());
                showSuccess("Saved key to " + selectedFile.getName() + ".");
            } catch (IOException exception) {
                throw new IllegalArgumentException("Could not save key: " + exception.getMessage(), exception);
            }
        });
    }

    private void copyOutput() {
        if (outputArea.getText().isBlank()) {
            showError("Output is empty.");
            return;
        }
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(outputArea.getText()), null);
        showSuccess("Output copied.");
    }

    private void clearData() {
        inputArea.setText("");
        outputArea.setText("");
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setText("Data cleared.");
    }

    private void refreshKeyInfo() {
        String key = keyField.getText();
        if (key == null || key.isBlank()) {
            keyInfoArea.setText("Generate or enter a valid key to inspect round keys.");
            return;
        }
        try {
            keyInfoArea.setText(desService.describeKey(key));
            keyInfoArea.setCaretPosition(0);
        } catch (IllegalArgumentException exception) {
            keyInfoArea.setText("Invalid key: " + exception.getMessage());
        }
    }

    private void showCard(String cardName, String label) {
        contentLayout.show(contentPanel, cardName);
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setText(label);
    }

    private void safelyRun(String actionName, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            showError(actionName + " failed: " + exception.getMessage());
        }
    }

    private void showSuccess(String message) {
        statusLabel.setForeground(SUCCESS_COLOR);
        statusLabel.setText(message);
    }

    private void showError(String message) {
        statusLabel.setForeground(ERROR_COLOR);
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "DES Studio", JOptionPane.ERROR_MESSAGE);
    }
}
