import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class EntryDialog extends JDialog {

    private JTextField descF, debitF, creditF, qtyF, unitF, unitPriceF, totalF;
    private boolean ok = false;

    public EntryDialog(Window parent) {
        super(parent, "إضافة / تعديل قيد", ModalityType.APPLICATION_MODAL);
        setSize(650, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        // ===== الحقول =====
        descF = new JTextField(30);
        debitF = new JTextField("0", 15);
        creditF = new JTextField("0", 15);
        qtyF = new JTextField("0", 15);
        unitF = new JTextField(15);
        unitPriceF = new JTextField("0", 15);

        totalF = new JTextField("0.00", 15);
        totalF.setEditable(false);
        totalF.setFont(getFont().deriveFont(Font.BOLD));

        // ===== تصميم الواجهة =====
        g.gridy = 0;
        g.gridx = 1; g.gridwidth = 3;
        form.add(descF, g);
        g.gridx = 0; g.gridwidth = 1;
        form.add(new JLabel("البيان:"), g);

        g.gridy = 1;
        addRow(form, g, "العدد:", qtyF, "الوحدة:", unitF);

        g.gridy = 2;
        addRow(form, g, "مدين:", debitF, "دائن:", creditF);

        g.gridy = 3;
        addRow(form, g, "الإفرادي:", unitPriceF, "الإجمالي:", totalF);

        // ===== حساب الإجمالي تلقائيًا =====
        DocumentListener dl = createCalculationListener();
        qtyF.getDocument().addDocumentListener(dl);
        unitPriceF.getDocument().addDocumentListener(dl);

        // ===== الأزرار =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.setBackground(Color.WHITE);

        JButton okBtn = new JButton("موافق");
        JButton cancelBtn = new JButton("إلغاء");

        buttons.add(okBtn);
        buttons.add(cancelBtn);

        okBtn.addActionListener(e -> onOk());
        cancelBtn.addActionListener(e -> setVisible(false));

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        // ===== التنقل بالأسهم =====
        ArrayList<Component> focusOrder = new ArrayList<>();
        focusOrder.add(descF);
        focusOrder.add(qtyF);
        focusOrder.add(unitF);
        focusOrder.add(debitF);
        focusOrder.add(creditF);
        focusOrder.add(unitPriceF);
        focusOrder.add(okBtn);
        focusOrder.add(cancelBtn);

        setFocusTraversalPolicy(new CustomFocusPolicy(focusOrder));
        setupArrowKeyNavigation();
    }

    // ================== التنقل ==================

    private void setupArrowKeyNavigation() {
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke("DOWN"), "focusNext");
        im.put(KeyStroke.getKeyStroke("RIGHT"), "focusNext");
        im.put(KeyStroke.getKeyStroke("UP"), "focusPrev");
        im.put(KeyStroke.getKeyStroke("LEFT"), "focusPrev");

        am.put("focusNext", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
            }
        });

        am.put("focusPrev", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusPreviousComponent();
            }
        });
    }

    private static class CustomFocusPolicy extends FocusTraversalPolicy {
        private final List<Component> order;

        public CustomFocusPolicy(List<Component> order) {
            this.order = new ArrayList<>(order);
        }

        @Override public Component getComponentAfter(Container c, Component comp) {
            int i = (order.indexOf(comp) + 1) % order.size();
            return order.get(i);
        }

        @Override public Component getComponentBefore(Container c, Component comp) {
            int i = order.indexOf(comp) - 1;
            return order.get(i < 0 ? order.size() - 1 : i);
        }

        @Override public Component getFirstComponent(Container c) { return order.get(0); }
        @Override public Component getLastComponent(Container c) { return order.get(order.size() - 1); }
        @Override public Component getDefaultComponent(Container c) { return order.get(0); }
    }

    // ================== منطق ==================

    private void onOk() {
        try {
            parseDouble(debitF.getText());
            parseDouble(creditF.getText());
            parseDouble(qtyF.getText());
            parseDouble(unitPriceF.getText());
            ok = true;
            setVisible(false);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "الرجاء التأكد من صحة القيم الرقمية",
                    "خطأ",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private DocumentListener createCalculationListener() {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { calc(); }
            public void removeUpdate(DocumentEvent e) { calc(); }
            public void changedUpdate(DocumentEvent e) { calc(); }

            private void calc() {
                try {
                    double q = parseDouble(qtyF.getText());
                    double p = parseDouble(unitPriceF.getText());
                    totalF.setText(String.format("%.2f", q * p));
                } catch (Exception e) {
                    totalF.setText("0.00");
                }
            }
        };
    }

    private void addRow(JPanel p, GridBagConstraints g,
                        String l1, JComponent f1,
                        String l2, JComponent f2) {

        g.gridx = 1; p.add(f1, g);
        g.gridx = 0; p.add(new JLabel(l1), g);

        if (l2 != null && f2 != null) {
            g.gridx = 3; p.add(f2, g);
            g.gridx = 2; p.add(new JLabel(l2), g);
        }
    }

    private double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return 0.0;
        return Double.parseDouble(s.trim());
    }

    // ================== API ==================

    public boolean isOk() {
        return ok;
    }

    public AccountEntry getEntry() {
        return new AccountEntry(
                descF.getText().trim(),
                parseDouble(debitF.getText()),
                parseDouble(creditF.getText()),
                parseDouble(qtyF.getText()),
                unitF.getText().trim(),
                parseDouble(unitPriceF.getText())
        );
    }
}
