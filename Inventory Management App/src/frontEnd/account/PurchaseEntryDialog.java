package frontEnd.account;

import backEnd.account.AccountInventoryKind;
import backEnd.account.AccountStockMovementType;
import backEnd.sanitary.SanitaryService;
import backEnd.tile.TileService;
import backEnd.warehouse.WarehouseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class PurchaseEntryDialog extends JDialog {

    private final TileService tileService;
    private final SanitaryService sanitaryService;
    private final WarehouseManager warehouseManager;

    private final JComboBox<String> kindBox = new JComboBox<>(
            new String[]{"بلاط", "أداة صحية"});
    private final JComboBox<String> movementBox = new JComboBox<>(
            new String[]{"لا يضاف إلى المستودع", "موجود بالمستودع", "عنصر جديد"});

    private boolean ok;
    private PurchaseData data;

    public PurchaseEntryDialog(
            Window parent,
            TileService tileService,
            SanitaryService sanitaryService,
            WarehouseManager warehouseManager) {

        super(parent, "إضافة بند إلى فاتورة الشراء", ModalityType.APPLICATION_MODAL);

        this.tileService = tileService;
        this.sanitaryService = sanitaryService;
        this.warehouseManager = warehouseManager;

        setLayout(new BorderLayout(10, 10));
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JPanel form = PurchaseDialogSupport.formPanel();
        PurchaseDialogSupport.addRow(form, 0, "نوع المادة:", kindBox);
        PurchaseDialogSupport.addRow(form, 1, "طريقة الإدخال:", movementBox);

        kindBox.setFont(new Font("Tahoma", Font.PLAIN, 17));
        movementBox.setFont(new Font("Tahoma", Font.PLAIN, 17));

        JButton continueButton = new JButton("متابعة");
        JButton cancelButton = new JButton("إلغاء");

        continueButton.addActionListener(e -> openSelectedDialog());
        cancelButton.addActionListener(e -> dispose());

        InputMap input = getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getRootPane().getActionMap();

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        actions.put("cancel", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                cancelButton.doClick();
            }
        });

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "continue");
        actions.put("continue", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                continueButton.doClick();
            }
        });

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "next");
        actions.put("next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                movementBox.requestFocusInWindow();
            }
        });

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "previous");
        actions.put("previous", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                kindBox.requestFocusInWindow();
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(continueButton);
        buttons.add(cancelButton);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(560, 260));
        pack();
        setLocationRelativeTo(parent);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                kindBox.requestFocusInWindow();
            }
        });
    }

    private void openSelectedDialog() {
        int kindIndex = kindBox.getSelectedIndex();
        int movementIndex = movementBox.getSelectedIndex();

        if (kindIndex < 0 || movementIndex < 0) {
            return;
        }

        Window owner = this;

        if (movementIndex == 0) {
            if (kindIndex == 0) {
                PurchaseNotInStockTileDialog dialog =
                        new PurchaseNotInStockTileDialog(owner);
                dialog.showDialog();
                if (dialog.isOk()) finish(dialog.getData());
            } else {
                PurchaseNotInStockSanitaryDialog dialog =
                        new PurchaseNotInStockSanitaryDialog(owner);
                dialog.showDialog();
                if (dialog.isOk()) finish(dialog.getData());
            }
            return;
        }

        if (movementIndex == 1) {
            if (kindIndex == 0) {
                PurchaseExistingTileDialog dialog =
                        new PurchaseExistingTileDialog(
                                owner, tileService, warehouseManager);
                dialog.showDialog();
                if (dialog.isOk()) finish(dialog.getData());
            } else {
                PurchaseExistingSanitaryDialog dialog =
                        new PurchaseExistingSanitaryDialog(
                                owner, sanitaryService);
                dialog.showDialog();
                if (dialog.isOk()) finish(dialog.getData());
            }
            return;
        }

        if (kindIndex == 0) {
            PurchaseNewTileDialog dialog =
                    new PurchaseNewTileDialog(owner, warehouseManager);
            dialog.showDialog();
            if (dialog.isOk()) finish(dialog.getData());
        } else {
            PurchaseNewSanitaryDialog dialog =
                    new PurchaseNewSanitaryDialog(owner, warehouseManager);
            dialog.showDialog();
            if (dialog.isOk()) finish(dialog.getData());
        }
    }

    private void finish(PurchaseData value) {
        data = value;
        ok = true;
        dispose();
    }

    public boolean isOk() {
        return ok;
    }

    public PurchaseData getData() {
        return data;
    }

    public static class PurchaseData {
        public AccountInventoryKind kind;
        public AccountStockMovementType movement;

        public backEnd.tile.TileItem tileItem;
        public backEnd.sanitary.SanitaryItem sanitaryItem;

        public String material;
        public String code;
        public String name;
        public String grade;

        public double quantity1;
        public double quantity2;
        public double boxArea;
        public double price;

        public String unit;

        public backEnd.tile.TileItem.MaterialType tileMaterialType;
        public backEnd.tile.TileItem.SubType tileSubtype;
        public backEnd.tile.TileItem.Location location;

        public backEnd.warehouse.Warehouse warehouse;
        public backEnd.sanitary.SanitaryItem.SanitaryType sanitaryType;
    }
}
