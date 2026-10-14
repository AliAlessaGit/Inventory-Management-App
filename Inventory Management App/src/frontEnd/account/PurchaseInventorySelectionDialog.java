package frontEnd.account;

import backEnd.sanitary.SanitaryItem;
import backEnd.sanitary.SanitaryService;
import backEnd.tile.TileFilter;
import backEnd.tile.TileFilterService;
import backEnd.tile.TileItem;
import backEnd.tile.TileService;
import backEnd.warehouse.WarehouseManager;
import frontEnd.tile.TileFilterPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class PurchaseInventorySelectionDialog {

    private PurchaseInventorySelectionDialog() {}

    public static TileItem selectTile(
            Window owner,
            TileService tileService,
            WarehouseManager warehouseManager) {

        final TileItem[] result = new TileItem[1];

        JDialog dialog = new JDialog(
                owner,
                "اختيار البلاط من المستودع",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField searchField = new JTextField();
        PurchaseDialogSupport.styleTextField(searchField);

        JButton filterButton = new JButton("فلترة");
        JButton selectButton = new JButton("اختيار");
        JButton cancelButton = new JButton("إلغاء");

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        top.add(PurchaseDialogSupport.label("بحث:"), BorderLayout.EAST);
        top.add(searchField, BorderLayout.CENTER);
        top.add(filterButton, BorderLayout.WEST);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"الرمز", "الاسم", "النخب", "المتوفر", "مساحة الصندوق", "السعر"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        List<TileItem> displayed = new ArrayList<>();
        TileFilter filter = new TileFilter();
        TileFilterService filterService = new TileFilterService();

        Runnable reload = () -> {
            model.setRowCount(0);
            displayed.clear();
            String query = searchField.getText() == null
                    ? "" : searchField.getText().trim();

            for (TileItem item : filterService.filterAndSearch(
                    tileService.getAll(), filter, query)) {
                displayed.add(item);
                model.addRow(new Object[]{
                        item.getCode(),
                        item.getName(),
                        item.getGrade(),
                        format(item.getBoxes()),
                        format(item.getBoxArea()),
                        format(item.getPrice())
                });
            }
        };

        reload.run();

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void refresh() { reload.run(); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
        });

        final JPopupMenu[] popup = new JPopupMenu[1];
        filterButton.addActionListener(e -> {
            if (popup[0] == null) {
                popup[0] = new JPopupMenu();
                TileFilterPanel filterPanel = new TileFilterPanel(
                        tileService.getAll(),
                        warehouseManager == null ? List.of() : warehouseManager.getWarehouses(),
                        filter
                );
                filterPanel.setOnFilterApplied(() -> {
                    reload.run();
                    popup[0].setVisible(false);
                });
                filterPanel.setOnClearApplied(reload);
                popup[0].setLayout(new BorderLayout());
                popup[0].add(filterPanel, BorderLayout.CENTER);
            }
            popup[0].show(filterButton, 0, filterButton.getHeight());
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        bottom.add(selectButton);
        bottom.add(cancelButton);

        Runnable choose = () -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog,
                        "حدد عنصرًا من المستودع أولاً",
                        "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            if (modelRow >= 0 && modelRow < displayed.size()) {
                result[0] = displayed.get(modelRow);
                dialog.dispose();
            }
        };

        selectButton.addActionListener(e -> choose.run());
        cancelButton.addActionListener(e -> dialog.dispose());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) choose.run();
            }
        });
        table.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) choose.run();
            }
        });

        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.add(top, BorderLayout.NORTH);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setMinimumSize(new Dimension(850, 520));
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                searchField.requestFocusInWindow();
            }
        });
        dialog.setVisible(true);

        return result[0];
    }

    public static SanitaryItem selectSanitary(
            Window owner,
            SanitaryService sanitaryService) {

        final SanitaryItem[] result = new SanitaryItem[1];

        JDialog dialog = new JDialog(
                owner,
                "اختيار الأداة الصحية من المستودع",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField searchField = new JTextField();
        PurchaseDialogSupport.styleTextField(searchField);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        top.add(PurchaseDialogSupport.label("بحث:"), BorderLayout.EAST);
        top.add(searchField, BorderLayout.CENTER);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"الاسم", "النخب", "المتوفر", "السعر"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        List<SanitaryItem> displayed = new ArrayList<>();

        Runnable reload = () -> {
            model.setRowCount(0);
            displayed.clear();
            String query = searchField.getText() == null
                    ? "" : searchField.getText().trim().toLowerCase();

            for (SanitaryItem item : sanitaryService.getAll()) {
                String text = (item.getName() + " " + item.getGrade()).toLowerCase();
                if (query.isEmpty() || text.contains(query)) {
                    displayed.add(item);
                    model.addRow(new Object[]{
                            item.getName(),
                            item.getGrade(),
                            item.getQuantity(),
                            format(item.getPrice())
                    });
                }
            }
        };

        reload.run();

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void refresh() { reload.run(); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JButton selectButton = new JButton("اختيار");
        JButton cancelButton = new JButton("إلغاء");
        bottom.add(selectButton);
        bottom.add(cancelButton);

        Runnable choose = () -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog,
                        "حدد عنصرًا من المستودع أولاً",
                        "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            if (modelRow >= 0 && modelRow < displayed.size()) {
                result[0] = displayed.get(modelRow);
                dialog.dispose();
            }
        };

        selectButton.addActionListener(e -> choose.run());
        cancelButton.addActionListener(e -> dialog.dispose());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) choose.run();
            }
        });
        table.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) choose.run();
            }
        });

        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.add(top, BorderLayout.NORTH);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setMinimumSize(new Dimension(700, 500));
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                searchField.requestFocusInWindow();
            }
        });
        dialog.setVisible(true);

        return result[0];
    }

    private static String format(double value) {
        return String.format("%.2f", value);
    }
}
