import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Invoice implements Serializable {
    private static int counter = 1;
    private String invoiceNumber; // الآن String مثل FA0001

    private String customerName;
    private String customerPhone;

    private List<InvoiceItem> items = new ArrayList<>();
    private LocalDateTime date;

    // قائمة الدفعات
    private final List<Payment> payments = new ArrayList<>();

    // -- START: ADDED CODE FOR DISCOUNT --
    private double discount; // متغير جديد للحسم
    // -- END: ADDED CODE FOR DISCOUNT --

    public Invoice() {
        this.invoiceNumber = String.format("FA%04d", counter++);
        this.date = LocalDateTime.now();
        this.customerName = "";
        this.customerPhone = "";
        this.discount = 0.0; // -- ADDED: Initialize discount to 0
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public List<InvoiceItem> getItems() { return items; }
    public void addItem(InvoiceItem item) { items.add(item); }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getInvoiceNumber() { return invoiceNumber; }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        return date.format(formatter);
    }

    /**
     *  يمثل إجمالي قيمة الأصناف قبل أي حسم.
     */
    public double getTotal() {
        double total = 0.0;
        for (InvoiceItem item : items) total += item.getTotalPrice();
        total-=getDiscount();
        return total;
    }

    // -- START: NEW AND MODIFIED METHODS FOR DISCOUNT --

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        // التأكد من أن الحسم ليس قيمة سالبة
        this.discount = Math.max(0, discount);
    }

    /**
     *  يمثل الإجمالي النهائي بعد تطبيق الحسم.
     */
    public double getFinalTotal() {
        return getTotal() - getDiscount();
    }

    /**
     * تم التعديل: يحسب المبلغ المتبقي بناءً على الإجمالي بعد الحسم.
     */
    public double getRemaining() {
        return getTotal() - getPaymentsTotal();
    }
    // -- END: NEW AND MODIFIED METHODS FOR DISCOUNT --


    public int getItemCount() { return items.size(); }

    // إدارة الدفعات
    public void addPayment(double amount, LocalDateTime date) {
        payments.add(new Payment(amount, date));
    }
    public List<Payment> getPayments() { return payments; }
    public double getPaymentsTotal() {
        double sum = 0.0;
        for (Payment p : payments) sum += p.getAmount();
        return sum;
    }

    // تهيئة العداد من الفواتير المحفوظة
    public static synchronized void initCounterFromExisting(List<Invoice> existing) {
        if (existing == null || existing.isEmpty()) {
            counter = 1;
            return;
        }
        int max = 0;
        for (Invoice inv : existing) {
            if (inv != null && inv.getInvoiceNumber() != null && inv.getInvoiceNumber().startsWith("FA")) {
                try {
                    int num = Integer.parseInt(inv.getInvoiceNumber().substring(2));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        counter = max + 1;
    }

    // تستخدم عند التحميل من الملف لضبط الرقم مباشرة
    public void setInvoiceNumberWhenLoading(String number) { this.invoiceNumber = number; }

    public static synchronized void setNextInvoiceNumber(int next) {
        if (next > 0) counter = next;
    }
}