import com.formdev.flatlaf.FlatLightLaf;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class MainFrame extends JFrame {
    private TileService tileService;
    private SanitaryService sanitaryService;
    private InvoiceService invoiceService;
    private WarehouseManager warehouseManager;
    private AccountsManager accountsManager;

    public MainFrame() {
        super("إدارة معارض السيراميك _ مطور من قبل المهندس علي العيسى");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        try {
            Image image = ImageIO.read(getClass().getResource("/im/111.png"));
            this.setIconImage(image);
        } catch (Exception e) {
            // لا يجب أن يوقف التطبيق، يمكن طباعة الخطأ فقط
            e.printStackTrace();
        }

        // --- تم التعديل: تبسيط وتحويل إلى JSON ---

        // 1) تحميل المستودعات (أصبح تلقائيًا داخل المُنشئ)
        // تم تمرير المسار الصحيح للملف
        warehouseManager = new WarehouseManager("data/warehouses.json");
        List<Warehouse> warehouses = warehouseManager.getWarehouses();

        // 2) الخدمات المعتمدة على المستودعات (تم تحديث المسارات إلى .json)
        tileService = new TileService("data/tiles.json", warehouses);
        sanitaryService = new SanitaryService("data/sanitary.json", warehouses);
        invoiceService = new InvoiceService("data/invoices.json");

        // 3) تهيئة مدير الحسابات (التحميل أصبح تلقائيًا داخل المُنشئ)
        accountsManager = new AccountsManager("data/accounts.json");

        // لم نعد بحاجة لكتل try-catch للتحميل، لأن كل كلاس يهتم بنفسه.

        // 4) واجهات البرنامج (تبويبات)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Tahoma", Font.BOLD, 18));
        tabbedPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        // تم تمرير warehouseManager إلى TilePanel لاستخدامه عند الحاجة
        tabbedPane.addTab("البلاط", new TilePanel(tileService, warehouseManager));
        tabbedPane.addTab("الأدوات الصحية", new SanitaryPanel(sanitaryService));
        tabbedPane.addTab("إضافة عنصر", new AddItemPanel(tileService, sanitaryService, warehouses));
        tabbedPane.addTab("فاتورة جديدة", new NewInvoicePanel(tileService, sanitaryService, invoiceService));
        tabbedPane.addTab("الفواتير", new InvoicePanel(invoiceService, tileService, sanitaryService));
        tabbedPane.addTab("المستودعات", new WarehousePanel(warehouseManager)); // هنا لا يوجد تغيير
        tabbedPane.addTab("الحسابات", new AccountsPanel(accountsManager));

        add(tabbedPane, BorderLayout.CENTER);

        // 5) الحفظ عند إغلاق التطبيق (اختياري الآن ولكن جيد كإجراء احترازي)
        // بما أن الحفظ يتم تلقائيا عند كل تغيير، هذا السطر ليس ضروريا جدا
        // لكنه لا يضر، ويضمن حفظ أي شيء قد يكون فاتنا.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // يمكنك استدعاء saveQuietly لكل الخدمات إذا أردت
                accountsManager.saveQuietly();
                // tileService.save(); // -> تمكينها إذا أردت
                // sanitaryService.save(); // -> تمكينها إذا أردت
                // warehouseManager.save(); // -> تمكينها إذا أردت
            }
        });

        // 6) إعداد اختصارات لوحة المفاتيح العامة
        setupGlobalKeyboardActions(tabbedPane);
    }

    // ... دالة setupGlobalKeyboardActions تبقى كما هي ...
    private void setupGlobalKeyboardActions(JTabbedPane tabbedPane) { /* ... نفس الكود ... */ }

    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Component.orientation", ComponentOrientation.RIGHT_TO_LEFT);
            UIManager.put("defaultFont", new Font("Tahoma", Font.PLAIN, 18));
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}