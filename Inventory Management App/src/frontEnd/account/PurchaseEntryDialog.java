package frontEnd.account;

import backEnd.account.AccountInventoryKind;
import backEnd.account.AccountStockMovementType;
import backEnd.sanitary.SanitaryItem;
import backEnd.sanitary.SanitaryService;
import backEnd.tile.TileItem;
import backEnd.tile.TileService;
import backEnd.warehouse.Warehouse;
import backEnd.warehouse.WarehouseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class PurchaseEntryDialog extends JDialog {

    private final TileService tileService;
    private final SanitaryService sanitaryService;
    private final WarehouseManager warehouseManager;

    private final JComboBox<AccountInventoryKind> kindBox =
            new JComboBox<>(AccountInventoryKind.values());
    private final JComboBox<AccountStockMovementType> movementBox =
            new JComboBox<>(AccountStockMovementType.values());

    private final CardLayout cards = new CardLayout();
    private final JPanel kindCards = new JPanel(cards);

    private final JTextField materialField = new JTextField();
    private final JTextField quantity1Field = new JTextField("0");
    private final JTextField quantity2Field = new JTextField("0");
    private final JTextField unitField = new JTextField("قطعة");
    private final JTextField priceField = new JTextField("0");
    private final JTextField boxAreaField = new JTextField("0");

    private final JTextField searchField = new JTextField();
    private final JComboBox<TileItem> tileBox = new JComboBox<>();
    private final JComboBox<SanitaryItem> sanitaryBox = new JComboBox<>();

    private final JTextField newTileCode = new JTextField();
    private final JTextField newTileName = new JTextField();
    private final JTextField newTileGrade = new JTextField();
    private final JTextField newTileBoxArea = new JTextField("0");
    private final JTextField newTileBoxes = new JTextField("0");
    private final JComboBox<TileItem.MaterialType> newTileMaterial =
            new JComboBox<>(TileItem.MaterialType.values());
    private final JComboBox<TileItem.SubType> newTileSubtype =
            new JComboBox<>(TileItem.SubType.values());
    private final JComboBox<TileItem.Location> newTileLocation =
            new JComboBox<>(TileItem.Location.values());
    private final JComboBox<Warehouse> newTileWarehouse = new JComboBox<>();
    private final JTextField newTilePrice = new JTextField("0");

    private final JTextField newSanitaryName = new JTextField();
    private final JTextField newSanitaryGrade = new JTextField();
    private final JTextField newSanitaryQuantity = new JTextField("0");
    private final JComboBox<SanitaryItem.SanitaryType> newSanitaryType =
            new JComboBox<>(SanitaryItem.SanitaryType.values());
    private final JComboBox<Warehouse> newSanitaryWarehouse = new JComboBox<>();
    private final JTextField newSanitaryPrice = new JTextField("0");

    private boolean ok;
    private PurchaseData data;

    public PurchaseEntryDialog(Window parent,
                               TileService tileService,
                               SanitaryService sanitaryService,
                               WarehouseManager warehouseManager) {
        super(parent, "إضافة مادة إلى فاتورة الشراء", ModalityType.APPLICATION_MODAL);
        this.tileService = tileService;
        this.sanitaryService = sanitaryService;
        this.warehouseManager = warehouseManager;

        setSize(900, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(15, 20, 8, 20));
        GridBagConstraints h = new GridBagConstraints();
        h.insets = new Insets(5, 8, 5, 8);
        h.fill = GridBagConstraints.HORIZONTAL;
        h.weightx = 1;
        addHeaderRow(header, h, 0, "نوع المادة:", kindBox);
        addHeaderRow(header, h, 1, "طريقة الإدخال:", movementBox);

        buildCards();

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttons.setBackground(Color.WHITE);
        JButton okButton = new JButton("موافق");
        JButton cancelButton = new JButton("إلغاء");
        buttons.add(okButton);
        buttons.add(new JLabel("←"));
        buttons.add(cancelButton);
        okButton.addActionListener(e -> accept());
        cancelButton.addActionListener(e -> dispose());

        kindBox.addActionListener(e -> refreshCards());
        movementBox.addActionListener(e -> refreshCards());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterExisting(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterExisting(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterExisting(); }
        });

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(kindCards), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        installNavigation(okButton, cancelButton);
        refreshCards();
    }

    private void addHeaderRow(JPanel panel, GridBagConstraints g, int row,
                              String label, JComponent component) {
        g.gridy = row;
        g.gridx = 0;
        g.weightx = .2;
        panel.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = .8;
        panel.add(component, g);
    }

    private void buildCards() {
        kindCards.add(buildNotInStockPanel(), "NOT_IN_STOCK");
        kindCards.add(buildExistingPanel(), "EXISTING_ITEM");
        kindCards.add(buildNewPanel(), "NEW_ITEM");
    }

    private JPanel buildNotInStockPanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "المادة:", materialField, "اكتب اسم المادة كما تريد ظهوره في الفاتورة");
        addRow(panel, 1, "الكمية 1:", quantity1Field, "في البلاط = المساحة الكلية");
        addRow(panel, 2, "الكمية 2:", quantity2Field, "في البلاط = عدد الصناديق");
        addRow(panel, 3, "الوحدة:", unitField, "للبلاط يتم تثبيتها على متر");
        addRow(panel, 4, "السعر:", priceField, "سعر وحدة القياس");
        addRow(panel, 5, "مساحة الصندوق:", boxAreaField, "للاستخدام عند إدخال بيانات البلاط");
        return panel;
    }

    private JPanel buildExistingPanel() {
        JPanel panel = formPanel();
        JPanel search = new JPanel(new BorderLayout(8, 8));
        search.setBackground(Color.WHITE);
        search.add(new JLabel("بحث:"), BorderLayout.WEST);
        search.add(searchField, BorderLayout.CENTER);
        panel.add(search, constraints(0));

        JPanel tilePanel = formPanel();
        addRow(tilePanel, 0, "صنف البلاط:", tileBox, "اختر الصنف الموجود");
        addRow(tilePanel, 1, "عدد الصناديق:", quantity2Field, "الكمية 2");
        addRow(tilePanel, 2, "السعر:", priceField, "السعر الجديد");

        JPanel sanitaryPanel = formPanel();
        addRow(sanitaryPanel, 0, "الصنف الصحي:", sanitaryBox, "اختر الصنف الموجود");
        addRow(sanitaryPanel, 1, "الكمية:", quantity1Field, "الكمية 1 والكمية 2");
        addRow(sanitaryPanel, 2, "الوحدة:", unitField, "قطعة افتراضياً ويمكن تعديلها");
        addRow(sanitaryPanel, 3, "السعر:", priceField, "السعر الجديد");

        panel.add(tilePanel, constraints(1));
        panel.add(sanitaryPanel, constraints(2));
        tilePanel.setName("tileExisting");
        sanitaryPanel.setName("sanitaryExisting");
        loadExistingItems();
        return panel;
    }

    private JPanel buildNewPanel() {
        JPanel panel = formPanel();
        JPanel tile = formPanel();
        addRow(tile, 0, "الرمز:", newTileCode, "رمز البلاط");
        addRow(tile, 1, "الاسم:", newTileName, "اسم البلاط");
        addRow(tile, 2, "النخب:", newTileGrade, "النخب");
        addRow(tile, 3, "مساحة الصندوق:", newTileBoxArea, "متر مربع");
        addRow(tile, 4, "عدد الصناديق:", newTileBoxes, "الكمية 2");
        addRow(tile, 5, "نوع المادة:", newTileMaterial, "سيراميك أو غرانيت");
        addRow(tile, 6, "النوع:", newTileSubtype, "أرضيات أو جدران أو نعلات");
        addRow(tile, 7, "الموقع:", newTileLocation, "موقع التخزين");
        addRow(tile, 8, "المستودع:", newTileWarehouse, "المستودع");
        addRow(tile, 9, "السعر:", newTilePrice, "السعر");

        JPanel sanitary = formPanel();
        addRow(sanitary, 0, "الاسم:", newSanitaryName, "اسم الأداة الصحية");
        addRow(sanitary, 1, "النخب:", newSanitaryGrade, "النخب");
        addRow(sanitary, 2, "الكمية:", newSanitaryQuantity, "كمية صحيحة");
        addRow(sanitary, 3, "النوع:", newSanitaryType, "نوع الأداة الصحية");
        addRow(sanitary, 4, "المستودع:", newSanitaryWarehouse, "المستودع");
        addRow(sanitary, 5, "السعر:", newSanitaryPrice, "السعر");

        panel.add(tile, constraints(0));
        panel.add(sanitary, constraints(1));

        List<Warehouse> warehouses = warehouseManager == null
                ? List.of() : warehouseManager.getWarehouses();
        for (Warehouse w : warehouses) {
            newTileWarehouse.addItem(w);
            newSanitaryWarehouse.addItem(w);
        }
        return panel;
    }

    private JPanel formPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(12, 18, 12, 18));
        return p;
    }

    private GridBagConstraints constraints(int row) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row;
        g.gridx = 0;
        g.gridwidth = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 4, 4, 4);
        return g;
    }

    private void addRow(JPanel panel, int row, String label,
                        JComponent field, String hint) {
        GridBagConstraints g = constraints(row);
        g.gridx = 0;
        g.weightx = .18;
        panel.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = .57;
        panel.add(field, g);
        g.gridx = 2;
        g.weightx = .25;
        JLabel l = new JLabel(hint);
        l.setForeground(new Color(100, 100, 100));
        panel.add(l, g);
    }

    private void refreshCards() {
        AccountStockMovementType movement =
                (AccountStockMovementType) movementBox.getSelectedItem();
        AccountInventoryKind kind =
                (AccountInventoryKind) kindBox.getSelectedItem();
        if (movement == null || kind == null) return;

        cards.show(kindCards, movement.name());

        if (movement == AccountStockMovementType.NOT_IN_STOCK) {
            unitField.setText(kind == AccountInventoryKind.TILE ? "متر" : "قطعة");
        }
        if (movement == AccountStockMovementType.EXISTING_ITEM) {
            updateExistingVisibility(kind);
        }
        if (movement == AccountStockMovementType.NEW_ITEM) {
            // حقول الصنفين موجودة في نفس الصفحة، لكن سيتم استعمال النوع المختار فقط.
        }
    }

    private void updateExistingVisibility(AccountInventoryKind kind) {
        // لا نغير بنية الكارت، بل نحدد العنصر المختار ونترك الحقول الأخرى غير مؤثرة.
        if (kind == AccountInventoryKind.TILE && tileBox.getItemCount() > 0) {
            tileBox.setSelectedIndex(0);
        }
        if (kind == AccountInventoryKind.SANITARY && sanitaryBox.getItemCount() > 0) {
            sanitaryBox.setSelectedIndex(0);
        }
    }

    private void loadExistingItems() {
        tileBox.removeAllItems();
        for (TileItem item : tileService.getAll()) tileBox.addItem(item);
        sanitaryBox.removeAllItems();
        for (SanitaryItem item : sanitaryService.getAll()) sanitaryBox.addItem(item);
        tileBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TileItem t) setText(t.getCode() + " - " + t.getName() + " - " + t.getGrade());
                return this;
            }
        });
        sanitaryBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SanitaryItem s) setText(s.getName() + " - " + s.getGrade());
                return this;
            }
        });
    }

    private void filterExisting() {
        String q = searchField.getText().trim().toLowerCase();
        Object selectedTile = tileBox.getSelectedItem();
        Object selectedSanitary = sanitaryBox.getSelectedItem();
        tileBox.removeAllItems();
        for (TileItem item : tileService.getAll()) {
            String text = (item.getCode() + " " + item.getName() + " " + item.getGrade()).toLowerCase();
            if (q.isEmpty() || text.contains(q)) tileBox.addItem(item);
        }
        sanitaryBox.removeAllItems();
        for (SanitaryItem item : sanitaryService.getAll()) {
            String text = (item.getName() + " " + item.getGrade()).toLowerCase();
            if (q.isEmpty() || text.contains(q)) sanitaryBox.addItem(item);
        }
        if (selectedTile != null) tileBox.setSelectedItem(selectedTile);
        if (selectedSanitary != null) sanitaryBox.setSelectedItem(selectedSanitary);
    }

    private void accept() {
        try {
            AccountInventoryKind kind = (AccountInventoryKind) kindBox.getSelectedItem();
            AccountStockMovementType movement = (AccountStockMovementType) movementBox.getSelectedItem();
            if (kind == null || movement == null) throw new IllegalArgumentException("نوع المادة وطريقة الإدخال مطلوبان");

            data = new PurchaseData();
            data.kind = kind;
            data.movement = movement;

            if (movement == AccountStockMovementType.NOT_IN_STOCK) {
                data.material = materialField.getText().trim();
                if (data.material.isEmpty()) throw new IllegalArgumentException("المادة مطلوبة");
                data.quantity1 = number(quantity1Field);
                data.quantity2 = number(quantity2Field);
                data.unit = unitField.getText().trim();
                data.price = number(priceField);
                data.boxArea = number(boxAreaField);
                if (kind == AccountInventoryKind.TILE) data.unit = "متر";
            } else if (movement == AccountStockMovementType.EXISTING_ITEM) {
                if (kind == AccountInventoryKind.TILE) {
                    data.tileItem = (TileItem) tileBox.getSelectedItem();
                    if (data.tileItem == null) throw new IllegalArgumentException("اختر صنف البلاط");
                    data.quantity2 = number(quantity2Field);
                    data.price = number(priceField);
                } else {
                    data.sanitaryItem = (SanitaryItem) sanitaryBox.getSelectedItem();
                    if (data.sanitaryItem == null) throw new IllegalArgumentException("اختر الصنف الصحي");
                    data.quantity1 = number(quantity1Field);
                    data.unit = unitField.getText().trim();
                    data.price = number(priceField);
                }
            } else {
                if (kind == AccountInventoryKind.TILE) {
                    data.code = newTileCode.getText().trim();
                    data.name = newTileName.getText().trim();
                    data.grade = newTileGrade.getText().trim();
                    data.boxArea = number(newTileBoxArea);
                    data.quantity2 = number(newTileBoxes);
                    data.tileMaterialType = (TileItem.MaterialType) newTileMaterial.getSelectedItem();
                    data.tileSubtype = (TileItem.SubType) newTileSubtype.getSelectedItem();
                    data.location = (TileItem.Location) newTileLocation.getSelectedItem();
                    data.warehouse = (Warehouse) newTileWarehouse.getSelectedItem();
                    data.price = number(newTilePrice);
                    if (data.name.isEmpty() || data.grade.isEmpty()) throw new IllegalArgumentException("اسم البلاط والنخب مطلوبان");
                } else {
                    data.name = newSanitaryName.getText().trim();
                    data.grade = newSanitaryGrade.getText().trim();
                    data.quantity1 = number(newSanitaryQuantity);
                    data.sanitaryType = (SanitaryItem.SanitaryType) newSanitaryType.getSelectedItem();
                    data.warehouse = (Warehouse) newSanitaryWarehouse.getSelectedItem();
                    data.price = number(newSanitaryPrice);
                    if (data.name.isEmpty() || data.grade.isEmpty()) throw new IllegalArgumentException("اسم الصنف والنخب مطلوبان");
                }
            }
            ok = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage() == null ? "البيانات غير صحيحة" : ex.getMessage(),
                    "تنبيه", JOptionPane.WARNING_MESSAGE);
        }
    }

    private double number(JTextField field) {
        return Double.parseDouble(field.getText().trim().replace(',', '.'));
    }

    private void installNavigation(JButton okButton, JButton cancelButton) {
        List<Component> order = new ArrayList<>();
        order.add(kindBox);
        order.add(movementBox);
        order.add(okButton);
        order.add(cancelButton);
        InputMap input = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getRootPane().getActionMap();
        input.put(KeyStroke.getKeyStroke("DOWN"), "next");
        input.put(KeyStroke.getKeyStroke("UP"), "previous");
        input.put(KeyStroke.getKeyStroke("ENTER"), "enter");
        actions.put("next", new AbstractAction() { public void actionPerformed(ActionEvent e) { move(order, 1); } });
        actions.put("previous", new AbstractAction() { public void actionPerformed(ActionEvent e) { move(order, -1); } });
        actions.put("enter", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (c == okButton) okButton.doClick();
                else if (c == cancelButton) cancelButton.doClick();
                else move(order, 1);
            }
        });
    }

    private void move(List<Component> order, int direction) {
        Component current = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        int index = order.indexOf(current);
        if (index < 0) index = 0;
        int next = Math.max(0, Math.min(order.size() - 1, index + direction));
        order.get(next).requestFocusInWindow();
    }

    public boolean isOk() { return ok; }
    public PurchaseData getData() { return data; }

    public static class PurchaseData {
        public AccountInventoryKind kind;
        public AccountStockMovementType movement;
        public TileItem tileItem;
        public SanitaryItem sanitaryItem;
        public String material;
        public String code;
        public String name;
        public String grade;
        public double quantity1;
        public double quantity2;
        public double boxArea;
        public double price;
        public String unit;
        public TileItem.MaterialType tileMaterialType;
        public TileItem.SubType tileSubtype;
        public TileItem.Location location;
        public Warehouse warehouse;
        public SanitaryItem.SanitaryType sanitaryType;
    }
}
