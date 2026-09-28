package backEnd.account;

import java.io.Serializable;

public enum AccountStockMovementType implements Serializable {

    NOT_IN_STOCK("لا يضاف إلى المستودع"),
    EXISTING_ITEM("صنف موجود"),
    NEW_ITEM("صنف جديد");

    private final String arabicName;

    AccountStockMovementType(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }

    @Override
    public String toString() {
        return arabicName;
    }
}