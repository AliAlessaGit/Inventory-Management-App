import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class AccountsPanel extends JPanel {
    private final AccountsManager manager;

    // --- المتغيرات على مستوى الكلاس ---
    private JTable accountsTable;
    private AccountsTableModel accountsModel;

    private JTable entriesTable;
    private EntriesTableModel entriesModel;

    private JLabel balanceLabel;
    private JButton addEntryBtn;
    private JButton removeAccountBtn;
    private JButton deleteEntryBtn; // تم نقله هنا ليصبح متاحاً في كل الكلاس

    private Account currentAccount;

    // مستمع لتحديد الصفوف في جدول القيود، يتم إنشاؤه مرة واحدة فقط
    private final ListSelectionListener entrySelectionListener = e -> {
        if (!e.getValueIsAdjusting()) {
            // تفعيل زر الحذف فقط إذا كان هناك صف محدد
            deleteEntryBtn.setEnabled(entriesTable.getSelectedRow() >= 0);
        }
    };

    public AccountsPanel(AccountsManager manager) {
        this.manager = manager;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel rightPanel = buildRightPanel();
        JPanel centerPanel = buildCenterPanel();

        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBackground(Color.WHITE);
        right.setPreferredSize(new Dimension(300, 0));
        right.setBorder(new LineBorder(Color.BLACK));

        JPanel buttons = new JPanel(new GridLayout(2, 1, 8, 8));
        buttons.setBackground(Color.WHITE);

        JButton addAccountBtn = new JButton("إضافة حساب جديد");
        removeAccountBtn = new JButton("إزالة حساب");
        removeAccountBtn.setEnabled(false);

        buttons.add(addAccountBtn);
        buttons.add(removeAccountBtn);

        accountsModel = new AccountsTableModel(manager.getAccounts());
        accountsTable = new JTable(accountsModel);
        styleTable(accountsTable);
        accountsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane accountsScroll = new JScrollPane(accountsTable);
        accountsScroll.setBorder(new LineBorder(Color.BLACK));
        accountsScroll.getViewport().setBackground(Color.WHITE);

        // --- أحداث الأزرار ---
        addAccountBtn.addActionListener(e -> onAddAccount());
        removeAccountBtn.addActionListener(e -> onRemoveAccount());

        // مستمع لاختيار الحساب
        accountsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onAccountSelected(accountsTable.getSelectedRow());
            }
        });

        right.add(buttons, BorderLayout.NORTH);
        right.add(accountsScroll, BorderLayout.CENTER);
        return right;
    }

    private JPanel buildCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(Color.WHITE);
        center.setBorder(new LineBorder(Color.BLACK));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setBackground(Color.WHITE);

        addEntryBtn = new JButton("إضافة قيد");
        addEntryBtn.setEnabled(false);

        deleteEntryBtn = new JButton("حذف قيد"); // تم تغيير التعريف إلى إسناد
        deleteEntryBtn.setEnabled(false);

        topBar.add(addEntryBtn); // إضافة زر "إضافة قيد"
        topBar.add(deleteEntryBtn); // إضافة زر "حذف قيد"

        entriesModel = new EntriesTableModel(null);
        entriesTable = new JTable(entriesModel);
        entriesTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        entriesTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        styleTable(entriesTable);


        // إضافة مستمع تحديد الصفوف مرة واحدة فقط
        entriesTable.getSelectionModel().addListSelectionListener(entrySelectionListener);

        // جعل عمود "البيان" بخط عريض
        DefaultTableCellRenderer boldRenderer = new DefaultTableCellRenderer();
        boldRenderer.setFont(boldRenderer.getFont().deriveFont(Font.BOLD));
        entriesTable.getColumnModel().getColumn(3).setCellRenderer(boldRenderer);

        JScrollPane entriesScroll = new JScrollPane(entriesTable);
        entriesScroll.setBorder(new LineBorder(Color.BLACK));
        entriesScroll.getViewport().setBackground(Color.WHITE);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Color.WHITE);
        balanceLabel = new JLabel("الرصيد الحالي: 0.0 (متزن)");
        balanceLabel.setForeground(Color.BLACK);
        bottom.add(balanceLabel);

        // --- أحداث الأزرار ---
        addEntryBtn.addActionListener(e -> onAddEntry());
        deleteEntryBtn.addActionListener(e -> onDeleteEntry());

        center.add(topBar, BorderLayout.NORTH);
        center.add(entriesScroll, BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);
        return center;
    }

    private void styleTable(JTable table) {
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setGridColor(Color.BLACK);
        table.setShowGrid(true);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(230, 230, 230));
        table.setSelectionForeground(Color.BLACK);
    }

    private void onAddAccount() {
        String name = JOptionPane.showInputDialog(this, "أدخل اسم الحساب:", "حساب جديد", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;

        if (manager.nameExists(name.trim())) {
            JOptionPane.showMessageDialog(this, "اسم الحساب موجود مسبقاً.", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }
        manager.addAccount(name.trim());
        manager.saveQuietly(); // حفظ التغييرات
        ((AbstractTableModel) accountsTable.getModel()).fireTableDataChanged();

        int newIndex = manager.getAccounts().size() - 1;
        accountsTable.setRowSelectionInterval(newIndex, newIndex);
        accountsTable.scrollRectToVisible(accountsTable.getCellRect(newIndex, 0, true));
    }

    private void onRemoveAccount() {
        int row = accountsTable.getSelectedRow();
        if (row < 0) return;

        Account acc = manager.getAccounts().get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "هل تريد بالتأكيد إزالة الحساب \"" + acc.getName() + "\"؟",
                "تأكيد الحذف", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            manager.removeAccount(acc);
            manager.saveQuietly(); // حفظ التغييرات
            ((AbstractTableModel) accountsTable.getModel()).fireTableDataChanged();
        }
    }

    private void onAccountSelected(int row) {
        if (row < 0 || row >= manager.getAccounts().size()) {
            currentAccount = null;
            entriesModel.setEntries(null);
            addEntryBtn.setEnabled(false);
            removeAccountBtn.setEnabled(false);
            deleteEntryBtn.setEnabled(false);
        } else {
            currentAccount = manager.getAccounts().get(row);
            entriesModel.setEntries(currentAccount.getEntries());
            addEntryBtn.setEnabled(true);
            removeAccountBtn.setEnabled(true);
            deleteEntryBtn.setEnabled(false); // إعادة ضبط زر الحذف عند تغيير الحساب
        }
        updateBalanceLabel();
    }

    private void onAddEntry() {
        if (currentAccount == null) return;

        EntryDialog dlg = new EntryDialog(SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);

        if (dlg.isOk()) { // تم التصحيح إلى isOk()
            AccountEntry entry = dlg.getEntry();
            currentAccount.addEntry(entry);
            manager.saveQuietly(); // حفظ التغييرات
            entriesModel.fireTableDataChanged();
            updateBalanceLabel();
        }
    }

    private void onDeleteEntry() {
        int selectedRow = entriesTable.getSelectedRow();
        if (selectedRow < 0 || currentAccount == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, "هل تريد بالتأكيد حذف هذا القيد؟", "تأكيد الحذف", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            currentAccount.getEntries().remove(selectedRow);
            manager.saveQuietly(); // حفظ التغييرات
            entriesModel.fireTableDataChanged();
            updateBalanceLabel();
        }
    }

    private void updateBalanceLabel() {
        if (currentAccount == null) {
            balanceLabel.setText("الرصيد الحالي: 0.0 (متزن)");
        } else {
            double balance = currentAccount.getBalance();
            String side = currentAccount.getSide();
            balanceLabel.setText(String.format("الرصيد الحالي: %.2f (%s)", Math.abs(balance), side));
        }
    }
}