package frontEnd.account.sale;

import backEnd.account.AccountInventoryKind;
import backEnd.account.AccountStockMovementType;
import backEnd.sanitary.SanitaryItem;
import backEnd.sanitary.SanitaryService;
import backEnd.tile.TileItem;
import backEnd.tile.TileService;
import backEnd.warehouse.WarehouseManager;
import frontEnd.account.PurchaseInventorySelectionDialog;

import javax.swing.*;
import java.awt.*;

/**
 * نافذة مستقلة لإضافة بند بيع. لا تعدّل المخزون بنفسها؛
 * تعيد البيانات إلى AccountInvoiceService لتنفيذ الحركة بشكل مركزي.
 */
public final class SaleEntryDialog extends JDialog {
    private final TileService tileService;
    private final SanitaryService sanitaryService;
    private final WarehouseManager warehouseManager;
    private boolean ok;
    private SaleData data;

    private final JComboBox<String> kind = new JComboBox<>(new String[]{"بلاط", "أداة صحية"});
    private final JComboBox<String> source = new JComboBox<>(new String[]{"من المستودع", "من خارج المستودع"});

    public SaleEntryDialog(Window owner, TileService tileService,
                           SanitaryService sanitaryService, WarehouseManager warehouseManager) {
        super(owner, "إضافة بند بيع", ModalityType.APPLICATION_MODAL);
        this.tileService = tileService;
        this.sanitaryService = sanitaryService;
        this.warehouseManager = warehouseManager;
        setLayout(new BorderLayout(10, 10));
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 10, 18));
        form.add(new JLabel("نوع المادة:"));
        form.add(kind);
        form.add(new JLabel("طريقة التخريج:"));
        form.add(source);

        JButton next = new JButton("متابعة");
        JButton cancel = new JButton("إلغاء");
        next.addActionListener(e -> continueEntry());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(next);
        buttons.add(cancel);
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        setMinimumSize(new Dimension(520, 200));
        pack();
        setLocationRelativeTo(owner);
        getRootPane().setDefaultButton(next);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void continueEntry() {
        try {
            boolean tile = kind.getSelectedIndex() == 0;
            boolean inStock = source.getSelectedIndex() == 0;
            SaleData d = new SaleData();
            d.kind = tile ? AccountInventoryKind.TILE : AccountInventoryKind.SANITARY;

            if (inStock && tile) {
                TileItem item = PurchaseInventorySelectionDialog.selectTile(this, tileService, warehouseManager);
                if (item == null) return;
                JTextField boxes = new JTextField("1");
                JTextField price = new JTextField(String.valueOf(item.getPrice()));
                JPanel panel = fields("عدد الصناديق:", boxes, "سعر المتر:", price);
                if (JOptionPane.showConfirmDialog(this, panel, "بيع بلاط من المستودع",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
                double q = number(boxes, "عدد الصناديق");
                double p = number(price, "السعر");
                if (q <= 0) throw new IllegalArgumentException("عدد الصناديق يجب أن يكون أكبر من صفر");
                d.movement = AccountStockMovementType.EXISTING_ITEM;
                d.tileItem = item; d.quantity2 = q; d.quantity1 = q * item.getBoxArea();
                d.material = item.getCode() + " " + item.getName() + " " + item.getGrade();
                d.price = p;
            } else if (inStock) {
                SanitaryItem item = PurchaseInventorySelectionDialog.selectSanitary(this, sanitaryService);
                if (item == null) return;
                JTextField quantity = new JTextField("1");
                JTextField price = new JTextField(String.valueOf(item.getPrice()));
                JPanel panel = fields("الكمية:", quantity, "سعر القطعة:", price);
                if (JOptionPane.showConfirmDialog(this, panel, "بيع أداة صحية من المستودع",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
                double q = number(quantity, "الكمية"), p = number(price, "السعر");
                if (q <= 0 || q != Math.rint(q)) throw new IllegalArgumentException("الكمية يجب أن تكون عدداً صحيحاً أكبر من صفر");
                d.movement = AccountStockMovementType.EXISTING_ITEM;
                d.sanitaryItem = item; d.quantity1 = q; d.quantity2 = q;
                d.material = item.getName() + " " + item.getGrade(); d.price = p; d.unit = "قطعة";
            } else if (tile) {
                JTextField code = new JTextField();
                JTextField name = new JTextField();
                JTextField grade = new JTextField();
                JTextField area = new JTextField();
                JTextField boxes = new JTextField("1");
                JTextField price = new JTextField();
                JPanel panel = fields("الرمز:", code, "اسم البلاط:", name, "النخب:", grade,
                        "مساحة الصندوق:", area, "عدد الصناديق:", boxes, "سعر المتر:", price);
                if (JOptionPane.showConfirmDialog(this, panel, "بيع بلاط من خارج المستودع",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
                String n = required(name, "اسم البلاط"), g = required(grade, "النخب");
                double a = number(area, "مساحة الصندوق"), q = number(boxes, "عدد الصناديق"), p = number(price, "السعر");
                if (a <= 0 || q <= 0) throw new IllegalArgumentException("المساحة وعدد الصناديق يجب أن يكونا أكبر من صفر");
                d.movement = AccountStockMovementType.NOT_IN_STOCK;
                d.material = (code.getText().trim().isEmpty() ? "" : code.getText().trim() + " ") + n + " " + g;
                d.quantity2 = q; d.quantity1 = a * q; d.price = p; d.unit = "متر";
            } else {
                JTextField name = new JTextField();
                JTextField grade = new JTextField();
                JTextField quantity = new JTextField("1");
                JTextField price = new JTextField();
                JPanel panel = fields("اسم الأداة:", name, "النخب:", grade, "الكمية:", quantity, "سعر القطعة:", price);
                if (JOptionPane.showConfirmDialog(this, panel, "بيع أداة صحية من خارج المستودع",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
                String n = required(name, "اسم الأداة"), g = required(grade, "النخب");
                double q = number(quantity, "الكمية"), p = number(price, "السعر");
                if (q <= 0 || q != Math.rint(q)) throw new IllegalArgumentException("الكمية يجب أن تكون عدداً صحيحاً أكبر من صفر");
                d.movement = AccountStockMovementType.NOT_IN_STOCK;
                d.material = n + " " + g; d.quantity1 = q; d.quantity2 = q; d.price = p; d.unit = "قطعة";
            }
            data = d;
            ok = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "تنبيه", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static JPanel fields(Object... pairs) {
        JPanel p = new JPanel(new GridLayout(pairs.length / 2, 2, 8, 8));
        p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        for (int i = 0; i < pairs.length; i += 2) {
            p.add(new JLabel(String.valueOf(pairs[i])));
            p.add((JComponent) pairs[i + 1]);
        }
        return p;
    }

    private static double number(JTextField f, String label) {
        try {
            double n = Double.parseDouble(f.getText().trim().replace(',', '.'));
            if (!Double.isFinite(n) || n < 0) throw new Exception();
            return n;
        } catch (Exception e) { throw new IllegalArgumentException(label + " غير صالح"); }
    }

    private static String required(JTextField f, String label) {
        String s = f.getText().trim();
        if (s.isEmpty()) throw new IllegalArgumentException(label + " مطلوب");
        return s;
    }

    public boolean isOk() { return ok; }
    public SaleData getData() { return data; }

    public static final class SaleData {
        public AccountInventoryKind kind;
        public AccountStockMovementType movement;
        public TileItem tileItem;
        public SanitaryItem sanitaryItem;
        public String material, unit = "قطعة";
        public double quantity1, quantity2, price;
    }
}
