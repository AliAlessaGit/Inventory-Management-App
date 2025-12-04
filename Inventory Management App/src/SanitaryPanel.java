import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class SanitaryPanel extends JPanel {
    private SanitaryService sanitaryService;
    private JTable sanitaryTable;
    private DefaultTableModel sanitaryTableModel;
    private List<SanitaryItem> displayedItems;

    // --- التغيير: إضافة لون التركيز ---
    private final Color FOCUS_YELLOW = Color.YELLOW;
    private final Font EXTRA_LARGE_FONT = new Font("Tahoma", Font.BOLD, 20);
    private final Font LARGE_FONT = new Font("Tahoma", Font.PLAIN, 18);
    private final Font BUTTON_FONT = new Font("Tahoma", Font.BOLD, 18);
    private final Font TABLE_FONT = new Font("Tahoma", Font.PLAIN, 19);
    private final Font HEADER_FONT = new Font("Tahoma", Font.BOLD, 17);
    private final Color PRIMARY_COLOR = new Color(63, 81, 181);

    private JLabel sumLabel;

    public SanitaryPanel(SanitaryService sanitaryService) {
        this.sanitaryService = sanitaryService;
        this.displayedItems = new ArrayList<>();
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initUI();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 15));
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JPanel searchAndRefreshPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JTextField searchField = new JTextField(20);
        searchField.setFont(LARGE_FONT);
        searchField.setPreferredSize(new Dimension(250, 40));

        JButton refreshButton = new JButton("تحديث");
        refreshButton.setFont(BUTTON_FONT);
        refreshButton.setBackground(PRIMARY_COLOR);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setPreferredSize(new Dimension(150, 40));
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        searchAndRefreshPanel.add(searchField);
        searchAndRefreshPanel.add(refreshButton);

        String[] filterOptions = new String[SanitaryItem.SanitaryType.values().length + 1];
        filterOptions[0] = "كل الأنواع";
        for (int i = 0; i < SanitaryItem.SanitaryType.values().length; i++) {
            filterOptions[i + 1] = SanitaryItem.SanitaryType.values()[i].getArabicName();
        }
        JComboBox<String> filterCombo = new JComboBox<>(filterOptions);
        filterCombo.setFont(LARGE_FONT);
        filterCombo.setPreferredSize(new Dimension(200, 40));
        filterCombo.setSelectedIndex(0);
        filterPanel.add(filterCombo);

        topPanel.add(searchAndRefreshPanel, BorderLayout.WEST);
        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"الاسم", "النوع", "النخب", "السعر", "الكمية", "القيمة الإجمالية"};
        sanitaryTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        sanitaryTable = new JTable(sanitaryTableModel);
        sanitaryTable.setFont(TABLE_FONT);
        sanitaryTable.setRowHeight(40);
        sanitaryTable.getTableHeader().setFont(HEADER_FONT);
        sanitaryTable.getTableHeader().setBackground(PRIMARY_COLOR);
        sanitaryTable.getTableHeader().setForeground(Color.WHITE);
        sanitaryTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        sanitaryTable.setSelectionBackground(new Color(197, 202, 233));
        sanitaryTable.setSelectionForeground(Color.BLACK);
        sanitaryTable.setGridColor(new Color(224, 224, 224));

        setColumnWidths(sanitaryTable);

        JScrollPane scrollPane = new JScrollPane(sanitaryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
        add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton deleteButton = new JButton("حذف");
        JButton editButton = new JButton("تعديل");
        JButton dispatchButton = new JButton("تخريج");

        // --- التغيير: إنشاء FocusListener لتغيير لون الإطار ---
        Border defaultButtonBorder = deleteButton.getBorder();
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
        for (JButton btn : new JButton[]{deleteButton, editButton, dispatchButton, refreshButton}) {
            btn.setFont(BUTTON_FONT);
            btn.setBackground(PRIMARY_COLOR);
            btn.setForeground(Color.WHITE);
            btn.setPreferredSize(buttonSize);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setFocusPainted(false);
            btn.addFocusListener(buttonFocusListener);
        }
        refreshButton.setPreferredSize(new Dimension(100, 40));

        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);
        buttonPanel.add(dispatchButton);

        sumLabel = new JLabel(" المجموع: 0.00 $");
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

        refreshButton.addActionListener(e -> refreshTable());
        deleteButton.addActionListener(e -> deleteSelectedItem());
        editButton.addActionListener(e -> editSelectedItem());
        searchField.addActionListener(e -> searchByName(searchField.getText()));

        dispatchButton.addActionListener(e -> {
            int selectedRow = sanitaryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "الرجاء اختيار عنصر أولاً.", "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }
            SanitaryItem item = displayedItems.get(selectedRow);
            String quantityStr = JOptionPane.showInputDialog(this, "أدخل الكمية المراد إنقاصها:", "تخريج كمية", JOptionPane.PLAIN_MESSAGE);

            if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                try {
                    int qtyToDecrease = Integer.parseInt(quantityStr.trim());
                    if (qtyToDecrease <= 0 || qtyToDecrease > item.getQuantity()) {
                        JOptionPane.showMessageDialog(this, "الكمية غير صالحة أو أكبر من المتاح.", "خطأ", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    SanitaryItem updated = new SanitaryItem(item.getName(), item.getPrice(), item.getQuantity() - qtyToDecrease, item.getType(), item.getWarehouse(), item.getGrade());
                    sanitaryService.update(item, updated);
                    sanitaryService.save();
                    refreshTable((String) filterCombo.getSelectedItem());
                    JOptionPane.showMessageDialog(this, "تم إنقاص الكمية بنجاح.", "تم", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "الرجاء إدخال رقم صحيح للكمية.", "خطأ إدخال", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        sanitaryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // --- التغيير: الضغط المزدوج أصبح للتخريج ---
                if (e.getClickCount() == 2 && sanitaryTable.getSelectedRow() != -1) {
                    dispatchButton.doClick();
                }
            }
        });

        addKeyboardNavigation(searchField, refreshButton, filterCombo, sanitaryTable, deleteButton, editButton, dispatchButton);

        refreshTable();
    }

    private void setColumnWidths(JTable table) {
        // --- التغيير: تغيير عرض عمود يؤثر على العمود التالي ---
        table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        // القيم التالية هي للعرض المبدئي فقط
        int[] widths = {
                250, // الاسم
                199, // النوع
                199, // النخب
                199, // السعر
                199, // الكمية
                199  // القيمة الإجمالية
        };

        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(widths[i]);
        }
    }
    private void addKeyboardNavigation(JTextField searchField,
                                       JButton refreshBtn,
                                       JComboBox<String> filterCombo,
                                       JTable table,
                                       JButton deleteBtn,
                                       JButton editBtn,
                                       JButton dispatchBtn) {

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
                    filterCombo.requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    table.requestFocusInWindow();
                    if (table.getRowCount() > 0) {
                        table.setRowSelectionInterval(0, 0);
                    }
                }
            }
        });

        filterCombo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ENTER) {
                    int next = (filterCombo.getSelectedIndex() + 1) % filterCombo.getItemCount();
                    filterCombo.setSelectedIndex(next);
                    refreshTable((String) filterCombo.getSelectedItem());
                    e.consume();
                } else if (code == KeyEvent.VK_UP) {
                    int count = filterCombo.getItemCount();
                    int prev = (filterCombo.getSelectedIndex() - 1 + count) % count;
                    filterCombo.setSelectedIndex(prev);
                    refreshTable((String) filterCombo.getSelectedItem());
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
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {              // التحرك نحو اليسار (بحسب ترتيب الأزرار)
                    if (source == dispatchBtn) {
                        editBtn.requestFocusInWindow();
                    } else if (source == editBtn) {
                        deleteBtn.requestFocusInWindow();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {              // التحرك نحو اليمين
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
    private void deleteSelectedItem() {
        int selectedRow = sanitaryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, " الرجاء اختيار عنصر للحذف", "لا يوجد عنصر مُختار", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SanitaryItem toDelete = displayedItems.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "هل أنت متأكد من حذف العنصر:" + toDelete.getName() + " (النخب: " + toDelete.getGrade() + ")؟",
                "تأكيد الحذف", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            sanitaryService.remove(toDelete);
            sanitaryService.save();
            refreshTable();
            JOptionPane.showMessageDialog(this, " تم حذف العنصر بنجاح", "تم الحذف", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void editSelectedItem() {
        int selectedRow = sanitaryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, " الرجاء اختيار عنصر للتعديل", "لا يوجد عنصر مُختار", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SanitaryItem target = displayedItems.get(selectedRow);

        JTextField nameField = new JTextField(target.getName(), 20);
        JTextField priceField = new JTextField(String.valueOf(target.getPrice()), 20);
        JTextField quantityField = new JTextField(String.valueOf(target.getQuantity()), 20);
        JTextField gradeField = new JTextField(target.getGrade(), 20);

        JComboBox<SanitaryItem.SanitaryType> typeCombo = new JComboBox<>(SanitaryItem.SanitaryType.values());
        typeCombo.setSelectedItem(target.getType());

        JPanel panel = new JPanel(new GridLayout(5, 2, 20, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel nameLabel = new JLabel(" الاسم:");
        JLabel priceLabel = new JLabel(" السعر:");
        JLabel quantityLabel = new JLabel(" الكمية:");
        JLabel typeLabel = new JLabel(" النوع:");
        JLabel gradeLabel = new JLabel(" النخب:");
        nameLabel.setFont(HEADER_FONT);
        priceLabel.setFont(HEADER_FONT);
        quantityLabel.setFont(HEADER_FONT);
        typeLabel.setFont(HEADER_FONT);
        gradeLabel.setFont(HEADER_FONT);
        panel.add(nameField);      panel.add(nameLabel);
        panel.add(priceField);     panel.add(priceLabel);
        panel.add(quantityField);  panel.add(quantityLabel);
        panel.add(typeCombo);      panel.add(typeLabel);
        panel.add(gradeField);     panel.add(gradeLabel);

        int result = JOptionPane.showConfirmDialog(this, panel, "✏️ تعديل عنصر صحي", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String newName = nameField.getText().trim();
                double newPrice = Double.parseDouble(priceField.getText().trim());
                int newQuantity = Integer.parseInt(quantityField.getText().trim());
                SanitaryItem.SanitaryType newType = (SanitaryItem.SanitaryType) typeCombo.getSelectedItem();
                String newGrade = gradeField.getText().trim();

                if (newName.isEmpty() || newType == null || newGrade.isEmpty()) {
                    JOptionPane.showMessageDialog(this, " الرجاء ملء جميع الحقول بشكل صحيح", "بيانات ناقصة", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                SanitaryItem updated = new SanitaryItem(newName, newPrice, newQuantity, newType, target.getWarehouse(), newGrade);
                sanitaryService.update(target, updated);
                sanitaryService.save();
                refreshTable();
                JOptionPane.showMessageDialog(this, " تم تعديل العنصر بنجاح", "تم التعديل", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "❌ تأكد من إدخال السعر والكمية بشكل صحيح", "خطأ في البيانات", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            refreshTable();
            return;
        }
        List<SanitaryItem> matched = sanitaryService.getAll().stream()
                .filter(item -> item.getName().toLowerCase().contains(name.trim().toLowerCase()))
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
        populateTable(matched);

        if (matched.isEmpty()) {
            JOptionPane.showMessageDialog(this, " لا توجد نتائج للبحث: " + name, "لا توجد نتائج", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void refreshTable() { refreshTable("كل الأنواع"); }

    public void refreshTable(String filter) {
        List<SanitaryItem> filtered = sanitaryService.filter(filter);
        filtered.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        populateTable(filtered);
    }

    private void populateTable(List<SanitaryItem> items) {
        sanitaryTableModel.setRowCount(0);
        this.displayedItems = items;
        double sum = 0;

        for (SanitaryItem item : displayedItems) {
            double totalValue = item.getTotalValue();
            sanitaryTableModel.addRow(new Object[]{
                    item.getName(), item.getType().getArabicName(), item.getGrade(),
                    String.format("%.2f $", item.getPrice()), String.valueOf(item.getQuantity()),
                    String.format("%.2f $", totalValue)
            });
            sum += totalValue;
        }

        sumLabel.setText(String.format(" المجموع الكلي: %.2f $", sum));
        if (sum > 10000) {
            sumLabel.setForeground(new Color(76, 175, 80));
        } else if (sum > 5000) {
            sumLabel.setForeground(new Color(255, 152, 0));
        } else {
            sumLabel.setForeground(new Color(96, 125, 139));
        }
    }
}