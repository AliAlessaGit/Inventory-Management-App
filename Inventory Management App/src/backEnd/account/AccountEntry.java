package backEnd.account;

import java.io.Serializable;

public class AccountEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private String material;
    private double quantity1;
    private String unit;
    private double price;
    private double total;
    private double quantity2;

    public AccountEntry() {
    }

    public AccountEntry(
            String material,
            double quantity1,
            String unit,
            double price,
            double total,
            double quantity2
    ) {
        this.material = material;
        setQuantity1(quantity1);
        setUnit(unit);
        setPrice(price);
        setTotal(total);
        setQuantity2(quantity2);
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        if (material == null || material.trim().isEmpty()) {
            throw new IllegalArgumentException("المادة مطلوبة");
        }

        this.material = material.trim();
    }

    public double getQuantity1() {
        return quantity1;
    }

    public void setQuantity1(double quantity1) {
        validateNumber(quantity1, "الكمية 1");
        this.quantity1 = quantity1;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            this.unit = "قطعة";
        } else {
            this.unit = unit.trim();
        }
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        validateNumber(price, "السعر");
        this.price = price;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        validateNumber(total, "الإجمالي");
        this.total = total;
    }

    public void calculateTotal() {
        this.total = this.quantity1 * this.price;
    }

    public double getQuantity2() {
        return quantity2;
    }

    public void setQuantity2(double quantity2) {
        validateNumber(quantity2, "الكمية 2");
        this.quantity2 = quantity2;
    }

    public void calculateTile(
            double boxArea,
            double boxes,
            double price
    ) {
        validateNumber(boxArea, "مساحة الصندوق");
        validateNumber(boxes, "عدد الصناديق");
        validateNumber(price, "السعر");

        this.quantity2 = boxes;
        this.quantity1 = boxArea * boxes;
        this.unit = "متر";
        this.price = price;

        calculateTotal();
    }

    public void calculateSanitary(
            double quantity,
            double price
    ) {
        calculateSanitary(
                quantity,
                "قطعة",
                price
        );
    }

    public void calculateSanitary(
            double quantity,
            String unit,
            double price
    ) {
        validateNumber(quantity, "الكمية");
        validateNumber(price, "السعر");

        this.quantity1 = quantity;
        this.quantity2 = quantity;
        setUnit(unit);
        this.price = price;

        calculateTotal();
    }

    private void validateNumber(
            double value,
            String fieldName
    ) {
        if (
                Double.isNaN(value)
                        || Double.isInfinite(value)
                        || value < 0
        ) {
            throw new IllegalArgumentException(
                    fieldName + " غير صالح"
            );
        }
    }

    public boolean isTile() {
        return "متر".equals(unit);
    }

    public boolean isSanitary() {
        return "قطعة".equals(unit);
    }

    @Override
    public String toString() {
        return material
                + " - "
                + quantity1
                + " "
                + unit
                + " × "
                + price
                + " = "
                + total;
    }
}