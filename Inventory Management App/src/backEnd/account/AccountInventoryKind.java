package backEnd.account;

import java.io.Serializable;

public enum AccountInventoryKind implements Serializable {

    TILE("بلاط"),
    SANITARY("أدوات صحية");

    private final String arabicName;

    AccountInventoryKind(String arabicName) {
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