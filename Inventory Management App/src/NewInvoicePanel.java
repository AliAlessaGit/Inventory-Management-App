import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewInvoicePanel extends JPanel {
    private int lastInventoryRow = -1;
    private int lastInvoiceRow = -1;

    private final TileService tileService;
    private final SanitaryService sanitaryService;
    private final InvoiceService invoiceService;

    private Invoice currentInvoice;

    private JTable invoiceTable;
    private DefaultTableModel invoiceModel;
    private JTable inventoryTable;
    private DefaultTableModel inventoryModel;

    private JTextField customerNameField, customerPhoneField;
    private JTextField dateField;
    private JButton editDateButton;

    private JTextField paymentAmountField;
    private JButton addPaymentBtn;

    private JRadioButton tileRadio, sanitaryRadio;
    private JTextField searchField;
    private JButton searchBtn;

    private JButton removeItemBtn, clearBtn, saveBtn, addBtn;
    private JLabel totalLabel, paidLabel, remainingLabel;

    private List<Object> displayedInventoryItems;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public NewInvoicePanel(TileService tileService,
                           SanitaryService sanitaryService,
                           InvoiceService invoiceService) {
        this.tileService = tileService;
        this.sanitaryService = sanitaryService;
        this.invoiceService = invoiceService;
        this.currentInvoice = invoiceService.createNewInvoice();

        setLayout(new BorderLayout(5, 5));
        initUI();
    }

    private void initUI() {
        // الصف العلوي: اسم + هاتف + تاريخ + تعديل (كلها في صف واحد)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        editDateButton = new JButton("تعديل");
        topPanel.add(editDateButton);

        dateField = new JTextField(dtf.format(LocalDateTime.now()), 16);
        dateField.setEditable(false);
        topPanel.add(dateField);
        topPanel.add(new JLabel("تاريخ الفاتورة:"));
        customerPhoneField = new JTextField(12);
        topPanel.add(customerPhoneField);
        topPanel.add(new JLabel("هاتف:"));
        customerNameField = new JTextField(15);
        topPanel.add(customerNameField);

        topPanel.add(new JLabel("اسم العميل:"));
        editDateButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "أدخل التاريخ (yyyy-MM-dd HH:mm)", dateField.getText());
            if (input != null && !input.isBlank()) {
                try {
                    LocalDateTime dt = LocalDateTime.parse(input, dtf);
                    dateField.setText(input);
                    currentInvoice.setDate(dt);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "صيغة غير صحيحة");
                }
            }
        });

        add(topPanel, BorderLayout.NORTH);

        // الوسط: جداول بنفس العرض
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.5);
        split.setOneTouchExpandable(false);
        split.setContinuousLayout(true);

        // يسار: الدفعة + جدول الفاتورة
        JPanel left = new JPanel(new BorderLayout(5, 5));
        JPanel paymentRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        paymentAmountField = new JTextField(10);
        addPaymentBtn = new JButton("إضافة دفعة");
        addPaymentBtn.addActionListener(e -> addPaymentFromUI());

        paymentRow.add(addPaymentBtn);
        paymentRow.add(paymentAmountField);
        left.add(paymentRow, BorderLayout.NORTH);

        invoiceModel = new DefaultTableModel(new String[]{"الرمز", "العنصر", "النخب", "الكمية", "سعر الوحدة", "الإجمالي"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        invoiceTable = new JTable(invoiceModel);
        invoiceTable.setRowHeight(24);
        left.add(new JScrollPane(invoiceTable), BorderLayout.CENTER);

        // يمين: فلاتر + بحث + جدول مخزون
        JPanel right = new JPanel(new BorderLayout(5, 5));
        JPanel filters = new JPanel();
        filters.setLayout(new BoxLayout(filters, BoxLayout.Y_AXIS));

        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tileRadio = new JRadioButton("بلاط", true);
        sanitaryRadio = new JRadioButton("أدوات صحية");
        ButtonGroup bg = new ButtonGroup();
        bg.add(tileRadio);
        bg.add(sanitaryRadio);
        typeRow.add(tileRadio);
        typeRow.add(sanitaryRadio);
        filters.add(typeRow);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        searchField = new JTextField(15);
        searchBtn = new JButton("بحث");
        searchRow.add(searchBtn);
        searchRow.add(searchField);

        filters.add(searchRow);

        right.add(filters, BorderLayout.NORTH);

        inventoryModel = new DefaultTableModel(new String[]{"الرمز", "الاسم", "النخب", "متوفر", "مساحة الصندوق", "السعر"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        inventoryTable = new JTable(inventoryModel);
        inventoryTable.setRowHeight(24);
        right.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        split.setLeftComponent(left);
        split.setRightComponent(right);
        add(split, BorderLayout.CENTER);

        // الشريط السفلي ثابت: يسار الأزرار، وسط القيم، يمين زر إضافة للمخزون
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 5));

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        removeItemBtn = new JButton("إزالة عنصر");
        removeItemBtn.addActionListener(e -> removeSelectedInvoiceItem());
        clearBtn = new JButton("إلغاء الفاتورة");
        clearBtn.addActionListener(e -> clearInvoice());
        saveBtn = new JButton("حفظ الفاتورة");
        saveBtn.addActionListener(e -> saveInvoice());
        leftButtons.add(removeItemBtn);
        leftButtons.add(clearBtn);
        leftButtons.add(saveBtn);

        JPanel totalsCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        totalLabel = new JLabel("الإجمالي: 0.00");
        paidLabel = new JLabel("المدفوع: 0.00");
        remainingLabel = new JLabel("الباقي: 0.00");
        Font f = totalLabel.getFont().deriveFont(Font.BOLD, 16f);
        totalLabel.setFont(f); paidLabel.setFont(f); remainingLabel.setFont(f);
        totalsCenter.add(totalLabel);
        totalsCenter.add(paidLabel);
        totalsCenter.add(remainingLabel);

        addBtn = new JButton("إضافة إلى الفاتورة→");
        addBtn.addActionListener(e -> addSelectedToInvoice());

        bottomPanel.add(leftButtons, BorderLayout.WEST);
        bottomPanel.add(totalsCenter, BorderLayout.CENTER);
        bottomPanel.add(addBtn, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.5));

        tileRadio.addActionListener(e -> reloadInventory());
        sanitaryRadio.addActionListener(e -> reloadInventory());
        searchField.addActionListener(e -> reloadInventory());
        searchBtn.addActionListener(e -> reloadInventory());
        paymentAmountField.addActionListener(e -> addPaymentFromUI());

        reloadInventory();
        setupKeyboardNavigation();
        updateTotalsAndPayments();
    }

    private Map<String, Double> getReservedQuantities() {
        Map<String, Double> reserved = new HashMap<>();
        for (InvoiceItem item : currentInvoice.getItems()) {
            String key = item.isTile() ? "T:" + item.getCode() : "S:" + item.getName() + "::" + item.getGrade();
            reserved.put(key, reserved.getOrDefault(key, 0.0) + item.getQuantity());
        }
        return reserved;
    }
    // داخل NewInvoicePanel
    private Map<Long, Double> getReservedQuantitiesById() {
        Map<Long, Double> reserved = new HashMap<>();
        for (InvoiceItem item : currentInvoice.getItems()) {
            reserved.put(item.getIdNumber(), reserved.getOrDefault(item.getIdNumber(), 0.0) + item.getQuantity());
        }
        return reserved;
    }

    private void reloadInventory() {
        inventoryModel.setRowCount(0);
        // <<<<<<< جديد: تهيئة القائمة في كل مرة يتم فيها تحديث الجدول
        this.displayedInventoryItems = new ArrayList<>();

        String searchText = searchField.getText().trim().toLowerCase();
        Map<Long, Double> reservedById = getReservedQuantitiesById();

        if (tileRadio.isSelected()) {
            List<TileItem> items = searchText.isEmpty() ? tileService.getAll() : tileService.searchByNameOrCode(searchText);
            for (TileItem it : items) {
                double reserved = reservedById.getOrDefault(it.getIdNumber(), 0.0);
                double available = it.getBoxes() - reserved;
                inventoryModel.addRow(new Object[]{
                        it.getCode(), it.getName(), it.getGrade(),
                        String.format("%.2f", available), it.getBoxArea(), it.getPrice()
                });
                // <<<<<<< جديد: إضافة الكائن نفسه إلى القائمة الموازية
                displayedInventoryItems.add(it);
            }
        } else {
            List<SanitaryItem> items = sanitaryService.getAll().stream()
                    .filter(it -> searchText.isEmpty() || it.getName().toLowerCase().contains(searchText))
                    .toList();
            for (SanitaryItem it : items) {
                double reserved = reservedById.getOrDefault(it.getIdNumber(), 0.0);
                double available = it.getQuantity() - reserved;
                inventoryModel.addRow(new Object[]{
                        "", it.getName(), it.getGrade(),
                        String.format("%.2f", available), "", it.getPrice()
                });
                // <<<<<<< جديد: إضافة الكائن نفسه إلى القائمة الموازية
                displayedInventoryItems.add(it);
            }
        }
    }
    private void addSelectedToInvoice() {
        int r = inventoryTable.getSelectedRow();
        if (r < 0 || displayedInventoryItems == null || r >= displayedInventoryItems.size()) {
            return; // حماية ضد الأخطاء
        }

        try {
            LocalDateTime itemDate;
            try {
                itemDate = LocalDateTime.parse(dateField.getText(), dtf);
            } catch (Exception parseEx) {
                itemDate = LocalDateTime.now();
                dateField.setText(dtf.format(itemDate));
            }

            String qtyStr = JOptionPane.showInputDialog(this, "الكمية:", "1");
            if (qtyStr == null) return;
            double qty = Double.parseDouble(qtyStr.trim().replace(',', '.'));
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "كمية غير صالحة", "خطأ", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // <<<<<<< تعديل: الحصول على الكائن مباشرة من القائمة الموازية
            Object selectedItemObject = displayedInventoryItems.get(r);

            // التحقق من نوع العنصر المحدد (بلاط أو أدوات صحية)
            if (selectedItemObject instanceof TileItem t) {
                // الآن المتغير 't' هو الكائن الصحيح والمحدد 100%
                double available = Double.parseDouble(inventoryModel.getValueAt(r, 3).toString().replace(',', '.'));
                if (qty > available) {
                    JOptionPane.showMessageDialog(this, "الكمية المطلوبة غير متاحة.", "خطأ", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // السعر مأخوذ مباشرة من الكائن الصحيح
                String prStr = JOptionPane.showInputDialog(this, "سعر الوحدة:", String.valueOf(t.getPrice()));
                if (prStr == null) return;
                double unitPrice = Double.parseDouble(prStr.trim().replace(',', '.'));

                // استخدام idNumber من الكائن الصحيح
                InvoiceItem ii = new InvoiceItem(
                        t.getIdNumber(), t.getName(), qty, unitPrice, t.getBoxArea(), true, t.getCode(), t.getGrade(), itemDate
                );
                currentInvoice.addItem(ii);
                invoiceModel.addRow(new Object[]{t.getCode(), t.getName(), t.getGrade(), qty, unitPrice, String.format("%.2f", ii.getTotalPrice())});

            } else if (selectedItemObject instanceof SanitaryItem s) {
                // الآن المتغير 's' هو الكائن الصحيح والمحدد 100%
                double available = Double.parseDouble(inventoryModel.getValueAt(r, 3).toString().replace(',', '.'));
                if (qty > available) {
                    JOptionPane.showMessageDialog(this, "الكمية المطلوبة غير متاحة.", "خطأ", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // السعر مأخوذ مباشرة من الكائن الصحيح
                String prStr = JOptionPane.showInputDialog(this, "سعر الوحدة:", String.valueOf(s.getPrice()));
                if (prStr == null) return;
                double unitPrice = Double.parseDouble(prStr.trim().replace(',', '.'));

                // استخدام idNumber من الكائن الصحيح
                InvoiceItem ii = new InvoiceItem(
                        s.getIdNumber(), s.getName(), qty, unitPrice, false, s.getGrade(), itemDate
                );
                currentInvoice.addItem(ii);
                invoiceModel.addRow(new Object[]{"", s.getName(), s.getGrade(), qty, unitPrice, String.format("%.2f", ii.getTotalPrice())});
            }

            updateTotalsAndPayments();
            reloadInventory();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "كمية أو سعر غير صحيح", "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void saveInvoice() {
        String c = customerNameField.getText().trim();
        String p = customerPhoneField.getText().trim();
        if (c.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "أكمل بيانات العميل", "تحذير", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentInvoice.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "لا توجد عناصر في الفاتورة", "تحذير", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ensureFirstPaymentAddedIfAny()) return;

        currentInvoice.setCustomerName(c);
        currentInvoice.setCustomerPhone(p);

        for (InvoiceItem item : currentInvoice.getItems()) {
            if (item.isTile()) {
                TileItem t = tileService.findById(item.getIdNumber());
                if (t != null) t.setBoxes(t.getBoxes() - item.getQuantity());
            } else {
                SanitaryItem s = sanitaryService.findById(item.getIdNumber());
                if (s != null) s.setQuantity(s.getQuantity() - (int) Math.round(item.getQuantity()));
            }
        }
        tileService.save();
        sanitaryService.save();

        invoiceService.add(currentInvoice);

        JOptionPane.showMessageDialog(this, "تم حفظ الفاتورة بنجاح. رقم الفاتورة: " + currentInvoice.getInvoiceNumber(), "نجاح", JOptionPane.INFORMATION_MESSAGE);

        clearInvoice();
    }
    private void clearInvoice() {
        currentInvoice = invoiceService.createNewInvoice();
        invoiceModel.setRowCount(0);
        customerNameField.setText("");
        customerPhoneField.setText("");
        dateField.setText(dtf.format(LocalDateTime.now()));
        paymentAmountField.setText("");
        updateTotalsAndPayments();
        reloadInventory();
    }

    private void removeSelectedInvoiceItem() {
        int r = invoiceTable.getSelectedRow();
        if (r < 0) return;
        currentInvoice.getItems().remove(r);
        invoiceModel.removeRow(r);
        updateTotalsAndPayments();
        reloadInventory();
    }

    private void addPaymentFromUI() {
        String amtStr = paymentAmountField.getText().trim().replace(',', '.');
        if (amtStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "أدخل مبلغ الدفعة", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double amt = Double.parseDouble(amtStr);
            if (amt <= 0) {
                JOptionPane.showMessageDialog(this, "مبلغ غير صالح", "خطأ", JOptionPane.ERROR_MESSAGE);
                return;
            }
            LocalDateTime dt = currentInvoice.getDate() != null ? currentInvoice.getDate() : LocalDateTime.parse(dateField.getText().trim(), dtf);
            currentInvoice.addPayment(amt, dt);
            paymentAmountField.setText("");
            updateTotalsAndPayments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "بيانات الدفعة غير صحيحة", "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean ensureFirstPaymentAddedIfAny() {
        String amtStr = paymentAmountField.getText().trim().replace(',', '.');
        if (amtStr.isEmpty()) return true;
        try {
            double amt = Double.parseDouble(amtStr);
            if (amt <= 0) {
                JOptionPane.showMessageDialog(this, "مبلغ أول دفعة غير صالح", "خطأ", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            LocalDateTime dt = currentInvoice.getDate() != null ? currentInvoice.getDate() : LocalDateTime.parse(dateField.getText().trim(), dtf);
            currentInvoice.addPayment(amt, dt);
            paymentAmountField.setText("");
            updateTotalsAndPayments();
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "بيانات الدفعة غير صحيحة", "خطأ", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void updateTotalsAndPayments() {
        double total = currentInvoice.getTotal();
        double paid = currentInvoice.getPaymentsTotal();
        double remaining = total - paid;
        double absRem = Math.abs(remaining);

        totalLabel.setText(String.format("الإجمالي: %.2f", total));
        paidLabel.setText(String.format("المدفوع: %.2f", paid));
        if (Math.abs(remaining) < 1e-6) {
            remainingLabel.setText(String.format("الباقي: %.2f (%s)", 0.0, "مدفوع بالكامل"));
        } else if (remaining > 0) {
            remainingLabel.setText(String.format("الباقي: %.2f (%s)", absRem, "باقي عليه"));
        } else {
            remainingLabel.setText(String.format("الباقي: %.2f (%s)", absRem, "مسبق الدفع"));
        }
    }

    private void disableArrowKeysInTextField(JTextField field) {
        InputMap im = field.getInputMap(JComponent.WHEN_FOCUSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
    }
    private void setupKeyboardNavigation() {
        // --- 0. تعطيل الأسهم غير المرغوب فيها لمنع تبديل الواجهة ---
        // هذا القسم يمنع انتقال حدث الضغط على السهم إلى الحاوية الأم (مثل JTabbedPane)
        // عن طريق ربط السهم بوظيفة فارغة لا تفعل شيئًا.
        bind(customerNameField, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), () -> {});
        bind(customerNameField, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), () -> {});
        bind(customerPhoneField, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), () -> {});
        bind(customerPhoneField, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), () -> {});
        bind(addBtn, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), () -> {});
        bind(addBtn, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), () -> {});
        bind(removeItemBtn, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), () -> {});
        bind(removeItemBtn, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), () -> {});
        bind(editDateButton, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), () -> {});


        // تعطيل الأسهم الأفقية في حقول النص لتمكين التنقل بـ Ctrl
        disableArrowKeysInTextField(searchField);
        disableArrowKeysInTextField(paymentAmountField);

        // --- 1. التنقل في الشريط العلوي (بيانات العميل) ---
        // التنقل الأفقي باستخدام Ctrl (الكود الخاص بك)
        bind(customerNameField, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK), () -> customerPhoneField.requestFocusInWindow());
        bind(customerPhoneField, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK), () -> customerNameField.requestFocusInWindow());
        bind(customerPhoneField, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK), () -> editDateButton.requestFocusInWindow());
        bind(editDateButton, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), () -> customerPhoneField.requestFocusInWindow());


        // التنقل العمودي للأسفل
        bind(customerNameField, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), () -> tileRadio.requestFocusInWindow());
        bind(customerPhoneField, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), () -> tileRadio.requestFocusInWindow());
        bind(dateField, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), () -> tileRadio.requestFocusInWindow());
        bind(editDateButton, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), () -> paymentAmountField.requestFocusInWindow());

        // --- 2. التنقل في قسم الفلاتر والبحث والدفع ---
        // من الفلاتر للأعلى (رجوع)
        bind(tileRadio, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), () -> customerNameField.requestFocusInWindow());
        bind(sanitaryRadio, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), () -> customerNameField.requestFocusInWindow());

        // التنقل بين فلاتر البلاط والصحي
        bind(tileRadio, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), () -> sanitaryRadio.requestFocusInWindow());
        bind(sanitaryRadio, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), () -> tileRadio.requestFocusInWindow());

        // من الفلاتر للأسفل إلى البحث
        bind(tileRadio, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), () -> searchField.requestFocusInWindow());
        bind(sanitaryRadio, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), () -> searchField.requestFocusInWindow());

        // من البحث للأعلى وللأسفل
        bind(searchField, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), () -> tileRadio.requestFocusInWindow());
        bind(searchField, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), () -> inventoryTable.requestFocusInWindow());
        bind(searchBtn, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), () -> tileRadio.requestFocusInWindow());
        bind(searchBtn, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), () -> inventoryTable.requestFocusInWindow());


        // من حقل الدفعة للأعلى وللأسفل
        bind(paymentAmountField, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), () -> editDateButton.requestFocusInWindow());
        bind(paymentAmountField, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), () -> invoiceTable.requestFocusInWindow());

        // التنقل الأفقي بين قسم الفلاتر/البحث وقسم الدفعة
        bind(tileRadio, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), () -> paymentAmountField.requestFocusInWindow());
        bind(searchField, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), () -> paymentAmountField.requestFocusInWindow());
        bind(paymentAmountField, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), () -> tileRadio.requestFocusInWindow());
        bind(addPaymentBtn, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), () -> tileRadio.requestFocusInWindow());


        // --- 3. التنقل في الجداول واختصاراتها ---
        // جدول المخزون (يمين)
        InputMap invMap = inventoryTable.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap invAct = inventoryTable.getActionMap();
        invMap.put(KeyStroke.getKeyStroke("ENTER"), "toAdd");
        invAct.put("toAdd", new AbstractAction() { public void actionPerformed(ActionEvent e) {
            lastInventoryRow = inventoryTable.getSelectedRow();
            addBtn.requestFocusInWindow();
        }});
        invMap.put(KeyStroke.getKeyStroke("UP"), "invUp");
        invAct.put("invUp", new AbstractAction() { public void actionPerformed(ActionEvent e) {
            int r = inventoryTable.getSelectedRow();
            if (r <= 0) searchField.requestFocusInWindow();
            else inventoryTable.changeSelection(r - 1, 0, false, false);
        }});

        // زر الإضافة والرجوع منه بـ Backspace
        bind(addBtn, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), () -> addBtn.doClick());
        bind(addBtn, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), () -> {
            inventoryTable.requestFocusInWindow();
            if (lastInventoryRow >= 0 && lastInventoryRow < inventoryTable.getRowCount()) {
                inventoryTable.setRowSelectionInterval(lastInventoryRow, lastInventoryRow);
            }
        });

        // جدول الفاتورة (يسار)
        InputMap invcMap = invoiceTable.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap invcAct = invoiceTable.getActionMap();
        invcMap.put(KeyStroke.getKeyStroke("ENTER"), "toFooter");
        invcAct.put("toFooter", new AbstractAction(){ public void actionPerformed(ActionEvent e){
            lastInvoiceRow = invoiceTable.getSelectedRow();
            removeItemBtn.requestFocusInWindow();
        }});
        invcMap.put(KeyStroke.getKeyStroke("UP"), "upToPay");
        invcAct.put("upToPay", new AbstractAction(){ public void actionPerformed(ActionEvent e){
            int r = invoiceTable.getSelectedRow();
            if (r <= 0) paymentAmountField.requestFocusInWindow();
            else invoiceTable.changeSelection(r - 1, 0, false, false);
        }});

        // زر إزالة عنصر والرجوع منه بـ Backspace
        bind(removeItemBtn, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), () -> {
            invoiceTable.requestFocusInWindow();
            if(lastInvoiceRow >= 0 && lastInvoiceRow < invoiceTable.getRowCount()){
                invoiceTable.setRowSelectionInterval(lastInvoiceRow, lastInvoiceRow);
            }
        });

        // --- 4. اختصارات عامة ---
        // Shift للتبديل بين الجدولين
        bind(inventoryTable, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), () -> invoiceTable.requestFocusInWindow());
        bind(invoiceTable, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), () -> inventoryTable.requestFocusInWindow());

        // اختصارات على مستوى الواجهة كلها
        String saveKey = "saveAction";
        this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), saveKey);
        this.getActionMap().put(saveKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveBtn.doClick();
            }
        });

        String customerKey = "toCustomer";
        this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), customerKey);
        this.getActionMap().put(customerKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customerNameField.requestFocusInWindow();
            }
        });

        String paymentKey = "toPayment";
        this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK), paymentKey);
        this.getActionMap().put(paymentKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paymentAmountField.requestFocusInWindow();
            }
        });
    }
    private void bind(JComponent c, KeyStroke ks, Runnable r) {
        String key = "act-" + c.hashCode() + "-" + ks.toString();
        c.getInputMap(JComponent.WHEN_FOCUSED).put(ks, key);
        c.getActionMap().put(key, new AbstractAction(){ public void actionPerformed(ActionEvent e){ r.run(); }});
    }
}