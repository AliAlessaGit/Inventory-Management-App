import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InvoiceItem implements Serializable {
    private long idNumber;   // معرف مطابق للعنصر في المخزون
    private String name;
    private double quantity;
    private double unitPrice;
    private double boxArea; // للبلاط فقط
    private boolean isTile;
    private String code;    // للبلاط فقط
    private String grade;   // النخب
    private LocalDateTime date; // <<<<<<<  أضف هذا الحقل

    // --- كونستركتور للأدوات الصحية ---
    // داخل InvoiceItem
    public InvoiceItem(long idNumber, String name, double quantity, double unitPrice, double boxArea, boolean isTile, String code, String grade, LocalDateTime date) {
        // ... الإسنادات الموجودة مسبقًا
        this.idNumber = idNumber;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.boxArea = boxArea;
        this.isTile = isTile;
        this.code = code;
        this.grade = grade;
        this.date = date; // <<<<<<< أضف هذا السطر
    }

    // هذا مثال لمنشئ الأدوات الصحية
    public InvoiceItem(long idNumber, String name, double quantity, double unitPrice, boolean isTile, String grade, LocalDateTime date) {
        // ... الإسنادات الموجودة مسبقًا
        this.idNumber = idNumber;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.isTile = isTile;
        this.grade = grade;
        this.date = date; // <<<<<<< أضف هذا السطر
        this.boxArea = 0; // or whatever default
        this.code = ""; // or whatever default
    }

    // <<<<<<< أضف هذا التابع (method)
    public String getFormattedDate() {
        if (this.date == null) {
            return ""; // أو تاريخ الفاتورة الافتراضي إذا لزم الأمر
        }
        return this.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    // ==== Getters ====
    public long getIdNumber() { return idNumber; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getBoxArea() { return boxArea; }
    public boolean isTile() { return isTile; }
    public String getCode() { return isTile ? code : ""; }
    public String getGrade() { return grade; }

    public double getTotalPrice() {
        if (isTile) {
            return quantity * boxArea * unitPrice;
        } else {
            return quantity * unitPrice;
        }
    }
}