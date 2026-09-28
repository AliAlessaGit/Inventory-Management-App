package frontEnd.account;

import backEnd.account.AccountInvoiceType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AccountInvoiceDialog extends JDialog {

    private final JComboBox<AccountInvoiceType> typeBox =
            new JComboBox<>(AccountInvoiceType.values());
    private final JTextField dateField =
            new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
    private final JTextField detailsField = new JTextField();
    private final JTextField amountField = new JTextField("0");

    private boolean ok;
    private LocalDate date;
    private String details;
    private double amount;
    private AccountInvoiceType type;

    public AccountInvoiceDialog(Window parent) {
        super(parent, "سند حساب جديد", ModalityType.APPLICATION_MODAL);
        setSize(620, 430);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(22, 28, 12, 28));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        addRow(form, g, 0, "أصل السند:", typeBox, "اختر نوع الحركة المالية");
        addRow(form, g, 1, "التاريخ:", dateField, "الصيغة: سنة/شهر/يوم");
        addRow(form, g, 2, "البيان:", detailsField, "يمكن إضافة تفاصيل بجانب رقم الفاتورة");
        addRow(form, g, 3, "المبلغ:", amountField, "يستخدم فقط للدفع والقبض");

        typeBox.addActionListener(e -> updateAmountState());
        updateAmountState();

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

    private void addRow(JPanel panel, GridBagConstraints g, int row,
                        String label, JComponent field, String hint) {
        g.gridy = row;
        g.gridx = 0;
        g.weightx = 0.25;
        panel.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = 0.75;
        panel.add(field, g);
        g.gridy = row;
        g.gridx = 2;
        g.weightx = 0.55;
        JLabel explanation = new JLabel(hint);
        explanation.setForeground(new Color(100, 100, 100));
        panel.add(explanation, g);
    }

    private void updateAmountState() {
        AccountInvoiceType selected = (AccountInvoiceType) typeBox.getSelectedItem();
        boolean enabled = selected == AccountInvoiceType.PAYMENT || selected == AccountInvoiceType.RECEIPT;
        amountField.setEnabled(enabled);
        amountField.setEditable(enabled);
        if (!enabled) amountField.setText("0");
    }

    private void accept() {
        try {
            type = (AccountInvoiceType) typeBox.getSelectedItem();
            date = LocalDate.parse(dateField.getText().trim(),
                    DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            details = detailsField.getText().trim();
            amount = parseAmount(amountField.getText());
            if (amount < 0) throw new IllegalArgumentException("المبلغ لا يمكن أن يكون سالباً");
            ok = true;
            dispose();
        } catch (DateTimeParseException ex) {
            showError("التاريخ غير صحيح. استخدم الصيغة سنة/شهر/يوم");
        } catch (Exception ex) {
            showError(ex.getMessage() == null ? "القيمة المدخلة غير صحيحة" : ex.getMessage());
        }
    }

    private double parseAmount(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        return Double.parseDouble(text.trim());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "تنبيه", JOptionPane.WARNING_MESSAGE);
    }

    private void installNavigation(JButton okButton, JButton cancelButton) {
        List<Component> order = new ArrayList<>();
        order.add(typeBox);
        order.add(dateField);
        order.add(detailsField);
        order.add(amountField);
        order.add(okButton);
        order.add(cancelButton);

        InputMap input = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getRootPane().getActionMap();
        input.put(KeyStroke.getKeyStroke("DOWN"), "nextField");
        input.put(KeyStroke.getKeyStroke("RIGHT"), "nextField");
        input.put(KeyStroke.getKeyStroke("UP"), "previousField");
        input.put(KeyStroke.getKeyStroke("LEFT"), "previousField");
        input.put(KeyStroke.getKeyStroke("ENTER"), "enterField");

        actions.put("nextField", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { move(order, 1); }
        });
        actions.put("previousField", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { move(order, -1); }
        });
        actions.put("enterField", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Component current = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                int index = order.indexOf(current);
                if (index >= 0 && index < order.size() - 2) move(order, 1);
                else if (current == okButton) okButton.doClick();
                else if (current == cancelButton) cancelButton.doClick();
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
    public AccountInvoiceType getInvoiceType() { return type; }
    public LocalDate getDate() { return date; }
    public String getDetails() { return details; }
    public double getAmount() { return amount; }
}
