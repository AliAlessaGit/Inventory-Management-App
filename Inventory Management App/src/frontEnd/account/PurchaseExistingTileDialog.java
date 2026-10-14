package frontEnd.account;

import backEnd.account.AccountInventoryKind;
import backEnd.account.AccountStockMovementType;
import backEnd.tile.TileItem;
import backEnd.tile.TileService;
import backEnd.warehouse.WarehouseManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PurchaseExistingTileDialog {

    private final Window owner;
    private final TileService tileService;
    private final WarehouseManager warehouseManager;
    private final PurchaseEntryDialog.PurchaseData data =
            new PurchaseEntryDialog.PurchaseData();
    private boolean ok;

    public PurchaseExistingTileDialog(
            Window owner,
            TileService tileService,
            WarehouseManager warehouseManager) {
        this.owner = owner;
        this.tileService = tileService;
        this.warehouseManager = warehouseManager;
    }

    public void showDialog() {
        TileItem item = PurchaseInventorySelectionDialog.selectTile(
                owner, tileService, warehouseManager);
        if (item == null) {
            return;
        }

        JDialog dialog = new JDialog(
                owner, "شراء بلاط موجود في المستودع",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField code = new JTextField();
        JTextField name = new JTextField();
        JTextField boxes = new JTextField();
        JTextField price = new JTextField(format(item.getPrice()));

        for (JTextField f : new JTextField[]{code, name, boxes, price}) {
            PurchaseDialogSupport.styleTextField(f);
        }

        JLabel unit = new JLabel("متر", SwingConstants.CENTER);
        JLabel boxArea = new JLabel(format(item.getBoxArea()), SwingConstants.CENTER);
        JLabel material = new JLabel("", SwingConstants.CENTER);
        JLabel quantity1 = new JLabel("0.00", SwingConstants.CENTER);
        JLabel total = new JLabel("0.00", SwingConstants.CENTER);

        JPanel form = PurchaseDialogSupport.formPanel();
        PurchaseDialogSupport.addRow(form, 0, "الرمز (للتعديل):", code);
        PurchaseDialogSupport.addRow(form, 1, "الاسم (للتعديل):", name);
        PurchaseDialogSupport.addRow(form, 2, "الوحدة:", unit);
        PurchaseDialogSupport.addRow(form, 3, "مساحة الصندوق:", boxArea);
        PurchaseDialogSupport.addRow(form, 4, "الكمية 2 (عدد الصناديق):", boxes);
        PurchaseDialogSupport.addRow(form, 5, "السعر:", price);
        PurchaseDialogSupport.addRow(form, 6, "المادة:", material);
        PurchaseDialogSupport.addRow(form, 7, "الكمية 1:", quantity1);
        PurchaseDialogSupport.addRow(form, 8, "الإجمالي:", total);

        Runnable calculate = () -> {
            try {
                double count = Double.parseDouble(boxes.getText().trim().replace(',', '.'));
                double p = Double.parseDouble(price.getText().trim().replace(',', '.'));
                double q1 = item.getBoxArea() * count;
                quantity1.setText(format(q1));
                total.setText(format(q1 * p));
            } catch (Exception ignored) {
                quantity1.setText("0.00");
                total.setText("0.00");
            }

            String finalCode = code.getText().trim().isEmpty()
                    ? item.getCode() : code.getText().trim();
            String finalName = name.getText().trim().isEmpty()
                    ? item.getName() : name.getText().trim();
            material.setText(buildMaterial(
                    finalCode, finalName, item));
        };

        code.getDocument().addDocumentListener(simple(calculate));
        name.getDocument().addDocumentListener(simple(calculate));
        boxes.getDocument().addDocumentListener(simple(calculate));
        price.getDocument().addDocumentListener(simple(calculate));
        calculate.run();

        JButton okButton = new JButton("موافق");
        JButton cancelButton = new JButton("إلغاء");

        okButton.addActionListener(e -> {
            try {
                double count = PurchaseDialogSupport.number(boxes, "عدد الصناديق");
                double p = PurchaseDialogSupport.number(price, "السعر");
                if (count <= 0) throw new IllegalArgumentException("عدد الصناديق يجب أن يكون أكبر من صفر");

                data.kind = AccountInventoryKind.TILE;
                data.movement = AccountStockMovementType.EXISTING_ITEM;
                data.tileItem = item;
                data.code = code.getText().trim();
                data.name = name.getText().trim();
                data.quantity2 = count;
                data.quantity1 = item.getBoxArea() * count;
                data.boxArea = item.getBoxArea();
                data.unit = "متر";
                data.price = p;
                ok = true;
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "تنبيه", JOptionPane.WARNING_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        PurchaseDialogSupport.installNavigation(
                dialog, List.of(code, name, boxes, price), okButton, cancelButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(okButton);
        buttons.add(cancelButton);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setMinimumSize(new Dimension(700, 560));
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                code.requestFocusInWindow();
            }
        });
        dialog.setVisible(true);
    }

    private static String buildMaterial(
            String code,
            String name,
            TileItem item) {
        return code.trim() + " "
                + name.trim() + " "
                + item.getSubType().name() + " "
                + item.getMaterialType().name() + " "
                + item.getGrade();
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
