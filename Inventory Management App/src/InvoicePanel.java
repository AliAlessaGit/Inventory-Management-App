import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class InvoicePanel extends JPanel {
    private static final double EPS = 1e-6;

    private final InvoiceService invoiceService;
    private final TileService tileService;
    private final SanitaryService sanitaryService;

    private JTable invoiceTable;
    private DefaultTableModel invoiceTableModel;
    private List<Invoice> currentInvoices;
    private JLabel totalSumLabel;
    private JTextField searchNameField;
    private JButton dateFilterBtn, searchBtn, refreshBtn, viewBtn, deleteBtn;

    private final JPanel lockedButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

    public InvoicePanel(InvoiceService invoiceService, TileService tileService, SanitaryService sanitaryService) {
        this.invoiceService = invoiceService;
        this.tileService = tileService;
        this.sanitaryService = sanitaryService;
        this.currentInvoices = new ArrayList<>();

        setLayout(new BorderLayout(5, 5));
        initUI();
        setupKeyboardNavigation();
    }

    private void initUI() {

        JPanel north = new JPanel(new BorderLayout(5, 5));
        north.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        dateFilterBtn = new JButton("تصفية بالتاريخ");
        north.add(dateFilterBtn, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchNameField = new JTextField(20);
        searchBtn = new JButton("بحث");

        searchPanel.add(searchBtn);
        searchPanel.add(searchNameField);
        searchPanel.add(new JLabel("بحث بالاسم أو الرقم:"));

        north.add(searchPanel, BorderLayout.EAST);

        add(north, BorderLayout.NORTH);

        String[] cols = {"رقم الفاتورة", "اسم العميل", "الهاتف", "التاريخ", "الإجمالي"};
        invoiceTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        invoiceTable = new JTable(invoiceTableModel);
        invoiceTable.setRowHeight(28);
        invoiceTable.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(invoiceTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        JPanel allButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        refreshBtn = new JButton("تحديث");
        viewBtn = new JButton("عرض الفاتورة");
        deleteBtn = new JButton("حذف الفاتورة");

        lockedButtonsPanel.add(viewBtn);
        lockedButtonsPanel.add(deleteBtn);

        allButtonsPanel.add(refreshBtn);
        allButtonsPanel.add(lockedButtonsPanel);

        totalSumLabel = new JLabel("إجمالي الفواتير: 0.00");
        totalSumLabel.setFont(totalSumLabel.getFont().deriveFont(Font.BOLD, 16f));
        south.add(allButtonsPanel, BorderLayout.WEST);
        south.add(totalSumLabel, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> refreshTable(null));
        dateFilterBtn.addActionListener(e -> showDateFilterDialog());
        searchBtn.addActionListener(e -> applyNameOrIdSearch());
        searchNameField.addActionListener(e -> applyNameOrIdSearch());
        viewBtn.addActionListener(e -> viewSelectedInvoice());
        deleteBtn.addActionListener(e -> deleteSelectedInvoice());

        refreshTable(null);
    }

    private void showDateFilterDialog() {
        JTextField fromDay = new JTextField(2);
        JTextField fromMonth = new JTextField(2);
        JTextField fromYear = new JTextField(2);
        JTextField toDay = new JTextField(2);
        JTextField toMonth = new JTextField(2);
        JTextField toYear = new JTextField(2);

        JPanel panel = new JPanel(new GridLayout(2, 7, 5, 5));

        panel.add(fromYear);  panel.add(new JLabel("سنة"));
        panel.add(fromMonth);      panel.add(new JLabel("شهر"));
        panel.add(fromDay);        panel.add(new JLabel("من:            يوم"));

        panel.add(new JLabel(""));

        panel.add(toYear);        panel.add(new JLabel("سنة"));
        panel.add(toMonth);        panel.add(new JLabel("شهر"));
        panel.add(toDay);        panel.add(new JLabel("إلى:            يوم"));

        int result = JOptionPane.showConfirmDialog(this, panel, "تصفية حسب التاريخ",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int fd = Integer.parseInt(fromDay.getText().trim());
                int fm = Integer.parseInt(fromMonth.getText().trim());
                int fy = 2000 + Integer.parseInt(fromYear.getText().trim());
                int td = Integer.parseInt(toDay.getText().trim());
                int tm = Integer.parseInt(toMonth.getText().trim());
                int ty = 2000 + Integer.parseInt(toYear.getText().trim());

                LocalDateTime from = LocalDate.of(fy, fm, fd).atStartOfDay();
                LocalDateTime to = LocalDate.of(ty, tm, td).atTime(23, 59, 59);

                refreshTable(invoiceService.filterByDate(from, to));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "صيغة التاريخ غير صحيحة", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyNameOrIdSearch() {
        String query = searchNameField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable(null);
        } else {
            List<Invoice> filtered = invoiceService.getAll().stream()
                    .filter(inv -> inv.getCustomerName().toLowerCase().contains(query)
                            || inv.getInvoiceNumber().toLowerCase().contains(query))
                    .toList();
            refreshTable(filtered);
        }
    }

    private void refreshTable(List<Invoice> list) {
        String selectedInvoiceId = null;
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow != -1 && selectedRow < currentInvoices.size()) {
            selectedInvoiceId = currentInvoices.get(selectedRow).getInvoiceNumber();
        }

        invoiceTableModel.setRowCount(0);
        List<Invoice> invoicesToDisplay = (list == null)
                ? new ArrayList<>(invoiceService.getAll())
                : new ArrayList<>(list);

        invoicesToDisplay.sort(Comparator.comparing(Invoice::getDate).reversed());
        this.currentInvoices = invoicesToDisplay;

        double sum = 0;
        int newSelectedRow = -1;
        for (int i = 0; i < currentInvoices.size(); i++) {
            Invoice inv = currentInvoices.get(i);
            invoiceTableModel.addRow(new Object[]{
                    inv.getInvoiceNumber(), inv.getCustomerName(), inv.getCustomerPhone(),
                    inv.getFormattedDate(), String.format("%.2f", inv.getTotal())
            });
            sum += inv.getTotal();
            if (selectedInvoiceId != null && inv.getInvoiceNumber().equals(selectedInvoiceId)) {
                newSelectedRow = i;
            }
        }
        totalSumLabel.setText(String.format("إجمالي الفواتير: %.2f", sum));

        if (newSelectedRow != -1) {
            invoiceTable.setRowSelectionInterval(newSelectedRow, newSelectedRow);
        }
    }

    private void viewSelectedInvoice() {
        int row = invoiceTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "الرجاء تحديد فاتورة أولاً.", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Invoice inv = currentInvoices.get(row);

        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "تفاصيل الفاتورة رقم: " + inv.getInvoiceNumber(), true);
        dlg.setLayout(new BorderLayout(5, 5));
        dlg.setResizable(true);
        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel hdr = new JPanel(new GridLayout(2, 1));
        hdr.add(new JLabel("  اسم العميل: " + inv.getCustomerName()));
        hdr.add(new JLabel("  الهاتف: " + inv.getCustomerPhone()));
        topPanel.add(hdr, BorderLayout.CENTER);

        JCheckBox showDetailsCheck = new JCheckBox("إظهار تفاصيل المرتجعات", true);
        topPanel.add(showDetailsCheck, BorderLayout.SOUTH);
        dlg.add(topPanel, BorderLayout.NORTH);

        String[] tCols = {"الرمز", "الاسم", "النخب", "عدد الصناديق", "مساحة الصندوق", "المساحة الإجمالية", "السعر", "الإجمالي", "التاريخ"};
        DefaultTableModel tModel = new DefaultTableModel(tCols, 0);
        DefaultTableModel tModelNoDetails = new DefaultTableModel(tCols, 0);
        JTable tilesTable = new JTable(tModel);
        JTable tilesTableNoDetails = new JTable(tModelNoDetails);

        String[] sCols = {"الاسم", "النخب", "الكمية", "السعر", "الإجمالي", "التاريخ"};
        DefaultTableModel sModel = new DefaultTableModel(sCols, 0);
        DefaultTableModel sModelNoDetails = new DefaultTableModel(sCols, 0);
        JTable sanitaryTable = new JTable(sModel);
        JTable sanitaryTableNoDetails = new JTable(sModelNoDetails);

        for (JTable table : new JTable[]{tilesTable, tilesTableNoDetails, sanitaryTable, sanitaryTableNoDetails}) {
            table.setRowHeight(24);
        }

        JPanel cardPanel = new JPanel(new CardLayout());
        JPanel detailsViewPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        detailsViewPanel.add(new JScrollPane(tilesTable));
        detailsViewPanel.add(new JScrollPane(sanitaryTable));
        JPanel noDetailsViewPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        noDetailsViewPanel.add(new JScrollPane(tilesTableNoDetails));
        noDetailsViewPanel.add(new JScrollPane(sanitaryTableNoDetails));
        cardPanel.add(detailsViewPanel, "Details");
        cardPanel.add(noDetailsViewPanel, "NoDetails");

        showDetailsCheck.addActionListener(e -> {
            CardLayout cl = (CardLayout) (cardPanel.getLayout());
            cl.show(cardPanel, showDetailsCheck.isSelected() ? "Details" : "NoDetails");
        });

        final List<InvoiceItem>[] displayedTileItemsRef = new List[]{new ArrayList<>()};
        final List<InvoiceItem>[] displayedSanitaryItemsRef = new List[]{new ArrayList<>()};

        Runnable reloadItemTables = () -> {
            tModel.setRowCount(0); sModel.setRowCount(0);
            tModelNoDetails.setRowCount(0); sModelNoDetails.setRowCount(0);
            displayedTileItemsRef[0] = new ArrayList<>();
            displayedSanitaryItemsRef[0] = new ArrayList<>();

            class AggregatedItem {
                double sold = 0, returned = 0, boxArea = 0, unitPrice = 0;
            }
            Map<String, AggregatedItem> aggregatedTiles = new LinkedHashMap<>();
            Map<String, AggregatedItem> aggregatedSanitary = new LinkedHashMap<>();

            for (InvoiceItem it : inv.getItems()) {
                if (it.isTile()) {
                    tModel.addRow(new Object[]{it.getCode(), it.getName(), it.getGrade(), it.getQuantity(), it.getBoxArea(),
                            String.format("%.2f", it.getBoxArea() * it.getQuantity()), String.format("%.2f", it.getUnitPrice()),
                            String.format("%.2f", it.getTotalPrice()), it.getFormattedDate()});
                    displayedTileItemsRef[0].add(it); // NEW: الحفاظ على العناصر المعروضة لعمليات الحذف
                    String key = it.getCode() + "::" + it.getName() + "::" + it.getGrade() + "::" + it.getBoxArea();
                    AggregatedItem agg = aggregatedTiles.computeIfAbsent(key, k -> new AggregatedItem());
                    agg.boxArea = it.getBoxArea();
                    if (it.getUnitPrice() >= 0) {
                        agg.sold += it.getQuantity();
                        if (agg.unitPrice == 0) agg.unitPrice = it.getUnitPrice();
                    } else {
                        agg.returned += it.getQuantity();
                    }
                } else {
                    sModel.addRow(new Object[]{it.getName(), it.getGrade(), it.getQuantity(), String.format("%.2f", it.getUnitPrice()),
                            String.format("%.2f", it.getTotalPrice()), it.getFormattedDate()});

                    displayedSanitaryItemsRef[0].add(it);

                    String key = it.getName() + "::" + it.getGrade();
                    AggregatedItem agg = aggregatedSanitary.computeIfAbsent(key, k -> new AggregatedItem());
                    if (it.getUnitPrice() >= 0) {
                        agg.sold += it.getQuantity();
                        if (agg.unitPrice == 0) agg.unitPrice = it.getUnitPrice();
                    } else {
                        agg.returned += it.getQuantity();
                    }
                }
            }

            aggregatedTiles.forEach((key, agg) -> {
                double netQty = agg.sold - agg.returned;
                if (netQty > EPS) {
                    String[] parts = key.split("::");
                    double totalValue = (netQty * agg.boxArea) * agg.unitPrice;
                    tModelNoDetails.addRow(new Object[]{parts[0], parts[1], parts[2], String.format("%.2f", netQty),
                            agg.boxArea, String.format("%.2f", netQty * agg.boxArea), String.format("%.2f", agg.unitPrice),
                            String.format("%.2f", totalValue), inv.getFormattedDate()});
                }
            });
            aggregatedSanitary.forEach((key, agg) -> {
                double netQty = agg.sold - agg.returned;
                if (netQty > EPS) {
                    String[] parts = key.split("::");
                    double totalValue = netQty * agg.unitPrice;
                    sModelNoDetails.addRow(new Object[]{parts[0], parts[1], String.format("%.2f", netQty),
                            String.format("%.2f", agg.unitPrice), String.format("%.2f", totalValue), inv.getFormattedDate()});
                }
            });
        };
        reloadItemTables.run();

        JPanel rightPayments = new JPanel(new BorderLayout(5, 5));
        rightPayments.setPreferredSize(new Dimension(340, 0));
        JPanel paymentsControl = new JPanel();
        paymentsControl.setLayout(new BoxLayout(paymentsControl, BoxLayout.Y_AXIS));
        JTextField payDateField = new JTextField(dtf.format(LocalDateTime.now()), 16);
        payDateField.setMaximumSize(new Dimension(Integer.MAX_VALUE, payDateField.getPreferredSize().height));
        JButton editPayDateBtn = new JButton("تعديل التاريخ");
        editPayDateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField payAmountField = new JTextField(10);
        payAmountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, payAmountField.getPreferredSize().height));
        JButton addPaymentBtn = new JButton("إضافة دفعة");
        addPaymentBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // -- START: ADDED CODE --
        JButton editDiscountBtn = new JButton("تعديل الحسم");
        editDiscountBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        // -- END: ADDED CODE --

        paymentsControl.add(payDateField); paymentsControl.add(Box.createVerticalStrut(5));
        paymentsControl.add(editPayDateBtn); paymentsControl.add(Box.createVerticalStrut(8));
        paymentsControl.add(payAmountField); paymentsControl.add(Box.createVerticalStrut(5));
        paymentsControl.add(addPaymentBtn);
        paymentsControl.add(Box.createVerticalStrut(8)); // -- ADDED
        paymentsControl.add(editDiscountBtn);             // -- ADDED

        rightPayments.add(paymentsControl, BorderLayout.NORTH);

        String[] pCols = {"قيمة الدفعة", "تاريخ الدفعة"};
        DefaultTableModel pModel = new DefaultTableModel(pCols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable paymentsTable = new JTable(pModel);
        paymentsTable.setRowHeight(22);
        JScrollPane paymentsScroll = new JScrollPane(paymentsTable);
        paymentsScroll.setPreferredSize(new Dimension(320, 140));
        rightPayments.add(paymentsScroll, BorderLayout.CENTER);

        JPanel extraButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addReturnBtn = new JButton("إضافة مرتجع");
        JButton addItemBtn = new JButton("إضافة عنصر");
        extraButtonsPanel.add(addReturnBtn); extraButtonsPanel.add(addItemBtn);
        rightPayments.add(extraButtonsPanel, BorderLayout.SOUTH);

        Runnable reloadPaymentsTable = () -> { pModel.setRowCount(0); for (Payment p : inv.getPayments()) { pModel.addRow(new Object[]{String.format("%.2f", p.getAmount()), dtf.format(p.getDate())}); } };
        reloadPaymentsTable.run();

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cardPanel, rightPayments);
        centerSplit.setResizeWeight(0.80);
        dlg.add(centerSplit, BorderLayout.CENTER);

        JLabel lblTotal = new JLabel("", SwingConstants.CENTER);
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));
        dlg.add(lblTotal, BorderLayout.SOUTH);

        // -- MODIFIED: The refreshTotals runnable is updated --
        Runnable refreshTotals = () -> {
            double total = inv.getTotal();
            double discount = inv.getDiscount();
          //  double finalTotal = inv.getFinalTotal();
            double paid = inv.getPaymentsTotal();
            double remaining = inv.getRemaining();

            String remainingStatus = "";
            if (Math.abs(remaining) < EPS) {
                remainingStatus = "(مدفوع بالكامل)";
            } else if (remaining > 0) {
                remainingStatus = "(عليه دفع)";
            } else {
                remainingStatus = "(رصيد له)";
            }

            // Updated text to show discount details
            lblTotal.setText(String.format("الإجمالي: %.2f | الحسم: %.2f | المدفوع: %.2f | الباقي: %.2f %s",
                    total, discount, paid, Math.abs(remaining), remainingStatus));

            refreshTable(null); // Refresh main table in case total changed
        };
        refreshTotals.run();

        addDeleteLineShortcut(paymentsTable, () -> {
            int selectedPayRow = paymentsTable.getSelectedRow();
            if (selectedPayRow < 0) {
                JOptionPane.showMessageDialog(dlg, "اختر دفعة لحذفها.", "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(dlg, "هل أنت متأكد من حذف هذه الدفعة؟", "تأكيد الحذف", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                inv.getPayments().remove(selectedPayRow);
                invoiceService.saveToFile();
                reloadPaymentsTable.run();
                refreshTotals.run();
            }
        });

        addDeleteLineShortcut(tilesTable, () -> deleteSelectedInvoiceLine(dlg, inv, true, tilesTable, displayedTileItemsRef, reloadItemTables, refreshTotals));
        addDeleteLineShortcut(sanitaryTable, () -> deleteSelectedInvoiceLine(dlg, inv, false, sanitaryTable, displayedSanitaryItemsRef, reloadItemTables, refreshTotals));

        editPayDateBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(dlg, "أدخل التاريخ (yyyy-MM-dd HH:mm)", payDateField.getText());
            if (input != null && !input.isBlank()) {
                try {
                    LocalDateTime.parse(input, dtf);
                    payDateField.setText(input);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "صيغة غير صحيحة");
                }
            }
        });

        // -- START: ADDED CODE --
        // Add action listener for the new discount button
        editDiscountBtn.addActionListener(e -> {
            String currentDiscountStr = String.format("%.2f", inv.getDiscount());
            String input = JOptionPane.showInputDialog(dlg, "أدخل قيمة الحسم الجديدة:", currentDiscountStr);

            if (input != null) {
                try {
                    double newDiscount = Double.parseDouble(input.trim().replace(',', '.'));
                    if (newDiscount < 0) {
                        JOptionPane.showMessageDialog(dlg, "قيمة الحسم لا يمكن أن تكون سالبة.", "خطأ", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Optional: Check if discount is more than the total
                    if (newDiscount > inv.getTotal()) {
                        int choice = JOptionPane.showConfirmDialog(
                                dlg,
                                "قيمة الحسم أكبر من إجمالي الفاتورة. هل أنت متأكد؟",
                                "تأكيد",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (choice != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }

                    inv.setDiscount(newDiscount);
                    invoiceService.saveToFile(); // Save the change immediately
                    refreshTotals.run(); // Update the display

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg, "قيمة الحسم غير صحيحة.", "خطأ", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // -- END: ADDED CODE --

        addPaymentBtn.addActionListener(e -> {
            String amtStr = payAmountField.getText().trim().replace(',', '.');
            if (amtStr.isEmpty()) return;
            try {
                double amt = Double.parseDouble(amtStr);
                LocalDateTime dt = LocalDateTime.parse(payDateField.getText().trim(), dtf);
                inv.addPayment(amt, dt);
                invoiceService.saveToFile();
                payAmountField.setText("");
                reloadPaymentsTable.run();
                refreshTotals.run();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "بيانات الدفعة غير صحيحة", "خطأ", JOptionPane.ERROR_MESSAGE); }
        });
        payAmountField.addActionListener(addPaymentBtn.getActionListeners()[0]);

        addItemBtn.addActionListener(e -> showAddItemOrReturnDialog(dlg, inv, reloadItemTables, refreshTotals, showDetailsCheck, false, payDateField.getText()));
        addReturnBtn.addActionListener(e -> showAddItemOrReturnDialog(dlg, inv, reloadItemTables, refreshTotals, showDetailsCheck, true, payDateField.getText()));

        dlg.setMinimumSize(new Dimension(1250, 600));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
    private void deleteSelectedInvoice() {
        int row = invoiceTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "الرجاء تحديد فاتورة أولاً.", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Invoice invToDelete = currentInvoices.get(row);

        String message = String.format("هل أنت متأكد من حذف الفاتورة رقم %s للعميل %s؟", invToDelete.getInvoiceNumber(), invToDelete.getCustomerName());
        int choice = JOptionPane.showConfirmDialog(this, message, "تأكيد الحذف", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            invoiceService.deleteInvoice(invToDelete.getInvoiceNumber());
            refreshTable(null);
        }
    }

    private void setupKeyboardNavigation() {
        searchNameField.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "toTable");
        searchNameField.getActionMap().put("toTable", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { invoiceTable.requestFocusInWindow(); }
        });
        invoiceTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("UP"), "upFromTable");
        invoiceTable.getActionMap().put("upFromTable", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (invoiceTable.getSelectedRow() <= 0) {
                    searchNameField.requestFocusInWindow();
                } else {
                    int newRow = Math.max(0, invoiceTable.getSelectedRow() - 1);
                    invoiceTable.setRowSelectionInterval(newRow, newRow);
                }
            }
        });
        invoiceTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "selectInvoice");
        invoiceTable.getActionMap().put("selectInvoice", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (invoiceTable.getSelectedRow() >= 0) { viewBtn.requestFocusInWindow(); }
            }
        });
    }

    private void showAddItemOrReturnDialog(Window owner, Invoice inv, Runnable reloadItemTables,
                                           Runnable refreshTotals, JCheckBox showDetailsCheck, boolean isReturn, String dateString) {
        JDialog dlg = new JDialog(owner, isReturn ? "إضافة مرتجع" : "إضافة عنصر", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(5, 5));
        dlg.setResizable(true);
        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        final LocalDateTime itemDate;
        LocalDateTime itemDate1;
        try {
            itemDate1 = LocalDateTime.parse(dateString, dtf);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dlg, "تاريخ الإضافة غير صالح، سيتم استخدام التاريخ الحالي.", "تحذير", JOptionPane.WARNING_MESSAGE);
            itemDate1 = LocalDateTime.now();
        }

        itemDate = itemDate1;
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JRadioButton tileRadio = new JRadioButton("بلاط", true);
        JRadioButton sanitaryRadio = new JRadioButton("أدوات صحية");
        ButtonGroup bg = new ButtonGroup();
        bg.add(tileRadio); bg.add(sanitaryRadio);
        filtersPanel.add(tileRadio); filtersPanel.add(sanitaryRadio);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JTextField searchField = new JTextField(18);
        JButton applyBtn = new JButton("تطبيق");
        searchRow.add(new JLabel("بحث:")); searchRow.add(searchField); searchRow.add(applyBtn);

        String[] invCols = isReturn
                ? new String[]{"الرمز", "الاسم", "النخب", "متاح للإرجاع", "السعر الأصلي"}
                : new String[]{"الرمز", "الاسم", "النخب", "متوفر", "مساحة الصندوق", "السعر"};
        DefaultTableModel invModel = new DefaultTableModel(invCols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable invTable = new JTable(invModel);
        invTable.setRowHeight(24);

        JButton confirmAddBtn = new JButton(isReturn ? "إرجاع العنصر المحدد" : "إضافة إلى الفاتورة");

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(filtersPanel); north.add(searchRow);

        dlg.add(north, BorderLayout.NORTH);
        dlg.add(new JScrollPane(invTable), BorderLayout.CENTER);
        dlg.add(confirmAddBtn, BorderLayout.SOUTH);

        final List<Object>[] displayedForDialogRef = new List[]{new ArrayList<>()};

        class ReturnableLine {
            long idNumber;
            boolean isTile;
            String code, name, grade;
            double boxArea, unitPrice, availableToReturn;
        }

        Runnable reloadInventory = () -> {
            invModel.setRowCount(0);
            displayedForDialogRef[0] = new ArrayList<>();
            String q = searchField.getText().trim().toLowerCase();

            if (isReturn) {
                List<ReturnableLine> lines = new ArrayList<>();

                for (InvoiceItem it : inv.getItems()) {
                    if (it.getUnitPrice() < 0) continue;
                    boolean isTileItem = it.isTile();
                    if ((tileRadio.isSelected() && !isTileItem) || (sanitaryRadio.isSelected() && isTileItem)) continue;

                    boolean passesFilter = q.isEmpty()
                            || it.getName().toLowerCase().contains(q)
                            || (isTileItem && it.getCode() != null && it.getCode().toLowerCase().contains(q));
                    if (!passesFilter) continue;

                    double alreadyReturned = inv.getItems().stream()
                            .filter(x -> x.getIdNumber() == it.getIdNumber() && x.getUnitPrice() < 0)
                            .mapToDouble(InvoiceItem::getQuantity).sum();

                    double available = it.getQuantity() - alreadyReturned;
                    if (available > EPS) {
                        ReturnableLine rl = new ReturnableLine();
                        rl.idNumber = it.getIdNumber();
                        rl.isTile = isTileItem;
                        rl.code = it.getCode();
                        rl.name = it.getName();
                        rl.grade = it.getGrade();
                        rl.boxArea = isTileItem ? it.getBoxArea() : 0.0;
                        rl.unitPrice = Math.abs(it.getUnitPrice());
                        rl.availableToReturn = available;
                        lines.add(rl);
                    }
                }

                for (ReturnableLine rl : lines) {
                    displayedForDialogRef[0].add(rl);
                    invModel.addRow(new Object[]{
                            rl.code, rl.name, rl.grade,
                            String.format("%.2f", rl.availableToReturn),
                            String.format("%.2f", rl.unitPrice)
                    });
                }

            } else {
                if (tileRadio.isSelected()) {
                    List<TileItem> items = tileService.getAll();
                    if (!q.isEmpty()) items = tileService.searchByNameOrCode(q);
                    for (TileItem it : items) {
                        displayedForDialogRef[0].add(it);
                        invModel.addRow(new Object[]{
                                it.getCode(), it.getName(), it.getGrade(),
                                String.format("%.2f", it.getBoxes()), it.getBoxArea(), it.getPrice()
                        });
                    }
                } else {
                    List<SanitaryItem> items = sanitaryService.getAll().stream()
                            .filter(it -> q.isEmpty() || it.getName().toLowerCase().contains(q))
                            .toList();
                    for (SanitaryItem it : items) {
                        displayedForDialogRef[0].add(it);
                        invModel.addRow(new Object[]{
                                "", it.getName(), it.getGrade(), it.getQuantity(), "", it.getPrice()
                        });
                    }
                }
            }
            if (invModel.getRowCount() > 0) invTable.setRowSelectionInterval(0, 0);
        };

        tileRadio.addActionListener(e -> reloadInventory.run());
        sanitaryRadio.addActionListener(e -> reloadInventory.run());
        applyBtn.addActionListener(e -> reloadInventory.run());
        searchField.addActionListener(e -> reloadInventory.run());
        reloadInventory.run();

        confirmAddBtn.addActionListener(e -> {
            int r = invTable.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(dlg, "اختر عنصرًا أولًا", "تنبيه", JOptionPane.WARNING_MESSAGE); return; }

            boolean tile = tileRadio.isSelected();
            if (isReturn) {
                ReturnableLine sel = (ReturnableLine) displayedForDialogRef[0].get(r);
                String qtyStr = JOptionPane.showInputDialog(dlg, "الكمية (متاحة للإرجاع: " +
                        invModel.getValueAt(r, 3) + "):", "1");
                if (qtyStr == null) return;
                String priceStr = JOptionPane.showInputDialog(dlg, "سعر الوحدة:", String.valueOf(sel.unitPrice));
                if (priceStr == null) return;

                try {
                    double qty = Double.parseDouble(qtyStr.trim().replace(',', '.'));
                    double prc = Double.parseDouble(priceStr.trim().replace(',', '.'));
                    double availableToReturn = Double.parseDouble(invModel.getValueAt(r, 3).toString());
                    if (qty > availableToReturn + EPS || qty <= 0) {
                        JOptionPane.showMessageDialog(dlg, "كمية غير صالحة للإرجاع", "خطأ", JOptionPane.ERROR_MESSAGE); return;
                    }

                    double finalUnitPrice = -Math.abs(prc);

                    if (sel.isTile) {
                        TileItem tileItem = tileService.findById(sel.idNumber);
                        if (tileItem != null) {
                            tileItem.setBoxes(tileItem.getBoxes() + qty);
                            tileService.save();
                        }
                        InvoiceItem ii = new InvoiceItem(sel.idNumber, sel.name, qty, finalUnitPrice, sel.boxArea, true, sel.code, sel.grade, itemDate);
                        inv.addItem(ii);
                    } else {
                        SanitaryItem sanitaryItem = sanitaryService.findById(sel.idNumber);
                        if (sanitaryItem != null) {
                            sanitaryItem.setQuantity(sanitaryItem.getQuantity() + (int) Math.round(qty));
                            sanitaryService.save();
                        }
                        InvoiceItem ii = new InvoiceItem(sel.idNumber, sel.name, qty, finalUnitPrice, false, sel.grade, itemDate);
                        inv.addItem(ii);
                    }

                    invoiceService.saveToFile();
                    reloadItemTables.run();
                    refreshTotals.run();
                    dlg.dispose();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "قيمة كمية/سعر غير صحيحة", "خطأ", JOptionPane.ERROR_MESSAGE);
                }

            } else {
                Object obj = displayedForDialogRef[0].get(r);
                if (tile) {
                    TileItem tileItem = (TileItem) obj;
                    String qtyStr = JOptionPane.showInputDialog(dlg, "الكمية:", "1");
                    if (qtyStr == null) return;
                    String priceStr = JOptionPane.showInputDialog(dlg, "سعر الوحدة:", String.valueOf(tileItem.getPrice()));
                    if (priceStr == null) return;

                    try {
                        double qty = Double.parseDouble(qtyStr.trim().replace(',', '.'));
                        double prc = Double.parseDouble(priceStr.trim().replace(',', '.'));
                        if (qty <= 0) { JOptionPane.showMessageDialog(dlg, "كمية غير صالحة", "خطأ", JOptionPane.ERROR_MESSAGE); return; }

                        tileItem.setBoxes(tileItem.getBoxes() - qty);
                        tileService.save();

                        InvoiceItem ii = new InvoiceItem(tileItem.getIdNumber(), tileItem.getName(), qty, prc,
                                tileItem.getBoxArea(), true, tileItem.getCode(), tileItem.getGrade(), itemDate);
                        inv.addItem(ii);

                        invoiceService.saveToFile();
                        reloadItemTables.run();
                        refreshTotals.run();
                        dlg.dispose();

                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dlg, "قيمة كمية/سعر غير صحيحة", "خطأ", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    SanitaryItem sanitary = (SanitaryItem) obj;
                    String qtyStr = JOptionPane.showInputDialog(dlg, "الكمية:", "1");
                    if (qtyStr == null) return;
                    String priceStr = JOptionPane.showInputDialog(dlg, "سعر الوحدة:", String.valueOf(sanitary.getPrice()));
                    if (priceStr == null) return;

                    try {
                        double qty = Double.parseDouble(qtyStr.trim().replace(',', '.'));
                        double prc = Double.parseDouble(priceStr.trim().replace(',', '.'));
                        if (qty <= 0) { JOptionPane.showMessageDialog(dlg, "كمية غير صالحة", "خطأ", JOptionPane.ERROR_MESSAGE); return; }

                        sanitary.setQuantity(sanitary.getQuantity() - (int) Math.round(qty));
                        sanitaryService.save();

                        InvoiceItem ii = new InvoiceItem(sanitary.getIdNumber(), sanitary.getName(), qty, prc, false, sanitary.getGrade(), itemDate);
                        inv.addItem(ii);

                        invoiceService.saveToFile();
                        reloadItemTables.run();
                        refreshTotals.run();
                        dlg.dispose();

                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dlg, "قيمة كمية/سعر غير صحيحة", "خطأ", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        dlg.setMinimumSize(new Dimension(800, 520));
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    private void addDeleteLineShortcut(JTable table, Runnable action) {
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        table.getInputMap(JComponent.WHEN_FOCUSED).put(ks, "deleteLine");
        table.getActionMap().put("deleteLine", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    private void deleteSelectedInvoiceLine(Window parent,
                                           Invoice inv,
                                           boolean isTileTable,
                                           JTable table,
                                           List<InvoiceItem>[] displayedItemsRef,
                                           Runnable reloadItemTables,
                                           Runnable refreshTotals) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(parent, "اختر سطرًا أولاً.", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }

        InvoiceItem selected = displayedItemsRef[0].get(row);
        boolean isReturnItem = selected.getUnitPrice() < 0;
        boolean isTileItem = selected.isTile();

        TileItem matchedTile = isTileItem ? tileService.findById(selected.getIdNumber()) : null;
        SanitaryItem matchedSanitary = isTileItem ? null : sanitaryService.findById(selected.getIdNumber());
        boolean itemFound = matchedTile != null || matchedSanitary != null;

        boolean returnToStock = false;
        if (!isReturnItem && itemFound) {
            int returnChoice = JOptionPane.showConfirmDialog(
                    parent,
                    "تم العثور على العنصر في المستودع."+
             "       هل تريد إعادة الكمية إلى المخزون؟",
                    "إرجاع للمخزون",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            returnToStock = (returnChoice == JOptionPane.YES_OPTION);
        }

        if (!itemFound) {
            int missingChoice = JOptionPane.showConfirmDialog(
                    parent,
                    "لم يتم العثور على العنصر في المستودع.   "

                            + "هل تريد الحذف على الرغم من عدم وجود العنصر في المستودع؟"

                            + "(قد تكون حذفته، أضف العنصر إلى المستودع ثم احذفه)",
                    "العنصر غير موجود",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (missingChoice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        String itemDesc = selected.getName() + " - " + selected.getGrade();
        int deleteChoice = JOptionPane.showConfirmDialog(
                parent,
                "هل أنت متأكد من حذف هذا السطر من الفاتورة؟"
                 + itemDesc,
                "تأكيد الحذف",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (deleteChoice != JOptionPane.YES_OPTION) {
            return;
        }

        inv.getItems().remove(selected);

        try {
            double quantity = selected.getQuantity();

            if (!isReturnItem && returnToStock && itemFound) {
                if (matchedTile != null) {
                    matchedTile.setBoxes(matchedTile.getBoxes() + quantity);
                    tileService.save();      // استخدم اسم طريقتك للحفظ بالـ JSON
                } else if (matchedSanitary != null) {
                    int qty = (int) Math.round(quantity);
                    matchedSanitary.setQuantity(matchedSanitary.getQuantity() + qty);
                    sanitaryService.save();  // أو اسم طريقتك
                }
            }

            if (isReturnItem && itemFound) {
                if (matchedTile != null) {
                    matchedTile.setBoxes(matchedTile.getBoxes() - quantity);
                    if (matchedTile.getBoxes() < 0) matchedTile.setBoxes(0);
                    tileService.save();
                } else if (matchedSanitary != null) {
                    int qty = (int) Math.round(quantity);
                    int newQty = matchedSanitary.getQuantity() - qty;
                    matchedSanitary.setQuantity(Math.max(newQty, 0));
                    sanitaryService.save();
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    parent,
                    "حدث خطأ أثناء تحديث المخزون: " + ex.getMessage(),
                    "خطأ",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        invoiceService.saveToFile();
        reloadItemTables.run();
        refreshTotals.run();
    }    private static boolean eq(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }
    private static boolean eqDouble(double a, double b) { return Math.abs(a - b) < EPS; }

}