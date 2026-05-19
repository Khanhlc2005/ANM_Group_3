package ui.components;

import ui.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;

public final class UiFactory {
    private UiFactory() {
    }

    public static JPanel dashboardCard(String title, String description) {
        JPanel panel = new RoundedCardPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.add(cardHeader(title, description), BorderLayout.NORTH);
        return panel;
    }

    public static JPanel transparentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    public static JPanel transparentPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(140, 42));
        button.setMaximumSize(new Dimension(140, 42));
        button.setBackground(AppTheme.PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return button;
    }

    public static JButton outlineButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(140, 42));
        button.setMaximumSize(new Dimension(140, 42));
        button.setBackground(AppTheme.PANEL_BACKGROUND);
        button.setForeground(AppTheme.PRIMARY_DARK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(AppTheme.PRIMARY_COLOR));
        return button;
    }

    public static JButton neutralButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(248, 250, 252));
        button.setForeground(Color.DARK_GRAY);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        return button;
    }

    private static JPanel cardHeader(String title, String description) {
        JPanel header = transparentPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(Color.DARK_GRAY);
        header.add(titleLabel);

        if (description != null && !description.isBlank()) {
            JLabel descriptionLabel = new JLabel(description);
            descriptionLabel.setForeground(AppTheme.TEXT_MUTED);
            descriptionLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
            header.add(descriptionLabel);
        }

        return header;
    }
}
