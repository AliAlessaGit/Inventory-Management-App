package frontEnd.account;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

final class PurchaseDialogSupport {

    private PurchaseDialogSupport() {}

    static void styleTextField(JTextField field) {
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setFont(new Font("Tahoma", Font.PLAIN, 17));
    }

    static JLabel label(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));
        return label;
    }

    static JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        return panel;
    }

    static void addRow(JPanel panel, int row, String labelText, JComponent field) {
        GridBagConstraints gl = new GridBagConstraints();
        gl.gridy = row;
        gl.gridx = 0;
        gl.weightx = .30;
        gl.fill = GridBagConstraints.HORIZONTAL;
        gl.insets = new Insets(7, 7, 7, 7);
        panel.add(label(labelText), gl);

        GridBagConstraints gf = new GridBagConstraints();
        gf.gridy = row;
        gf.gridx = 1;
        gf.weightx = .70;
        gf.fill = GridBagConstraints.HORIZONTAL;
        gf.insets = new Insets(7, 7, 7, 7);
        panel.add(field, gf);
    }

    static double number(JTextField field, String name) {
        String text = field.getText().trim().replace(',', '.');
        if (text.isEmpty()) {
            throw new IllegalArgumentException(name + " مطلوب");
        }
        try {
            double value = Double.parseDouble(text);
            if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) {
                throw new IllegalArgumentException(name + " غير صالح");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " غير صالح");
        }
    }

    static void installNavigation(
            JDialog dialog,
            List<? extends JComponent> fields,
            JButton okButton,
            JButton cancelButton) {

        InputMap rootInput = dialog.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap rootActions = dialog.getRootPane().getActionMap();

        rootInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelDialog");
        rootActions.put("cancelDialog", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                cancelButton.doClick();
            }
        });

        for (int i = 0; i < fields.size(); i++) {
            JComponent current = fields.get(i);
            final int index = i;

            InputMap input = current.getInputMap(JComponent.WHEN_FOCUSED);
            ActionMap actions = current.getActionMap();

            input.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "nextField");
            actions.put("nextField", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (index + 1 < fields.size()) {
                        fields.get(index + 1).requestFocusInWindow();
                    } else {
                        okButton.requestFocusInWindow();
                    }
                }
            });

            input.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "previousField");
            actions.put("previousField", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (index > 0) {
                        fields.get(index - 1).requestFocusInWindow();
                    }
                }
            });

            if (current instanceof JTextField textField) {
                textField.addActionListener(e -> {
                    if (index + 1 < fields.size()) {
                        fields.get(index + 1).requestFocusInWindow();
                    } else {
                        okButton.requestFocusInWindow();
                    }
                });
            }
        }

        installButtonNavigation(okButton, cancelButton);
        installButtonNavigation(cancelButton, okButton);
    }

    private static void installButtonNavigation(
            JButton current,
            JButton other) {

        InputMap input = current.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actions = current.getActionMap();

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "otherButton");
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "otherButton");
        actions.put("otherButton", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                other.requestFocusInWindow();
            }
        });

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "clickButton");
        actions.put("clickButton", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                current.doClick();
            }
        });
    }

    static void installComboNavigation(
            JComponent component,
            JComponent previous,
            JComponent next) {

        InputMap input = component.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actions = component.getActionMap();

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "previousComponent");
        actions.put("previousComponent", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                previous.requestFocusInWindow();
            }
        });

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "nextComponent");
        actions.put("nextComponent", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                next.requestFocusInWindow();
            }
        });
    }

    static void prepareDialog(JDialog dialog, int width, int height) {
        dialog.setMinimumSize(new Dimension(width, height));
        dialog.pack();
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.getRootPane().setDefaultButton(null);
    }
}
