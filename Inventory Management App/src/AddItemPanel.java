import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

public class AddItemPanel extends JPanel {
    private final TileService tileService;
    private final SanitaryService sanitaryService;
    private final List<Warehouse> warehouses;

    private JRadioButton tileRadio;
    private JRadioButton sanitaryRadio;

    private final Font EXTRA_LARGE_FONT = new Font("Tahoma", Font.BOLD, 18);
    private final Font FIELD_FONT = new Font("Tahoma", Font.PLAIN, 17);
    private final Font LABEL_FONT = new Font("Tahoma", Font.BOLD, 17);
    private final Font BUTTON_FONT = new Font("Tahoma", Font.BOLD, 20);
    private final Color PRIMARY_COLOR = new Color(63, 81, 181);

    public AddItemPanel(TileService tileService, SanitaryService sanitaryService, List<Warehouse> warehouses) {
        this.tileService = tileService;
        this.sanitaryService = sanitaryService;
        this.warehouses = warehouses;
        setLayout(new BorderLayout(10, 10));
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        initUI();
    }

    private void initUI() {
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 15));
        typePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ButtonGroup group = new ButtonGroup();
        tileRadio = new JRadioButton("إضافة بلاط");
        sanitaryRadio = new JRadioButton("إضافة أدوات صحية");
        tileRadio.setFont(EXTRA_LARGE_FONT);
        sanitaryRadio.setFont(EXTRA_LARGE_FONT);
        tileRadio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sanitaryRadio.setCursor(new Cursor(Cursor.HAND_CURSOR));

        group.add(tileRadio);
        group.add(sanitaryRadio);
        tileRadio.setSelected(true);

        typePanel.add(tileRadio);
        typePanel.add(sanitaryRadio);

        JPanel tileForm = createTileForm();
        JPanel sanitaryForm = createSanitaryForm();
        sanitaryForm.setVisible(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new CardLayout());
        contentPanel.add(tileForm, "TILE");
        contentPanel.add(sanitaryForm, "SANITARY");

        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();

        tileRadio.addActionListener(e -> {
            cardLayout.show(contentPanel, "TILE");
            tileForm.requestFocusInWindow();
        });
        sanitaryRadio.addActionListener(e -> {
            cardLayout.show(contentPanel, "SANITARY");
            sanitaryForm.requestFocusInWindow();
        });

        registerTypeSwitchingWithArrows(this);

        add(typePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTileForm() {
        // 11×2 مع صف للموقع
        JPanel panel = new JPanel(new GridLayout(11, 2, 8, 15));
        panel.setBorder(createTitledBorder("بيانات البلاط"));
        panel.setName("TILE_FORM");

        JTextField codeField = createTextField();
        JTextField nameField = createTextField();
        JTextField priceField = createTextField();
        JTextField gradeField = createTextField();
        JTextField boxesField = createTextField();
        JTextField capacityField = createTextField();

        JComboBox<TileItem.MaterialType> materialCombo = new JComboBox<>(TileItem.MaterialType.values());
        JComboBox<TileItem.SubType> typeCombo = new JComboBox<>(TileItem.SubType.values());
        JComboBox<TileItem.Location> locationCombo = new JComboBox<>(TileItem.Location.values());
        JComboBox<Warehouse> warehouseCombo = new JComboBox<>();

        setupComboBox(materialCombo);
        setupComboBox(typeCombo);
        setupComboBox(locationCombo);
        setupComboBox(warehouseCombo);

        // Renderer عربي للموقع
        locationCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof TileItem.Location loc) value = loc.getArabicName();
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        for (Warehouse w : warehouses) {
            warehouseCombo.addItem(w);
        }
        warehouseCombo.setRenderer(new WarehouseRenderer(FIELD_FONT));

        JButton addButton = createAddButton("➕ إضافة البلاط");
        addButton.addActionListener(e -> {
            try {
                String code = codeField.getText().trim();
                if (code.isEmpty()) code = "00"; // التعديل: افتراض "00" عند ترك الرمز فارغاً

                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                double boxes = Double.parseDouble(boxesField.getText().trim());
                double capacity = Double.parseDouble(capacityField.getText().trim());
                Warehouse warehouse = (Warehouse) warehouseCombo.getSelectedItem();
                String grade = gradeField.getText().trim();
                TileItem.MaterialType materialType = (TileItem.MaterialType) materialCombo.getSelectedItem();
                TileItem.SubType subType = (TileItem.SubType) typeCombo.getSelectedItem();
                TileItem.Location location = (TileItem.Location) locationCombo.getSelectedItem();

                // التعديل: إزالة شرط الرمز من التحقق
                if (name.isEmpty() || grade.isEmpty() || warehouse == null || location == null) {
                    showWarning("الرجاء إدخال جميع البيانات المطلوبة");
                    return;
                }

                TileItem item = new TileItem(name, code, price, boxes, capacity, materialType, subType, warehouse, grade, location);
                tileService.add(item);
                tileService.save();
                showSuccess("تمت إضافة البلاط بنجاح:"
                         + "الرمز: " + code);

                clearFields(codeField, nameField, priceField, boxesField, capacityField, gradeField);
                codeField.requestFocusInWindow();
            } catch (NumberFormatException ex) {
                showError("الرجاء إدخال قيم رقمية صحيحة للسعر والكمية");
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("حدث خطأ: " + ex.getMessage());
            }
        });

        panel.add(codeField);panel.add(createLabel("الرمز:"));
        panel.add(nameField);panel.add(createLabel("اسم النقشة:"));
        panel.add(priceField);panel.add(createLabel("السعر للمتر المربع:"));
        panel.add(gradeField);panel.add(createLabel("النخب (الجودة):"));
        panel.add(boxesField);panel.add(createLabel("عدد الصناديق:"));
        panel.add(capacityField);panel.add(createLabel("مساحة الصندوق (م²):"));
        panel.add(materialCombo);panel.add(createLabel("نوع المادة:"));
        panel.add(typeCombo);panel.add(createLabel("النوع الفرعي:"));
        panel.add(locationCombo);panel.add(createLabel("الموقع:"));
        panel.add(warehouseCombo);panel.add(createLabel("المستودع:"));
        panel.add(new JLabel());

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.add(addButton);
        panel.add(buttonWrapper);

        List<JComponent> tileOrder = Arrays.asList(
                codeField, nameField, priceField, gradeField,
                boxesField, capacityField, materialCombo, typeCombo,
                locationCombo, warehouseCombo, addButton
        );
        registerFocusNavigation(panel, tileOrder);
        return panel;
    }

    private JPanel createSanitaryForm() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 8, 15));
        panel.setBorder(createTitledBorder("بيانات الأدوات الصحية"));
        panel.setName("SANITARY_FORM");

        JTextField nameField = createTextField();
        JTextField priceField = createTextField();
        JTextField quantityField = createTextField();
        JTextField gradeField = createTextField();

        JComboBox<SanitaryItem.SanitaryType> typeCombo = new JComboBox<>(SanitaryItem.SanitaryType.values());
        JComboBox<Warehouse> warehouseCombo = new JComboBox<>();

        setupComboBox(typeCombo);
        setupComboBox(warehouseCombo);
        typeCombo.setRenderer(new SanitaryTypeRenderer(FIELD_FONT));

        for (Warehouse w : warehouses) { warehouseCombo.addItem(w); }
        warehouseCombo.setRenderer(new WarehouseRenderer(FIELD_FONT));

        JButton addButton = createAddButton("➕ إضافة الأداة");
        addButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int quantity = Integer.parseInt(quantityField.getText().trim());
                String grade = gradeField.getText().trim();
                SanitaryItem.SanitaryType type = (SanitaryItem.SanitaryType) typeCombo.getSelectedItem();
                Warehouse warehouse = (Warehouse) warehouseCombo.getSelectedItem();

                if (name.isEmpty() || grade.isEmpty() || warehouse == null) {
                    showWarning("الرجاء إدخال جميع البيانات");
                    return;
                }
                sanitaryService.add(new SanitaryItem(name, price, quantity, type, warehouse, grade));
                sanitaryService.save();
                showSuccess("تمت إضافة الأداة الصحية بنجاح");

                clearFields(nameField, priceField, quantityField, gradeField);
                nameField.requestFocusInWindow();
            } catch (NumberFormatException ex) {
                showError("الرجاء إدخال قيم صحيحة للسعر والكمية");
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("حدث خطأ: " + ex.getMessage());
            }
        });

        panel.add(nameField);panel.add(createLabel("اسم الأداة:"));
        panel.add(priceField);panel.add(createLabel("السعر للوحدة:"));
        panel.add(gradeField);panel.add(createLabel("النخب:"));
        panel.add(quantityField); panel.add(createLabel("الكمية:"));
        panel.add(typeCombo);panel.add(createLabel("نوع الأداة:"));
        panel.add(warehouseCombo);panel.add(createLabel("المستودع:"));
        panel.add(new JLabel());

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.add(addButton);
        panel.add(buttonWrapper);

        List<JComponent> sanitaryOrder = Arrays.asList(
                nameField,
                priceField,
                gradeField,
                quantityField,
                typeCombo,
                warehouseCombo,
                addButton
        );
        registerFocusNavigation(panel, sanitaryOrder);
        return panel;
    }

    // ======= Helpers =======
    private TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
                " " + title + " ",
                TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
                LABEL_FONT, PRIMARY_COLOR
        );
    }
    private JTextField createTextField() {
        JTextField f = new JTextField();
        f.setFont(FIELD_FONT);
        f.setPreferredSize(new Dimension(180, 32));
        f.setHorizontalAlignment(SwingConstants.CENTER);
        return f;
    }
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(LABEL_FONT);
        label.setOpaque(true);
        label.setBackground(new Color(245, 245, 245));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return label;
    }
    private JButton createAddButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(300, 35));
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    private void setupComboBox(JComboBox<?> combo) {
        combo.setFont(FIELD_FONT);
        combo.setPreferredSize(new Dimension(180, 32));
        ((JLabel) combo.getRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }
    private void clearFields(JTextField... fields) {
        for(JTextField field : fields) field.setText("");
    }
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "تم بنجاح", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "بيانات ناقصة", JOptionPane.WARNING_MESSAGE);
    }
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "خطأ في البيانات", JOptionPane.ERROR_MESSAGE);
    }

    // ======= التنقل =======
    private void addEnterCycleAction(JComboBox<?> combo) {
        combo.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "cycleNext");
        combo.getActionMap().put("cycleNext", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (combo.getItemCount() > 0) {
                    int nextIndex = (combo.getSelectedIndex() + 1) % combo.getItemCount();
                    combo.setSelectedIndex(nextIndex);
                }
            }
        });
    }
    private void registerFocusNavigation(JComponent container, List<? extends JComponent> focusables) {
        container.setFocusTraversalPolicyProvider(true);
        container.setFocusTraversalPolicy(new CustomFocusPolicy(focusables));
        for (JComponent comp : focusables) {
            InputMap im = comp.getInputMap(JComponent.WHEN_FOCUSED);
            ActionMap am = comp.getActionMap();
            im.put(KeyStroke.getKeyStroke("DOWN"), "focusNext");
            am.put("focusNext", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { focusNext(comp, focusables); }
            });
            im.put(KeyStroke.getKeyStroke("UP"), "focusPrev");
            am.put("focusPrev", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { focusPrev(comp, focusables); }
            });
            if (comp instanceof JComboBox) {
                addEnterCycleAction((JComboBox<?>) comp);
            } else if (comp instanceof JButton) {
                im.put(KeyStroke.getKeyStroke("ENTER"), "clickButton");
                am.put("clickButton", new AbstractAction() {
                    @Override public void actionPerformed(ActionEvent e) { ((JButton)comp).doClick(); }
                });
            } else {
                im.put(KeyStroke.getKeyStroke("ENTER"), "focusNext");
            }
        }
    }
    private void focusNext(Component current, List<? extends Component> order) {
        int i = order.indexOf(current);
        if (i >= 0) order.get((i + 1) % order.size()).requestFocusInWindow();
    }
    private void focusPrev(Component current, List<? extends Component> order) {
        int i = order.indexOf(current);
        if (i >= 0) order.get((i - 1 + order.size()) % order.size()).requestFocusInWindow();
    }
    private void registerTypeSwitchingWithArrows(JComponent root) {
        InputMap im = root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke("LEFT"), "switchToTile");
        am.put("switchToTile", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if (!tileRadio.isSelected()) tileRadio.doClick(); }
        });
        im.put(KeyStroke.getKeyStroke("RIGHT"), "switchToSanitary");
        am.put("switchToSanitary", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if (!sanitaryRadio.isSelected()) sanitaryRadio.doClick(); }
        });
    }

    // ======= Renderers & Focus Policy =======
    private static class WarehouseRenderer extends DefaultListCellRenderer {
        private final Font font;
        public WarehouseRenderer(Font font) { this.font = font; }
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Warehouse w) value = w.getDisplayName();
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            c.setFont(font);
            ((JLabel) c).setHorizontalAlignment(JLabel.RIGHT);
            return c;
        }
    }
    private static class SanitaryTypeRenderer extends DefaultListCellRenderer {
        private final Font font;
        public SanitaryTypeRenderer(Font font) { this.font = font; }
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof SanitaryItem.SanitaryType st) value = st.getArabicName();
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            c.setFont(font);
            ((JLabel) c).setHorizontalAlignment(JLabel.RIGHT);
            return c;
        }
    }
    private static class CustomFocusPolicy extends FocusTraversalPolicy {
        private final List<? extends JComponent> order;
        public CustomFocusPolicy(List<? extends JComponent> order) { this.order = order; }
        @Override public Component getComponentAfter(Container a, Component c) { int i = order.indexOf(c); return order.get((i + 1) % order.size()); }
        @Override public Component getComponentBefore(Container a, Component c) { int i = order.indexOf(c); return order.get((i - 1 + order.size()) % order.size()); }
        @Override public Component getFirstComponent(Container a) { return order.isEmpty() ? null : order.get(0); }
        @Override public Component getLastComponent(Container a) { return order.isEmpty() ? null : order.get(order.size() - 1); }
        @Override public Component getDefaultComponent(Container a) { return getFirstComponent(a); }
    }
}