package backEnd.account;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AccountInvoice implements Serializable {

    private static final long serialVersionUID = 1L;

    private static int counter = 1;

    private String number;
    private LocalDate date;
    private AccountInvoiceType type;
    private String description;
    private double amount;

    private final List<AccountEntry> entries =
            new ArrayList<>();

    private final List<AccountStockMovement> stockMovements =
            new ArrayList<>();

    public AccountInvoice(
            AccountInvoiceType type,
            LocalDate date,
            String description
    ) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "نوع السند مطلوب"
            );
        }

        if (date == null) {
            throw new IllegalArgumentException(
                    "تاريخ السند مطلوب"
            );
        }

        this.number = nextNumber();
        this.type = type;
        this.date = date;
        this.description =
                description == null
                        ? ""
                        : description.trim();
    }

    private static synchronized String nextNumber() {
        return String.format(
                "FA%05d",
                counter++
        );
    }

    /**
     * إعادة تهيئة العداد بعد تحميل البيانات.
     *
     * رقم الفاتورة يبقى فريدًا على مستوى جميع الشركات.
     */
    public static synchronized void initCounter(
            List<Account> accounts
    ) {
        int max = 0;

        if (accounts != null) {

            for (Account account : accounts) {

                if (
                        account == null
                                || account.getAccountInvoices() == null
                ) {
                    continue;
                }

                for (
                        AccountInvoice invoice :
                        account.getAccountInvoices()
                ) {

                    if (
                            invoice == null
                                    || invoice.number == null
                    ) {
                        continue;
                    }

                    String digits =
                            invoice.number.replaceAll(
                                    "\\D+",
                                    ""
                            );

                    if (digits.isEmpty()) {
                        continue;
                    }

                    try {
                        max = Math.max(
                                max,
                                Integer.parseInt(digits)
                        );
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        counter = max + 1;
    }

    public void setNumberWhenLoading(String number) {
        if (
                number != null
                        && !number.trim().isEmpty()
        ) {
            this.number = number.trim();
        }
    }

    public String getNumber() {
        return number;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {

        if (date == null) {
            throw new IllegalArgumentException(
                    "تاريخ السند مطلوب"
            );
        }

        this.date = date;
    }

    public String getFormattedDate() {

        return date == null
                ? ""
                : date.format(
                DateTimeFormatter.ofPattern(
                        "yyyy/MM/dd"
                )
        );
    }

    public AccountInvoiceType getType() {
        return type;
    }

    public void setType(
            AccountInvoiceType type
    ) {

        if (type == null) {
            throw new IllegalArgumentException(
                    "نوع السند مطلوب"
            );
        }

        this.type = type;
    }

    /**
     * البيان الظاهر للمستخدم يتضمن رقم الفاتورة
     * بالإضافة إلى التفاصيل التي كتبها المستخدم.
     */
    public String getDescription() {

        if (
                description == null
                        || description.isEmpty()
        ) {
            return "رقم الفاتورة: " + number;
        }

        return "رقم الفاتورة: "
                + number
                + " - "
                + description;
    }

    /**
     * التفاصيل التي أدخلها المستخدم فقط.
     */
    public String getDetails() {
        return description;
    }

    public void setDescription(String description) {

        this.description =
                description == null
                        ? ""
                        : description.trim();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {

        validateAmount(amount);

        this.amount = amount;
    }

    public List<AccountEntry> getEntries() {
        return entries;
    }

    public void addEntry(AccountEntry entry) {

        if (entry == null) {
            throw new IllegalArgumentException(
                    "القيد مطلوب"
            );
        }

        entries.add(entry);
    }

    public void removeEntry(AccountEntry entry) {
        entries.remove(entry);
    }

    public void clearEntries() {
        entries.clear();
    }

    public List<AccountStockMovement> getStockMovements() {
        return stockMovements;
    }

    public void addStockMovement(
            AccountStockMovement movement
    ) {

        if (movement == null) {
            throw new IllegalArgumentException(
                    "حركة المستودع مطلوبة"
            );
        }

        stockMovements.add(movement);
    }

    public void clearStockMovements() {
        stockMovements.clear();
    }

    /**
     * المبيع:
     * المدين = مجموع تفاصيل الفاتورة.
     *
     * الدفع:
     * المدين = المبلغ.
     */
    public double getTotalDebit() {

        switch (type) {

            case SALE:
                return entries.stream()
                        .mapToDouble(
                                AccountEntry::getTotal
                        )
                        .sum();

            case PAYMENT:
                return amount;

            default:
                return 0.0;
        }
    }

    /**
     * الشراء:
     * الدائن = مجموع تفاصيل الفاتورة.
     *
     * القبض:
     * الدائن = المبلغ.
     */
    public double getTotalCredit() {

        switch (type) {

            case PURCHASE:
                return entries.stream()
                        .mapToDouble(
                                AccountEntry::getTotal
                        )
                        .sum();

            case RECEIPT:
                return amount;

            default:
                return 0.0;
        }
    }

    /**
     * الرصيد = المدين - الدائن.
     *
     * المبيع والدفع = موجب.
     * الشراء والقبض = سالب.
     */
    public double getTotal() {

        return getTotalDebit()
                - getTotalCredit();
    }

    public boolean isPurchase() {
        return type == AccountInvoiceType.PURCHASE;
    }

    public boolean isSale() {
        return type == AccountInvoiceType.SALE;
    }

    public boolean isPayment() {
        return type == AccountInvoiceType.PAYMENT;
    }

    public boolean isReceipt() {
        return type == AccountInvoiceType.RECEIPT;
    }

    private void validateAmount(double value) {

        if (
                Double.isNaN(value)
                        || Double.isInfinite(value)
                        || value < 0
        ) {
            throw new IllegalArgumentException(
                    "المبلغ غير صالح"
            );
        }
    }
}