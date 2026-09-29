package frontEnd.account;

import backEnd.account.*;
import backEnd.sanitary.SanitaryService;
import backEnd.tile.TileService;
import backEnd.warehouse.WarehouseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AccountTabPanel extends JPanel {

    private final Account account;
    private final AccountsManager accountsManager;
    private final AccountInvoiceService invoiceService;

    private final TileService tileService;
    private final SanitaryService sanitaryService;
    private final WarehouseManager warehouseManager;

    private final CardLayout contentCards =
            new CardLayout();

    private final JPanel content =
            new JPanel(contentCards);

    private final AccountInvoicesTableModel invoicesModel;

    private final JTable invoicesTable;

    private final EntriesTableModel entriesModel =
            new EntriesTableModel(null);

    private final JTable entriesTable =
            new JTable(entriesModel);

    /*
     * Breadcrumb:
     *
     * اسم الحساب > رقم الفاتورة
     */
    private final JLabel breadcrumbCompany =
            new JLabel();

    private final JLabel breadcrumbArrow =
            new JLabel(" > ");

    private final JLabel breadcrumbInvoice =
            new JLabel();

    /*
     * رأس الحساب.
     */
    private final JLabel accountLabel =
            new JLabel();

    private final JLabel balanceLabel =
            new JLabel();

    private final JLabel invoiceTotalLabel =
            new JLabel();

    /*
     * أزرار الحساب.
     */
    private final JButton newDocumentButton =
            new JButton("سند جديد");

    private final JButton deleteDocumentButton =
            new JButton("إزالة سند");

    private final JButton addDescriptionButton =
            new JButton("إضافة بيان");
    /*
     * أزرار الفاتورة.
     */
    private final JButton addEntryButton =
            new JButton("إضافة بند");

    private final JButton deleteEntryButton =
            new JButton("حذف بند");

    private final JButton backButton =
            new JButton("العودة إلى الحساب");

    private AccountInvoice currentInvoice;

    public AccountTabPanel(
            Account account,
            AccountsManager accountsManager,
            TileService tileService,
            SanitaryService sanitaryService,
            WarehouseManager warehouseManager
    ) {

        this.account = account;
        this.accountsManager = accountsManager;
        this.tileService = tileService;
        this.sanitaryService = sanitaryService;
        this.warehouseManager = warehouseManager;

        this.invoiceService =
                new AccountInvoiceService(
                        accountsManager,
                        tileService,
                        sanitaryService
                );

        setLayout(
                new BorderLayout(
                        0,
                        8
                )
        );

        setBackground(
                new Color(
                        247,
                        248,
                        250
                )
        );

        setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );

        invoicesModel =
                new AccountInvoicesTableModel(
                        account.getAccountInvoices()
                );

        invoicesTable =
                new JTable(invoicesModel);

        styleTable(invoicesTable);
        installInvoiceRowColors();

        styleTable(entriesTable);

        add(
                buildBreadcrumb(),
                BorderLayout.NORTH
        );

        content.setOpaque(false);

        content.add(
                buildOverview(),
                "overview"
        );

        content.add(
                buildInvoiceDetails(),
                "details"
        );

        add(
                content,
                BorderLayout.CENTER
        );

        showOverview();
    }

    /*
     * ==========================================
     * Breadcrumb
     * ==========================================
     */

    private JPanel buildBreadcrumb() {

        JPanel panel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                3,
                                7
                        )
                );

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                new EmptyBorder(
                        3,
                        15,
                        3,
                        15
                )
        );

        /*
         * نريد:
         *
         * اسم الحساب > رقم الفاتورة
         *
         * من اليسار.
         */
        panel.setComponentOrientation(
                ComponentOrientation.LEFT_TO_RIGHT
        );

        breadcrumbCompany.setText(
                account.getName()
        );

        breadcrumbCompany.setFont(
                new Font(
                        "Tahoma",
                        Font.BOLD,
                        17
                )
        );

        breadcrumbCompany.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        breadcrumbCompany.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent e
                    ) {
                        showOverview();
                    }
                }
        );

        breadcrumbArrow.setFont(
                new Font(
                        "Tahoma",
                        Font.BOLD,
                        17
                )
        );

        breadcrumbArrow.setForeground(
                new Color(
                        125,
                        125,
                        125
                )
        );

        breadcrumbInvoice.setFont(
                new Font(
                        "Tahoma",
                        Font.PLAIN,
                        16
                )
        );

        breadcrumbInvoice.setForeground(
                new Color(
                        80,
                        80,
                        80
                )
        );

        panel.add(
                breadcrumbCompany
        );

        /*
         * السهم سيظهر فقط عندما نفتح فاتورة.
         */
        panel.add(
                breadcrumbArrow
        );

        panel.add(
                breadcrumbInvoice
        );

        return panel;
    }

    /*
     * ==========================================
     * واجهة الحساب
     * ==========================================
     */

    private JPanel buildOverview() {

        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        panel.setOpaque(false);

        panel.setBorder(
                new EmptyBorder(
                        8,
                        12,
                        8,
                        12
                )
        );

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(Color.WHITE);

        header.setBorder(
                new EmptyBorder(
                        10,
                        15,
                        10,
                        15
                )
        );

        /*
         * حساب : "اسم"
         *
         * في المنتصف.
         */
        accountLabel.setText(
                "حساب : " + account.getName()
        );

        accountLabel.setFont(
                new Font(
                        "Tahoma",
                        Font.BOLD,
                        22
                )
        );

        accountLabel.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        /*
         * الرصيد على اليسار.
         */
        balanceLabel.setFont(
                new Font(
                        "Tahoma",
                        Font.BOLD,
                        18
                )
        );

        balanceLabel.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        header.add(
                balanceLabel,
                BorderLayout.WEST
        );

        header.add(
                accountLabel,
                BorderLayout.CENTER
        );

        panel.add(
                header,
                BorderLayout.NORTH
        );

        /*
         * الجدول.
         */
        panel.add(
                new JScrollPane(
                        invoicesTable
                ),
                BorderLayout.CENTER
        );

        /*
         * الأزرار في المنتصف أسفل الجدول.
         */
        panel.add(
                buildOverviewButtons(),
                BorderLayout.SOUTH
        );

        /*
         * عند الضغط على سند:
         *
         * SALE / PURCHASE -> افتح
         *
         * PAYMENT / RECEIPT -> لا شيء
         */
        invoicesTable.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        // الفتح فقط بالضغط مرتين
                        if (e.getClickCount() != 2) {
                            return;
                        }

                        int viewRow =
                                invoicesTable.getSelectedRow();

                        if (viewRow < 0) {
                            return;
                        }

                        // تحويل صف العرض إلى صف الـ Model
                        int modelRow =
                                invoicesTable.convertRowIndexToModel(
                                        viewRow
                                );

                        if (
                                modelRow < 0
                                        || modelRow >= account
                                        .getAccountInvoices()
                                        .size()
                        ) {
                            return;
                        }

                        AccountInvoice invoice =
                                account
                                        .getAccountInvoices()
                                        .get(modelRow);

                        // PAYMENT و RECEIPT لا يفتحان
                        if (!invoice.isPurchase()
                                && !invoice.isSale()) {
                            return;
                        }

                        openInvoice(modelRow);
                    }
                }
        );

        newDocumentButton.addActionListener(
                e -> addInvoice()
        );

        deleteDocumentButton.addActionListener(
                e -> deleteSelectedInvoice()
        );

        addDescriptionButton.addActionListener(
                e -> addDescription()
        );

        return panel;
    }

    private void addDescription() {
        int viewRow = invoicesTable.getSelectedRow();

        if (viewRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "يرجى تحديد سند أولاً",
                    "تنبيه",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = invoicesTable.convertRowIndexToModel(viewRow);

        AccountInvoice invoice =
                account.getAccountInvoices().get(modelRow);

        String currentDescription = invoice.getDetails();

        String description = (String) JOptionPane.showInputDialog(
                this,
                "اكتب بيان السند:",
                "إضافة بيان",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                currentDescription == null ? "" : currentDescription
        );

        if (description == null) {
            return;
        }

        invoice.setDescription(description.trim());

        invoicesModel.fireTableDataChanged();
    }
    private JPanel buildOverviewButtons() {

        JPanel buttons =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                10,
                                6
                        )
                );

        buttons.setBackground(Color.WHITE);

        styleActionButton(
                newDocumentButton
        );

        styleActionButton(
                deleteDocumentButton
        );

        styleActionButton(
                addDescriptionButton
        );

        buttons.add(
                newDocumentButton
        );

        buttons.add(
                deleteDocumentButton
        );

        buttons.add(
                addDescriptionButton
        );


        return buttons;
    }

    /*
     * ==========================================
     * واجهة الفاتورة
     * ==========================================
     */

    private JPanel buildInvoiceDetails() {

        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        panel.setOpaque(false);

        panel.setBorder(
                new EmptyBorder(
                        8,
                        12,
                        8,
                        12
                )
        );

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(Color.WHITE);

        header.setBorder(
                new EmptyBorder(
                        10,
                        15,
                        10,
                        15
                )
        );

        JLabel title =
                new JLabel(
                        "تفاصيل الفاتورة"
                );

        title.setFont(
                new Font(
                        "Tahoma",
                        Font.BOLD,
                        18
                )
        );

        header.add(
                title,
                BorderLayout.EAST
        );

        invoiceTotalLabel.setFont(
                new Font(
                        "Tahoma",
                        Font.BOLD,
                        18
                )
        );

        invoiceTotalLabel.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        invoiceTotalLabel.setText(
                currentInvoice == null
                        ? "الإجمالي: 0.00"
                        : String.format(
                        "الإجمالي: %.2f",
                        currentInvoice.getTotalDebit()
                                + currentInvoice.getTotalCredit()
                )
        );

        header.add(
                invoiceTotalLabel,
                BorderLayout.WEST
        );

        panel.add(
                header,
                BorderLayout.NORTH
        );

        panel.add(
                new JScrollPane(
                        entriesTable
                ),
                BorderLayout.CENTER
        );

        /*
         * الأزرار:
         *
         * إضافة بند
         * حذف بند
         * العودة إلى الحساب
         */
        panel.add(
                buildDetailButtons(),
                BorderLayout.SOUTH
        );

        addEntryButton.addActionListener(
                e -> addEntry()
        );

        deleteEntryButton.addActionListener(
                e -> deleteSelectedEntry()
        );

        backButton.addActionListener(
                e -> showOverview()
        );

        return panel;
    }

    private JPanel buildDetailButtons() {

        JPanel buttons =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                10,
                                6
                        )
                );

        buttons.setBackground(Color.WHITE);

        styleActionButton(
                addEntryButton
        );

        styleActionButton(
                deleteEntryButton
        );

        styleActionButton(
                backButton
        );

        buttons.add(
                addEntryButton
        );

        buttons.add(
                deleteEntryButton
        );

        buttons.add(
                backButton
        );

        return buttons;
    }

    /*
     * ==========================================
     * إنشاء سند
     * ==========================================
     */

    private void addInvoice() {

        AccountInvoiceDialog dialog =
                new AccountInvoiceDialog(
                        SwingUtilities
                                .getWindowAncestor(this)
                );

        dialog.setVisible(true);

        if (!dialog.isOk()) {
            return;
        }

        AccountInvoice invoice;

        if (
                dialog.getInvoiceType()
                        == AccountInvoiceType.PAYMENT
        ) {

            invoice =
                    invoiceService.createPayment(
                            account,
                            dialog.getDate(),
                            dialog.getAmount(),
                            dialog.getDetails()
                    );

        } else if (
                dialog.getInvoiceType()
                        == AccountInvoiceType.RECEIPT
        ) {

            invoice =
                    invoiceService.createReceipt(
                            account,
                            dialog.getDate(),
                            dialog.getAmount(),
                            dialog.getDetails()
                    );

        } else {

            invoice =
                    invoiceService.createInvoice(
                            account,
                            dialog.getInvoiceType(),
                            dialog.getDate(),
                            dialog.getDetails()
                    );
        }

        refresh();

        /*
         * مهم:
         *
         * الدفع والقبض لا يفتحان.
         *
         * الشراء والمبيع يفتحان.
         */
        if (
                invoice.isPurchase()
                        || invoice.isSale()
        ) {

            openInvoice(
                    account.getAccountInvoices()
                            .indexOf(invoice)
            );
        }
    }

    /*
     * ==========================================
     * فتح الفاتورة
     * ==========================================
     */

    private void openInvoice(int row) {

        if (
                row < 0
                        || row >= account
                        .getAccountInvoices()
                        .size()
        ) {
            return;
        }

        AccountInvoice invoice =
                account
                        .getAccountInvoices()
                        .get(row);

        /*
         * الدفع والقبض لا يفتحان.
         */
        if (
                !invoice.isPurchase()
                        && !invoice.isSale()
        ) {
            return;
        }

        currentInvoice = invoice;

        breadcrumbInvoice.setText(
                currentInvoice.getNumber()
        );

        updateDetails();

        contentCards.show(
                content,
                "details"
        );
    }

    /*
     * ==========================================
     * إضافة بند
     * ==========================================
     */

    private void addEntry() {

        if (currentInvoice == null) {
            return;
        }

        try {

            if (
                    currentInvoice.getType()
                            == AccountInvoiceType.PURCHASE
            ) {

                PurchaseEntryDialog dialog =
                        new PurchaseEntryDialog(
                                SwingUtilities
                                        .getWindowAncestor(this),
                                tileService,
                                sanitaryService,
                                warehouseManager
                        );

                dialog.setVisible(true);

                if (!dialog.isOk()) {
                    return;
                }

                PurchaseEntryDialog.PurchaseData d =
                        dialog.getData();

                if (
                        d.movement
                                == AccountStockMovementType
                                .NOT_IN_STOCK
                ) {

                    invoiceService
                            .addPurchaseNotInStock(
                                    currentInvoice,
                                    d.kind,
                                    d.material,
                                    d.quantity1,
                                    d.unit,
                                    d.price,
                                    d.quantity2
                            );

                } else if (
                        d.movement
                                == AccountStockMovementType
                                .EXISTING_ITEM
                ) {

                    if (
                            d.kind
                                    == AccountInventoryKind.TILE
                    ) {

                        invoiceService
                                .addPurchaseExistingTile(
                                        currentInvoice,
                                        d.tileItem,
                                        d.code,
                                        d.name,
                                        d.quantity2,
                                        d.price
                                );

                    } else {

                        invoiceService
                                .addPurchaseExistingSanitary(
                                        currentInvoice,
                                        d.sanitaryItem,
                                        d.quantity1,
                                        d.unit,
                                        d.price
                                );
                    }

                } else {

                    if (
                            d.kind
                                    == AccountInventoryKind.TILE
                    ) {

                        invoiceService
                                .addPurchaseNewTile(
                                        currentInvoice,
                                        d.code,
                                        d.name,
                                        d.grade,
                                        d.boxArea,
                                        d.quantity2,
                                        d.tileMaterialType,
                                        d.tileSubtype,
                                        d.warehouse,
                                        d.location,
                                        d.price
                                );

                    } else {

                        if (
                                d.quantity1
                                        != Math.rint(
                                        d.quantity1
                                )
                        ) {

                            throw new IllegalArgumentException(
                                    "كمية الأدوات الصحية يجب أن تكون عدداً صحيحاً"
                            );
                        }

                        invoiceService
                                .addPurchaseNewSanitary(
                                        currentInvoice,
                                        d.name,
                                        d.price,
                                        (int) d.quantity1,
                                        d.sanitaryType,
                                        d.warehouse,
                                        d.grade
                                );
                    }
                }

            } else if (
                    currentInvoice.getType()
                            == AccountInvoiceType.SALE
            ) {

                EntryDialog dialog =
                        new EntryDialog(
                                SwingUtilities
                                        .getWindowAncestor(this)
                        );

                dialog.setVisible(true);

                if (!dialog.isOk()) {
                    return;
                }

                AccountEntry entry =
                        dialog.getEntry();

                invoiceService.addSaleEntry(
                        currentInvoice,
                        entry.getMaterial(),
                        entry.getQuantity1(),
                        entry.getUnit(),
                        entry.getPrice(),
                        entry.getQuantity2()
                );
            }

            refresh();

            updateDetails();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage() == null
                            ? "تعذر إضافة البند"
                            : ex.getMessage(),
                    "تنبيه",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /*
     * ==========================================
     * حذف بند
     * ==========================================
     */

    private void deleteSelectedEntry() {

        if (currentInvoice == null) {
            return;
        }

        int row =
                entriesTable.getSelectedRow();

        if (
                row < 0
                        || row >= currentInvoice
                        .getEntries()
                        .size()
        ) {
            return;
        }

        int answer =
                JOptionPane.showConfirmDialog(
                        this,
                        "هل تريد حذف البند المحدد؟",
                        "تأكيد الحذف",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (
                answer
                        != JOptionPane.YES_OPTION
        ) {
            return;
        }

        try {

            invoiceService.deleteEntry(
                    currentInvoice,
                    row
            );

            refresh();

            updateDetails();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage() == null
                            ? "تعذر حذف البند"
                            : ex.getMessage(),
                    "تنبيه",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /*
     * ==========================================
     * حذف سند
     * ==========================================
     */

    private void deleteSelectedInvoice() {

        int viewRow =
                invoicesTable.getSelectedRow();

        if (viewRow < 0) {
            return;
        }

        int modelRow =
                invoicesTable.convertRowIndexToModel(
                        viewRow
                );

        if (
                modelRow < 0
                        || modelRow >= account
                        .getAccountInvoices()
                        .size()
        ) {
            return;
        }

        deleteInvoice(
                account
                        .getAccountInvoices()
                        .get(modelRow)
        );
    }

    private void deleteInvoice(
            AccountInvoice invoice
    ) {

        int answer =
                JOptionPane.showConfirmDialog(
                        this,
                        "هل تريد إزالة السند "
                                + invoice.getNumber()
                                + "؟",
                        "تأكيد الحذف",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (
                answer
                        != JOptionPane.YES_OPTION
        ) {
            return;
        }

        invoiceService.deleteInvoice(
                account,
                invoice
        );

        currentInvoice = null;

        showOverview();
    }

    /*
     * ==========================================
     * تحديث
     * ==========================================
     */

    public void showOverview() {

        currentInvoice = null;

        breadcrumbInvoice.setText("");
        invoiceTotalLabel.setText("الإجمالي: 0.00");

        contentCards.show(
                content,
                "overview"
        );

        refresh();
    }

    public void refresh() {

        invoicesModel.fireTableDataChanged();

        updateBalance();

        if (currentInvoice != null) {
            updateDetails();
        }
    }

    private void updateDetails() {

        if (currentInvoice == null) {
            return;
        }

        entriesModel.setEntries(
                currentInvoice.getEntries()
        );

        invoiceTotalLabel.setText(
                String.format(
                        "الإجمالي: %.2f",
                        currentInvoice.getTotalDebit()
                                + currentInvoice.getTotalCredit()
                )
        );
    }

    private void updateBalance() {

        balanceLabel.setText(
                String.format(
                        "الرصيد: %.2f   (%s)",
                        account.getBalance(),
                        account.getSide()
                )
        );
    }

    /*
     * ==========================================
     * شكل الأزرار
     * ==========================================
     */

    private void styleActionButton(
            JButton button
    ) {

        button.setFont(
                new Font(
                        "Tahoma",
                        Font.BOLD,
                        17
                )
        );

        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );
    }


    /*
     * ==========================================
     * شكل الجداول
     *
     * قريب من أسلوب جدول البلاط.
     * ==========================================
     */

    private void styleTable(JTable table) {

        table.setFont(
                new Font(
                        "Tahoma",
                        Font.PLAIN,
                        18
                )
        );

        table.setRowHeight(36);

        table.setAutoCreateRowSorter(true);

        table.setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

// لون الصف عند تحديده
        table.setSelectionBackground(
                new Color(180, 180, 180) // رمادي غامق
        );

        table.setSelectionForeground(
                Color.BLACK
        );


        table.setShowVerticalLines(false);

        table.setGridColor(
                new Color(
                        225,
                        225,
                        225
                )
        );

        table.setFillsViewportHeight(true);

        /*
         * جميع محتويات الخلايا في المنتصف.
         */
        DefaultTableCellRenderer centerRenderer =
                new DefaultTableCellRenderer();

        centerRenderer.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        table.setDefaultRenderer(
                Object.class,
                centerRenderer
        );

        table.setDefaultRenderer(
                String.class,
                centerRenderer
        );

        /*
         * تنسيق الأرقام في واجهة الحسابات فقط.
         *
         * يمنع ظهور فواصل الآلاف:
         *
         * 1000.00
         * بدل:
         * 1,000.00
         */
        DefaultTableCellRenderer doubleRenderer =
                new DefaultTableCellRenderer() {

                    @Override
                    protected void setValue(Object value) {

                        setHorizontalAlignment(
                                SwingConstants.CENTER
                        );

                        if (value instanceof Number) {

                            setText(
                                    String.format(
                                            java.util.Locale.US,
                                            "%.2f",
                                            ((Number) value).doubleValue()
                                    )
                            );

                        } else {

                            setText(
                                    value == null
                                            ? ""
                                            : value.toString()
                            );
                        }
                    }
                };

        table.setDefaultRenderer(
                Double.class,
                doubleRenderer
        );

        table.setDefaultRenderer(
                Integer.class,
                centerRenderer
        );

        table.setDefaultRenderer(
                Long.class,
                centerRenderer
        );
    }
    private void installInvoiceRowColors() {

        invoicesTable.setDefaultRenderer(
                Object.class,
                new InvoiceRowColorRenderer()
        );

        invoicesTable.setDefaultRenderer(
                String.class,
                new InvoiceRowColorRenderer()
        );

        invoicesTable.setDefaultRenderer(
                Double.class,
                new InvoiceRowColorRenderer()
        );
    }

    private class InvoiceRowColorRenderer
            extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {

            Component component =
                    super.getTableCellRendererComponent(
                            table,
                            value,
                            isSelected,
                            hasFocus,
                            row,
                            column
                    );

            setHorizontalAlignment(SwingConstants.CENTER);

            int modelRow =
                    table.convertRowIndexToModel(row);

            AccountInvoice invoice =
                    modelRow >= 0
                            && modelRow < account.getAccountInvoices().size()
                            ? account.getAccountInvoices().get(modelRow)
                            : null;

            Color foreground = Color.BLACK;

            if (invoice != null) {
                switch (invoice.getType()) {
                    case PAYMENT ->
                            foreground = new Color(0, 102, 204);
                    case RECEIPT ->
                            foreground = new Color(0, 128, 0);
                    case SALE ->
                            foreground = new Color(75, 0, 130);
                    case PURCHASE ->
                            foreground = Color.BLACK;
                }
            }

            setForeground(foreground);

            return component;
        }
    }

    private void installColumnResizeBehavior(
            JTable table
    ) {

        JTableHeader header =
                table.getTableHeader();

        final int[] startX = { -1 };
        final int[] startColumn = { -1 };

        header.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mousePressed(
                            MouseEvent e
                    ) {

                        startX[0] = e.getX();

                        startColumn[0] =
                                header.columnAtPoint(e.getPoint());
                    }

                    @Override
                    public void mouseReleased(
                            MouseEvent e
                    ) {

                        startX[0] = -1;
                        startColumn[0] = -1;

                        /*
                         * نعيد الوضع الطبيعي.
                         */
                        table.setAutoResizeMode(
                                JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS
                        );
                    }
                }
        );

        header.addMouseMotionListener(
                new MouseAdapter() {

                    @Override
                    public void mouseDragged(
                            MouseEvent e
                    ) {

                        if (startX[0] < 0) {
                            return;
                        }

                        int delta =
                                e.getX() - startX[0];

                        /*
                         * السحب إلى اليمين:
                         *
                         * نريد أن تتغير الأعمدة الأخرى.
                         */
                        if (delta > 0) {

                            table.setAutoResizeMode(
                                    JTable.AUTO_RESIZE_ALL_COLUMNS
                            );

                            /*
                             * السحب إلى اليسار:
                             *
                             * نريد أن يبقى تغيير العرض
                             * على العمود الذي يتم سحبه.
                             */
                        } else if (delta < 0) {

                            table.setAutoResizeMode(
                                    JTable.AUTO_RESIZE_OFF
                            );
                        }
                    }
                }
        );
    }
}