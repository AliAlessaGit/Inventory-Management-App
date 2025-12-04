import java.io.Serializable;

public class TileItem extends InventoryItem implements Serializable {

    public enum MaterialType { سيراميك, غرانيت }
    public enum SubType { ارضيات, جدران ,نعلات }

    public enum Location {
        رقم_1("رقم 1"),
        رقم_2("رقم 2"),
        رقم_3("رقم 3"),
        رقم_4("رقم 4"),
        رقم_5("رقم 5"),
        رقم_6("رقم 6");

        private final String arabicName;
        Location(String arabicName) { this.arabicName = arabicName; }
        public String getArabicName() { return arabicName; }
    }
    // داخل TileItem
    public TileItem(long idNumber, String name, String code, double price, double boxes, double boxArea,
                    MaterialType materialType, SubType subType,
                    Warehouse warehouse, String grade, Location location) {
        super(idNumber, name, price);
        setCode(code);
        setBoxes(boxes);
        setBoxArea(boxArea);
        setMaterialType(materialType);
        setSubType(subType);
        setGrade(grade);
        this.warehouse = warehouse;
        this.warehouseNumber = warehouse.getId();
        setLocation(location);
    }
    public void applyUpdatesFrom(TileItem other) {
        if (other == null) return;
        setName(other.getName());
        setGrade(other.getGrade());
        setCode(other.getCode());
        setBoxes(other.getBoxes());
        setBoxArea(other.getBoxArea());
        setPrice(other.getPrice());
        setMaterialType(other.getMaterialType());
        setSubType(other.getSubType());
        setWarehouse(other.getWarehouse());
        // اضف ما يلزم من حقول
    }
    private String code; // أزلنا القيمة الافتراضية "00" من هنا
    private double boxes;
    private double boxArea;
    private MaterialType materialType;
    private SubType subType;
    private int warehouseNumber;
    private String grade;
    private Warehouse warehouse;
    private Location location;

    public TileItem(String name, String code, double price, double boxes, double boxArea,
                    MaterialType materialType, SubType subType,
                    Warehouse warehouse, String grade, Location location) {
        super(name, price);
        setCode(code); // المنطق الجديد سيُطبق هنا
        setBoxes(boxes);
        setBoxArea(boxArea);
        setMaterialType(materialType);
        setSubType(subType);
        setGrade(grade);
        this.warehouse = warehouse;
        this.warehouseNumber = warehouse.getId();
        setLocation(location);
    }

    public double getTotalArea() {
        return boxes * boxArea;
    }

    @Override
    public double getTotalValue() {
        return getTotalArea() * getPrice();
    }

    @Override
    public String getDetails() {
        return super.getBasicInfo() + String.format(
                "%nالرمز: %s%nنوع المادة: %s%nالنوع: %s%nالمستودع: %d%nالنخب: %s%n"
                        + "الموقع: %s%nعدد الصناديق: %.2f%nمساحة الصندوق: %.2f م²%n"
                        + "المساحة الإجمالية: %.2f م²%nالقيمة الإجمالية: %.2f $",
                code, materialType.name(), subType.name(), warehouseNumber, grade,
                location != null ? location.getArabicName() : "-",
                boxes, boxArea, getTotalArea(), getTotalValue()
        );
    }

    @Override
    public void withdraw(double area) {
        double availableArea = getTotalArea();
        if (area <= 0) return;
        if (area > availableArea) {
            boxes = 0;
            return;
        }
        double boxesNeeded = area / boxArea;
        boxes -= boxesNeeded;
        if (boxes < 0) boxes = 0;
    }

    // Getters & Setters
    public String getCode() { return code; }

    // التعديل: إذا كان الرمز فارغاً، ضع "00"
    public void setCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            this.code = "00";
        } else {
            this.code = code.trim();
        }
    }

    public double getBoxes() { return boxes; }
    public void setBoxes(double boxes) {
        if (boxes < 0) throw new IllegalArgumentException("عدد الصناديق لا يمكن أن يكون سالباً");
        this.boxes = boxes;
    }
    public void setBoxes(int boxes) { // Overload for convenience
        if (boxes < 0) throw new IllegalArgumentException("عدد الصناديق لا يمكن أن يكون سالباً");
        this.boxes = boxes;
    }

    public double getBoxArea() { return boxArea; }
    public void setBoxArea(double boxArea) {
        if (boxArea <= 0) throw new IllegalArgumentException("مساحة الصندوق يجب أن تكون أكبر من الصفر");
        this.boxArea = boxArea;
    }

    public MaterialType getMaterialType() { return materialType; }
    public void setMaterialType(MaterialType materialType) {
        if (materialType == null) throw new IllegalArgumentException("نوع المادة مطلوب");
        this.materialType = materialType;
    }

    public SubType getSubType() { return subType; }
    public void setSubType(SubType subType) {
        if (subType == null) throw new IllegalArgumentException("النوع الفرعي مطلوب");
        this.subType = subType;
    }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public int getWarehouseNumber() { return warehouseNumber; }
    public void setWarehouseNumber(int warehouseNumber) {
        if (warehouseNumber < 1 || warehouseNumber > 3) {
            throw new IllegalArgumentException("رقم المستودع يجب أن يكون بين 1 و 3");
        }
        this.warehouseNumber = warehouseNumber;
    }

    public String getGrade() { return grade; }
    public void setGrade(String grade) {
        if (grade == null || grade.trim().isEmpty()) {
            throw new IllegalArgumentException("النخب لا يمكن أن يكون فارغاً");
        }
        this.grade = grade.trim();
    }

    public Location getLocation() { return location; }
    public void setLocation(Location location) {
        if (location == null) throw new IllegalArgumentException("الموقع مطلوب");
        this.location = location;
    }
}