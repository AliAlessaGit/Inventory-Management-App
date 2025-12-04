import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EntryDialog extends JDialog {
    private JTextField dateF, descF, debitF, creditF, qtyF, unitF, unitPriceF, totalF;
    private boolean ok = false;

    public EntryDialog(Window parent) {
        super(parent, "إضافة / تعديل قيد", ModalityType.APPLICATION_MODAL);
        setSize(650, 480);
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

        // --- تهيئة الحقول ---
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dateF = new JTextField(dtf.format(LocalDate.now()), 15);
        descF = new JTextField(30);
        debitF = new JTextField("0", 15);
        creditF = new JTextField("0", 15);
        qtyF = new JTextField("0", 15);
        unitF = new JTextField(15);
        unitPriceF = new JTextField("0", 15);
        totalF = new JTextField("0.00", 15);
        totalF.setEditable(false);
        totalF.setFont(getFont().deriveFont(Font.BOLD));

        // --- تصميم الواجهة ---
        g.gridy = 0; g.gridx = 1; g.gridwidth = 3; form.add(dateF, g);
        g.gridx = 0; g.gridwidth = 1; form.add(new JLabel("التاريخ:"), g);

        g.gridy = 1; g.gridx = 1; g.gridwidth = 3; form.add(descF, g);
        g.gridx = 0; g.gridwidth = 1; form.add(new JLabel("البيان:"), g);

        g.gridy = 2; addRow(form, g, "العدد:", qtyF, "الوحدة:", unitF);
        g.gridy = 3; addRow(form, g, "مدين:", debitF, "دائن:", creditF);
        g.gridy = 4; addRow(form, g, "الإفرادي:", unitPriceF, "الإجمالي:", totalF);

        // --- المستمع لحساب الإجمالي ---
        DocumentListener dl = createCalculationListener();
        qtyF.getDocument().addDocumentListener(dl);
        unitPriceF.getDocument().addDocumentListener(dl);

        // --- لوحة الأزرار ---
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

        // =================================================================
        // =========== الجزء الجديد: إعداد التنقل بالأسهم ===================
        // =================================================================

        // 1. تحديد ترتيب الحقول والأزرار للتنقل
        ArrayList<Component> focusOrder = new ArrayList<>();
        focusOrder.add(dateF);
        focusOrder.add(descF);
        focusOrder.add(qtyF);
        focusOrder.add(unitF);
        focusOrder.add(debitF);
        focusOrder.add(creditF);
        focusOrder.add(unitPriceF);
        focusOrder.add(okBtn);
        focusOrder.add(cancelBtn);

        // 2. تطبيق سياسة التنقل المخصصة
        setFocusTraversalPolicy(new CustomFocusPolicy(focusOrder));

        // 3. ربط مفاتيح الأسهم بأوامر التنقل
        setupArrowKeyNavigation();
    }

    // --- الدوال المساعدة للتنقل ---

    private void setupArrowKeyNavigation() {
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        // السهم للأسفل أو لليمين -> انتقل للتالي
        im.put(KeyStroke.getKeyStroke("DOWN"), "focusNext");
        im.put(KeyStroke.getKeyStroke("RIGHT"), "focusNext");
        am.put("focusNext", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
            }
        });

        // السهم للأعلى أو لليسار -> انتقل للسابق
        im.put(KeyStroke.getKeyStroke("UP"), "focusPrevious");
        im.put(KeyStroke.getKeyStroke("LEFT"), "focusPrevious");
        am.put("focusPrevious", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusPreviousComponent();
            }
        });
    }

    // --- كلاس داخلي لإدارة سياسة التنقل ---
    private static class CustomFocusPolicy extends FocusTraversalPolicy {
        private final List<Component> order;

        public CustomFocusPolicy(List<Component> order) {
            this.order = new ArrayList<>(order);
        }

        @Override
        public Component getComponentAfter(Container aContainer, Component aComponent) {
            int idx = (order.indexOf(aComponent) + 1) % order.size();
            return order.get(idx);
        }

        @Override
        public Component getComponentBefore(Container aContainer, Component aComponent) {
            int idx = order.indexOf(aComponent) - 1;
            if (idx < 0) {
                idx = order.size() - 1;
            }
            return order.get(idx);
        }

        @Override public Component getFirstComponent(Container aContainer) { return order.get(0); }
        @Override public Component getLastComponent(Container aContainer) { return order.get(order.size() - 1); }
        @Override public Component getDefaultComponent(Container aContainer) { return getFirstComponent(aContainer); }
    }

    // --- بقية دوال الكلاس ---

    private void onOk() {
        try {
            parseDouble(debitF.getText());
            parseDouble(creditF.getText());
            parseDouble(qtyF.getText());
            parseDouble(unitPriceF.getText());
            ok = true;
            setVisible(false);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "الرجاء التحقق من أن القيم الرقمية المدخلة صحيحة.", "خطأ في الإدخال", JOptionPane.ERROR_MESSAGE);
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
                } catch (Exception ex) {
                    totalF.setText("0.00");
                }
            }
        };
    }

    private void addRow(JPanel p, GridBagConstraints g, String lbl1, JComponent field1, String lbl2, JComponent field2) {
        g.gridx = 1; p.add(field1, g);
        g.gridx = 0; p.add(new JLabel(lbl1), g);

        if (lbl2 != null && field2 != null) {
            g.gridx = 3; p.add(field2, g);
            g.gridx = 2; p.add(new JLabel(lbl2), g);
        }
    }

    private double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(s.trim());
    }

    public boolean isOk() { return ok; }

    public AccountEntry getEntry() {
        return new AccountEntry(
                dateF.getText().trim(),
                descF.getText().trim(),
                parseDouble(debitF.getText()),
                parseDouble(creditF.getText()),
                parseDouble(qtyF.getText()),
                unitF.getText().trim(),
                parseDouble(unitPriceF.getText())
        );
    }
}