import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InvoiceDetailsDialog {

    private static final double EPS = 1e-6;

    private final Window owner;

    private final Invoice invoice;

    private final InvoiceService invoiceService;
    private final TileService tileService;
    private final SanitaryService sanitaryService;
    private final WarehouseManager warehouseManager;
    private final CurrencySettingsService currencySettingsService;

    // =========================================================
    // إعدادات الواجهة
    // =========================================================


    private static final int CUSTOMER_PANEL_WIDTH = 500;

    private static final int CUSTOMER_PANEL_HEIGHT = 15;
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 700;

    private static final int HEADER_HEIGHT = 125;
    private static final int TABLE_ROW_HEIGHT = 28;

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 38;

    private static final int DATE_LABEL_WIDTH = 190;
    private static final int DATE_LABEL_HEIGHT = 38;

    private static final int LABEL_FONT_SIZE = 15;
    private static final int TOTAL_FONT_SIZE = 18;

    private static final int PAYMENT_BUTTON_WIDTH = 150;
    private static final int PAYMENT_BUTTON_HEIGHT = 40;

    private static final int PAYMENT_FIELD_WIDTH = 180;
    private static final int PAYMENT_FIELD_HEIGHT = 40;

    private final DateTimeFormatter dtf =
            DateTimeFormatter.ofPattern(
                    "yyyy/MM/dd HH:mm"
            );

    public InvoiceDetailsDialog(
            Window owner,
            Invoice invoice,
            InvoiceService invoiceService,
            TileService tileService,
            SanitaryService sanitaryService,
            WarehouseManager warehouseManager,
            CurrencySettingsService currencySettingsService) {

        this.owner = owner;
        this.invoice = invoice;

        this.invoiceService = invoiceService;
        this.tileService = tileService;
        this.sanitaryService = sanitaryService;
        this.warehouseManager = warehouseManager;
        this.currencySettingsService = currencySettingsService;
    }

    // =========================================================
    // فتح نافذة الفاتورة
    // =========================================================


    public void show() {

        JDialog dlg =
                new JDialog(
                        owner,
                        "تفاصيل الفاتورة رقم: "
                                + invoice.getInvoiceNumber(),
                        Dialog.ModalityType.APPLICATION_MODAL
                );

        dlg.setLayout(
                new BorderLayout(
                        8,
                        8
                )
        );

        dlg.setResizable(true);

        dlg.getRootPane()
                .registerKeyboardAction(
                        e -> dlg.dispose(),
                        KeyStroke.getKeyStroke(
                                KeyEvent.VK_ESCAPE,
                                0
                        ),
                        JComponent.WHEN_IN_FOCUSED_WINDOW
                );

        // =====================================================
        // الجزء العلوي
        // =====================================================

        JPanel topPanel =
                new JPanel(
                        new BorderLayout(
                                15,
                                5
                        )
                );

        topPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        5,
                        10
                )
        );

        /*
         * يمكنك التحكم بارتفاع الجزء العلوي من HEADER_HEIGHT
         * ويمكنك التحكم بعرض بيانات العميل من CUSTOMER_PANEL_WIDTH
         * الموجودين في أعلى الكلاس.
         *
         * إذا لم تكن هذه الثوابت موجودة لديك، أضفها إلى الكلاس:
         *
         * private static final int HEADER_HEIGHT = 175;
         * private static final int CUSTOMER_PANEL_WIDTH = 500;
         * private static final int CUSTOMER_PANEL_HEIGHT = 145;
         */

        topPanel.setPreferredSize(
                new Dimension(
                        0,
                        HEADER_HEIGHT
                )
        );

        // =====================================================
        // بيانات العميل - في اليمين
        // =====================================================

        JPanel customerPanel =
                new JPanel(
                        new BorderLayout()
                );

        customerPanel.setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );

        customerPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "بيانات العميل"
                )
        );

        JLabel lblName =
                new JLabel(
                        "اسم العميل: "
                                + invoice.getCustomerName()
                );

        JLabel lblPhone =
                new JLabel(
                        "الهاتف: "
                                + invoice.getCustomerPhone()
                );

        JLabel lblInvoiceNumber =
                new JLabel(
                        "رقم الفاتورة: "
                                + invoice.getInvoiceNumber()
                );

        lblName.setFont(
                lblName.getFont()
                        .deriveFont(
                                Font.BOLD,
                                (float) LABEL_FONT_SIZE
                        )
        );

        lblPhone.setFont(
                lblPhone.getFont()
                        .deriveFont(
                                (float) LABEL_FONT_SIZE
                        )
        );

        lblInvoiceNumber.setFont(
                lblInvoiceNumber.getFont()
                        .deriveFont(
                                (float) LABEL_FONT_SIZE
                        )
        );

        lblName.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        lblPhone.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        lblInvoiceNumber.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        JPanel customerDataPanel =
                new JPanel();

        customerDataPanel.setLayout(
                new BoxLayout(
                        customerDataPanel,
                        BoxLayout.Y_AXIS
                )
        );

        customerDataPanel.setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );

        customerDataPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        8,
                        15,
                        8,
                        15
                )
        );

        customerDataPanel.add(
                lblName
        );

        customerDataPanel.add(
                Box.createVerticalStrut(2)
        );

        customerDataPanel.add(
                lblPhone
        );

        customerDataPanel.add(
                Box.createVerticalStrut(2)
        );

        customerDataPanel.add(
                lblInvoiceNumber
        );

        customerPanel.add(
                customerDataPanel,
                BorderLayout.CENTER
        );

        /*
         * التحكم بحجم صندوق بيانات العميل.
         *
         * إذا كانت البيانات ما زالت مضغوطة:
         * زد CUSTOMER_PANEL_WIDTH أو CUSTOMER_PANEL_HEIGHT.
         */
        customerPanel.setPreferredSize(
                new Dimension(
                        CUSTOMER_PANEL_WIDTH,
                        CUSTOMER_PANEL_HEIGHT
                )
        );

        customerPanel.setMinimumSize(
                new Dimension(
                        580,
                        100
                )
        );

        // =====================================================
        // أزرار الجزء العلوي
        // =====================================================

        JButton editCustomerBtn =
                new JButton(
                        "تعديل بيانات العميل"
                );

        editCustomerBtn.setPreferredSize(
                new Dimension(
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
        );

        JPanel rightControls =
                new JPanel(
                        new GridLayout(
                                2,
                                2,
                                8,
                                8
                        )
                );

        rightControls.setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );

        JLabel lblPayDate =
                new JLabel(
                        dtf.format(
                                LocalDateTime.now()
                        ),
                        SwingConstants.CENTER
                );

        lblPayDate.setPreferredSize(
                new Dimension(
                        DATE_LABEL_WIDTH,
                        DATE_LABEL_HEIGHT
                )
        );

        lblPayDate.setBorder(
                BorderFactory.createLineBorder(
                        Color.GRAY
                )
        );

        lblPayDate.setFont(
                lblPayDate.getFont()
                        .deriveFont(
                                (float) LABEL_FONT_SIZE
                        )
        );

        JButton editPayDateBtn =
                new JButton(
                        "تعديل التاريخ"
                );

        editPayDateBtn.setPreferredSize(
                new Dimension(
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
        );

        JButton editDiscountBtn =
                new JButton(
                        "تعديل الحسم"
                );

        editDiscountBtn.setPreferredSize(
                new Dimension(
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
        );

        rightControls.add(
                editPayDateBtn
        );

        rightControls.add(
                lblPayDate
        );

        rightControls.add(
                editDiscountBtn
        );

        rightControls.add(
                editCustomerBtn
        );

        // =====================================================
        // أزرار عرض الجداول الجديدة
        // =====================================================

        JButton showTilesBtn =
                new JButton(
                        "جدول البلاط"
                );

        JButton showSanitaryBtn =
                new JButton(
                        "جدول الأدوات"
                );

        showTilesBtn.setPreferredSize(
                new Dimension(
                        BUTTON_WIDTH+ 10,
                        BUTTON_HEIGHT+5
                )
        );

        showSanitaryBtn.setPreferredSize(
                new Dimension(
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
        );

        /*
         * الزران فوق بعضهما:
         *
         * عرض جدول البلاط
         * عرض جدول الأدوات الصحية
         */
        JPanel tablesButtonsPanel =
                new JPanel();

        tablesButtonsPanel.setLayout(
                new BoxLayout(
                        tablesButtonsPanel,
                        BoxLayout.Y_AXIS
                )
        );

        tablesButtonsPanel.setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );

        showTilesBtn.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        showSanitaryBtn.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        tablesButtonsPanel.add(
                showTilesBtn
        );

        tablesButtonsPanel.add(
                Box.createVerticalStrut(8)
        );

        tablesButtonsPanel.add(
                showSanitaryBtn
        );

        // =====================================================
        // تجميع أزرار الجزء العلوي
        //
        // الجداول الجديدة + أزرار التاريخ والحسم والعميل
        // =====================================================

        JPanel topControlsPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                5
                        )
                );

        topControlsPanel.setComponentOrientation(
                ComponentOrientation.LEFT_TO_RIGHT
        );

        topControlsPanel.add(
                tablesButtonsPanel
        );

        topControlsPanel.add(
                rightControls
        );

        // =====================================================
        // توزيع الجزء العلوي
        //
        // بيانات العميل إلى اليمين
        // الأزرار إلى اليسار
        // =====================================================

        topPanel.add(
                customerPanel,
                BorderLayout.EAST
        );

        topPanel.add(
                topControlsPanel,
                BorderLayout.WEST
        );

        dlg.add(
                topPanel,
                BorderLayout.NORTH
        );

        // =====================================================
        // اختيار إظهار التفاصيل
        // =====================================================

        JCheckBox showDetailsCheck =
                new JCheckBox(
                        "إظهار المرتجعات",
                        true
                );

        showDetailsCheck.setFont(
                showDetailsCheck.getFont()
                        .deriveFont(
                                (float) LABEL_FONT_SIZE
                        )
        );

        // =====================================================
        // جداول البلاط
        // =====================================================

        String[] tCols = {
                "الرمز",
                "الاسم",
                "النخب",
                "عدد الصناديق",
                "مساحة الصندوق",
                "المساحة الإجمالية",
                "السعر",
                "الإجمالي",
                "التاريخ"
        };

        DefaultTableModel tModel =
                new DefaultTableModel(
                        tCols,
                        0
                );

        DefaultTableModel tModelNoDetails =
                new DefaultTableModel(
                        tCols,
                        0
                );

        JTable tilesTable =
                new JTable(
                        tModel
                );

        JTable tilesTableNoDetails =
                new JTable(
                        tModelNoDetails
                );

        // =====================================================
        // جداول الأدوات الصحية
        // =====================================================

        String[] sCols = {
                "النوع",
                "الاسم",
                "النخب",
                "الكمية",
                "السعر",
                "الإجمالي",
                "التاريخ"
        };

        DefaultTableModel sModel =
                new DefaultTableModel(
                        sCols,
                        0
                );

        DefaultTableModel sModelNoDetails =
                new DefaultTableModel(
                        sCols,
                        0
                );

        JTable sanitaryTable =
                new JTable(
                        sModel
                );

        JTable sanitaryTableNoDetails =
                new JTable(
                        sModelNoDetails
                );

        // =====================================================
        // إعداد الجداول
        // =====================================================

        for (JTable table :
                new JTable[]{
                        tilesTable,
                        tilesTableNoDetails,
                        sanitaryTable,
                        sanitaryTableNoDetails
                }) {

            table.setRowHeight(
                    TABLE_ROW_HEIGHT
            );

            table.setAutoResizeMode(
                    JTable.AUTO_RESIZE_ALL_COLUMNS
            );

            table.setFillsViewportHeight(
                    true
            );

            table.getTableHeader()
                    .setReorderingAllowed(
                            false
                    );
        }

        // =====================================================
        // CardLayout
        // =====================================================

        JPanel cardPanel =
                new JPanel(
                        new CardLayout()
                );

        JPanel detailsViewPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                1,
                                0,
                                8
                        )
                );

        detailsViewPanel.add(
                new JScrollPane(
                        tilesTable
                )
        );

        detailsViewPanel.add(
                new JScrollPane(
                        sanitaryTable
                )
        );

        JPanel noDetailsViewPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                1,
                                0,
                                8
                        )
                );

        noDetailsViewPanel.add(
                new JScrollPane(
                        tilesTableNoDetails
                )
        );

        noDetailsViewPanel.add(
                new JScrollPane(
                        sanitaryTableNoDetails
                )
        );

        cardPanel.add(
                detailsViewPanel,
                "Details"
        );

        cardPanel.add(
                noDetailsViewPanel,
                "NoDetails"
        );

        showDetailsCheck.addActionListener(
                e -> {

                    CardLayout cl =
                            (CardLayout)
                                    cardPanel.getLayout();

                    cl.show(
                            cardPanel,
                            showDetailsCheck.isSelected()
                                    ? "Details"
                                    : "NoDetails"
                    );
                }
        );

        dlg.add(
                cardPanel,
                BorderLayout.CENTER
        );

        // =====================================================
        // القوائم المستخدمة للحذف
        // =====================================================

        final List<InvoiceItem>[] displayedTileItemsRef =
                new List[]{
                        new ArrayList<>()
                };

        final List<InvoiceItem>[] displayedSanitaryItemsRef =
                new List[]{
                        new ArrayList<>()
                };


        Runnable reloadItemTables =
                () -> {

                    tModel.setRowCount(0);
                    sModel.setRowCount(0);

                    tModelNoDetails.setRowCount(0);
                    sModelNoDetails.setRowCount(0);

                    displayedTileItemsRef[0] =
                            new ArrayList<>();

                    displayedSanitaryItemsRef[0] =
                            new ArrayList<>();

                    for (InvoiceItem it :
                            invoice.getItems()) {

                        // =================================================
                        // البلاط
                        // =================================================

                        if (it.isTile()) {

                            // -------------------------------
                            // جدول التفاصيل
                            // -------------------------------

                            tModel.addRow(
                                    new Object[]{
                                            it.getCode(),
                                            it.getName(),
                                            it.getGrade(),
                                            it.getQuantity(),
                                            it.getBoxArea(),
                                            String.format(
                                                    "%.2f",
                                                    it.getBoxArea()
                                                            * it.getQuantity()
                                            ),
                                            String.format(
                                                    "%.2f",
                                                    it.getUnitPrice()
                                            ),
                                            String.format(
                                                    "%.2f",
                                                    it.getTotalPrice()
                                            ),
                                            it.getFormattedDate()
                                    }
                            );

                            displayedTileItemsRef[0]
                                    .add(it);

                            // -------------------------------
                            // NoDetails
                            //
                            // المرتجع لا يظهر
                            // -------------------------------

                            if (it.getUnitPrice() >= 0) {

                                tModelNoDetails.addRow(
                                        new Object[]{
                                                it.getCode(),
                                                it.getName(),
                                                it.getGrade(),
                                                String.format(
                                                        "%.2f",
                                                        it.getQuantity()
                                                ),
                                                it.getBoxArea(),
                                                String.format(
                                                        "%.2f",
                                                        it.getBoxArea()
                                                                * it.getQuantity()
                                                ),
                                                String.format(
                                                        "%.2f",
                                                        it.getUnitPrice()
                                                ),
                                                String.format(
                                                        "%.2f",
                                                        it.getTotalPrice()
                                                ),
                                                it.getFormattedDate()
                                        }
                                );
                            }

                        }

                        // =================================================
                        // الأدوات الصحية والعناصر الخارجية
                        // =================================================

                        else {

                            displayedSanitaryItemsRef[0]
                                    .add(it);

                            Object[] row;

                            if (it.isExternalItem()) {

                                row =
                                        new Object[]{
                                                "عنصر خارجي",
                                                it.getName(),
                                                "-",
                                                it.getQuantity(),
                                                String.format(
                                                        "%.2f",
                                                        it.getUnitPrice()
                                                ),
                                                String.format(
                                                        "%.2f",
                                                        it.getTotalPrice()
                                                ),
                                                it.getFormattedDate()
                                        };

                                // -----------------------------------------
                                // العنصر الخارجي يظهر دائمًا
                                // -----------------------------------------

                                sModel.addRow(row);

                                sModelNoDetails.addRow(row);

                            } else {

                                row =
                                        new Object[]{
                                                "أداة صحية",
                                                it.getName(),
                                                it.getGrade(),
                                                it.getQuantity(),
                                                String.format(
                                                        "%.2f",
                                                        it.getUnitPrice()
                                                ),
                                                String.format(
                                                        "%.2f",
                                                        it.getTotalPrice()
                                                ),
                                                it.getFormattedDate()
                                        };

                                // -----------------------------------------
                                // التفاصيل
                                // -----------------------------------------

                                sModel.addRow(row);

                                // -----------------------------------------
                                // NoDetails
                                //
                                // المرتجع لا يظهر
                                // -----------------------------------------

                                if (it.getUnitPrice() >= 0) {

                                    sModelNoDetails.addRow(row);
                                }
                            }
                        }
                    }
                };

        reloadItemTables.run();

        // =====================================================
        // أزرار أسفل الفاتورة
        // =====================================================

        JPanel extraButtonsPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                12,
                                8
                        )
                );

        extraButtonsPanel.setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );

        JButton invoicesBtn =
                new JButton(
                        "الفواتير"
                );

        JButton addItemBtn =
                new JButton(
                        "إضافة عنصر"
                );

        JButton addReturnBtn =
                new JButton(
                        "إضافة مرتجع"
                );

        JButton addExternalBtn =
                new JButton(
                        "عنصر خارجي"
                );

        for (JButton button :
                new JButton[]{
                        invoicesBtn,
                        addItemBtn,
                        addReturnBtn,
                        addExternalBtn
                }) {

            button.setPreferredSize(
                    new Dimension(
                            BUTTON_WIDTH,
                            BUTTON_HEIGHT
                    )
            );
        }

        showDetailsCheck.setPreferredSize(
                new Dimension(
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
        );

        extraButtonsPanel.add(
                invoicesBtn
        );

        extraButtonsPanel.add(
                addItemBtn
        );

        extraButtonsPanel.add(
                addReturnBtn
        );

        extraButtonsPanel.add(
                addExternalBtn
        );

        extraButtonsPanel.add(
                showDetailsCheck
        );

        // =====================================================
        // الإجمالي
        // =====================================================

        JLabel lblTotal =
                new JLabel(
                        "",
                        SwingConstants.CENTER
                );

        lblTotal.setFont(
                lblTotal.getFont()
                        .deriveFont(
                                Font.BOLD,
                                (float) TOTAL_FONT_SIZE
                        )
        );

        JPanel bottomPanel =
                new JPanel();

        bottomPanel.setLayout(
                new BoxLayout(
                        bottomPanel,
                        BoxLayout.Y_AXIS
                )
        );

        bottomPanel.add(
                extraButtonsPanel
        );

        bottomPanel.add(
                lblTotal
        );

        dlg.add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        // =====================================================
        // تحديث الإجماليات
        // =====================================================

        Runnable refreshTotals =
                () -> {

                    double total =
                            invoice.getTotal();

                    double discount =
                            invoice.getDiscount();

                    double paid =
                            invoice.getPaymentsTotal();

                    double remaining =
                            invoice.getRemaining();

                    String remainingStatus;

                    if (Math.abs(remaining) < EPS) {

                        remainingStatus =
                                "(مدفوع بالكامل)";

                    } else if (remaining > 0) {

                        remainingStatus =
                                "(عليه دفع)";

                    } else {

                        remainingStatus =
                                "(رصيد له)";
                    }

                    lblTotal.setText(
                            String.format(
                                    "الإجمالي: %.2f | الحسم: %.2f | المدفوع: %.2f | الباقي: %.2f %s",
                                    total,
                                    discount,
                                    paid,
                                    Math.abs(remaining),
                                    remainingStatus
                            )
                    );

                    refreshItemTables(
                            reloadItemTables
                    );
                };

        // =====================================================
        // حذف سطر البلاط
        // =====================================================

        addDeleteLineShortcut(
                tilesTable,
                () ->
                        deleteSelectedInvoiceLine(
                                dlg,
                                invoice,
                                tilesTable,
                                displayedTileItemsRef,
                                reloadItemTables,
                                refreshTotals
                        )
        );

        // =====================================================
        // حذف سطر الصحية
        // =====================================================

        addDeleteLineShortcut(
                sanitaryTable,
                () ->
                        deleteSelectedInvoiceLine(
                                dlg,
                                invoice,
                                sanitaryTable,
                                displayedSanitaryItemsRef,
                                reloadItemTables,
                                refreshTotals
                        )
        );

        // =====================================================
        // تعديل العميل
        // =====================================================

        editCustomerBtn.addActionListener(
                e -> {

                    JTextField nameField =
                            new JTextField(
                                    invoice.getCustomerName(),
                                    20
                            );

                    JTextField phoneField =
                            new JTextField(
                                    invoice.getCustomerPhone(),
                                    20
                            );

                    JPanel panel =
                            new JPanel(
                                    new GridLayout(
                                            2,
                                            2,
                                            5,
                                            5
                                    )
                            );

                    panel.setComponentOrientation(
                            ComponentOrientation.RIGHT_TO_LEFT
                    );

                    panel.add(
                            new JLabel(
                                    "اسم العميل:"
                            )
                    );

                    panel.add(
                            nameField
                    );

                    panel.add(
                            new JLabel(
                                    "رقم الهاتف:"
                            )
                    );

                    panel.add(
                            phoneField
                    );

                    int result =
                            JOptionPane.showConfirmDialog(
                                    dlg,
                                    panel,
                                    "تعديل بيانات العميل",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE
                            );

                    if (
                            result
                                    == JOptionPane.OK_OPTION
                    ) {

                        String newName =
                                nameField.getText()
                                        .trim();

                        String newPhone =
                                phoneField.getText()
                                        .trim();

                        if (newName.isEmpty()) {

                            JOptionPane.showMessageDialog(
                                    dlg,
                                    "اسم العميل لا يمكن أن يكون فارغًا",
                                    "خطأ",
                                    JOptionPane.ERROR_MESSAGE
                            );

                            return;
                        }

                        invoice.setCustomerName(
                                newName
                        );

                        invoice.setCustomerPhone(
                                newPhone
                        );

                        lblName.setText(
                                "اسم العميل: "
                                        + newName
                        );

                        lblPhone.setText(
                                "الهاتف: "
                                        + newPhone
                        );

                        invoiceService.saveToFile();
                    }
                }
        );

        // =====================================================
        // تعديل التاريخ
        // =====================================================

        editPayDateBtn.addActionListener(
                e -> {

                    String input =
                            JOptionPane.showInputDialog(
                                    dlg,
                                    "أدخل التاريخ (yyyy/MM/dd HH:mm)",
                                    lblPayDate.getText()
                            );

                    if (
                            input != null
                                    &&
                                    !input.isBlank()
                    ) {

                        try {

                            LocalDateTime.parse(
                                    input,
                                    dtf
                            );

                            lblPayDate.setText(
                                    input
                            );

                        } catch (Exception ex) {

                            JOptionPane.showMessageDialog(
                                    dlg,
                                    "صيغة غير صحيحة",
                                    "خطأ",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }
        );

        // =====================================================
        // تعديل الحسم
        // =====================================================

        editDiscountBtn.addActionListener(
                e -> {

                    String currentDiscountStr =
                            String.format(
                                    "%.2f",
                                    invoice.getDiscount()
                            );

                    String input =
                            JOptionPane.showInputDialog(
                                    dlg,
                                    "أدخل قيمة الحسم الجديدة:",
                                    currentDiscountStr
                            );

                    if (input == null) {
                        return;
                    }

                    try {

                        double newDiscount =
                                Double.parseDouble(
                                        input.trim()
                                                .replace(
                                                        ',',
                                                        '.'
                                                )
                                );

                        if (newDiscount < 0) {

                            JOptionPane.showMessageDialog(
                                    dlg,
                                    "قيمة الحسم لا يمكن أن تكون سالبة.",
                                    "خطأ",
                                    JOptionPane.ERROR_MESSAGE
                            );

                            return;
                        }

                        if (
                                newDiscount
                                        > invoice.getTotal()
                        ) {

                            int choice =
                                    JOptionPane.showConfirmDialog(
                                            dlg,
                                            "قيمة الحسم أكبر من إجمالي الفاتورة. هل أنت متأكد؟",
                                            "تأكيد",
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.WARNING_MESSAGE
                                    );

                            if (
                                    choice
                                            != JOptionPane.YES_OPTION
                            ) {
                                return;
                            }
                        }

                        invoice.setDiscount(
                                newDiscount
                        );

                        invoiceService.saveToFile();

                        refreshTotals.run();

                    } catch (
                            NumberFormatException ex
                    ) {

                        JOptionPane.showMessageDialog(
                                dlg,
                                "قيمة الحسم غير صحيحة.",
                                "خطأ",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
        );

        // =====================================================
        // إضافة عنصر
        // =====================================================

        addItemBtn.addActionListener(
                e ->
                        new InvoiceItemDialog(
                                dlg,
                                invoice,
                                invoiceService,
                                tileService,
                                sanitaryService,
                                warehouseManager,
                                false,
                                lblPayDate.getText(),
                                reloadItemTables,
                                refreshTotals
                        ).show()
        );

        // =====================================================
        // إضافة مرتجع
        // =====================================================

        addReturnBtn.addActionListener(
                e ->
                        new InvoiceItemDialog(
                                dlg,
                                invoice,
                                invoiceService,
                                tileService,
                                sanitaryService,
                                warehouseManager,
                                true,
                                lblPayDate.getText(),
                                reloadItemTables,
                                refreshTotals
                        ).show()
        );

        // =====================================================
        // إضافة عنصر خارجي
        // =====================================================

        addExternalBtn.addActionListener(
                e ->
                        new InvoiceExternalItemDialog(
                                dlg,
                                invoice,
                                invoiceService,
                                reloadItemTables,
                                refreshTotals
                        ).show()
        );

        // =====================================================
        // الفواتير / الدفعات
        // =====================================================

        invoicesBtn.addActionListener(
                e ->
                        new InvoicePaymentsDialog(
                                dlg,
                                invoice,
                                invoiceService,
                                currencySettingsService,
                                dtf,
                                refreshTotals
                        ).show()
        );

        // =====================================================
        // نافذة جدول البلاط
        //
        // نستخدم invoice.getItems() مباشرة للحفاظ على الترتيب
        // =====================================================

        Runnable showTilesPopup =
                () -> {

                    JDialog popup =
                            new JDialog(
                                    dlg,
                                    "جدول البلاط - الفاتورة رقم: "
                                            + invoice.getInvoiceNumber(),
                                    Dialog.ModalityType.MODELESS
                            );

                    popup.setLayout(
                            new BorderLayout(
                                    8,
                                    8
                            )
                    );

                    popup.setResizable(
                            true
                    );

                    DefaultTableModel popupModel =
                            new DefaultTableModel(
                                    tCols,
                                    0
                            );

                    JTable popupTable =
                            new JTable(
                                    popupModel
                            );

                    popupTable.setRowHeight(
                            TABLE_ROW_HEIGHT
                    );

                    popupTable.setAutoResizeMode(
                            JTable.AUTO_RESIZE_ALL_COLUMNS
                    );

                    popupTable.setFillsViewportHeight(
                            true
                    );

                    popupTable.getTableHeader()
                            .setReorderingAllowed(
                                    false
                            );

                    for (InvoiceItem it :
                            invoice.getItems()) {

                        if (!it.isTile()) {
                            continue;
                        }

                        popupModel.addRow(
                                new Object[]{
                                        it.getCode(),
                                        it.getName(),
                                        it.getGrade(),
                                        it.getQuantity(),
                                        it.getBoxArea(),
                                        String.format(
                                                "%.2f",
                                                it.getBoxArea()
                                                        * it.getQuantity()
                                        ),
                                        String.format(
                                                "%.2f",
                                                it.getUnitPrice()
                                        ),
                                        String.format(
                                                "%.2f",
                                                it.getTotalPrice()
                                        ),
                                        it.getFormattedDate()
                                }
                        );
                    }

                    popup.add(
                            new JScrollPane(
                                    popupTable
                            ),
                            BorderLayout.CENTER
                    );

                    popup.setMinimumSize(
                            new Dimension(
                                    900,
                                    400
                            )
                    );

                    popup.setSize(
                            1200,
                            550
                    );

                    popup.setLocationRelativeTo(
                            dlg
                    );

                    popup.setVisible(
                            true
                    );
                };

        // =====================================================
        // نافذة جدول الأدوات الصحية
        //
        // العناصر الخارجية تظهر دائمًا
        // والترتيب مطابق لترتيب invoice.getItems()
        // =====================================================

        Runnable showSanitaryPopup =
                () -> {

                    JDialog popup =
                            new JDialog(
                                    dlg,
                                    "جدول الأدوات الصحية - الفاتورة رقم: "
                                            + invoice.getInvoiceNumber(),
                                    Dialog.ModalityType.MODELESS
                            );

                    popup.setLayout(
                            new BorderLayout(
                                    8,
                                    8
                            )
                    );

                    popup.setResizable(
                            true
                    );

                    DefaultTableModel popupModel =
                            new DefaultTableModel(
                                    sCols,
                                    0
                            );

                    JTable popupTable =
                            new JTable(
                                    popupModel
                            );

                    popupTable.setRowHeight(
                            TABLE_ROW_HEIGHT
                    );

                    popupTable.setAutoResizeMode(
                            JTable.AUTO_RESIZE_ALL_COLUMNS
                    );

                    popupTable.setFillsViewportHeight(
                            true
                    );

                    popupTable.getTableHeader()
                            .setReorderingAllowed(
                                    false
                            );

                    for (InvoiceItem it :
                            invoice.getItems()) {

                        if (it.isTile()) {
                            continue;
                        }

                        if (it.isExternalItem()) {

                            popupModel.addRow(
                                    new Object[]{
                                            "عنصر خارجي",
                                            it.getName(),
                                            "-",
                                            it.getQuantity(),
                                            String.format(
                                                    "%.2f",
                                                    it.getUnitPrice()
                                            ),
                                            String.format(
                                                    "%.2f",
                                                    it.getTotalPrice()
                                            ),
                                            it.getFormattedDate()
                                    }
                            );

                        } else {

                            popupModel.addRow(
                                    new Object[]{
                                            "أداة صحية",
                                            it.getName(),
                                            it.getGrade(),
                                            it.getQuantity(),
                                            String.format(
                                                    "%.2f",
                                                    it.getUnitPrice()
                                            ),
                                            String.format(
                                                    "%.2f",
                                                    it.getTotalPrice()
                                            ),
                                            it.getFormattedDate()
                                    }
                            );
                        }
                    }

                    popup.add(
                            new JScrollPane(
                                    popupTable
                            ),
                            BorderLayout.CENTER
                    );

                    popup.setMinimumSize(
                            new Dimension(
                                    800,
                                    400
                            )
                    );

                    popup.setSize(
                            1100,
                            550
                    );

                    popup.setLocationRelativeTo(
                            dlg
                    );

                    popup.setVisible(
                            true
                    );
                };

        // =====================================================
        // أحداث أزرار عرض الجداول
        // =====================================================

        showTilesBtn.addActionListener(
                e ->
                        showTilesPopup.run()
        );

        showSanitaryBtn.addActionListener(
                e ->
                        showSanitaryPopup.run()
        );

        // =====================================================
        // تحديث أولي
        // =====================================================

        refreshTotals.run();

        // =====================================================
        // حجم النافذة الرئيسية
        // =====================================================

        dlg.setMinimumSize(
                new Dimension(
                        1100,
                        700
                )
        );

        dlg.setSize(
                WINDOW_WIDTH,
                WINDOW_HEIGHT
        );

        dlg.setLocationRelativeTo(
                owner
        );

        dlg.setVisible(
                true
        );
    }


    // =========================================================
    // أدوات مساعدة
    // =========================================================

    private void refreshItemTables(
            Runnable reloadItemTables) {

        reloadItemTables.run();
    }

    private void addDeleteLineShortcut(
            JTable table,
            Runnable action) {

        KeyStroke ks =
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_D,
                        KeyEvent.CTRL_DOWN_MASK
                                |
                                KeyEvent.SHIFT_DOWN_MASK
                );

        table.getInputMap(
                JComponent.WHEN_FOCUSED
        ).put(
                ks,
                "deleteLine"
        );

        table.getActionMap()
                .put(
                        "deleteLine",
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(
                                    ActionEvent e) {
                                action.run();
                            }
                        }
                );
    }

    private void deleteSelectedInvoiceLine(
            Window parent,
            Invoice inv,
            JTable table,
            List<InvoiceItem>[] displayedItemsRef,
            Runnable reloadItemTables,
            Runnable refreshTotals) {

        int row =
                table.getSelectedRow();

        if (row < 0) {

            JOptionPane.showMessageDialog(
                    parent,
                    "اختر سطرًا أولاً.",
                    "تنبيه",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (
                row
                        >= displayedItemsRef[0].size()
        ) {
            return;
        }

        InvoiceItem selected =
                displayedItemsRef[0]
                        .get(row);

        // =====================================================
        // العنصر الخارجي
        // =====================================================

        if (selected.isExternalItem()) {

            int deleteChoice =
                    JOptionPane.showConfirmDialog(
                            parent,
                            "هل أنت متأكد من حذف العنصر الخارجي؟\n"
                                    + selected.getName()
                                    + " - العدد: "
                                    + selected.getQuantity(),
                            "حذف عنصر خارجي",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

            if (
                    deleteChoice
                            != JOptionPane.YES_OPTION
            ) {
                return;
            }

            inv.getItems()
                    .remove(selected);

            invoiceService.saveToFile();

            reloadItemTables.run();
            refreshTotals.run();

            return;
        }

        boolean isReturnItem =
                selected.getUnitPrice() < 0;

        boolean isTileItem =
                selected.isTile();

        TileItem matchedTile =
                isTileItem
                        ? tileService.findById(
                        selected.getIdNumber()
                )
                        : null;

        SanitaryItem matchedSanitary =
                isTileItem
                        ? null
                        : sanitaryService.findById(
                        selected.getIdNumber()
                );

        boolean itemFound =
                matchedTile != null
                        ||
                        matchedSanitary != null;

        boolean returnToStock =
                false;

        if (
                !isReturnItem
                        &&
                        itemFound
        ) {

            int returnChoice =
                    JOptionPane.showConfirmDialog(
                            parent,
                            "تم العثور على العنصر في المستودع.\n"
                                    + "هل تريد إعادة الكمية إلى المخزون؟",
                            "إرجاع للمخزون",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

            returnToStock =
                    returnChoice
                            == JOptionPane.YES_OPTION;
        }

        if (!itemFound) {

            int missingChoice =
                    JOptionPane.showConfirmDialog(
                            parent,
                            "لم يتم العثور على العنصر في المستودع.\n"
                                    + "هل تريد الحذف على الرغم من عدم وجود العنصر؟",
                            "العنصر غير موجود",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

            if (
                    missingChoice
                            != JOptionPane.YES_OPTION
            ) {
                return;
            }
        }

        int deleteChoice =
                JOptionPane.showConfirmDialog(
                        parent,
                        "هل أنت متأكد من حذف هذا السطر من الفاتورة؟\n"
                                + selected.getName()
                                + " - "
                                + selected.getGrade(),
                        "تأكيد الحذف",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (
                deleteChoice
                        != JOptionPane.YES_OPTION
        ) {
            return;
        }

        inv.getItems()
                .remove(selected);

        double quantity =
                selected.getQuantity();

        try {

            if (
                    !isReturnItem
                            &&
                            returnToStock
                            &&
                            itemFound
            ) {

                if (matchedTile != null) {

                    matchedTile.setBoxes(
                            matchedTile.getBoxes()
                                    + quantity
                    );

                    tileService.save();

                } else if (matchedSanitary != null) {

                    int qty =
                            (int) Math.round(
                                    quantity
                            );

                    matchedSanitary.setQuantity(
                            matchedSanitary.getQuantity()
                                    + qty
                    );

                    sanitaryService.save();
                }
            }

            if (
                    isReturnItem
                            &&
                            itemFound
            ) {

                if (matchedTile != null) {

                    matchedTile.setBoxes(
                            matchedTile.getBoxes()
                                    - quantity
                    );

                    if (
                            matchedTile.getBoxes()
                                    < 0
                    ) {

                        matchedTile.setBoxes(
                                0
                        );
                    }

                    tileService.save();

                } else if (matchedSanitary != null) {

                    int qty =
                            (int) Math.round(
                                    quantity
                            );

                    int newQty =
                            matchedSanitary.getQuantity()
                                    - qty;

                    matchedSanitary.setQuantity(
                            Math.max(
                                    newQty,
                                    0
                            )
                    );

                    sanitaryService.save();
                }
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    parent,
                    "حدث خطأ أثناء تحديث المخزون: "
                            + ex.getMessage(),
                    "خطأ",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        invoiceService.saveToFile();

        reloadItemTables.run();
        refreshTotals.run();
    }
// =========================================================
// نافذة عرض جدول البلاط
// =========================================================

    private void showTilesPopup(Window owner) {

        JDialog popup =
                new JDialog(
                        owner,
                        "البلاط المضاف إلى الفاتورة",
                        Dialog.ModalityType.MODELESS
                );

        popup.setLayout(
                new BorderLayout(
                        8,
                        8
                )
        );

        // =====================================================
        // نفس أعمدة جدول البلاط الموجود في الفاتورة
        // =====================================================

        String[] columns = {
                "الرمز",
                "الاسم",
                "النخب",
                "عدد الصناديق",
                "مساحة الصندوق",
                "المساحة الإجمالية",
                "السعر",
                "الإجمالي",
                "التاريخ"
        };

        DefaultTableModel model =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        JTable table =
                new JTable(model);

        table.setRowHeight(
                TABLE_ROW_HEIGHT
        );

        table.setFillsViewportHeight(
                true
        );

        table.setAutoResizeMode(
                JTable.AUTO_RESIZE_ALL_COLUMNS
        );

        table.getTableHeader()
                .setReorderingAllowed(
                        false
                );

        // =====================================================
        // إضافة البلاط بنفس ترتيب الفاتورة
        // =====================================================

        for (InvoiceItem item :
                invoice.getItems()) {

            if (!item.isTile()) {
                continue;
            }

            model.addRow(
                    new Object[]{
                            item.getCode(),
                            item.getName(),
                            item.getGrade(),
                            item.getQuantity(),
                            item.getBoxArea(),
                            String.format(
                                    "%.2f",
                                    item.getBoxArea()
                                            * item.getQuantity()
                            ),
                            String.format(
                                    "%.2f",
                                    item.getUnitPrice()
                            ),
                            String.format(
                                    "%.2f",
                                    item.getTotalPrice()
                            ),
                            item.getFormattedDate()
                    }
            );
        }

        popup.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        // =====================================================
        // حجم قابل للتحكم من المستخدم
        // =====================================================

        popup.setMinimumSize(
                new Dimension(
                        900,
                        400
                )
        );

        popup.setSize(
                1100,
                500
        );

        popup.setLocationRelativeTo(
                owner
        );

        popup.setVisible(
                true
        );
    }

    // =========================================================

// نافذة عرض الأدوات الصحية
// =========================================================

    private void showSanitaryPopup(Window owner) {

        JDialog popup =
                new JDialog(
                        owner,
                        "الأدوات الصحية المضافة إلى الفاتورة",
                        Dialog.ModalityType.MODELESS
                );

        popup.setLayout(
                new BorderLayout(
                        8,
                        8
                )
        );

        // =====================================================
        // نفس أعمدة جدول الأدوات الصحية
        // =====================================================

        String[] columns = {
                "النوع",
                "الاسم",
                "النخب",
                "الكمية",
                "السعر",
                "الإجمالي",
                "التاريخ"
        };

        DefaultTableModel model =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        JTable table =
                new JTable(model);

        table.setRowHeight(
                TABLE_ROW_HEIGHT
        );

        table.setFillsViewportHeight(
                true
        );

        table.setAutoResizeMode(
                JTable.AUTO_RESIZE_ALL_COLUMNS
        );

        table.getTableHeader()
                .setReorderingAllowed(
                        false
                );

        // =====================================================
        // إضافة الأدوات بنفس ترتيب الفاتورة
        // =====================================================

        for (InvoiceItem item :
                invoice.getItems()) {

            if (item.isTile()) {
                continue;
            }

            String type;

            if (item.isExternalItem()) {

                type = "عنصر خارجي";

            } else {

                type = "أداة صحية";
            }

            model.addRow(
                    new Object[]{
                            type,
                            item.getName(),
                            item.isExternalItem()
                                    ? "-"
                                    : item.getGrade(),
                            item.getQuantity(),
                            String.format(
                                    "%.2f",
                                    item.getUnitPrice()
                            ),
                            String.format(
                                    "%.2f",
                                    item.getTotalPrice()
                            ),
                            item.getFormattedDate()
                    }
            );
        }

        popup.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        // =====================================================
        // حجم النافذة
        // =====================================================

        popup.setMinimumSize(
                new Dimension(
                        800,
                        400
                )
        );

        popup.setSize(
                1000,
                500
        );

        popup.setLocationRelativeTo(
                owner
        );

        popup.setVisible(
                true
        );
    }


}