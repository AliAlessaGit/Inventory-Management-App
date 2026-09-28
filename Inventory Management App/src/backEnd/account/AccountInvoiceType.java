package backEnd.account;

import java.io.Serializable;

public enum AccountInvoiceType implements Serializable {

    SALE("مبيع", true),
    PURCHASE("شراء", true),
    PAYMENT("دفع", false),
    RECEIPT("قبض", false);

    private final String arabicName;
    private final boolean hasEntries;

    AccountInvoiceType(String arabicName, boolean hasEntries) {
        this.arabicName = arabicName;
        this.hasEntries = hasEntries;
    }

    public String getArabicName() {
        return arabicName;
    }

    public boolean hasEntries() {
        return hasEntries;
    }

    @Override
    public String toString() {
        return arabicName;
    }
}