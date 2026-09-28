package frontEnd.account;

import backEnd.account.AccountEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class EntryDialog extends JDialog {

    private final JTextField materialField = new JTextField();
    private final JTextField quantity1Field = new JTextField("0");
    private final JTextField unitField = new JTextField("قطعة");
    private final JTextField priceField = new JTextField("0");
    private final JTextField totalField = new JTextField("0.00");
    private final JTextField quantity2Field = new JTextField("0");

    private boolean ok;

    public EntryDialog(Window parent) {
        super(parent, "إضافة سطر إلى المبيع", ModalityType.APPLICATION_MODAL);
        setSize(720, 480);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        totalField.setEditable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 25, 10, 25));

        addRow(form, 0, "المادة:", materialField, "اسم المادة");
        addRow(form, 1, "الكمية 1:", quantity1Field, "الكمية الأساسية");
        addRow(form, 2, "الوحدة:", unitField, "الوحدة");
        addRow(form, 3, "السعر:", priceField, "سعر الوحدة");
        addRow(form, 4, "الإجمالي:", totalField, "يحسب تلقائياً");
        addRow(form, 5, "الكمية 2:", quantity2Field, "الكمية الثانية");

        javax.swing.event.DocumentListener listener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculate(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculate(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculate(); }
        };
        quantity1Field.getDocument().addDocumentListener(listener);
        priceField.getDocument().addDocumentListener(listener);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttons.setBackground(Color.WHITE);
        JButton okButton = new JButton("موافق");
        JButton cancelButton = new JButton("إلغاء");
        buttons.add(okButton);
        buttons.add(new JLabel("←"));
        buttons.add(cancelButton);
        okButton.addActionListener(e -> accept());
        cancelButton.addActionListener(e -> dispose());

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        installNavigation(okButton, cancelButton);
    }

    private void addRow(JPanel panel, int row, String label, JComponent field, String hint) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row;
        g.insets = new Insets(7, 7, 7, 7);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = .2;
        g.gridx = 0;
        panel.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = .55;
        panel.add(field, g);
        g.gridx = 2;
        g.weightx = .25;
        JLabel explanation = new JLabel(hint);
        explanation.setForeground(new Color(100, 100, 100));
        panel.add(explanation, g);
    }

    private void calculate() {
        try {
            double q = number(quantity1Field);
            double p = number(priceField);
            totalField.setText(String.format("%.2f", q * p));
        } catch (Exception ignored) {
            totalField.setText("0.00");
        }
    }

    private void accept() {
        try {
            if (materialField.getText().trim().isEmpty())
                throw new IllegalArgumentException("المادة مطلوبة");
            number(quantity1Field);
            number(quantity2Field);
            number(priceField);
            ok = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage() == null ? "القيم غير صحيحة" : ex.getMessage(),
                    "تنبيه", JOptionPane.WARNING_MESSAGE);
        }
    }

    private double number(JTextField field) {
        return Double.parseDouble(field.getText().trim().replace(',', '.'));
    }

    private void installNavigation(JButton okButton, JButton cancelButton) {
        List<Component> order = new ArrayList<>();
        order.add(materialField);
        order.add(quantity1Field);
        order.add(unitField);
        order.add(priceField);
        order.add(quantity2Field);
        order.add(okButton);
        order.add(cancelButton);
        InputMap input = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getRootPane().getActionMap();
        input.put(KeyStroke.getKeyStroke("DOWN"), "next");
        input.put(KeyStroke.getKeyStroke("RIGHT"), "next");
        input.put(KeyStroke.getKeyStroke("UP"), "previous");
        input.put(KeyStroke.getKeyStroke("LEFT"), "previous");
        input.put(KeyStroke.getKeyStroke("ENTER"), "enter");
        actions.put("next", new AbstractAction() { public void actionPerformed(ActionEvent e) { move(order, 1); } });
        actions.put("previous", new AbstractAction() { public void actionPerformed(ActionEvent e) { move(order, -1); } });
        actions.put("enter", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (c == okButton) okButton.doClick();
                else if (c == cancelButton) cancelButton.doClick();
                else move(order, 1);
            }
        });
    }

    private void move(List<Component> order, int direction) {
        Component current = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        int index = order.indexOf(current);
        if (index < 0) index = 0;
        int next = Math.max(0, Math.min(order.size() - 1, index + direction));
        order.get(next).requestFocusInWindow();
    }

    public boolean isOk() { return ok; }

    public AccountEntry getEntry() {
        AccountEntry entry = new AccountEntry();
        entry.setMaterial(materialField.getText().trim());
        entry.setQuantity1(number(quantity1Field));
        entry.setUnit(unitField.getText().trim());
        entry.setPrice(number(priceField));
        entry.setQuantity2(number(quantity2Field));
        entry.calculateTotal();
        return entry;
    }
}
