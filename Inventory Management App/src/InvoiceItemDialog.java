import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InvoiceItemDialog {

    private static final double EPS = 1e-6;

    private final Window owner;
    private final Invoice invoice;

    private final InvoiceService invoiceService;
    private final TileService tileService;
    private final SanitaryService sanitaryService;
    private final WarehouseManager warehouseManager;

    private final boolean isReturn;
    private final String dateString;

    private final Runnable reloadItemTables;
    private final Runnable refreshTotals;

    public InvoiceItemDialog(
            Window owner,
            Invoice invoice,
            InvoiceService invoiceService,
            TileService tileService,
            SanitaryService sanitaryService,
            WarehouseManager warehouseManager,
            boolean isReturn,
            String dateString,
            Runnable reloadItemTables,
            Runnable refreshTotals) {

        this.owner = owner;
        this.invoice = invoice;
        this.invoiceService = invoiceService;
        this.tileService = tileService;
        this.sanitaryService = sanitaryService;
        this.warehouseManager = warehouseManager;
        this.isReturn = isReturn;
        this.dateString = dateString;
        this.reloadItemTables = reloadItemTables;
        this.refreshTotals = refreshTotals;
    }

    public void show() {

        JDialog dlg =
                new JDialog(
                        owner,
                        isReturn
                                ? "إضافة مرتجع"
                                : "إضافة عنصر",
                        Dialog.ModalityType.APPLICATION_MODAL
                );

        dlg.setLayout(
                new BorderLayout(
                        5,
                        5
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

        // =========================================================
        // التاريخ
        // =========================================================

        final DateTimeFormatter dtf =
                DateTimeFormatter.ofPattern(
                        "yyyy/MM/dd HH:mm"
                );

        final LocalDateTime itemDate;
        LocalDateTime itemDate1;

        try {

            itemDate1 =
                    LocalDateTime.parse(
                            dateString,
                            dtf
                    );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    dlg,
                    "تاريخ الإضافة غير صالح، سيتم استخدام التاريخ الحالي.",
                    "تحذير",
                    JOptionPane.WARNING_MESSAGE
            );

            itemDate1 =
                    LocalDateTime.now();
        }

        // =========================================================
        // حالة الفلتر
        // =========================================================

        itemDate = itemDate1;
        final TileFilter currentFilter =
                new TileFilter();

        final TileFilterService tileFilterService =
                new TileFilterService();

        // =========================================================
        // نوع العنصر
        // =========================================================

        JPanel filtersPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                8,
                                5
                        )
                );

        JRadioButton tileRadio =
                new JRadioButton(
                        "بلاط",
                        true
                );

        JRadioButton sanitaryRadio =
                new JRadioButton(
                        "أدوات صحية"
                );

        ButtonGroup group =
                new ButtonGroup();

        group.add(tileRadio);
        group.add(sanitaryRadio);

        filtersPanel.add(
                tileRadio
        );

        filtersPanel.add(
                sanitaryRadio
        );

        // =========================================================
        // البحث + تطبيق + فلترة
        // =========================================================

        JPanel searchRow =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                8,
                                0
                        )
                );

        JTextField searchField =
                new JTextField(18);

        JButton applyBtn =
                new JButton(
                        "تطبيق"
                );

        JButton filterButton =
                new JButton(
                        "فلترة ▼"
                );

        filterButton.setFocusPainted(
                false
        );

        searchRow.add(
                new JLabel(
                        "بحث:"
                )
        );

        searchRow.add(
                searchField
        );

        searchRow.add(
                applyBtn
        );

        if (!isReturn) {

            searchRow.add(
                    filterButton
            );
        }

        // =========================================================
        // جدول العناصر
        // =========================================================

        String[] invCols;

        if (isReturn) {

            invCols =
                    new String[]{
                            "الرمز",
                            "الاسم",
                            "النخب",
                            "متاح للإرجاع",
                            "السعر الأصلي"
                    };

        } else {

            invCols =
                    new String[]{
                            "الرمز",
                            "الاسم",
                            "النخب",
                            "متوفر",
                            "مساحة الصندوق",
                            "السعر"
                    };
        }

        DefaultTableModel invModel =
                new DefaultTableModel(
                        invCols,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        JTable invTable =
                new JTable(
                        invModel
                );

        invTable.setRowHeight(
                24
        );

        invTable.setFillsViewportHeight(
                true
        );

        invTable.getTableHeader()
                .setReorderingAllowed(
                        false
                );

        // =========================================================
        // زر التأكيد
        // =========================================================

        JButton confirmAddBtn =
                new JButton(
                        isReturn
                                ? "إرجاع العنصر المحدد"
                                : "إضافة إلى الفاتورة"
                );

        // =========================================================
        // الجزء العلوي
        // =========================================================

        JPanel north =
                new JPanel();

        north.setLayout(
                new BoxLayout(
                        north,
                        BoxLayout.Y_AXIS
                )
        );

        north.add(
                filtersPanel
        );

        north.add(
                searchRow
        );

        dlg.add(
                north,
                BorderLayout.NORTH
        );

        dlg.add(
                new JScrollPane(
                        invTable
                ),
                BorderLayout.CENTER
        );

        dlg.add(
                confirmAddBtn,
                BorderLayout.SOUTH
        );

        // =========================================================
        // قائمة العناصر المعروضة
        // =========================================================

        final List<Object>[] displayedForDialogRef =
                new List[]{
                        new ArrayList<>()
                };

        // =========================================================
        // سطر المرتجع
        // =========================================================

        class ReturnableLine {

            long idNumber;

            boolean isTile;

            String code;

            String name;

            String grade;

            double boxArea;

            double unitPrice;

            double availableToReturn;
        }

        // =========================================================
        // تحديث زر الفلترة
        // =========================================================

        final Runnable updateFilterButtonText =
                () -> {

                    if (isReturn) {
                        return;
                    }

                    int count = 0;

                    if (!currentFilter
                            .getMaterials()
                            .isEmpty()) {

                        count++;
                    }

                    if (!currentFilter
                            .getSubTypes()
                            .isEmpty()) {

                        count++;
                    }

                    if (!currentFilter
                            .getWarehouses()
                            .isEmpty()) {

                        count++;
                    }

                    if (!currentFilter
                            .getLocations()
                            .isEmpty()) {

                        count++;
                    }

                    if (!currentFilter
                            .getGrades()
                            .isEmpty()) {

                        count++;
                    }

                    if (!currentFilter
                            .getBoxAreas()
                            .isEmpty()) {

                        count++;
                    }

                    if (count == 0) {

                        filterButton.setText(
                                "فلترة ▼"
                        );

                    } else {

                        filterButton.setText(
                                "فلترة (" +
                                        count +
                                        ") ▼"
                        );
                    }
                };

        // =========================================================
        // إعادة تحميل الجدول
        // =========================================================

        final Runnable reloadInventory =
                () -> {

                    invModel.setRowCount(
                            0
                    );

                    displayedForDialogRef[0] =
                            new ArrayList<>();

                    String q =
                            searchField
                                    .getText()
                                    .trim()
                                    .toLowerCase();

                    // =================================================
                    // المرتجع
                    // =================================================

                    if (isReturn) {

                        List<ReturnableLine> lines =
                                new ArrayList<>();

                        for (
                                InvoiceItem it :
                                invoice.getItems()
                        ) {

                            if (
                                    it.getUnitPrice()
                                            < 0
                            ) {
                                continue;
                            }

                            boolean isTileItem =
                                    it.isTile();

                            if (
                                    (
                                            tileRadio.isSelected()
                                                    &&
                                                    !isTileItem
                                    )
                                            ||
                                            (
                                                    sanitaryRadio.isSelected()
                                                            &&
                                                            isTileItem
                                            )
                            ) {
                                continue;
                            }

                            boolean passesSearch =
                                    q.isEmpty()
                                            ||
                                            containsIgnoreCase(
                                                    it.getName(),
                                                    q
                                            )
                                            ||
                                            (
                                                    isTileItem
                                                            &&
                                                            containsIgnoreCase(
                                                                    it.getCode(),
                                                                    q
                                                            )
                                            );

                            if (!passesSearch) {
                                continue;
                            }

                            double alreadyReturned =
                                    invoice.getItems()
                                            .stream()
                                            .filter(
                                                    x ->
                                                            x.getIdNumber()
                                                                    ==
                                                                    it.getIdNumber()
                                                                    &&
                                                                    x.getUnitPrice()
                                                                            < 0
                                            )
                                            .mapToDouble(
                                                    InvoiceItem::getQuantity
                                            )
                                            .sum();

                            double available =
                                    it.getQuantity()
                                            -
                                            alreadyReturned;

                            if (
                                    available
                                            > EPS
                            ) {

                                ReturnableLine rl =
                                        new ReturnableLine();

                                rl.idNumber =
                                        it.getIdNumber();

                                rl.isTile =
                                        isTileItem;

                                rl.code =
                                        it.getCode();

                                rl.name =
                                        it.getName();

                                rl.grade =
                                        it.getGrade();

                                rl.boxArea =
                                        isTileItem
                                                ? it.getBoxArea()
                                                : 0.0;

                                rl.unitPrice =
                                        Math.abs(
                                                it.getUnitPrice()
                                        );

                                rl.availableToReturn =
                                        available;

                                lines.add(
                                        rl
                                );
                            }
                        }

                        for (
                                ReturnableLine rl :
                                lines
                        ) {

                            displayedForDialogRef[0]
                                    .add(rl);

                            invModel.addRow(
                                    new Object[]{
                                            rl.code,
                                            rl.name,
                                            rl.grade,
                                            String.format(
                                                    "%.2f",
                                                    rl.availableToReturn
                                            ),
                                            String.format(
                                                    "%.2f",
                                                    rl.unitPrice
                                            )
                                    }
                            );
                        }

                    }

                    // =================================================
                    // إضافة عنصر
                    // =================================================

                    else {

                        // ---------------------------------------------
                        // البلاط
                        // ---------------------------------------------

                        if (
                                tileRadio.isSelected()
                        ) {

                            List<TileItem> items =
                                    tileFilterService
                                            .filterAndSearch(
                                                    tileService.getAll(),
                                                    currentFilter,
                                                    q
                                            );

                            for (
                                    TileItem it :
                                    items
                            ) {

                                displayedForDialogRef[0]
                                        .add(it);

                                invModel.addRow(
                                        new Object[]{
                                                it.getCode(),
                                                it.getName(),
                                                it.getGrade(),
                                                String.format(
                                                        "%.2f",
                                                        it.getBoxes()
                                                ),
                                                it.getBoxArea(),
                                                it.getPrice()
                                        }
                                );
                            }

                            updateFilterButtonText.run();

                        }

                        // ---------------------------------------------
                        // الأدوات الصحية
                        // ---------------------------------------------

                        else {

                            List<SanitaryItem> items =
                                    sanitaryService
                                            .getAll()
                                            .stream()
                                            .filter(
                                                    it ->
                                                            q.isEmpty()
                                                                    ||
                                                                    containsIgnoreCase(
                                                                            it.getName(),
                                                                            q
                                                                    )
                                            )
                                            .toList();

                            for (
                                    SanitaryItem it :
                                    items
                            ) {

                                displayedForDialogRef[0]
                                        .add(it);

                                invModel.addRow(
                                        new Object[]{
                                                "",
                                                it.getName(),
                                                it.getGrade(),
                                                it.getQuantity(),
                                                "",
                                                it.getPrice()
                                        }
                                );
                            }
                        }
                    }

                    if (
                            invModel.getRowCount()
                                    > 0
                    ) {

                        invTable.setRowSelectionInterval(
                                0,
                                0
                        );
                    }
                };

        // =========================================================
        // Popup الفلتر
        // =========================================================

        final JPopupMenu filterPopup =
                new JPopupMenu();

        filterPopup.setLayout(
                new BorderLayout()
        );

        final TileFilterPanel tileFilterPanel =
                new TileFilterPanel(
                        tileService.getAll(),
                        warehouseManager.getWarehouses(),
                        currentFilter
                );

        tileFilterPanel.setOnFilterApplied(
                () -> {

                    reloadInventory.run();

                    updateFilterButtonText.run();

                    filterPopup.setVisible(
                            false
                    );
                }
        );

        tileFilterPanel.setOnClearApplied(
                () -> {

                    reloadInventory.run();

                    updateFilterButtonText.run();
                }
        );

        filterPopup.add(
                tileFilterPanel,
                BorderLayout.CENTER
        );

        // =========================================================
        // فتح / إغلاق Popup
        // =========================================================

        if (!isReturn) {

            filterButton.addActionListener(
                    e -> {

                        if (
                                filterPopup.isVisible()
                        ) {

                            filterPopup.setVisible(
                                    false
                            );

                        } else {

                            filterPopup.show(
                                    filterButton,
                                    0,
                                    filterButton.getHeight()
                            );
                        }
                    }
            );
        }

        // =========================================================
        // تغيير النوع
        // =========================================================

        tileRadio.addActionListener(
                e -> {

                    if (!isReturn) {

                        filterButton.setVisible(
                                true
                        );
                    }

                    reloadInventory.run();
                }
        );

        sanitaryRadio.addActionListener(
                e -> {

                    if (!isReturn) {

                        filterPopup.setVisible(
                                false
                        );

                        filterButton.setVisible(
                                false
                        );
                    }

                    reloadInventory.run();
                }
        );

        // =========================================================
        // زر تطبيق
        // =========================================================

        applyBtn.addActionListener(
                e ->
                        reloadInventory.run()
        );

        // =========================================================
        // Enter
        // =========================================================

        searchField.addActionListener(
                e ->
                        reloadInventory.run()
        );

        // =========================================================
        // البحث اللحظي
        // =========================================================

        searchField.getDocument()
                .addDocumentListener(
                        new SimpleDocumentListener(
                                reloadInventory::run
                        )
                );

        // =========================================================
        // أول تحميل
        // =========================================================

        reloadInventory.run();

        // =========================================================
        // إضافة / إرجاع العنصر
        // =========================================================

        confirmAddBtn.addActionListener(
                e -> {

                    int r =
                            invTable.getSelectedRow();

                    if (r < 0) {

                        JOptionPane.showMessageDialog(
                                dlg,
                                "اختر عنصرًا أولًا",
                                "تنبيه",
                                JOptionPane.WARNING_MESSAGE
                        );

                        return;
                    }

                    boolean tile =
                            tileRadio.isSelected();

                    // =================================================
                    // مرتجع
                    // =================================================

                    if (isReturn) {

                        ReturnableLine sel =
                                (ReturnableLine)
                                        displayedForDialogRef[0]
                                                .get(r);

                        String qtyStr =
                                JOptionPane.showInputDialog(
                                        dlg,
                                        "الكمية (متاحة للإرجاع: "
                                                +
                                                invModel.getValueAt(
                                                        r,
                                                        3
                                                )
                                                +
                                                "):",
                                        "1"
                                );

                        if (qtyStr == null) {
                            return;
                        }

                        String priceStr =
                                JOptionPane.showInputDialog(
                                        dlg,
                                        "سعر الوحدة:",
                                        String.valueOf(
                                                sel.unitPrice
                                        )
                                );

                        if (priceStr == null) {
                            return;
                        }

                        try {

                            double qty =
                                    Double.parseDouble(
                                            qtyStr
                                                    .trim()
                                                    .replace(
                                                            ',',
                                                            '.'
                                                    )
                                    );

                            double prc =
                                    Double.parseDouble(
                                            priceStr
                                                    .trim()
                                                    .replace(
                                                            ',',
                                                            '.'
                                                    )
                                    );

                            double availableToReturn =
                                    Double.parseDouble(
                                            invModel
                                                    .getValueAt(
                                                            r,
                                                            3
                                                    )
                                                    .toString()
                                    );

                            if (
                                    qty
                                            > availableToReturn
                                            + EPS
                                            ||
                                            qty <= 0
                            ) {

                                JOptionPane.showMessageDialog(
                                        dlg,
                                        "كمية غير صالحة للإرجاع",
                                        "خطأ",
                                        JOptionPane.ERROR_MESSAGE
                                );

                                return;
                            }

                            double finalUnitPrice =
                                    -Math.abs(
                                            prc
                                    );

                            if (sel.isTile) {

                                TileItem tileItem =
                                        tileService.findById(
                                                sel.idNumber
                                        );

                                if (tileItem != null) {

                                    tileItem.setBoxes(
                                            tileItem.getBoxes()
                                                    + qty
                                    );

                                    tileService.save();
                                }

                                InvoiceItem ii =
                                        new InvoiceItem(
                                                sel.idNumber,
                                                sel.name,
                                                qty,
                                                finalUnitPrice,
                                                sel.boxArea,
                                                true,
                                                sel.code,
                                                sel.grade,
                                                itemDate
                                        );

                                invoice.addItem(
                                        ii
                                );

                            } else {

                                SanitaryItem sanitaryItem =
                                        sanitaryService.findById(
                                                sel.idNumber
                                        );

                                if (sanitaryItem != null) {

                                    sanitaryItem.setQuantity(
                                            sanitaryItem.getQuantity()
                                                    +
                                                    (int) Math.round(qty)
                                    );

                                    sanitaryService.save();
                                }

                                InvoiceItem ii =
                                        new InvoiceItem(
                                                sel.idNumber,
                                                sel.name,
                                                qty,
                                                finalUnitPrice,
                                                false,
                                                sel.grade,
                                                itemDate
                                        );

                                invoice.addItem(
                                        ii
                                );
                            }

                            invoiceService.saveToFile();

                            reloadItemTables.run();

                            refreshTotals.run();

                            dlg.dispose();

                        } catch (Exception ex) {

                            JOptionPane.showMessageDialog(
                                    dlg,
                                    "قيمة كمية/سعر غير صحيحة",
                                    "خطأ",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }

                        return;
                    }

                    // =================================================
                    // إضافة عنصر عادي
                    // =================================================

                    Object obj =
                            displayedForDialogRef[0]
                                    .get(r);

                    // =================================================
                    // بلاط
                    // =================================================

                    if (tile) {

                        TileItem tileItem =
                                (TileItem) obj;

                        String qtyStr =
                                JOptionPane.showInputDialog(
                                        dlg,
                                        "الكمية:",
                                        "1"
                                );

                        if (qtyStr == null) {
                            return;
                        }

                        String priceStr =
                                JOptionPane.showInputDialog(
                                        dlg,
                                        "سعر الوحدة:",
                                        String.valueOf(
                                                tileItem.getPrice()
                                        )
                                );

                        if (priceStr == null) {
                            return;
                        }

                        try {

                            double qty =
                                    Double.parseDouble(
                                            qtyStr
                                                    .trim()
                                                    .replace(
                                                            ',',
                                                            '.'
                                                    )
                                    );

                            double prc =
                                    Double.parseDouble(
                                            priceStr
                                                    .trim()
                                                    .replace(
                                                            ',',
                                                            '.'
                                                    )
                                    );

                            if (qty <= 0) {

                                JOptionPane.showMessageDialog(
                                        dlg,
                                        "كمية غير صالحة",
                                        "خطأ",
                                        JOptionPane.ERROR_MESSAGE
                                );

                                return;
                            }

                            tileItem.setBoxes(
                                    tileItem.getBoxes()
                                            - qty
                            );

                            tileService.save();

                            InvoiceItem ii =
                                    new InvoiceItem(
                                            tileItem.getIdNumber(),
                                            tileItem.getName(),
                                            qty,
                                            prc,
                                            tileItem.getBoxArea(),
                                            true,
                                            tileItem.getCode(),
                                            tileItem.getGrade(),
                                            itemDate
                                    );

                            invoice.addItem(
                                    ii
                            );

                            invoiceService.saveToFile();

                            reloadItemTables.run();

                            refreshTotals.run();

                            dlg.dispose();

                        } catch (Exception ex) {

                            JOptionPane.showMessageDialog(
                                    dlg,
                                    "قيمة كمية/سعر غير صحيحة",
                                    "خطأ",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }

                    }

                    // =================================================
                    // أدوات صحية
                    // =================================================

                    else {

                        SanitaryItem sanitary =
                                (SanitaryItem) obj;

                        String qtyStr =
                                JOptionPane.showInputDialog(
                                        dlg,
                                        "الكمية:",
                                        "1"
                                );

                        if (qtyStr == null) {
                            return;
                        }

                        String priceStr =
                                JOptionPane.showInputDialog(
                                        dlg,
                                        "سعر الوحدة:",
                                        String.valueOf(
                                                sanitary.getPrice()
                                        )
                                );

                        if (priceStr == null) {
                            return;
                        }

                        try {

                            double qty =
                                    Double.parseDouble(
                                            qtyStr
                                                    .trim()
                                                    .replace(
                                                            ',',
                                                            '.'
                                                    )
                                    );

                            double prc =
                                    Double.parseDouble(
                                            priceStr
                                                    .trim()
                                                    .replace(
                                                            ',',
                                                            '.'
                                                    )
                                    );

                            if (qty <= 0) {

                                JOptionPane.showMessageDialog(
                                        dlg,
                                        "كمية غير صالحة",
                                        "خطأ",
                                        JOptionPane.ERROR_MESSAGE
                                );

                                return;
                            }

                            sanitary.setQuantity(
                                    sanitary.getQuantity()
                                            -
                                            (int) Math.round(qty)
                            );

                            sanitaryService.save();

                            InvoiceItem ii =
                                    new InvoiceItem(
                                            sanitary.getIdNumber(),
                                            sanitary.getName(),
                                            qty,
                                            prc,
                                            false,
                                            sanitary.getGrade(),
                                            itemDate
                                    );

                            invoice.addItem(
                                    ii
                            );

                            invoiceService.saveToFile();

                            reloadItemTables.run();

                            refreshTotals.run();

                            dlg.dispose();

                        } catch (Exception ex) {

                            JOptionPane.showMessageDialog(
                                    dlg,
                                    "قيمة كمية/سعر غير صحيحة",
                                    "خطأ",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }
        );

        // =========================================================
        // إعداد حجم النافذة
        // =========================================================

        dlg.setMinimumSize(
                new Dimension(
                        800,
                        520
                )
        );

        dlg.setLocationRelativeTo(
                owner
        );

        dlg.setVisible(
                true
        );
    }

    private boolean containsIgnoreCase(
            String value,
            String searchText) {

        if (
                value == null
                        ||
                        searchText == null
        ) {
            return false;
        }

        return value
                .toLowerCase()
                .contains(
                        searchText
                );
    }
}