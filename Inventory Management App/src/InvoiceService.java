import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvoiceService {
    private List<Invoice> invoices = new ArrayList<>();
    private final String dataFile;

    public InvoiceService(String dataFile) {
        this.dataFile = dataFile;
        loadFromFile();
        Invoice.initCounterFromExisting(this.invoices);
    }

    public List<Invoice> getAll() { return invoices; }

    public void add(Invoice invoice) {
        invoices.add(invoice);
        saveToFile();
    }

    public void remove(Invoice invoice) {
        invoices.remove(invoice);
        saveToFile();
    }

    public Invoice findById(String id) {
        if (id == null) return null;
        for (Invoice inv : invoices) {
            if (id.equalsIgnoreCase(inv.getInvoiceNumber())) return inv;
        }
        return null;
    }

    // مساعد إن رغبت بالبحث بترقيم رقمي
    public Invoice findByIntId(int id) {
        return findById(String.format("FA%04d", id));
    }

    public void deleteInvoice(String number) {
        invoices.removeIf(inv -> number != null && number.equalsIgnoreCase(inv.getInvoiceNumber()));
        saveToFile();
    }

    // تم التعديل: الحفظ باستخدام JSON
    public void saveToFile() {
        JSONUtil.writeAll(dataFile, invoices);
    }

    // تم التعديل: التحميل باستخدام JSON
    public void loadFromFile() {
        this.invoices = JSONUtil.readAll(dataFile, Invoice.class);
    }

    public List<Invoice> filterByDate(LocalDateTime from, LocalDateTime to) {
        List<Invoice> filtered = new ArrayList<>();
        for (Invoice inv : invoices) {
            LocalDateTime date = inv.getDate();
            if ((from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to))) {
                filtered.add(inv);
            }
        }
        return filtered;
    }

    public Invoice createNewInvoice() { return new Invoice(); }
}