import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
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
    private final JTable table;
    private final JLabel sumLabel;
    private List<TileItem> displayedItems;

    // --- التغيير: إضافة لون التركيز ---
    private final Color FOCUS_YELLOW = Color.YELLOW;
    private final Font EXTRA_LARGE_FONT = new Font("Tahoma", Font.BOLD, 20);
    private final Font LARGE_FONT = new Font("Tahoma", Font.PLAIN, 18);
    private final Font BUTTON_FONT = new Font("Tahoma", Font.BOLD, 18);
    private final Font TABLE_FONT = new Font("Tahoma", Font.PLAIN, 19);
    private final Font HEADER_FONT = new Font("Tahoma", Font.BOLD, 17);
    private final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private final Color LIGHT_GREY = new Color(224, 224, 224);
    private final Color SELECTION_BG = new Color(197, 202, 233);


    public TilePanel(TileService tileService, WarehouseManager warehouseManager) {
        this.tileService = tileService;
        this.warehouseManager = warehouseManager;
        this.displayedItems = new ArrayList<>();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout(10, 15));
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JPanel searchAndRefreshPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JTextField searchField = new JTextField(20);
        searchField.setFont(LARGE_FONT);
        searchField.setPreferredSize(new Dimension(250, 40));

        JButton refreshBtn = new JButton("تحديث");
        refreshBtn.setFont(BUTTON_FONT);
        refreshBtn.setBackground(PRIMARY_COLOR);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(150, 40));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        searchAndRefreshPanel.add(searchField);
        searchAndRefreshPanel.add(refreshBtn);

        JComboBox<String> warehouseCombo = new JComboBox<>();
        warehouseCombo.setFont(LARGE_FONT);
        warehouseCombo.setPreferredSize(new Dimension(200, 40));
        warehouseCombo.addItem("كل المستودعات");
        for (Warehouse w : warehouseManager.getWarehouses()) {
            warehouseCombo.addItem(w.getDisplayName());
        }
        filterPanel.add(warehouseCombo);

        topPanel.add(searchAndRefreshPanel, BorderLayout.WEST);
        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] cols = {
                "الرمز", "الاسم", "نوع المادة", "النوع الفرعي", "المستودع",
                "الموقع", "الدرجة", "السعر", "الصناديق", "مساحة الصندوق", "المساحة الإجمالية", "القيمة الإجمالية"
        };
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(TABLE_FONT);
        table.setRowHeight(40);
        table.getTableHeader().setFont(HEADER_FONT);
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(LIGHT_GREY);

        setColumnWidths(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
        add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton deleteBtn  = new JButton("حذف");
        JButton editBtn    = new JButton("تعديل");
        JButton dispatchBtn = new JButton("تخريج");

        // --- التغيير: إنشاء FocusListener لتغيير لون الإطار ---
        Border defaultButtonBorder = deleteBtn.getBorder();
        Border focusBorder = new CompoundBorder(BorderFactory.createLineBorder(FOCUS_YELLOW, 2), defaultButtonBorder);
        FocusListener buttonFocusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                ((JComponent) e.getSource()).setBorder(focusBorder);
            }
            @Override
            public void focusLost(FocusEvent e) {
                ((JComponent) e.getSource()).setBorder(defaultButtonBorder);
            }
        };

        Dimension buttonSize = new Dimension(110, 40);
        for (JButton btn : new JButton[]{deleteBtn, editBtn, dispatchBtn, refreshBtn}) {
            btn.setFont(BUTTON_FONT);
            btn.setBackground(PRIMARY_COLOR);
            btn.setForeground(Color.WHITE);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(buttonSize);
            btn.setFocusPainted(false); // مهم لإخفاء إطار التركيز الافتراضي
            btn.addFocusListener(buttonFocusListener);
        }
        refreshBtn.setPreferredSize(new Dimension(100, 40)); // حجم خاص لزر التحديث

        buttonPanel.add(deleteBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(dispatchBtn);

        sumLabel = new JLabel("المجموع: 0.00");
        sumLabel.setFont(EXTRA_LARGE_FONT);
        sumLabel.setForeground(PRIMARY_COLOR);
        sumLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        sumLabel.setHorizontalAlignment(SwingConstants.CENTER);

        controlPanel.add(buttonPanel, BorderLayout.CENTER);
        controlPanel.add(sumLabel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);

        Runnable applyWarehouseFilter = () -> {
            String sel = (String) warehouseCombo.getSelectedItem();
            List<TileItem> list = tileService.getAll();
            if (!"كل المستودعات".equals(sel)) {
                list = list.stream()
                        .filter(it -> it.getWarehouse().getDisplayName().equals(sel))
                        .collect(Collectors.toList());
            }
            loadTable(list);
        };

        refreshBtn.addActionListener(e -> loadTable(tileService.getAll()));

        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(table, "يجب اختيار عنصر أولاً من الجدول.", "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }
            TileItem itemToDelete = displayedItems.get(selectedRow);
            int choice = JOptionPane.showConfirmDialog(table,
                    "سيتم حذف العنصر: " + itemToDelete.getName() + " (الرمز: " + itemToDelete.getCode() + ")" +

                            "هل أنت متأكد؟", "تأكيد الحذف", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                try {
                    tileService.remove(itemToDelete);
                    tileService.save();
                    applyWarehouseFilter.run();
                    JOptionPane.showMessageDialog(table, "تم حذف العنصر بنجاح.", "تم", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(table, "حدث خطأ أثناء الحذف: " + ex.getMessage() ,  "خطأ", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(table, "يجب اختيار عنصر أولاً من الجدول.", "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }
            TileItem target = displayedItems.get(selectedRow);

            JTextField nameField = new JTextField(target.getName(), 15);
            JTextField codeField = new JTextField(target.getCode(), 15);
            JTextField priceField = new JTextField(String.valueOf(target.getPrice()), 15);
            JTextField boxesField = new JTextField(String.valueOf(target.getBoxes()), 15);
            JTextField boxAreaField = new JTextField(String.valueOf(target.getBoxArea()), 15);
            JTextField gradeField = new JTextField(target.getGrade(), 15);
            JComboBox<TileItem.MaterialType> materialCombo = new JComboBox<>(TileItem.MaterialType.values());
            materialCombo.setSelectedItem(target.getMaterialType());
            JComboBox<TileItem.SubType> subtypeCombo = new JComboBox<>(TileItem.SubType.values());
            subtypeCombo.setSelectedItem(target.getSubType());
            JComboBox<TileItem.Location> locationCombo = new JComboBox<>(TileItem.Location.values());
            locationCombo.setSelectedItem(target.getLocation());
            locationCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof TileItem.Location loc) value = loc.getArabicName();
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
            JComboBox<Warehouse> editWarehouseCombo = new JComboBox<>();
            for (Warehouse w : warehouseManager.getWarehouses()) {
                editWarehouseCombo.addItem(w);
                if (w.getId() == target.getWarehouse().getId()) {
                    editWarehouseCombo.setSelectedItem(w);
                }
            }
            editWarehouseCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof Warehouse w) value = w.getDisplayName();
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });

            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            panel.add(codeField);panel.add(new JLabel("الرمز:"));
            panel.add(nameField);panel.add(new JLabel("الاسم:"));
            panel.add(priceField);panel.add(new JLabel("السعر:"));
            panel.add(boxesField);panel.add(new JLabel("الصناديق:"));
            panel.add(boxAreaField);panel.add(new JLabel("مساحة الصندوق:"));
            panel.add(gradeField);panel.add(new JLabel("النخب:"));
            panel.add(materialCombo);panel.add(new JLabel("نوع المادة:"));
            panel.add(subtypeCombo);panel.add(new JLabel("النوع الفرعي:"));
            panel.add(locationCombo);panel.add(new JLabel("الموقع:"));
            panel.add(editWarehouseCombo);panel.add(new JLabel("المستودع:"));

            int result = JOptionPane.showConfirmDialog(this, panel, "تعديل عنصر البلاط", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String newCode = codeField.getText();
                    String newName = nameField.getText().trim();
                    double newPrice = Double.parseDouble(priceField.getText().trim());
                    double newBoxes = Double.parseDouble(boxesField.getText().trim());
                    double newArea = Double.parseDouble(boxAreaField.getText().trim());
                    String newGrade = gradeField.getText().trim();
                    TileItem.MaterialType newMat = (TileItem.MaterialType) materialCombo.getSelectedItem();
                    TileItem.SubType newSub = (TileItem.SubType) subtypeCombo.getSelectedItem();
                    TileItem.Location newLoc = (TileItem.Location) locationCombo.getSelectedItem();
                    Warehouse newWarehouse = (Warehouse) editWarehouseCombo.getSelectedItem();
                    if (newName.isEmpty() || newGrade.isEmpty() || newMat == null || newSub == null || newWarehouse == null || newLoc == null) {
                        JOptionPane.showMessageDialog(this, "يرجى ملء جميع الحقول بشكل صحيح.", "بيانات ناقصة", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    TileItem updated = new TileItem(newName, newCode, newPrice, newBoxes, newArea, newMat, newSub, newWarehouse, newGrade, newLoc);
                    tileService.update(target, updated);
                    tileService.save();
                    applyWarehouseFilter.run();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "تأكد من إدخال القيم الرقمية بشكل صحيح.", "خطأ إدخال", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "بيانات غير صالحة: " + ex.getMessage(), "خطأ إدخال", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        searchField.addActionListener(e -> {
            String txt = searchField.getText();
            List<TileItem> found = tileService.searchByNameOrCode(txt);
            loadTable(found);
        });

        dispatchBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(table, "يجب اختيار عنصر أولاً من الجدول.", "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }
            TileItem item = displayedItems.get(selectedRow);
            String quantityStr = JOptionPane.showInputDialog(this, "أدخل الكمية (صناديق) المراد إنقاصها:", "تخريج كمية", JOptionPane.PLAIN_MESSAGE);

            if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                try {
                    double qtyToDecrease = Double.parseDouble(quantityStr.trim());
                    if (qtyToDecrease <= 0 || qtyToDecrease > item.getBoxes()) {
                        JOptionPane.showMessageDialog(this, "الكمية غير صالحة أو أكبر من المتاح.", "خطأ", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    TileItem updated = new TileItem(item.getName(), item.getCode(), item.getPrice(), item.getBoxes() - qtyToDecrease, item.getBoxArea(), item.getMaterialType(), item.getSubType(), item.getWarehouse(), item.getGrade(), item.getLocation());
                    tileService.update(item, updated);
                    tileService.save();
                    applyWarehouseFilter.run();
                    JOptionPane.showMessageDialog(this, "تم إنقاص الكمية بنجاح.", "تم", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "الرجاء إدخال رقم صحيح للكمية.", "خطأ إدخال", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // --- التغيير: الضغط المزدوج أصبح للتخريج ---
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    dispatchBtn.doClick();
                }
            }
        });

        addKeyboardNavigation(searchField, refreshBtn, warehouseCombo, table, deleteBtn, editBtn, dispatchBtn, applyWarehouseFilter);

        loadTable(tileService.getAll());
    }

    private void setColumnWidths(JTable table) {
        // --- التغيير: تغيير عرض عمود يؤثر على العمود التالي ---
        table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        // القيم التالية هي للعرض المبدئي فقط
        int[] widths = {
                110, // الرمز
                195, // الاسم (أعرض)
                100, // نوع المادة
                100, // النوع الفرعي
                100, // المستودع
                80,  // الموقع
                80,  // الدرجة
                80,  // السعر
                80,  // الصناديق
                100, // مساحة الصندوق
                110, // المساحة الإجمالية
                110  // القيمة الإجمالية
        };

        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(widths[i]);
        }
    }
    private void addKeyboardNavigation(JTextField searchField,
                                       JButton refreshBtn,
                                       JComboBox<String> warehouseCombo,
                                       JTable table,
                                       JButton deleteBtn,
                                       JButton editBtn,
                                       JButton dispatchBtn,
                                       Runnable filterAction) {

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    table.requestFocusInWindow();
                    if (table.getRowCount() > 0) {
                        table.setRowSelectionInterval(0, 0);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    refreshBtn.requestFocusInWindow();
                }
            }
        });

        refreshBtn.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    searchField.requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    warehouseCombo.requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    table.requestFocusInWindow();
                    if (table.getRowCount() > 0) {
                        table.setRowSelectionInterval(0, 0);
                    }
                }
            }
        });

        warehouseCombo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ENTER) {
                    int next = (warehouseCombo.getSelectedIndex() + 1) % warehouseCombo.getItemCount();
                    warehouseCombo.setSelectedIndex(next);
                    filterAction.run();
                    e.consume();
                } else if (code == KeyEvent.VK_UP) {
                    int count = warehouseCombo.getItemCount();
                    int prev = (warehouseCombo.getSelectedIndex() - 1 + count) % count;
                    warehouseCombo.setSelectedIndex(prev);
                    filterAction.run();
                    e.consume();
                } else if (code == KeyEvent.VK_DOWN) {
                    table.requestFocusInWindow();
                    if (table.getRowCount() > 0) {
                        table.setRowSelectionInterval(0, 0);
                    }
                    e.consume();
                } else if (code == KeyEvent.VK_LEFT) {
                    refreshBtn.requestFocusInWindow();
                }
            }
        });

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && table.getSelectedRow() != -1) {
                    editBtn.requestFocusInWindow();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP && table.getSelectedRow() <= 0) {
                    searchField.requestFocusInWindow();
                    table.clearSelection();
                }
            }
        });

        KeyAdapter buttonNavListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Object source = e.getSource();
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    table.requestFocusInWindow();
                    table.clearSelection();
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {              // التحرك نحو اليمين (وفق ترتيب RTL)
                    if (source == dispatchBtn) {
                        editBtn.requestFocusInWindow();
                    } else if (source == editBtn) {
                        deleteBtn.requestFocusInWindow();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {              // التحرك نحو اليسار
                    if (source == deleteBtn) {
                        editBtn.requestFocusInWindow();
                    } else if (source == editBtn) {
                        dispatchBtn.requestFocusInWindow();
                    }
                }
            }
        };

        deleteBtn.addKeyListener(buttonNavListener);
        editBtn.addKeyListener(buttonNavListener);
        dispatchBtn.addKeyListener(buttonNavListener);
    }
    private void loadTable(List<TileItem> list) {
        model.setRowCount(0);
        this.displayedItems = list.stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .collect(Collectors.toList());

        displayedItems.forEach(it -> {
            double totalArea  = it.getBoxes() * it.getBoxArea();
            double totalValue = totalArea * it.getPrice();
            model.addRow(new Object[]{
                    it.getCode(), it.getName(), it.getMaterialType().name(), it.getSubType().name(),
                    it.getWarehouse().getDisplayName(), it.getLocation().getArabicName(), it.getGrade(),
                    String.format("%.2f", it.getPrice()), it.getBoxes(), it.getBoxArea(),
                    String.format("%.2f", totalArea), totalValue
            });
        });

        double sum = displayedItems.stream().mapToDouble(it -> it.getBoxes() * it.getBoxArea() * it.getPrice()).sum();

        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(String.format("%.2f", model.getValueAt(i, 11)), i, 11);
        }

        sumLabel.setText(String.format("المجموع الكلي: %.2f", sum));
        if (sum > 50000) sumLabel.setForeground(new Color(255,1 , 0));
        else if (sum > 10000) sumLabel.setForeground(new Color(255, 152, 0));
        else sumLabel.setForeground(new Color(96, 125, 139));
    }
}