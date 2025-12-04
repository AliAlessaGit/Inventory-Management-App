public abstract class InventoryItem {
    private static long idCounter = 1; // استخدمنا long لمدى أوسع
    private final long idNumber;       // رقم المعرف الداخلي الفريد
    private String name;
    private double price;

    public InventoryItem(String name, double price) {
        this.idNumber = idCounter++; // توليد رقم مميز تلقائيًا
        this.name = name;
        this.price = price;
    }// داخل InventoryItem
    protected InventoryItem(long idNumber, String name, double price) {
        this.idNumber = idNumber;
        setName(name);
        setPrice(price);
        bumpIdCounterIfNeeded(idNumber);
    }

    public static synchronized void bumpIdCounterIfNeeded(long usedId) {
        if (usedId >= idCounter) {
            idCounter = usedId + 1;
        }
    }

    // Getters
    public long getIdNumber() {
        return idNumber;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }

    // Setters
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("الاسم لا يمكن أن يكون فارغاً");
        }
        this.name = name.trim();
    }

    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("السعر لا يمكن أن يكون سالباً");
        this.price = price;
    }

    // دوال أساسية مشتركة
    public String getBasicInfo() {
        return String.format("ID: %d%nالاسم: %s%nالسعر: %.2f $", idNumber, name, price);
    }

    public abstract double getTotalValue();
    public abstract String getDetails();
    public abstract void withdraw(double amount);
}