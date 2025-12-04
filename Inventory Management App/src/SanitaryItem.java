public class SanitaryItem extends InventoryItem {
    public Warehouse getWarehouse() { return warehouse; }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public enum SanitaryType {
        LOCAL_WASHBASIN("مغسلة وطني"),
        LOCAL_BATHROOM("حمام وطني"),
        IMPORTED_WASHBASIN("مغسلة مستورد"),
        IMPORTED_BATHROOM("حمام مستورد"),
        BUCKDOOSH("بكدوش"),
        BATHTUB("بانيو"),
        SHOWER_CLOUD("سحابة دوش"),
        KITCHEN_SINK("مجلى"),
        OTHER_SANITARY("أدوات صحية أخرى");

        private final String arabicName;
        SanitaryType(String arabicName) { this.arabicName = arabicName; }
        public String getArabicName() { return arabicName; }
    }

    private int quantity;
    private SanitaryType type;
    private Warehouse warehouse;

    // NEW: النخب
    private String grade;

    public SanitaryItem(String name, double price, int quantity,
                        SanitaryType type, Warehouse selectedWarehouse, String grade) {
        super(name, price);
        setQuantity(quantity);
        setType(type);
        this.warehouse = selectedWarehouse;
        setGrade(grade);
    }

    @Override
    public double getTotalValue() { return getQuantity() * getPrice(); }
    public void applyUpdatesFrom(SanitaryItem other) {
        if (other == null) return;
        setName(other.getName());
        setGrade(other.getGrade());
        setType(other.getType());
        setQuantity(other.getQuantity());
        setPrice(other.getPrice());
        setWarehouse(other.getWarehouse());
        // أي حقول أخرى تريد تحديثها
    }
    @Override
    public String getDetails() {
        return getBasicInfo() + String.format(
                "%nالنوع: %s%nالنخب: %s%nالكمية: %d%nالقيمة الإجمالية: %.2f $",
                type.getArabicName(), grade, quantity, getTotalValue()
        );
    }

    @Override
    public void withdraw(double amount) {
        int intAmount = (int) Math.round(amount);
        if (intAmount <= 0) return;
        if (intAmount > quantity) {
            quantity = 0;
        } else {
            quantity -= intAmount;
        }
    }

    // Getters & Setters
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("الكمية لا يمكن أن تكون سالبة");
        this.quantity = quantity;
    }

    public SanitaryType getType() { return type; }
    public void setType(SanitaryType type) {
        if (type == null) throw new IllegalArgumentException("النوع لا يمكن أن يكون فارغاً");
        this.type = type;
    }

    public String getTypeName() { return type.getArabicName(); }

    public String getGrade() { return grade; }
    public void setGrade(String grade) {
        if (grade == null || grade.trim().isEmpty())
            throw new IllegalArgumentException("النخب لا يمكن أن يكون فارغاً");
        this.grade = grade.trim();
    }
}