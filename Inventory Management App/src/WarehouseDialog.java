import javax.swing.*;
import java.awt.*;

public class WarehouseDialog extends JDialog {
    private JTextField nameField;
    private JComboBox<Warehouse.Type> typeCombo;
    private JTextField ownerNameField;
    private JTextField ownerContactField;

    private boolean succeeded;
    private Warehouse warehouse;

    public WarehouseDialog(Warehouse existing) {
        setTitle(existing == null ? "إضافة مستودع" : "تعديل مستودع");
        setModal(true);
        setSize(300, 250);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 5, 5));

        add(new JLabel("اسم المستودع:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("النوع:"));
        typeCombo = new JComboBox<>(Warehouse.Type.values());
        add(typeCombo);

        add(new JLabel("اسم المالك:"));
        ownerNameField = new JTextField();
        add(ownerNameField);

        add(new JLabel("تواصل المالك:"));
        ownerContactField = new JTextField();
        add(ownerContactField);

        JButton okButton = new JButton("موافق");
        JButton cancelButton = new JButton("إلغاء");

        add(okButton);
        add(cancelButton);

        // تعبئة البيانات إن وُجدت
        if (existing != null) {
            nameField.setText(existing.getName());
            typeCombo.setSelectedItem(existing.getType());
            ownerNameField.setText(existing.getOwnerName());
            ownerContactField.setText(existing.getOwnerContact());
            this.warehouse = new Warehouse(existing.getId(), existing.getName(), existing.getType(), existing.getOwnerName(), existing.getOwnerContact());
        }

        okButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            Warehouse.Type type = (Warehouse.Type) typeCombo.getSelectedItem();
            String ownerName = ownerNameField.getText().trim();
            String ownerContact = ownerContactField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "يرجى إدخال اسم المستودع");
                return;
            }

            if (warehouse == null) {
                warehouse = new Warehouse(0, name, type, ownerName, ownerContact);
            } else {
                warehouse.setName(name);
                warehouse.setType(type);
                warehouse.setOwnerName(ownerName);
                warehouse.setOwnerContact(ownerContact);
            }

            succeeded = true;
            dispose();
        });

        cancelButton.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        setVisible(true);
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }
}
