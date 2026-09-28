package frontEnd.account;

import backEnd.account.*;
import backEnd.sanitary.SanitaryService;
import backEnd.tile.TileService;
import backEnd.warehouse.WarehouseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AccountTabPanel extends JPanel {

    private final Account account;
    private final AccountsManager accountsManager;
    private final AccountInvoiceService invoiceService;
    private final TileService tileService;
    private final SanitaryService sanitaryService;
    private final WarehouseManager warehouseManager;

    private final CardLayout contentCards = new CardLayout();
    private final JPanel content = new JPanel(contentCards);

    private final AccountInvoicesTableModel invoicesModel;
    private final JTable invoicesTable;
    private final EntriesTableModel entriesModel = new EntriesTableModel(null);
    private final JTable entriesTable = new JTable(entriesModel);

    private final JLabel breadcrumbCompany = new JLabel();
    private final JLabel breadcrumbArrow = new JLabel("  >  ");
    private final JLabel breadcrumbInvoice = new JLabel();
    private final JLabel balanceLabel = new JLabel();
    private final JLabel detailHeader = new JLabel();
    private final JLabel detailSummary = new JLabel();

    private AccountInvoice currentInvoice;

    public AccountTabPanel(Account account,
                           AccountsManager accountsManager,
                           TileService tileService,
                           SanitaryService sanitaryService,
                           WarehouseManager warehouseManager) {
        this.account = account;
        this.accountsManager = accountsManager;
        this.tileService = tileService;
        this.sanitaryService = sanitaryService;
        this.warehouseManager = warehouseManager;
        this.invoiceService = new AccountInvoiceService(accountsManager, tileService, sanitaryService);

        setLayout(new BorderLayout(0, 8));
        setBackground(new Color(247, 248, 250));
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        invoicesModel = new AccountInvoicesTableModel(account.getAccountInvoices());
        invoicesTable = new JTable(invoicesModel);
        styleTable(invoicesTable);
        styleTable(entriesTable);

        add(buildBreadcrumb(), BorderLayout.NORTH);
        content.add(buildOverview(), "overview");
        content.add(buildInvoiceDetails(), "details");
        add(content, BorderLayout.CENTER);
        showOverview();
    }

    private JPanel buildBreadcrumb() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(3, 15, 3, 15));

        breadcrumbCompany.setText(account.getName());
        breadcrumbCompany.setFont(new Font("Tahoma", Font.BOLD, 17));
        breadcrumbCompany.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        breadcrumbCompany.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { showOverview(); }
        });

        breadcrumbInvoice.setFont(new Font("Tahoma", Font.PLAIN, 16));
        breadcrumbInvoice.setForeground(new Color(80, 80, 80));
        panel.add(breadcrumbCompany);
        panel.add(breadcrumbArrow);
        panel.add(breadcrumbInvoice);
        return panel;
    }

    private JPanel buildOverview() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 15, 15, 15));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(new EmptyBorder(12, 15, 12, 15));
        JLabel title = new JLabel("حساب الشركة: " + account.getName());
        title.setFont(new Font("Tahoma", Font.BOLD, 22));
        top.add(title, BorderLayout.WEST);
        top.add(balanceLabel, BorderLayout.EAST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        actions.setBackground(Color.WHITE);
        JButton addInvoice = new JButton("سند جديد");
        JButton deleteInvoice = new JButton("حذف السند");
        actions.add(addInvoice);
        actions.add(deleteInvoice);
        top.add(actions, BorderLayout.SOUTH);

        addInvoice.addActionListener(e -> addInvoice());
        deleteInvoice.addActionListener(e -> deleteSelectedInvoice());

        invoicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoicesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && invoicesTable.getSelectedRow() >= 0) openInvoice(invoicesTable.getSelectedRow());
            }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(invoicesTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildInvoiceDetails() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 15, 15, 15));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(12, 15, 12, 15));
        detailHeader.setFont(new Font("Tahoma", Font.BOLD, 20));
        detailSummary.setFont(new Font("Tahoma", Font.PLAIN, 15));
        header.add(detailHeader, BorderLayout.WEST);
        header.add(detailSummary, BorderLayout.EAST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        actions.setBackground(Color.WHITE);
        JButton addEntry = new JButton("إضافة بند");
        JButton deleteInvoice = new JButton("حذف السند");
        JButton back = new JButton("العودة إلى الحساب");
        actions.add(addEntry);
        actions.add(deleteInvoice);
        actions.add(back);
        header.add(actions, BorderLayout.SOUTH);

        addEntry.addActionListener(e -> addEntry());
        deleteInvoice.addActionListener(e -> deleteCurrentInvoice());
        back.addActionListener(e -> showOverview());

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(entriesTable), BorderLayout.CENTER);
        return panel;
    }

    private void addInvoice() {
        AccountInvoiceDialog dialog = new AccountInvoiceDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (!dialog.isOk()) return;

        AccountInvoice invoice;
        if (dialog.getInvoiceType() == AccountInvoiceType.PAYMENT) {
            invoice = invoiceService.createPayment(account, dialog.getDate(), dialog.getAmount(), dialog.getDetails());
        } else if (dialog.getInvoiceType() == AccountInvoiceType.RECEIPT) {
            invoice = invoiceService.createReceipt(account, dialog.getDate(), dialog.getAmount(), dialog.getDetails());
        } else {
            invoice = invoiceService.createInvoice(account, dialog.getInvoiceType(), dialog.getDate(), dialog.getDetails());
        }

        refresh();
        openInvoice(account.getAccountInvoices().indexOf(invoice));
    }

    private void addEntry() {
        if (currentInvoice == null) return;

        try {
            if (currentInvoice.getType() == AccountInvoiceType.PURCHASE) {
                PurchaseEntryDialog dialog = new PurchaseEntryDialog(
                        SwingUtilities.getWindowAncestor(this),
                        tileService, sanitaryService, warehouseManager);
                dialog.setVisible(true);
                if (!dialog.isOk()) return;
                PurchaseEntryDialog.PurchaseData d = dialog.getData();

                if (d.movement == AccountStockMovementType.NOT_IN_STOCK) {
                    invoiceService.addPurchaseNotInStock(currentInvoice, d.kind, d.material,
                            d.quantity1, d.unit, d.price, d.quantity2);
                } else if (d.movement == AccountStockMovementType.EXISTING_ITEM) {
                    if (d.kind == AccountInventoryKind.TILE) {
                        invoiceService.addPurchaseExistingTile(currentInvoice, d.tileItem, d.quantity2, d.price);
                    } else {
                        invoiceService.addPurchaseExistingSanitary(currentInvoice, d.sanitaryItem,
                                d.quantity1, d.unit, d.price);
                    }
                } else {
                    if (d.kind == AccountInventoryKind.TILE) {
                        invoiceService.addPurchaseNewTile(currentInvoice, d.code, d.name, d.grade,
                                d.boxArea, d.quantity2, d.tileMaterialType, d.tileSubtype,
                                d.warehouse, d.location, d.price);
                    } else {
                        if (d.quantity1 != Math.rint(d.quantity1))
                            throw new IllegalArgumentException("كمية الأدوات الصحية يجب أن تكون عدداً صحيحاً");
                        invoiceService.addPurchaseNewSanitary(currentInvoice, d.name, d.price,
                                (int) d.quantity1, d.sanitaryType, d.warehouse, d.grade);
                    }
                }
            } else if (currentInvoice.getType() == AccountInvoiceType.SALE) {
                EntryDialog dialog = new EntryDialog(SwingUtilities.getWindowAncestor(this));
                dialog.setVisible(true);
                if (!dialog.isOk()) return;
                currentInvoice.addEntry(dialog.getEntry());
                accountsManager.saveQuietly();
            } else {
                return;
            }
            refresh();
            updateDetails();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage() == null ? "تعذر إضافة البند" : ex.getMessage(),
                    "تنبيه", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedInvoice() {
        int row = invoicesTable.getSelectedRow();
        if (row < 0) return;
        AccountInvoice invoice = account.getAccountInvoices().get(row);
        deleteInvoice(invoice);
    }

    private void deleteCurrentInvoice() {
        if (currentInvoice != null) deleteInvoice(currentInvoice);
    }

    private void deleteInvoice(AccountInvoice invoice) {
        int answer = JOptionPane.showConfirmDialog(this,
                "هل تريد حذف السند " + invoice.getNumber() + "؟",
                "تأكيد الحذف", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;

        invoiceService.deleteInvoice(account, invoice);
        currentInvoice = null;
        refresh();
        showOverview();
    }

    private void openInvoice(int row) {
        if (row < 0 || row >= account.getAccountInvoices().size()) return;
        currentInvoice = account.getAccountInvoices().get(row);
        breadcrumbInvoice.setText(currentInvoice.getNumber());
        updateDetails();
        contentCards.show(content, "details");
    }

    private void updateDetails() {
        if (currentInvoice == null) return;
        entriesModel.setEntries(currentInvoice.getEntries());
        detailHeader.setText(currentInvoice.getNumber() + " - " + currentInvoice.getType().getArabicName());
        detailSummary.setText(String.format("مدين: %.2f   |   دائن: %.2f   |   الرصيد: %.2f",
                currentInvoice.getTotalDebit(), currentInvoice.getTotalCredit(), currentInvoice.getTotal()));
    }

    public void showOverview() {
        currentInvoice = null;
        breadcrumbInvoice.setText("");
        contentCards.show(content, "overview");
        refresh();
    }

    public void refresh() {
        invoicesModel.fireTableDataChanged();
        updateBalance();
        if (currentInvoice != null) updateDetails();
    }

    private void updateBalance() {
        balanceLabel.setText(String.format("الرصيد: %.2f   (%s)", account.getBalance(), account.getSide()));
        balanceLabel.setFont(new Font("Tahoma", Font.BOLD, 17));
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Tahoma", Font.PLAIN, 15));
        table.setRowHeight(32);
        table.setAutoCreateRowSorter(true);
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 14));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
}
