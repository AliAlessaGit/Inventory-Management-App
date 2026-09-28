package backEnd.account;

/**
 * إنشاء قيود الحسابات حسب نوع المادة.
 */
public final class AccountEntryFactory {

    private AccountEntryFactory() {
    }

    /**
     * إنشاء قيد بلاط.
     *
     * الكمية 1 = مساحة الصندوق × عدد الصناديق
     * الإجمالي = الكمية 1 × السعر
     */
    public static AccountEntry createTileEntry(
            String material,
            double boxArea,
            double boxes,
            double price
    ) {
        if (boxes < 0) {
            throw new IllegalArgumentException(
                    "عدد الصناديق لا يمكن أن يكون سالباً"
            );
        }

        if (boxArea < 0) {
            throw new IllegalArgumentException(
                    "مساحة الصندوق لا يمكن أن تكون سالبة"
            );
        }

        if (price < 0) {
            throw new IllegalArgumentException(
                    "السعر لا يمكن أن يكون سالباً"
            );
        }

        AccountEntry entry = new AccountEntry();

        entry.setMaterial(material);
        entry.setUnit("متر");
        entry.setQuantity2(boxes);
        entry.setQuantity1(boxArea * boxes);
        entry.setPrice(price);
        entry.calculateTotal();

        return entry;
    }

    /**
     * إنشاء قيد أداة صحية.
     *
     * الكمية 1 = الكمية 2
     * الإجمالي = الكمية 1 × السعر
     */
    public static AccountEntry createSanitaryEntry(
            String material,
            double quantity,
            String unit,
            double price
    ) {
        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "الكمية لا يمكن أن تكون سالبة"
            );
        }

        if (price < 0) {
            throw new IllegalArgumentException(
                    "السعر لا يمكن أن يكون سالباً"
            );
        }

        AccountEntry entry = new AccountEntry();

        entry.setMaterial(material);
        entry.setUnit(
                unit == null || unit.trim().isEmpty()
                        ? "قطعة"
                        : unit
        );

        entry.setQuantity1(quantity);
        entry.setQuantity2(quantity);
        entry.setPrice(price);
        entry.calculateTotal();

        return entry;
    }
}