import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InvoicePanel extends JPanel {

    private final InvoiceService invoiceService;
    private final TileService tileService;
    private final SanitaryService sanitaryService;
    private final WarehouseManager warehouseManager;
    private final CurrencySettingsService currencySettingsService;

    private JTable invoiceTable;
    private DefaultTableModel invoiceTableModel;

    private List<Invoice> currentInvoices =
            new ArrayList<>();

    private JLabel totalSumLabel;

    private JTextField searchNameField;

    private JButton dateFilterBtn;
    private JButton searchBtn;
    private JButton refreshBtn;
    private JButton viewBtn;
    private JButton deleteBtn;

    private final JPanel lockedButtonsPanel =
            new JPanel(
                    new FlowLayout(
                            FlowLayout.CENTER,
                            10,
                            0
                    )
            );

    public InvoicePanel(
            InvoiceService invoiceService,
            TileService tileService,
            SanitaryService sanitaryService,
            WarehouseManager warehouseManager,
            CurrencySettingsService currencySettingsService) {

        this.invoiceService =
                invoiceService;

        this.tileService =
                tileService;

        this.sanitaryService =
                sanitaryService;

        this.warehouseManager =
                warehouseManager;

        this.currencySettingsService =
                currencySettingsService;

        setLayout(
                new BorderLayout(
                        5,
                        5
                )
        );

        initUI();

        setupKeyboardNavigation();
    }

    private void initUI() {

        JPanel north =
                new JPanel(
                        new BorderLayout(
                                5,
                                5
                        )
                );

        north.setBorder(
                BorderFactory.createEmptyBorder(
                        5,
                        5,
                        5,
                        5
                )
        );

        dateFilterBtn =
                new JButton(
                        "تصفية بالتاريخ"
                );

        north.add(
                dateFilterBtn,
                BorderLayout.WEST
        );

        JPanel searchPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                10,
                                0
                        )
                );

        searchNameField =
                new JTextField(
                        20
                );

        searchBtn =
                new JButton(
                        "بحث"
                );

        searchPanel.add(
                searchBtn
        );

        searchPanel.add(
                searchNameField
        );

        searchPanel.add(
                new JLabel(
                        "بحث بالاسم أو الرقم:"
                )
        );

        north.add(
                searchPanel,
                BorderLayout.EAST
        );

        add(
                north,
                BorderLayout.NORTH
        );

        String[] cols = {
                "رقم الفاتورة",
                "اسم العميل",
                "الهاتف",
                "التاريخ",
                "الإجمالي"
        };

        invoiceTableModel =
                new DefaultTableModel(
                        cols,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int r,
                            int c) {
                        return false;
                    }
                };

        invoiceTable =
                new JTable(
                        invoiceTableModel
                );

        invoiceTable.setRowHeight(
                28
        );

        invoiceTable.getTableHeader()
                .setReorderingAllowed(
                        false
                );

        add(
                new JScrollPane(
                        invoiceTable
                ),
                BorderLayout.CENTER
        );

        JPanel south =
                new JPanel(
                        new BorderLayout()
                );

        JPanel allButtonsPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                0
                        )
                );

        refreshBtn =
                new JButton(
                        "تحديث"
                );

        viewBtn =
                new JButton(
                        "عرض الفاتورة"
                );

        deleteBtn =
                new JButton(
                        "حذف الفاتورة"
                );

        lockedButtonsPanel.add(
                viewBtn
        );

        lockedButtonsPanel.add(
                deleteBtn
        );

        allButtonsPanel.add(
                refreshBtn
        );

        allButtonsPanel.add(
                lockedButtonsPanel
        );

        totalSumLabel =
                new JLabel(
                        "إجمالي الفواتير: 0.00"
                );

        totalSumLabel.setFont(
                totalSumLabel
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                16f
                        )
        );

        south.add(
                allButtonsPanel,
                BorderLayout.WEST
        );

        south.add(
                totalSumLabel,
                BorderLayout.EAST
        );

        add(
                south,
                BorderLayout.SOUTH
        );

        refreshBtn.addActionListener(
                e -> refreshTable(null)
        );

        dateFilterBtn.addActionListener(
                e -> showDateFilterDialog()
        );

        searchBtn.addActionListener(
                e -> applyNameOrIdSearch()
        );

        searchNameField.addActionListener(
                e -> applyNameOrIdSearch()
        );

        viewBtn.addActionListener(
                e -> viewSelectedInvoice()
        );

        deleteBtn.addActionListener(
                e -> deleteSelectedInvoice()
        );

        refreshTable(null);
    }

    private void showDateFilterDialog() {

        JTextField fromDay =
                new JTextField(2);

        JTextField fromMonth =
                new JTextField(2);

        JTextField fromYear =
                new JTextField(2);

        JTextField toDay =
                new JTextField(2);

        JTextField toMonth =
                new JTextField(2);

        JTextField toYear =
                new JTextField(2);

        JPanel panel =
                new JPanel(
                        new GridLayout(
                                2,
                                7,
                                5,
                                5
                        )
                );

        panel.add(fromYear);
        panel.add(new JLabel("سنة"));
        panel.add(fromMonth);
        panel.add(new JLabel("شهر"));
        panel.add(fromDay);
        panel.add(new JLabel("من:            يوم"));

        panel.add(new JLabel(""));

        panel.add(toYear);
        panel.add(new JLabel("سنة"));
        panel.add(toMonth);
        panel.add(new JLabel("شهر"));
        panel.add(toDay);
        panel.add(new JLabel("إلى:            يوم"));

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "تصفية حسب التاريخ",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

        if (
                result
                        == JOptionPane.OK_OPTION
        ) {

            try {

                int fd =
                        Integer.parseInt(
                                fromDay
                                        .getText()
                                        .trim()
                        );

                int fm =
                        Integer.parseInt(
                                fromMonth
                                        .getText()
                                        .trim()
                        );

                int fy =
                        2000
                                +
                                Integer.parseInt(
                                        fromYear
                                                .getText()
                                                .trim()
                                );

                int td =
                        Integer.parseInt(
                                toDay
                                        .getText()
                                        .trim()
                        );

                int tm =
                        Integer.parseInt(
                                toMonth
                                        .getText()
                                        .trim()
                        );

                int ty =
                        2000
                                +
                                Integer.parseInt(
                                        toYear
                                                .getText()
                                                .trim()
                                );

                LocalDateTime from =
                        LocalDate
                                .of(
                                        fy,
                                        fm,
                                        fd
                                )
                                .atStartOfDay();

                LocalDateTime to =
                        LocalDate
                                .of(
                                        ty,
                                        tm,
                                        td
                                )
                                .atTime(
                                        23,
                                        59,
                                        59
                                );

                refreshTable(
                        invoiceService
                                .filterByDate(
                                        from,
                                        to
                                )
                );

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "صيغة التاريخ غير صحيحة",
                        "خطأ",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void applyNameOrIdSearch() {

        String query =
                searchNameField
                        .getText()
                        .trim()
                        .toLowerCase();

        if (query.isEmpty()) {

            refreshTable(null);

            return;
        }

        List<Invoice> filtered =
                invoiceService
                        .getAll()
                        .stream()
                        .filter(
                                inv ->
                                        inv.getCustomerName()
                                                .toLowerCase()
                                                .contains(query)
                                                ||
                                                inv.getInvoiceNumber()
                                                        .toLowerCase()
                                                        .contains(query)
                        )
                        .toList();

        refreshTable(
                filtered
        );
    }

    private void refreshTable(
            List<Invoice> list) {

        String selectedInvoiceId =
                null;

        int selectedRow =
                invoiceTable.getSelectedRow();

        if (
                selectedRow != -1
                        &&
                        selectedRow
                                < currentInvoices.size()
        ) {

            selectedInvoiceId =
                    currentInvoices
                            .get(selectedRow)
                            .getInvoiceNumber();
        }

        invoiceTableModel
                .setRowCount(0);

        List<Invoice> invoicesToDisplay =
                (list == null)
                        ? new ArrayList<>(
                        invoiceService
                                .getAll()
                )
                        : new ArrayList<>(
                        list
                );

        invoicesToDisplay.sort(
                Comparator
                        .comparing(
                                Invoice::getDate
                        )
                        .reversed()
        );

        currentInvoices =
                invoicesToDisplay;

        double sum = 0;

        int newSelectedRow = -1;

        for (
                int i = 0;
                i < currentInvoices.size();
                i++
        ) {

            Invoice inv =
                    currentInvoices.get(i);

            invoiceTableModel.addRow(
                    new Object[]{
                            inv.getInvoiceNumber(),
                            inv.getCustomerName(),
                            inv.getCustomerPhone(),
                            inv.getFormattedDate(),
                            String.format(
                                    "%.2f",
                                    inv.getTotal()
                            )
                    }
            );

            sum +=
                    inv.getTotal();

            if (
                    selectedInvoiceId != null
                            &&
                            inv.getInvoiceNumber()
                                    .equals(
                                            selectedInvoiceId
                                    )
            ) {

                newSelectedRow =
                        i;
            }
        }

        totalSumLabel.setText(
                String.format(
                        "إجمالي الفواتير: %.2f",
                        sum
                )
        );

        if (newSelectedRow != -1) {

            invoiceTable
                    .setRowSelectionInterval(
                            newSelectedRow,
                            newSelectedRow
                    );
        }
    }

    private void viewSelectedInvoice() {

        int row =
                invoiceTable
                        .getSelectedRow();

        if (row < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "الرجاء تحديد فاتورة أولاً.",
                    "تنبيه",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Invoice inv =
                currentInvoices.get(row);

        InvoiceDetailsDialog detailsDialog =
                new InvoiceDetailsDialog(
                        SwingUtilities
                                .getWindowAncestor(
                                        this
                                ),
                        inv,
                        invoiceService,
                        tileService,
                        sanitaryService,
                        warehouseManager,
                        currencySettingsService
                );

        detailsDialog.show();
    }

    private void deleteSelectedInvoice() {

        int row =
                invoiceTable
                        .getSelectedRow();

        if (row < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "الرجاء تحديد فاتورة أولاً.",
                    "تنبيه",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Invoice invToDelete =
                currentInvoices.get(row);

        String message =
                String.format(
                        "هل أنت متأكد من حذف الفاتورة رقم %s للعميل %s؟",
                        invToDelete
                                .getInvoiceNumber(),
                        invToDelete
                                .getCustomerName()
                );

        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        message,
                        "تأكيد الحذف",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (
                choice
                        == JOptionPane.YES_OPTION
        ) {

            invoiceService.deleteInvoice(
                    invToDelete
                            .getInvoiceNumber()
            );

            refreshTable(null);
        }
    }

    private void setupKeyboardNavigation() {

        searchNameField
                .getInputMap()
                .put(
                        KeyStroke.getKeyStroke(
                                "DOWN"
                        ),
                        "toTable"
                );

        searchNameField
                .getActionMap()
                .put(
                        "toTable",
                        new AbstractAction() {

                            @Override
                            public void actionPerformed(
                                    ActionEvent e) {

                                invoiceTable
                                        .requestFocusInWindow();
                            }
                        }
                );

        invoiceTable
                .getInputMap(
                        JComponent.WHEN_FOCUSED
                )
                .put(
                        KeyStroke.getKeyStroke(
                                "UP"
                        ),
                        "upFromTable"
                );

        invoiceTable
                .getActionMap()
                .put(
                        "upFromTable",
                        new AbstractAction() {

                            @Override
                            public void actionPerformed(
                                    ActionEvent e) {

                                if (
                                        invoiceTable
                                                .getSelectedRow()
                                                <= 0
                                ) {

                                    searchNameField
                                            .requestFocusInWindow();

                                } else {

                                    int newRow =
                                            Math.max(
                                                    0,
                                                    invoiceTable
                                                            .getSelectedRow()
                                                            - 1
                                            );

                                    invoiceTable
                                            .setRowSelectionInterval(
                                                    newRow,
                                                    newRow
                                            );
                                }
                            }
                        }
                );

        invoiceTable
                .getInputMap(
                        JComponent.WHEN_FOCUSED
                )
                .put(
                        KeyStroke.getKeyStroke(
                                "ENTER"
                        ),
                        "selectInvoice"
                );

        invoiceTable
                .getActionMap()
                .put(
                        "selectInvoice",
                        new AbstractAction() {

                            @Override
                            public void actionPerformed(
                                    ActionEvent e) {

                                if (
                                        invoiceTable
                                                .getSelectedRow()
                                                >= 0
                                ) {

                                    viewBtn
                                            .requestFocusInWindow();
                                }
                            }
                        }
                );
    }
}