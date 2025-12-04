import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
// تم حذف: import java.io.IOException; لأننا لن نستخدمها هنا بعد الآن

public class WarehousePanel extends JPanel {
    private WarehouseManager manager;
    private JTable table;
    private DefaultTableModel tableModel;
    private static final String[] COLUMNS = {"ID", "الاسم", "النوع", "اسم المالك", "تواصل المالك"};

    public WarehousePanel(WarehouseManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout());

        // جدول العرض
        tableModel = new DefaultTableModel(COLUMNS, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- تم التعديل: إزالة أزرار الحفظ والتحميل ---
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("إضافة مستودع");
        JButton deleteButton = new JButton("حذف");
        JButton editButton = new JButton("تعديل");

        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        add(buttonsPanel, BorderLayout.SOUTH);

        refreshTable();

        // أحداث الأزرار (أصبحت أبسط)
        addButton.addActionListener(e -> openAddDialog());
        deleteButton.addActionListener(e -> deleteSelected());
        editButton.addActionListener(e -> editSelected());

        // تم حذف المستمعين (Listeners) الخاصين بأزرار الحفظ والتحميل
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Warehouse w : manager.getWarehouses()) {
            tableModel.addRow(new Object[]{
                    w.getId(), w.getName(), w.getType(), w.getOwnerName(), w.getOwnerContact()
            });
        }
    }

    private void openAddDialog() {
        WarehouseDialog dialog = new WarehouseDialog(null); // افترض وجود هذا الكلاس
        if (dialog.isSucceeded()) {
            // عند الإضافة، سيقوم Manager بالحفظ تلقائيًا
            manager.addWarehouse(dialog.getWarehouse());
            refreshTable();
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "الرجاء تحديد صف لحذفه.", "تنبيه", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object[] options = {"نعم، حذف", "لا"};
        int choice = JOptionPane.showOptionDialog(
                this,
              "  هل أنت متأكد من حذف المستودع المحدد؟"+
        "(ملاحظة: هذا الإجراء لا يحذف العناصر الموجودة داخل المستودع حالياً)",
        "تأكيد الحذف",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null, options, options[1]);

        if (choice == JOptionPane.YES_OPTION) {
            int id = (int) tableModel.getValueAt(row, 0);
            // عند الحذف، سيقوم Manager بالحفظ تلقائيًا
            manager.deleteWarehouse(id);
            refreshTable();
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "الرجاء تحديد صف لتعديله.", "تنبيه", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) table.getValueAt(row, 0);
        Warehouse original = manager.getWarehouses().stream().filter(w -> w.getId() == id).findFirst().orElse(null);

        if (original != null) {
            WarehouseDialog dialog = new WarehouseDialog(original);
            if (dialog.isSucceeded()) {
                // عند التعديل، سيقوم Manager بالحفظ تلقائيًا
                manager.updateWarehouse(dialog.getWarehouse());
                refreshTable();
            }
        }
    }
}