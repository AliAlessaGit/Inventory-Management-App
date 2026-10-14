package frontEnd.account;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PurchaseNotInStockSanitaryDialog {

    private final Window owner;
    private final PurchaseEntryDialog.PurchaseData data =
            new PurchaseEntryDialog.PurchaseData();
    private boolean ok;

    public PurchaseNotInStockSanitaryDialog(Window owner) {
        this.owner = owner;
    }

    public void showDialog() {
        JDialog dialog = new JDialog(
                owner, "شراء أداة صحية - لا تضاف إلى المستودع",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField material = new JTextField();
        JTextField unit = new JTextField("قطعة");
        JTextField quantity = new JTextField();
        JTextField price = new JTextField();

        for (JTextField f : new JTextField[]{material, unit, quantity, price}) {
            PurchaseDialogSupport.styleTextField(f);
        }

        JLabel total = new JLabel("0.00", SwingConstants.CENTER);

        JPanel form = PurchaseDialogSupport.formPanel();
        PurchaseDialogSupport.addRow(form, 0, "المادة:", material);
        PurchaseDialogSupport.addRow(form, 1, "الوحدة:", unit);
        PurchaseDialogSupport.addRow(form, 2, "الكمية 1:", quantity);
        PurchaseDialogSupport.addRow(form, 3, "السعر:", price);
        PurchaseDialogSupport.addRow(form, 4, "الإجمالي:", total);

        Runnable calculate = () -> {
            try {
                double q = Double.parseDouble(quantity.getText().trim().replace(',', '.'));
                double p = Double.parseDouble(price.getText().trim().replace(',', '.'));
                total.setText(String.format("%.2f", q * p));
            } catch (Exception ignored) {
                total.setText("0.00");
            }
        };
        quantity.getDocument().addDocumentListener(simple(calculate));
        price.getDocument().addDocumentListener(simple(calculate));

        JButton okButton = new JButton("موافق");
        JButton cancelButton = new JButton("إلغاء");

        okButton.addActionListener(e -> {
            try {
                String materialText = material.getText().trim();
                String unitText = unit.getText().trim();
                if (materialText.isEmpty()) throw new IllegalArgumentException("المادة مطلوبة");
                if (unitText.isEmpty()) throw new IllegalArgumentException("الوحدة مطلوبة");
                double q = PurchaseDialogSupport.number(quantity, "الكمية 1");
                double p = PurchaseDialogSupport.number(price, "السعر");
                if (q <= 0) throw new IllegalArgumentException("الكمية يجب أن تكون أكبر من صفر");

                data.kind = backEnd.account.AccountInventoryKind.SANITARY;
                data.movement = backEnd.account.AccountStockMovementType.NOT_IN_STOCK;
                data.material = materialText;
                data.unit = unitText;
                data.quantity1 = q;
                data.quantity2 = q;
                data.price = p;
                ok = true;
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "تنبيه", JOptionPane.WARNING_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        PurchaseDialogSupport.installNavigation(
                dialog, List.of(material, unit, quantity, price), okButton, cancelButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(okButton);
        buttons.add(cancelButton);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setMinimumSize(new Dimension(620, 400));
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
