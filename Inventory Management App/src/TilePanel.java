import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TilePanel extends JPanel {

    private final TileService tileService;
    private final WarehouseManager warehouseManager;
    private final DefaultTableModel model;
    private final CurrencySettingsService currencySettingsService;

    private boolean showPricesInSyrian = false;

    private JToggleButton currencyToggle;

    private final JTable table;
    private final JLabel sumLabel;

    /*
     * الفلتر الحالي.
     *
     * مهم:
     * لا يتم إنشاء TileFilter جديد عند كل عملية بحث
     * أو تعديل.
     */
    private final TileFilter currentFilter =
            new TileFilter();

    /*
     * نص البحث الحالي.
     */
    private String currentSearchText = "";

    /*
     * العناصر الظاهرة حاليًا في الجدول.
     */
    private List<TileItem> displayedItems =
            new ArrayList<>();

    /*
     * خدمة الفلترة.
     */
    private final TileFilterService tileFilterService =
            new TileFilterService();

    /*
     * زر الفلترة والقائمة.
     */
    private JButton filterButton;
    private JPopupMenu filterPopup;
    private TileFilterPanel tileFilterPanel;

    /*
     * مهم جدًا:
     * searchField أصبح field في الكلاس وليس متغيرًا محليًا
     * داخل constructor.
     */
    private JTextField searchField;


    /*
     * الألوان والخطوط.
     */
    private final Color FOCUS_YELLOW =
            Color.YELLOW;

    private final Font EXTRA_LARGE_FONT =
            new Font(
                    "Tahoma",
                    Font.BOLD,
                    20
            );

    private final Font LARGE_FONT =
            new Font(
                    "Tahoma",
                    Font.PLAIN,
                    18
            );

    private final Font BUTTON_FONT =
            new Font(
                    "Tahoma",
                    Font.BOLD,
                    18
            );

    private final Font TABLE_FONT =
            new Font(
                    "Tahoma",
                    Font.PLAIN,
                    19
            );

    private final Font HEADER_FONT =
            new Font(
                    "Tahoma",
                    Font.BOLD,
                    17
            );

    private final Color PRIMARY_COLOR =
            new Color(
                    63,
                    81,
                    181
            );

    private final Color LIGHT_GREY =
            new Color(
                    224,
                    224,
                    224
            );

    private final Color SELECTION_BG =
            new Color(
                    197,
                    202,
                    233
            );


    public TilePanel(
            TileService tileService,
            WarehouseManager warehouseManager,
            CurrencySettingsService currencySettingsService) {

        this.tileService =
                tileService;

        this.warehouseManager =
                warehouseManager;

        this.currencySettingsService =
                currencySettingsService;

        this.displayedItems =
                new ArrayList<>();


        /*
         * ==========================================
         * Layout الرئيسي
         * ==========================================
         */

        setLayout(
                new BorderLayout(
                        15,
                        15
                )
        );

        setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );


        /*
         * ==========================================
         * الجزء العلوي
         * ==========================================
         */

        JPanel topPanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                15
                        )
                );


        JPanel searchAndRefreshPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                0
                        )
                );


        /*
         * ==========================================
         * حقل البحث
         * ==========================================
         *
         * أصبح field في الكلاس.
         */

        searchField =
                new JTextField(20);

        searchField.setFont(
                LARGE_FONT
        );

        searchField.setPreferredSize(
                new Dimension(
                        250,
                        40
                )
        );


        /*
         * ==========================================
         * زر العملة
         * ==========================================
         */

        currencyToggle =
                new JToggleButton(
                        "USD"
                );

        currencyToggle.setFont(
                BUTTON_FONT
        );

        currencyToggle.setPreferredSize(
                new Dimension(
                        100,
                        40
                )
        );

        currencyToggle.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );


        currencyToggle.addActionListener(
                e -> {

                    showPricesInSyrian =
                            currencyToggle.isSelected();

                    currencyToggle.setText(
                            showPricesInSyrian
                                    ? "SYP"
                                    : "USD"
                    );

                    updateCurrencyColumnNames();

                    refreshCurrencyDisplay();
                }
        );


        /*
         * ==========================================
         * زر التحديث
         * ==========================================
         */

        JButton refreshBtn =
                new JButton(
                        "تحديث"
                );

        refreshBtn.setFont(
                BUTTON_FONT
        );

        refreshBtn.setBackground(
                PRIMARY_COLOR
        );

        refreshBtn.setForeground(
                Color.WHITE
        );

        refreshBtn.setPreferredSize(
                new Dimension(
                        150,
                        40
                )
        );

        refreshBtn.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );


        /*
         * ==========================================
         * زر الفلترة
         * ==========================================
         */

        filterButton =
                new JButton(
                        "فلترة ▼"
                );

        filterButton.setFont(
                BUTTON_FONT
        );

        filterButton.setFocusPainted(
                false
        );

        filterButton.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );


        filterButton.addActionListener(
                e -> toggleFilterPopup()
        );


        /*
         * إضافة عناصر البحث.
         */

        searchAndRefreshPanel.add(
                searchField
        );

        searchAndRefreshPanel.add(
                refreshBtn
        );

        searchAndRefreshPanel.add(
                currencyToggle
        );


        /*
         * ==========================================
         * إضافة الجزء العلوي
         * ==========================================
         */

        topPanel.add(
                searchAndRefreshPanel,
                BorderLayout.WEST
        );

        topPanel.add(
                filterButton,
                BorderLayout.EAST
        );


        add(
                topPanel,
                BorderLayout.NORTH
        );


        /*
         * ==========================================
         * الجدول
         * ==========================================
         */

        String[] cols = {

                "الرمز",
                "الاسم",
                "مادة",
                "نوع",
                "مستودع",
                "موقع",
                "النخب",
                "السعر",
                "الصناديق",
                "سعة ص",
                "مساحة إج",
                "اجمالي"
        };


        model =
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


        table =
                new JTable(model);


        updateCurrencyColumnNames();


        table.setFont(
                TABLE_FONT
        );

        table.setRowHeight(
                40
        );

        table.getTableHeader()
                .setFont(
                        HEADER_FONT
                );

        table.getTableHeader()
                .setBackground(
                        PRIMARY_COLOR
                );

        table.getTableHeader()
                .setForeground(
                        Color.WHITE
                );

        table.getTableHeader()
                .setPreferredSize(
                        new Dimension(
                                0,
                                45
                        )
                );

        table.setSelectionBackground(
                SELECTION_BG
        );

        table.setSelectionForeground(
                Color.BLACK
        );

        table.setGridColor(
                LIGHT_GREY
        );


        setColumnWidths(
                table
        );


        JScrollPane scrollPane =
                new JScrollPane(
                        table
                );

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        PRIMARY_COLOR,
                        2
                )
        );


        add(
                scrollPane,
                BorderLayout.CENTER
        );


        /*
         * ==========================================
         * لوحة التحكم السفلية
         * ==========================================
         */

        JPanel controlPanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        controlPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        0,
                        0,
                        0
                )
        );


        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                15,
                                10
                        )
                );


        JButton deleteBtn =
                new JButton(
                        "حذف"
                );

        JButton editBtn =
                new JButton(
                        "تعديل"
                );

        JButton dispatchBtn =
                new JButton(
                        "تخريج"
                );


        /*
         * ==========================================
         * Focus Border
         * ==========================================
         */

        Border defaultButtonBorder =
                deleteBtn.getBorder();

        Border focusBorder =
                new CompoundBorder(
                        BorderFactory.createLineBorder(
                                FOCUS_YELLOW,
                                2
                        ),
                        defaultButtonBorder
                );


        FocusListener buttonFocusListener =
                new FocusAdapter() {

                    @Override
                    public void focusGained(
                            FocusEvent e) {

                        ((JComponent) e.getSource())
                                .setBorder(
                                        focusBorder
                                );
                    }


                    @Override
                    public void focusLost(
                            FocusEvent e) {

                        ((JComponent) e.getSource())
                                .setBorder(
                                        defaultButtonBorder
                                );
                    }
                };


        Dimension buttonSize =
                new Dimension(
                        110,
                        40
                );


        for (JButton btn :
                new JButton[]{
                        deleteBtn,
                        editBtn,
                        dispatchBtn,
                        refreshBtn
                }) {

            btn.setFont(
                    BUTTON_FONT
            );

            btn.setBackground(
                    PRIMARY_COLOR
            );

            btn.setForeground(
                    Color.WHITE
            );

            btn.setCursor(
                    new Cursor(
                            Cursor.HAND_CURSOR
                    )
            );

            btn.setPreferredSize(
                    buttonSize
            );

            btn.setFocusPainted(
                    false
            );

            btn.addFocusListener(
                    buttonFocusListener
            );
        }


        refreshBtn.setPreferredSize(
                new Dimension(
                        100,
                        40
                )
        );


        buttonPanel.add(
                deleteBtn
        );

        buttonPanel.add(
                editBtn
        );

        buttonPanel.add(
                dispatchBtn
        );


        /*
         * ==========================================
         * المجموع
         * ==========================================
         */

        sumLabel =
                new JLabel(
                        "المجموع: 0.00"
                );

        sumLabel.setFont(
                EXTRA_LARGE_FONT
        );

        sumLabel.setForeground(
                PRIMARY_COLOR
        );

        sumLabel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                PRIMARY_COLOR,
                                2
                        ),
                        BorderFactory.createEmptyBorder(
                                8,
                                15,
                                8,
                                15
                        )
                )
        );

        sumLabel.setHorizontalAlignment(
                SwingConstants.CENTER
        );


        controlPanel.add(
                buttonPanel,
                BorderLayout.CENTER
        );

        controlPanel.add(
                sumLabel,
                BorderLayout.EAST
        );


        add(
                controlPanel,
                BorderLayout.SOUTH
        );


        /*
         * ==========================================
         * التحديث
         * ==========================================
         *
         * عند الضغط على تحديث يدويًا فقط:
         * نعيد جلب البيانات.
         *
         * أما البحث والفلترة والتعديل والتخريج
         * فلا تقوم بإعادة إنشاء الفلاتر.
         */

        refreshBtn.addActionListener(
                e -> {

                    List<TileItem> all =
                            tileService.getAll();

                    /*
                     * التحديث اليدوي يعيد التطبيق
                     * على حالة البحث والفلتر الحالية.
                     */
                    applyCurrentFilterAndSearch();
                }
        );


        /*
         * ==========================================
         * الحذف
         * ==========================================
         */

        deleteBtn.addActionListener(
                e -> {

                    int selectedRow =
                            table.getSelectedRow();


                    if (selectedRow < 0) {

                        JOptionPane.showMessageDialog(
                                table,
                                "يجب اختيار عنصر أولاً من الجدول.",
                                "تنبيه",
                                JOptionPane.WARNING_MESSAGE
                        );

                        return;
                    }


                    TileItem itemToDelete =
                            displayedItems.get(
                                    selectedRow
                            );


                    int choice =
                            JOptionPane.showConfirmDialog(
                                    table,

                                    "سيتم حذف العنصر: "
                                            + itemToDelete.getName()
                                            + " (الرمز: "
                                            + itemToDelete.getCode()
                                            + ")"
                                            + "\nهل أنت متأكد؟",

                                    "تأكيد الحذف",

                                    JOptionPane.YES_NO_OPTION,

                                    JOptionPane.WARNING_MESSAGE
                            );


                    if (choice ==
                            JOptionPane.YES_OPTION) {

                        try {

                            /*
                             * ملاحظة:
                             * الكود الأصلي لا يحتوي على
                             * tileService.delete().
                             *
                             * لذلك أبقيت سلوك save كما هو.
                             */
                            tileService.save();


                            JOptionPane.showMessageDialog(
                                    table,
                                    "تم حذف العنصر بنجاح.",
                                    "تم",
                                    JOptionPane.INFORMATION_MESSAGE
                            );


                        } catch (Exception ex) {

                            JOptionPane.showMessageDialog(
                                    table,
                                    "حدث خطأ أثناء الحذف: "
                                            + ex.getMessage(),
                                    "خطأ",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }
        );


        /*
         * ==========================================
         * التعديل
         * ==========================================
         */

        editBtn.addActionListener(
                e -> {

                    int selectedRow =
                            table.getSelectedRow();


                    if (selectedRow < 0) {

                        JOptionPane.showMessageDialog(
                                table,
                                "يجب اختيار عنصر أولاً من الجدول.",
                                "تنبيه",
                                JOptionPane.WARNING_MESSAGE
                        );

                        return;
                    }


                    TileItem target =
                            displayedItems.get(
                                    selectedRow
                            );


                    JTextField nameField =
                            new JTextField(
                                    target.getName(),
                                    15
                            );

                    JTextField codeField =
                            new JTextField(
                                    target.getCode(),
                                    15
                            );

                    JTextField priceField =
                            new JTextField(
                                    String.valueOf(
                                            target.getPrice()
                                    ),
                                    15
                            );

                    JTextField boxesField =
                            new JTextField(
                                    String.valueOf(
                                            target.getBoxes()
                                    ),
                                    15
                            );

                    JTextField boxAreaField =
                            new JTextField(
                                    String.valueOf(
                                            target.getBoxArea()
                                    ),
                                    15
                            );

                    JTextField gradeField =
                            new JTextField(
                                    target.getGrade(),
                                    15
                            );


                    JComboBox<TileItem.MaterialType>
                            materialCombo =
                            new JComboBox<>(
                                    TileItem.MaterialType.values()
                            );

                    materialCombo.setSelectedItem(
                            target.getMaterialType()
                    );


                    JComboBox<TileItem.SubType>
                            subtypeCombo =
                            new JComboBox<>(
                                    TileItem.SubType.values()
                            );

                    subtypeCombo.setSelectedItem(
                            target.getSubType()
                    );


                    JComboBox<TileItem.Location>
                            locationCombo =
                            new JComboBox<>(
                                    TileItem.Location.values()
                            );

                    locationCombo.setSelectedItem(
                            target.getLocation()
                    );


                    locationCombo.setRenderer(
                            new DefaultListCellRenderer() {

                                @Override
                                public Component
                                getListCellRendererComponent(
                                        JList<?> list,
                                        Object value,
                                        int index,
                                        boolean isSelected,
                                        boolean cellHasFocus) {

                                    if (value instanceof
                                            TileItem.Location loc) {

                                        value =
                                                loc.getArabicName();
                                    }

                                    return super
                                            .getListCellRendererComponent(
                                                    list,
                                                    value,
                                                    index,
                                                    isSelected,
                                                    cellHasFocus
                                            );
                                }
                            }
                    );


                    JComboBox<Warehouse>
                            editWarehouseCombo =
                            new JComboBox<>();


                    for (Warehouse w :
                            warehouseManager.getWarehouses()) {

                        editWarehouseCombo.addItem(
                                w
                        );


                        if (w.getId() ==
                                target.getWarehouse().getId()) {

                            editWarehouseCombo
                                    .setSelectedItem(
                                            w
                                    );
                        }
                    }


                    editWarehouseCombo.setRenderer(
                            new DefaultListCellRenderer() {

                                @Override
                                public Component
                                getListCellRendererComponent(
                                        JList<?> list,
                                        Object value,
                                        int index,
                                        boolean isSelected,
                                        boolean cellHasFocus) {

                                    if (value instanceof
                                            Warehouse w) {

                                        value =
                                                w.getDisplayName();
                                    }

                                    return super
                                            .getListCellRendererComponent(
                                                    list,
                                                    value,
                                                    index,
                                                    isSelected,
                                                    cellHasFocus
                                            );
                                }
                            }
                    );


                    JPanel panel =
                            new JPanel(
                                    new GridLayout(
                                            0,
                                            2,
                                            10,
                                            10
                                    )
                            );


                    panel.add(codeField);
                    panel.add(new JLabel("الرمز:"));

                    panel.add(nameField);
                    panel.add(new JLabel("الاسم:"));

                    panel.add(priceField);
                    panel.add(new JLabel("السعر:"));

                    panel.add(boxesField);
                    panel.add(new JLabel("الصناديق:"));

                    panel.add(boxAreaField);
                    panel.add(new JLabel("مساحة الصندوق:"));

                    panel.add(gradeField);
                    panel.add(new JLabel("النخب:"));

                    panel.add(materialCombo);
                    panel.add(new JLabel("نوع المادة:"));

                    panel.add(subtypeCombo);
                    panel.add(new JLabel("النوع الفرعي:"));

                    panel.add(locationCombo);
                    panel.add(new JLabel("الموقع:"));

                    panel.add(editWarehouseCombo);
                    panel.add(new JLabel("المستودع:"));


                    int result =
                            JOptionPane.showConfirmDialog(
                                    this,
                                    panel,
                                    "تعديل عنصر البلاط",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE
                            );


                    if (result ==
                            JOptionPane.OK_OPTION) {

                        try {

                            String newCode =
                                    codeField
                                            .getText();

                            String newName =
                                    nameField
                                            .getText()
                                            .trim();

                            double newPrice =
                                    Double.parseDouble(
                                            priceField
                                                    .getText()
                                                    .trim()
                                    );

                            double newBoxes =
                                    Double.parseDouble(
                                            boxesField
                                                    .getText()
                                                    .trim()
                                    );

                            double newArea =
                                    Double.parseDouble(
                                            boxAreaField
                                                    .getText()
                                                    .trim()
                                    );

                            String newGrade =
                                    gradeField
                                            .getText()
                                            .trim();


                            TileItem.MaterialType newMat =
                                    (TileItem.MaterialType)
                                            materialCombo
                                                    .getSelectedItem();


                            TileItem.SubType newSub =
                                    (TileItem.SubType)
                                            subtypeCombo
                                                    .getSelectedItem();


                            TileItem.Location newLoc =
                                    (TileItem.Location)
                                            locationCombo
                                                    .getSelectedItem();


                            Warehouse newWarehouse =
                                    (Warehouse)
                                            editWarehouseCombo
                                                    .getSelectedItem();


                            if (newName.isEmpty()
                                    || newGrade.isEmpty()
                                    || newMat == null
                                    || newSub == null
                                    || newWarehouse == null
                                    || newLoc == null) {

                                JOptionPane.showMessageDialog(
                                        this,
                                        "يرجى ملء جميع الحقول بشكل صحيح.",
                                        "بيانات ناقصة",
                                        JOptionPane.WARNING_MESSAGE
                                );

                                return;
                            }


                            TileItem updated =
                                    new TileItem(
                                            newName,
                                            newCode,
                                            newPrice,
                                            newBoxes,
                                            newArea,
                                            newMat,
                                            newSub,
                                            newWarehouse,
                                            newGrade,
                                            newLoc
                                    );


                            /*
                             * تحديث العنصر في الخدمة.
                             */
                            tileService.update(
                                    target,
                                    updated
                            );


                            /*
                             * الحفظ.
                             */
                            tileService.save();


                            /*
                             * ==================================
                             * مهم جدًا:
                             *
                             * لا ننفذ:
                             *
                             * loadTable(tileService.getAll())
                             *
                             * ولا ننشئ Filter جديد.
                             *
                             * فقط نعيد تطبيق الفلتر والبحث الحاليين.
                             * ==================================
                             */
                            applyCurrentFilterAndSearch();


                        } catch (
                                NumberFormatException ex) {

                            JOptionPane.showMessageDialog(
                                    this,
                                    "تأكد من إدخال القيم الرقمية بشكل صحيح.",
                                    "خطأ إدخال",
                                    JOptionPane.ERROR_MESSAGE
                            );


                        } catch (
                                IllegalArgumentException ex) {

                            JOptionPane.showMessageDialog(
                                    this,
                                    "بيانات غير صالحة: "
                                            + ex.getMessage(),
                                    "خطأ إدخال",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }
        );


        /*
         * ==========================================
         * البحث
         * ==========================================
         */

        searchField.addActionListener(
                e ->
                        applyCurrentFilterAndSearch()
        );


        searchField.getDocument()
                .addDocumentListener(
                        new DocumentListener() {

                            @Override
                            public void insertUpdate(
                                    DocumentEvent e) {

                                applyCurrentFilterAndSearch();
                            }


                            @Override
                            public void removeUpdate(
                                    DocumentEvent e) {

                                applyCurrentFilterAndSearch();
                            }


                            @Override
                            public void changedUpdate(
                                    DocumentEvent e) {

                                applyCurrentFilterAndSearch();
                            }
                        }
                );


        /*
         * ==========================================
         * التخريج
         * ==========================================
         */

        dispatchBtn.addActionListener(
                e -> {

                    int selectedRow =
                            table.getSelectedRow();


                    if (selectedRow < 0) {

                        JOptionPane.showMessageDialog(
                                table,
                                "يجب اختيار عنصر أولاً من الجدول.",
                                "تنبيه",
                                JOptionPane.WARNING_MESSAGE
                        );

                        return;
                    }


                    TileItem item =
                            displayedItems.get(
                                    selectedRow
                            );


                    String quantityStr =
                            JOptionPane.showInputDialog(
                                    this,
                                    "أدخل الكمية (صناديق) المراد إنقاصها:",
                                    "تخريج كمية",
                                    JOptionPane.PLAIN_MESSAGE
                            );


                    if (quantityStr != null
                            && !quantityStr.trim().isEmpty()) {

                        try {

                            double qtyToDecrease =
                                    Double.parseDouble(
                                            quantityStr
                                                    .trim()
                                    );


                            if (qtyToDecrease <= 0
                                    || qtyToDecrease
                                    > item.getBoxes()) {

                                JOptionPane.showMessageDialog(
                                        this,
                                        "الكمية غير صالحة أو أكبر من المتاح.",
                                        "خطأ",
                                        JOptionPane.ERROR_MESSAGE
                                );

                                return;
                            }


                            TileItem updated =
                                    new TileItem(
                                            item.getName(),
                                            item.getCode(),
                                            item.getPrice(),
                                            item.getBoxes()
                                                    - qtyToDecrease,
                                            item.getBoxArea(),
                                            item.getMaterialType(),
                                            item.getSubType(),
                                            item.getWarehouse(),
                                            item.getGrade(),
                                            item.getLocation()
                                    );


                            tileService.update(
                                    item,
                                    updated
                            );


                            tileService.save();


                            /*
                             * لا نعيد إنشاء الفلتر.
                             * نعيد فقط حساب النتيجة الحالية.
                             */
                            applyCurrentFilterAndSearch();


                            JOptionPane.showMessageDialog(
                                    this,
                                    "تم إنقاص الكمية بنجاح.",
                                    "تم",
                                    JOptionPane.INFORMATION_MESSAGE
                            );


                        } catch (
                                NumberFormatException ex) {

                            JOptionPane.showMessageDialog(
                                    this,
                                    "الرجاء إدخال رقم صحيح للكمية.",
                                    "خطأ إدخال",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }
        );


        /*
         * ==========================================
         * الضغط المزدوج = تخريج
         * ==========================================
         */

        table.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent e) {

                        if (e.getClickCount() == 2
                                && table.getSelectedRow()
                                != -1) {

                            dispatchBtn.doClick();
                        }
                    }
                }
        );


        /*
         * ==========================================
         * لوحة المفاتيح
         * ==========================================
         */

        addKeyboardNavigation(
                searchField,
                refreshBtn,
                table,
                deleteBtn,
                editBtn,
                dispatchBtn
        );


        /*
         * ==========================================
         * التحميل الأول
         * ==========================================
         */

        applyCurrentFilterAndSearch();
    }


    /*
     * ============================================================
     * عرض / إخفاء قائمة الفلترة
     * ============================================================
     */

    private void toggleFilterPopup() {

        if (filterPopup != null
                && filterPopup.isVisible()) {

            filterPopup.setVisible(false);

            return;
        }


        showFilterPopup();
    }


    /*
     * ============================================================
     * إنشاء القائمة
     * ============================================================
     */

    private void showFilterPopup() {

        if (filterPopup == null) {

            createFilterPopup();
        }


        /*
         * القائمة تظهر تحت زر الفلترة.
         */
        filterPopup.show(
                filterButton,
                0,
                filterButton.getHeight()
        );
    }


    /*
     * ============================================================
     * إنشاء TileFilterPanel
     * ============================================================
     */

    private void createFilterPopup() {

        filterPopup = new JPopupMenu();

        filterPopup.setLayout(
                new BorderLayout()
        );

        List<TileItem> items =
                tileService.getAll();

        tileFilterPanel =
                new TileFilterPanel(
                        items,
                        warehouseManager.getWarehouses(),
                        currentFilter
                );

        tileFilterPanel.setOnFilterApplied(
                () -> {

                    applyCurrentFilterAndSearch();

                    filterPopup.setVisible(false);
                }
        );

        tileFilterPanel.setOnClearApplied(
                () -> {

                    applyCurrentFilterAndSearch();

                    filterPopup.setVisible(false);
                }
        );

        filterPopup.add(
                tileFilterPanel,
                BorderLayout.CENTER
        );

        /*
         * تحديد حجم القائمة.
         */
        filterPopup.setPopupSize(
                390,
                560
        );
    }


    /*
     * ============================================================
     * تطبيق الفلتر + البحث
     * ============================================================
     */

    private void applyCurrentFilterAndSearch() {

        /*
         * قراءة نص البحث الحالي.
         */
        currentSearchText =
                searchField.getText() == null
                        ? ""
                        : searchField
                        .getText()
                        .trim();


        /*
         * جلب البيانات.
         *
         * الفلتر الحالي currentFilter
         * لا يتغير هنا.
         */
        List<TileItem> allItems =
                tileService.getAll();


        /*
         * تطبيق:
         *
         * Filter
         * +
         * Search
         */
        displayedItems =
                tileFilterService.filterAndSearch(
                        allItems,
                        currentFilter,
                        currentSearchText
                );


        /*
         * تحديث الجدول بالنتيجة فقط.
         */
        loadTable(
                displayedItems
        );


        /*
         * تحديث شكل زر الفلترة.
         */
        updateFilterButtonText();
    }


    /*
     * ============================================================
     * تحديث اسم زر الفلترة
     * ============================================================
     */

    private void updateFilterButtonText() {

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
                    "فلترة ("
                            + count
                            + ") ▼"
            );
        }
    }


    /*
     * ============================================================
     * تحميل الجدول
     * ============================================================
     */

    private void loadTable(
            List<TileItem> list) {

        model.setRowCount(0);


        /*
         * ترتيب العناصر.
         */
        this.displayedItems =
                list.stream()
                        .sorted(
                                (a, b) ->
                                        a.getName()
                                                .compareToIgnoreCase(
                                                        b.getName()
                                                )
                        )
                        .collect(
                                Collectors.toList()
                        );


        /*
         * إضافة الصفوف.
         */
        displayedItems.forEach(
                it -> {

                    double totalArea =
                            it.getBoxes()
                                    * it.getBoxArea();


                    double price =
                            it.getPrice();


                    double totalValue =
                            totalArea
                                    * price;


                    if (showPricesInSyrian) {

                        double rate =
                                currencySettingsService
                                        .getDollarToSyrianRate();


                        price =
                                price * rate;


                        totalValue =
                                totalValue * rate;
                    }


                    model.addRow(
                            new Object[]{

                                    it.getCode(),

                                    it.getName(),

                                    it.getMaterialType()
                                            .name(),

                                    it.getSubType()
                                            .name(),

                                    it.getWarehouse()
                                            .getDisplayName(),

                                    it.getLocation()
                                            .getArabicName(),

                                    it.getGrade(),

                                    String.format(
                                            "%.2f",
                                            price
                                    ),

                                    it.getBoxes(),

                                    it.getBoxArea(),

                                    String.format(
                                            "%.2f",
                                            totalArea
                                    ),

                                    String.format(
                                            "%.2f",
                                            totalValue
                                    )
                            }
                    );
                }
        );


        /*
         * حساب المجموع.
         */
        double sum =
                displayedItems.stream()
                        .mapToDouble(
                                it ->
                                        it.getBoxes()
                                                * it.getBoxArea()
                                                * it.getPrice()
                        )
                        .sum();


        if (showPricesInSyrian) {

            sum =
                    sum
                            * currencySettingsService
                            .getDollarToSyrianRate();
        }


        if (showPricesInSyrian) {

            sumLabel.setText(
                    String.format(
                            "المجموع الكلي: %,.2f SYP",
                            sum
                    )
            );

        } else {

            sumLabel.setText(
                    String.format(
                            "المجموع الكلي: %,.2f USD",
                            sum
                    )
            );
        }


        if (sum > 50000) {

            sumLabel.setForeground(
                    new Color(
                            255,
                            1,
                            0
                    )
            );

        } else if (sum > 10000) {

            sumLabel.setForeground(
                    new Color(
                            255,
                            152,
                            0
                    )
            );

        } else {

            sumLabel.setForeground(
                    new Color(
                            96,
                            125,
                            139
                    )
            );
        }
    }


    /*
     * ============================================================
     * عرض العملة
     * ============================================================
     */

    private void updateCurrencyColumnNames() {

        table.getColumnModel()
                .getColumn(7)
                .setHeaderValue(
                        showPricesInSyrian
                                ? "سعر S"
                                : "السعر (USD)"
                );


        table.getColumnModel()
                .getColumn(11)
                .setHeaderValue(
                        showPricesInSyrian
                                ? "القيمة الإجمالية (SYP)"
                                : "القيمة الإجمالية (USD)"
                );


        table.getTableHeader()
                .repaint();
    }


    /*
     * ============================================================
     * تحديث عرض العملة فقط
     * ============================================================
     */

    private void refreshCurrencyDisplay() {

        /*
         * لا نحتاج لجلب البيانات من جديد.
         *
         * displayedItems هي العناصر الموجودة حاليًا
         * بعد الفلترة والبحث.
         */
        loadTable(
                displayedItems
        );
    }


    /*
     * ============================================================
     * عرض الأعمدة
     * ============================================================
     */

    private void setColumnWidths(
            JTable table) {

        table.setAutoResizeMode(
                JTable.AUTO_RESIZE_NEXT_COLUMN
        );


        int[] widths = {

                110,
                195,
                100,
                100,
                100,
                80,
                80,
                80,
                80,
                100,
                110,
                110
        };


        for (
                int i = 0;
                i < widths.length
                        && i < table.getColumnCount();
                i++) {

            TableColumn column =
                    table.getColumnModel()
                            .getColumn(i);


            column.setPreferredWidth(
                    widths[i]
            );
        }
    }


    /*
     * ============================================================
     * التنقل بالكيبورد
     * ============================================================
     */

    private void addKeyboardNavigation(
            JTextField searchField,
            JButton refreshBtn,
            JTable table,
            JButton deleteBtn,
            JButton editBtn,
            JButton dispatchBtn) {


        /*
         * البحث.
         */

        searchField.addKeyListener(
                new KeyAdapter() {

                    @Override
                    public void keyPressed(
                            KeyEvent e) {

                        if (e.getKeyCode()
                                == KeyEvent.VK_DOWN) {

                            table.requestFocusInWindow();


                            if (table.getRowCount()
                                    > 0) {

                                table.setRowSelectionInterval(
                                        0,
                                        0
                                );
                            }

                        } else if (
                                e.getKeyCode()
                                        == KeyEvent.VK_RIGHT) {

                            refreshBtn
                                    .requestFocusInWindow();
                        }
                    }
                }
        );


        /*
         * التحديث.
         */

        refreshBtn.addKeyListener(
                new KeyAdapter() {

                    @Override
                    public void keyPressed(
                            KeyEvent e) {

                        if (e.getKeyCode()
                                == KeyEvent.VK_LEFT) {

                            searchField
                                    .requestFocusInWindow();

                        } else if (
                                e.getKeyCode()
                                        == KeyEvent.VK_DOWN) {

                            table
                                    .requestFocusInWindow();


                            if (table.getRowCount()
                                    > 0) {

                                table.setRowSelectionInterval(
                                        0,
                                        0
                                );
                            }
                        }
                    }
                }
        );


        /*
         * الجدول.
         */

        table.addKeyListener(
                new KeyAdapter() {

                    @Override
                    public void keyPressed(
                            KeyEvent e) {

                        if (e.getKeyCode()
                                == KeyEvent.VK_ENTER
                                && table.getSelectedRow()
                                != -1) {

                            editBtn
                                    .requestFocusInWindow();

                            e.consume();


                        } else if (
                                e.getKeyCode()
                                        == KeyEvent.VK_UP
                                        && table.getSelectedRow()
                                        <= 0) {

                            searchField
                                    .requestFocusInWindow();

                            table.clearSelection();
                        }
                    }
                }
        );


        /*
         * أزرار التحكم.
         */

        KeyAdapter buttonNavListener =
                new KeyAdapter() {

                    @Override
                    public void keyPressed(
                            KeyEvent e) {

                        Object source =
                                e.getSource();


                        if (e.getKeyCode()
                                == KeyEvent.VK_UP) {

                            table
                                    .requestFocusInWindow();

                            table.clearSelection();


                        } else if (
                                e.getKeyCode()
                                        == KeyEvent.VK_LEFT) {

                            if (source ==
                                    dispatchBtn) {

                                editBtn
                                        .requestFocusInWindow();

                            } else if (
                                    source ==
                                            editBtn) {

                                deleteBtn
                                        .requestFocusInWindow();
                            }


                        } else if (
                                e.getKeyCode()
                                        == KeyEvent.VK_RIGHT) {

                            if (source ==
                                    deleteBtn) {

                                editBtn
                                        .requestFocusInWindow();

                            } else if (
                                    source ==
                                            editBtn) {

                                dispatchBtn
                                        .requestFocusInWindow();
                            }
                        }
                    }
                };


        deleteBtn.addKeyListener(
                buttonNavListener
        );

        editBtn.addKeyListener(
                buttonNavListener
        );

        dispatchBtn.addKeyListener(
                buttonNavListener
        );
    }
}