package frontEnd.account;

import backEnd.account.AccountInventoryKind;
import backEnd.account.AccountStockMovementType;
import backEnd.sanitary.SanitaryItem;
import backEnd.warehouse.Warehouse;
import backEnd.warehouse.WarehouseManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PurchaseNewSanitaryDialog {

    private final Window owner;
    private final WarehouseManager warehouseManager;
    private final PurchaseEntryDialog.PurchaseData data =
            new PurchaseEntryDialog.PurchaseData();
    private boolean ok;

    public PurchaseNewSanitaryDialog(
            Window owner,
            WarehouseManager warehouseManager) {
        this.owner = owner;
        this.warehouseManager = warehouseManager;
    }

    public void showDialog() {
        JDialog dialog = new JDialog(
                owner, "شراء أداة صحية جديدة",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField name = new JTextField();
        JTextField grade = new JTextField();
        JTextField quantity = new JTextField();
        JTextField price = new JTextField();

        for (JTextField f : new JTextField[]{name, grade, quantity, price}) {
            PurchaseDialogSupport.styleTextField(f);
        }

        JComboBox<SanitaryItem.SanitaryType> type =
                new JComboBox<>(SanitaryItem.SanitaryType.values());
        JComboBox<Warehouse> warehouse = new JComboBox<>();

        type.setFont(new Font("Tahoma", Font.PLAIN, 17));
        warehouse.setFont(new Font("Tahoma", Font.PLAIN, 17));

        type.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                if (value instanceof SanitaryItem.SanitaryType t) {
                    value = t.getArabicName();
                }
                return super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
            }
        });

        if (warehouseManager != null) {
            for (Warehouse w : warehouseManager.getWarehouses()) {
                warehouse.addItem(w);
            }
        }
        warehouse.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Warehouse w) {
                    value = w.getDisplayName();
                }
                return super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
            }
        });

        JLabel material = new JLabel("", SwingConstants.CENTER);
        JLabel total = new JLabel("0.00", SwingConstants.CENTER);

        JPanel form = PurchaseDialogSupport.formPanel();
        PurchaseDialogSupport.addRow(form, 0, "الاسم:", name);
        PurchaseDialogSupport.addRow(form, 1, "النخب:", grade);
        PurchaseDialogSupport.addRow(form, 2, "الكمية 1:", quantity);
        PurchaseDialogSupport.addRow(form, 3, "نوع الأداة:", type);
        PurchaseDialogSupport.addRow(form, 4, "المستودع:", warehouse);
        PurchaseDialogSupport.addRow(form, 5, "السعر:", price);
        PurchaseDialogSupport.addRow(form, 6, "المادة:", material);
        PurchaseDialogSupport.addRow(form, 7, "الإجمالي:", total);

        Runnable calculate = () -> {
            try {
                double q = Double.parseDouble(quantity.getText().trim().replace(',', '.'));
                double p = Double.parseDouble(price.getText().trim().replace(',', '.'));
                total.setText(String.format("%.2f", q * p));
            } catch (Exception ignored) {
                total.setText("0.00");
            }
            String n = name.getText().trim();
            String g = grade.getText().trim();
            material.setText(n.isEmpty() && g.isEmpty() ? "" : n + " " + g);
        };

        name.getDocument().addDocumentListener(simple(calculate));
        grade.getDocument().addDocumentListener(simple(calculate));
        quantity.getDocument().addDocumentListener(simple(calculate));
        price.getDocument().addDocumentListener(simple(calculate));
        calculate.run();

        JButton okButton = new JButton("موافق");
        JButton cancelButton = new JButton("إلغاء");

        okButton.addActionListener(e -> {
            try {
                String n = required(name, "الاسم");
                String g = required(grade, "النخب");
                double q = PurchaseDialogSupport.number(quantity, "الكمية");
                double p = PurchaseDialogSupport.number(price, "السعر");

                if (q <= 0) throw new IllegalArgumentException("الكمية يجب أن تكون أكبر من صفر");
                if (q != Math.rint(q)) {
                    throw new IllegalArgumentException("كمية الأدوات الصحية يجب أن تكون عدداً صحيحاً");
                }
                if (type.getSelectedItem() == null || warehouse.getSelectedItem() == null) {
                    throw new IllegalArgumentException("أكمل نوع الأداة والمستودع");
                }

                data.kind = AccountInventoryKind.SANITARY;
                data.movement = AccountStockMovementType.NEW_ITEM;
                data.name = n;
                data.grade = g;
                data.quantity1 = q;
                data.quantity2 = q;
                data.sanitaryType = (SanitaryItem.SanitaryType) type.getSelectedItem();
                data.warehouse = (Warehouse) warehouse.getSelectedItem();
                data.price = p;
                data.unit = "قطعة";
                data.material = n + " " + g;
                ok = true;
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "تنبيه", JOptionPane.WARNING_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        PurchaseDialogSupport.installNavigation(
                dialog, List.of(name, grade, quantity, type, warehouse, price),
                okButton, cancelButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(okButton);
        buttons.add(cancelButton);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setMinimumSize(new Dimension(650, 480));
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                name.requestFocusInWindow();
            }
        });
        dialog.setVisible(true);
    }

    private static String required(JTextField field, String name) {
        String value = field.getText().trim();
        if (value.isEmpty()) throw new IllegalArgumentException(name + " مطلوب");
        return value;
    }

    private static javax.swing.event.DocumentListener simple(Runnable action) {
        return new javax.swing.event.DocumentListener() {
            private void run() { action.run(); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { run(); }
        };
    }

    public boolean isOk() { return ok; }
    public PurchaseEntryDialog.PurchaseData getData() { return data; }
}
