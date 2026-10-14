package frontEnd.account;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PurchaseNotInStockTileDialog {

    private final Window owner;
    private final PurchaseEntryDialog.PurchaseData data =
            new PurchaseEntryDialog.PurchaseData();
    private boolean ok;

    public PurchaseNotInStockTileDialog(Window owner) {
        this.owner = owner;
    }

    public void showDialog() {
        JDialog dialog = new JDialog(
                owner, "شراء بلاط - لا يضاف إلى المستودع",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField material = new JTextField();
        JTextField boxArea = new JTextField();
        JTextField boxes = new JTextField();
        JTextField price = new JTextField();

        for (JTextField f : new JTextField[]{material, boxArea, boxes, price}) {
            PurchaseDialogSupport.styleTextField(f);
        }

        JLabel quantity1 = new JLabel("0.00", SwingConstants.CENTER);
        JLabel total = new JLabel("0.00", SwingConstants.CENTER);

        JPanel form = PurchaseDialogSupport.formPanel();
        PurchaseDialogSupport.addRow(form, 0, "المادة:", material);
        PurchaseDialogSupport.addRow(form, 1, "مساحة الصندوق:", boxArea);
        PurchaseDialogSupport.addRow(form, 2, "الكمية 2 (عدد الصناديق):", boxes);
        PurchaseDialogSupport.addRow(form, 3, "السعر:", price);
        PurchaseDialogSupport.addRow(form, 4, "الكمية 1 (المساحة الإجمالية):", quantity1);
        PurchaseDialogSupport.addRow(form, 5, "الإجمالي:", total);

        Runnable calculate = () -> {
            try {
                double area = Double.parseDouble(boxArea.getText().trim().replace(',', '.'));
                double count = Double.parseDouble(boxes.getText().trim().replace(',', '.'));
                double p = Double.parseDouble(price.getText().trim().replace(',', '.'));
                double q1 = area * count;
                quantity1.setText(String.format("%.2f", q1));
                total.setText(String.format("%.2f", q1 * p));
            } catch (Exception ignored) {
                quantity1.setText("0.00");
                total.setText("0.00");
            }
        };

        boxArea.getDocument().addDocumentListener(simple(calculate));
        boxes.getDocument().addDocumentListener(simple(calculate));
        price.getDocument().addDocumentListener(simple(calculate));

        JButton okButton = new JButton("موافق");
        JButton cancelButton = new JButton("إلغاء");

        okButton.addActionListener(e -> {
            try {
                String materialText = material.getText().trim();
                if (materialText.isEmpty()) {
                    throw new IllegalArgumentException("المادة مطلوبة");
                }
                double area = PurchaseDialogSupport.number(boxArea, "مساحة الصندوق");
                double count = PurchaseDialogSupport.number(boxes, "عدد الصناديق");
                double p = PurchaseDialogSupport.number(price, "السعر");
                if (area <= 0) throw new IllegalArgumentException("مساحة الصندوق يجب أن تكون أكبر من صفر");
                if (count <= 0) throw new IllegalArgumentException("عدد الصناديق يجب أن يكون أكبر من صفر");

                data.kind = backEnd.account.AccountInventoryKind.TILE;
                data.movement = backEnd.account.AccountStockMovementType.NOT_IN_STOCK;
                data.material = materialText;
                data.boxArea = area;
                data.quantity2 = count;
                data.quantity1 = area * count;
                data.unit = "متر";
                data.price = p;
                ok = true;
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "تنبيه", JOptionPane.WARNING_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        List<JComponent> fields = List.of(material, boxArea, boxes, price);
        PurchaseDialogSupport.installNavigation(dialog, fields, okButton, cancelButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(okButton);
        buttons.add(cancelButton);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setMinimumSize(new Dimension(620, 440));
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                material.requestFocusInWindow();
            }
        });
        dialog.setVisible(true);
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
