package ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class DesFrame extends JFrame {
    private static final String WORKSPACE_CARD = "workspace";
    private static final String KEY_INFO_CARD = "keyInfo";

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);
    private final JLabel statusLabel = new JLabel("Ready");

    public DesFrame() {
        super("DES Studio");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 640));
        setLocationByPlatform(true);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        pack();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));

        JLabel title = new JLabel("DES Studio");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel subtitle = new JLabel("Manual DES learning workspace");

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(subtitle);
        header.add(text, BorderLayout.WEST);

        return header;
    }

    private JSplitPane buildBody() {
        contentPanel.add(buildWorkspacePanel(), WORKSPACE_CARD);
        contentPanel.add(buildKeyInfoPanel(), KEY_INFO_CARD);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildNavigationPanel(), contentPanel);
        splitPane.setResizeWeight(0);
        splitPane.setDividerLocation(180);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        return splitPane;
    }

    private JPanel buildNavigationPanel() {
        JPanel navigation = new JPanel();
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));
        navigation.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        navigation.setPreferredSize(new Dimension(180, 0));

        JButton workspaceButton = new JButton("Workspace");
        JButton keyInfoButton = new JButton("Key Info");
        workspaceButton.setHorizontalAlignment(SwingConstants.LEFT);
        keyInfoButton.setHorizontalAlignment(SwingConstants.LEFT);

        workspaceButton.addActionListener(event -> showCard(WORKSPACE_CARD, "Workspace"));
        keyInfoButton.addActionListener(event -> showCard(KEY_INFO_CARD, "Key Info"));

        navigation.add(workspaceButton);
        navigation.add(Box.createVerticalStrut(8));
        navigation.add(keyInfoButton);
        navigation.add(Box.createVerticalGlue());
        return navigation;
    }

    private JPanel buildWorkspacePanel() {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        JTextArea inputArea = new JTextArea(8, 32);
        JTextArea outputArea = new JTextArea(8, 32);
        outputArea.setEditable(false);

        JTextField keyField = new JTextField();
        JButton generateKeyButton = new JButton("Generate Key");
        JButton saveKeyButton = new JButton("Save Key");
        JButton encryptButton = new JButton("Encrypt");
        JButton decryptButton = new JButton("Decrypt");
        JButton loadFileButton = new JButton("Load File");
        JButton saveFileButton = new JButton("Save File");

        JRadioButton base64Option = new JRadioButton("Base64", true);
        JRadioButton hexOption = new JRadioButton("Hex");
        ButtonGroup encodingGroup = new ButtonGroup();
        encodingGroup.add(base64Option);
        encodingGroup.add(hexOption);

        addSection(workspace, "Input", new JScrollPane(inputArea), 0);
        addSection(workspace, "DES Key", buildKeyPanel(keyField, generateKeyButton, saveKeyButton), 1);
        addSection(workspace, "Cipher Text Format", buildEncodingPanel(base64Option, hexOption), 2);
        addSection(workspace, "Actions", buildActionPanel(encryptButton, decryptButton, loadFileButton, saveFileButton), 3);
        addSection(workspace, "Result", new JScrollPane(outputArea), 4);

        return workspace;
    }

    private JPanel buildKeyInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTextArea keyInfo = new JTextArea();
        keyInfo.setEditable(false);
        keyInfo.setText("""
                DES key information will be shown here.

                Planned fields:
                - Raw key bytes
                - Parity information
                - Generated round keys
                """);

        panel.add(new JScrollPane(keyInfo), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildKeyPanel(JTextField keyField, JButton generateKeyButton, JButton saveKeyButton) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.add(keyField, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(generateKeyButton);
        buttons.add(saveKeyButton);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildEncodingPanel(JRadioButton base64Option, JRadioButton hexOption) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.add(base64Option);
        panel.add(hexOption);
        return panel;
    }

    private JPanel buildActionPanel(JButton encryptButton, JButton decryptButton, JButton loadFileButton, JButton saveFileButton) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.add(encryptButton);
        panel.add(decryptButton);
        panel.add(loadFileButton);
        panel.add(saveFileButton);
        return panel;
    }

    private void addSection(JPanel target, String label, java.awt.Component component, int row) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.NORTHWEST;
        labelConstraints.insets = new Insets(0, 0, 12, 12);

        JLabel sectionLabel = new JLabel(label);
        sectionLabel.setFont(sectionLabel.getFont().deriveFont(Font.BOLD));
        target.add(sectionLabel, labelConstraints);

        GridBagConstraints componentConstraints = new GridBagConstraints();
        componentConstraints.gridx = 1;
        componentConstraints.gridy = row;
        componentConstraints.weightx = 1;
        componentConstraints.weighty = label.equals("Input") || label.equals("Result") ? 1 : 0;
        componentConstraints.fill = GridBagConstraints.BOTH;
        componentConstraints.insets = new Insets(0, 0, 12, 0);
        target.add(component, componentConstraints);
    }

    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        statusBar.add(statusLabel, BorderLayout.WEST);
        return statusBar;
    }

    private void showCard(String cardName, String label) {
        contentLayout.show(contentPanel, cardName);
        statusLabel.setText(label);
    }
}
