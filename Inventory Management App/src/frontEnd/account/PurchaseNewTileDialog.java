package frontEnd.account;

import backEnd.account.AccountInventoryKind;
import backEnd.account.AccountStockMovementType;
import backEnd.tile.TileItem;
import backEnd.warehouse.Warehouse;
import backEnd.warehouse.WarehouseManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseNewTileDialog {

    private final Window owner;
    private final WarehouseManager warehouseManager;
    private final PurchaseEntryDialog.PurchaseData data =
            new PurchaseEntryDialog.PurchaseData();
    private boolean ok;

    public PurchaseNewTileDialog(
            Window owner,
            WarehouseManager warehouseManager) {
        this.owner = owner;
        this.warehouseManager = warehouseManager;
    }

    public void showDialog() {
        JDialog dialog = new JDialog(
                owner, "شراء عنصر بلاط جديد",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField code = new JTextField();
        JTextField name = new JTextField();
        JTextField grade = new JTextField();
        JTextField boxArea = new JTextField();
        JTextField boxes = new JTextField();
        JTextField price = new JTextField();

        for (JTextField f : new JTextField[]{code, name, grade, boxArea, boxes, price}) {
            PurchaseDialogSupport.styleTextField(f);
        }

        JComboBox<TileItem.MaterialType> materialType =
                new JComboBox<>(TileItem.MaterialType.values());
        JComboBox<TileItem.SubType> subtype =
                new JComboBox<>(TileItem.SubType.values());
        JComboBox<TileItem.Location> location =
                new JComboBox<>(TileItem.Location.values());
        JComboBox<Warehouse> warehouse =
                new JComboBox<>();

        styleCombo(materialType);
        styleCombo(subtype);
        styleCombo(location);
        styleCombo(warehouse);

        location.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                if (value instanceof TileItem.Location l) {
                    value = l.getArabicName();
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

        JLabel quantity1 = new JLabel("0.00", SwingConstants.CENTER);
        JLabel material = new JLabel("", SwingConstants.CENTER);
        JLabel total = new JLabel("0.00", SwingConstants.CENTER);

        JPanel form = PurchaseDialogSupport.formPanel();
        PurchaseDialogSupport.addRow(form, 0, "الرمز:", code);
        PurchaseDialogSupport.addRow(form, 1, "الاسم:", name);
        PurchaseDialogSupport.addRow(form, 2, "النخب:", grade);
        PurchaseDialogSupport.addRow(form, 3, "مساحة الصندوق:", boxArea);
        PurchaseDialogSupport.addRow(form, 4, "عدد الصناديق (الكمية 2):", boxes);
        PurchaseDialogSupport.addRow(form, 5, "نوع المادة:", materialType);
        PurchaseDialogSupport.addRow(form, 6, "النوع الفرعي:", subtype);
        PurchaseDialogSupport.addRow(form, 7, "الموقع:", location);
        PurchaseDialogSupport.addRow(form, 8, "المستودع:", warehouse);
        PurchaseDialogSupport.addRow(form, 9, "السعر:", price);
        PurchaseDialogSupport.addRow(form, 10, "المادة:", material);
        PurchaseDialogSupport.addRow(form, 11, "الكمية 1:", quantity1);
        PurchaseDialogSupport.addRow(form, 12, "الإجمالي:", total);

        Runnable calculate = () -> {
            try {
                double area = Double.parseDouble(boxArea.getText().trim().replace(',', '.'));
                double count = Double.parseDouble(boxes.getText().trim().replace(',', '.'));
                double p = Double.parseDouble(price.getText().trim().replace(',', '.'));
                double q1 = area * count;
                quantity1.setText(format(q1));
                total.setText(format(q1 * p));
            } catch (Exception ignored) {
                quantity1.setText("0.00");
                total.setText("0.00");
            }

            String c = code.getText().trim();
            String n = name.getText().trim();
            String g = grade.getText().trim();
            Object mt = materialType.getSelectedItem();
            Object st = subtype.getSelectedItem();
            if (!c.isEmpty() || !n.isEmpty() || !g.isEmpty()) {
                material.setText(
                        c + " " + n + " "
                                + (st == null ? "" : st.toString()) + " "
                                + (mt == null ? "" : mt.toString()) + " "
                                + g
                );
            } else {
                material.setText("");
            }
        };

        for (JTextField f : new JTextField[]{code, name, grade, boxArea, boxes, price}) {
            f.getDocument().addDocumentListener(simple(calculate));
        }
        materialType.addActionListener(e -> calculate.run());
        subtype.addActionListener(e -> calculate.run());
        calculate.run();

        JButton okButton = new JButton("موافق");
        JButton cancelButton = new JButton("إلغاء");

        okButton.addActionListener(e -> {
            try {
                String c = required(code, "الرمز");
                String n = required(name, "الاسم");
                String g = required(grade, "النخب");
                double area = PurchaseDialogSupport.number(boxArea, "مساحة الصندوق");
                double count = PurchaseDialogSupport.number(boxes, "عدد الصناديق");
                double p = PurchaseDialogSupport.number(price, "السعر");

                if (area <= 0) throw new IllegalArgumentException("مساحة الصندوق يجب أن تكون أكبر من صفر");
                if (count <= 0) throw new IllegalArgumentException("عدد الصناديق يجب أن يكون أكبر من صفر");
                if (materialType.getSelectedItem() == null
                        || subtype.getSelectedItem() == null
                        || location.getSelectedItem() == null
                        || warehouse.getSelectedItem() == null) {
                    throw new IllegalArgumentException("أكمل بيانات البلاط والمستودع");
                }

                data.kind = AccountInventoryKind.TILE;
                data.movement = AccountStockMovementType.NEW_ITEM;
                data.code = c;
                data.name = n;
                data.grade = g;
                data.boxArea = area;
                data.quantity2 = count;
                data.quantity1 = area * count;
                data.tileMaterialType = (TileItem.MaterialType) materialType.getSelectedItem();
                data.tileSubtype = (TileItem.SubType) subtype.getSelectedItem();
                data.location = (TileItem.Location) location.getSelectedItem();
                data.warehouse = (Warehouse) warehouse.getSelectedItem();
                data.unit = "متر";
                data.price = p;
                ok = true;
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "تنبيه", JOptionPane.WARNING_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        List<JComponent> fields = new ArrayList<>();
        fields.add(code);
        fields.add(name);
        fields.add(grade);
        fields.add(boxArea);
        fields.add(boxes);
        fields.add(materialType);
        fields.add(subtype);
        fields.add(location);
        fields.add(warehouse);
        fields.add(price);

        PurchaseDialogSupport.installNavigation(dialog, fields, okButton, cancelButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(okButton);
        buttons.add(cancelButton);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setMinimumSize(new Dimension(720, 680));
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                code.requestFocusInWindow();
            }
        });
        dialog.setVisible(true);
    }

    private static String required(JTextField field, String name) {
        String value = field.getText().trim();
        if (value.isEmpty()) throw new IllegalArgumentException(name + " مطلوب");
        return value;
    }

    private static void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Tahoma", Font.PLAIN, 17));
        combo.setPreferredSize(new Dimension(250, 32));
    }

    private static String format(double value) {
        return String.format("%.2f", value);
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
