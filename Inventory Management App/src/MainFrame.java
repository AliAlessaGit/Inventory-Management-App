import app.AppPaths;
import backEnd.account.AccountsManager;
import backEnd.currency.CurrencySettingsService;
import backEnd.invoice.InvoiceService;
import backEnd.sanitary.SanitaryService;
import backEnd.tile.TileService;
import backEnd.warehouse.Warehouse;
import backEnd.warehouse.WarehouseManager;
import com.formdev.flatlaf.FlatLightLaf;
import frontEnd.account.AccountsPanel;
import frontEnd.currency.CurrencySettingsPanel;
import frontEnd.inventory.AddItemPanel;
import frontEnd.invoice.InvoicePanel;
import frontEnd.invoice.NewInvoicePanel;
import frontEnd.sanitary.SanitaryPanel;
import frontEnd.tile.TilePanel;
import frontEnd.warehouse.WarehousePanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.List;
public class MainFrame extends JFrame {
    private TileService tileService;
    private SanitaryService sanitaryService;
    private InvoiceService invoiceService;
    private WarehouseManager warehouseManager;
    private AccountsManager accountsManager;
    private CurrencySettingsService currencySettingsService;

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
        // المستودعات
        warehouseManager = new WarehouseManager(
                AppPaths.resolve(
                        "data/warehouses.json"
                ).toString()
        );

        List<Warehouse> warehouses =
                warehouseManager.getWarehouses();

// البلاط
        tileService = new TileService(
                AppPaths.resolve(
                        "data/tiles.json"
                ).toString(),
                warehouses
        );

// الأدوات الصحية
        sanitaryService = new SanitaryService(
                AppPaths.resolve(
                        "data/sanitary.json"
                ).toString(),
                warehouses
        );

// الفواتير
        invoiceService = new InvoiceService(
                AppPaths.resolve(
                        "data/invoices.json"
                ).toString()
        );

// إعدادات العملة
        currencySettingsService =
                new CurrencySettingsService(
                        AppPaths.resolve(
                                "data/settings.json"
                        ).toString()
                );

// الحسابات
        accountsManager = new AccountsManager(
                AppPaths.resolve(
                        "data/accounts.json"
                ).toString()
        );
        // لم نعد بحاجة لكتل try-catch للتحميل، لأن كل كلاس يهتم بنفسه.

        // 4) واجهات البرنامج (تبويبات)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Tahoma", Font.BOLD, 18));
        tabbedPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        tabbedPane.addTab(
                "البلاط",
                new TilePanel(
                        tileService,
                        warehouseManager,
                        currencySettingsService
                )
        );

        tabbedPane.addTab(
                "الأدوات الصحية",
                new SanitaryPanel(
                        sanitaryService,
                        currencySettingsService
                )
        );

        tabbedPane.addTab(
                "إضافة عنصر",
                new AddItemPanel(
                        tileService,
                        sanitaryService,
                        warehouses
                )
        );

        tabbedPane.addTab(
                "فاتورة جديدة",
                new NewInvoicePanel(
                        tileService,
                        sanitaryService,
                        invoiceService,
                        warehouseManager
                )
        );
        tabbedPane.addTab(
                "الفواتير",
                new InvoicePanel(
                        invoiceService,
                        tileService,
                        sanitaryService,
                        warehouseManager,
                        currencySettingsService
                )
        );

        tabbedPane.addTab(
                "المستودعات",
                new WarehousePanel(
                        warehouseManager
                )
        );

        tabbedPane.addTab(
                "الحسابات",
                new AccountsPanel(
                        accountsManager,
                        tileService,
                        sanitaryService,
                        warehouseManager
                )
        );

        tabbedPane.addTab(
                "العملة",
                new CurrencySettingsPanel(
                        currencySettingsService
                )
        );

        add(tabbedPane, BorderLayout.CENTER);

        // 5) الحفظ عند إغلاق التطبيق (اختياري الآن ولكن جيد كإجراء احترازي)
        // بما أن الحفظ يتم تلقائيا عند كل تغيير، هذا السطر ليس ضروريا جدا
        // لكنه لا يضر، ويضمن حفظ أي شيء قد يكون فاتنا.


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