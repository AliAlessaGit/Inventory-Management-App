import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class AccountsPanel extends JPanel {

    private final AccountsManager manager;

    private JTable accountsTable;
    private AccountsTableModel accountsModel;

    private JTable invoicesTable;
    private InvoicesTableModel invoicesModel;

    private JTable entriesTable;
    private EntriesTableModel entriesModel;

    private JLabel balanceLabel;
    private JButton addEntryBtn;
    private JButton deleteEntryBtn;
    private JButton removeAccountBtn;

    private Account currentAccount;
    private AccountInvoice currentInvoice;

    public AccountsPanel(AccountsManager manager) {
        this.manager = manager;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildAccountsPanel(), BorderLayout.EAST);
        add(buildCenterPanel(), BorderLayout.CENTER);
    }

    // ================= الحسابات =================

    private JPanel buildAccountsPanel() {
        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setPreferredSize(new Dimension(280, 0));
        right.setBorder(new LineBorder(Color.BLACK));
        right.setBackground(Color.WHITE);

        JButton addBtn = new JButton("إضافة حساب");
        removeAccountBtn = new JButton("حذف حساب");
        removeAccountBtn.setEnabled(false);

        JPanel top = new JPanel(new GridLayout(2, 1, 5, 5));
        top.setBackground(Color.WHITE);
        top.add(addBtn);
        top.add(removeAccountBtn);

        accountsModel = new AccountsTableModel(manager.getAccounts());
        accountsTable = new JTable(accountsModel);
        styleTable(accountsTable);

        accountsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                onAccountSelected(accountsTable.getSelectedRow());
        });

        addBtn.addActionListener(e -> onAddAccount());
        removeAccountBtn.addActionListener(e -> onRemoveAccount());

        right.add(top, BorderLayout.NORTH);
        right.add(new JScrollPane(accountsTable), BorderLayout.CENTER);
        return right;
    }

    // ================= الوسط =================

    private JPanel buildCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBorder(new LineBorder(Color.BLACK));
        center.setBackground(Color.WHITE);

        // ===== جدول الفواتير =====
        invoicesModel = new InvoicesTableModel(null);
        invoicesTable = new JTable(invoicesModel);
        styleTable(invoicesTable);

        invoicesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                onInvoiceSelected(invoicesTable.getSelectedRow());
        });

        // ===== جدول القيود =====
        entriesModel = new EntriesTableModel(null);
        entriesTable = new JTable(entriesModel);
        styleTable(entriesTable);

        DefaultTableCellRenderer bold = new DefaultTableCellRenderer();
        bold.setFont(bold.getFont().deriveFont(Font.BOLD));
        entriesTable.getColumnModel().getColumn(3).setCellRenderer(bold);

        // ===== الأزرار =====
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setBackground(Color.WHITE);

        addEntryBtn = new JButton("إضافة قيد");
        deleteEntryBtn = new JButton("حذف قيد");

        addEntryBtn.setEnabled(false);
        deleteEntryBtn.setEnabled(false);

        topBar.add(addEntryBtn);
        topBar.add(deleteEntryBtn);

        addEntryBtn.addActionListener(e -> onAddEntry());
        deleteEntryBtn.addActionListener(e -> onDeleteEntry());

        entriesTable.getSelectionModel().addListSelectionListener(e ->
                deleteEntryBtn.setEnabled(entriesTable.getSelectedRow() >= 0)
        );

        balanceLabel = new JLabel("الرصيد الحالي: 0.00 (متزن)");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Color.WHITE);
        bottom.add(balanceLabel);

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(invoicesTable),
                new JScrollPane(entriesTable)
        );
        split.setResizeWeight(0.35);

        center.add(topBar, BorderLayout.NORTH);
        center.add(split, BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);
        return center;
    }

    // ================= الأحداث =================

    private void onAccountSelected(int row) {
        if (row < 0) {
            currentAccount = null;
            invoicesModel.setInvoices(null);
            entriesModel.setEntries(null);
            return;
        }
        currentAccount = manager.getAccounts().get(row);
        invoicesModel.setInvoices(currentAccount.getAccountInvoices());
        entriesModel.setEntries(null);
        removeAccountBtn.setEnabled(true);
        updateBalance();
    }

    private void onInvoiceSelected(int row) {
        if (row < 0 || currentAccount == null) {
            currentInvoice = null;
            entriesModel.setEntries(null);
            addEntryBtn.setEnabled(false);
            return;
        }
        currentInvoice = currentAccount.getAccountInvoices().get(row);
        entriesModel.setEntries(currentInvoice.getEntries());
        addEntryBtn.setEnabled(true);
    }

    private void onAddEntry() {
        if (currentInvoice == null) return;

        EntryDialog dlg = new EntryDialog(SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);

        if (dlg.isOk()) {
            currentInvoice.addEntry(dlg.getEntry());
            manager.saveQuietly();
            entriesModel.fireTableDataChanged();
            invoicesModel.fireTableDataChanged();
            updateBalance();
        }
    }

    private void onDeleteEntry() {
        int r = entriesTable.getSelectedRow();
        if (r < 0 || currentInvoice == null) return;

        if (JOptionPane.showConfirmDialog(
                this,
                "هل تريد حذف هذا القيد؟",
                "تأكيد",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            currentInvoice.getEntries().remove(r);
            manager.saveQuietly();
            entriesModel.fireTableDataChanged();
            invoicesModel.fireTableDataChanged();
            updateBalance();
        }
    }

    // ================= أدوات =================

    private void onAddAccount() {
        String name = JOptionPane.showInputDialog(this, "اسم الحساب:");
        if (name == null || name.trim().isEmpty()) return;
        manager.addAccount(name.trim());
        manager.saveQuietly();
        accountsModel.fireTableDataChanged();
    }

    private void onRemoveAccount() {
        int r = accountsTable.getSelectedRow();
        if (r < 0) return;
        manager.removeAccount(manager.getAccounts().get(r));
        manager.saveQuietly();
        accountsModel.fireTableDataChanged();
    }

    private void updateBalance() {
        if (currentAccount == null) return;
        balanceLabel.setText(String.format(
                "الرصيد الحالي: %.2f (%s)",
                Math.abs(currentAccount.getBalance()),
                currentAccount.getSide()
        ));
    }

    private void styleTable(JTable t) {
        t.setRowHeight(28);
        t.setGridColor(Color.BLACK);
        t.setShowGrid(true);
        t.setSelectionBackground(new Color(230, 230, 230));
    }
}
