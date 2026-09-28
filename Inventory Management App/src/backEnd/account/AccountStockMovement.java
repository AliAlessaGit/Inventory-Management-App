package backEnd.account;

import java.io.Serializable;

public class AccountStockMovement implements Serializable {

    private static final long serialVersionUID = 1L;

    private AccountStockMovementType movementType;
    private AccountInventoryKind inventoryKind;

    private long inventoryItemId;

    private String material;

    private double quantity1;
    private double quantity2;

    private double price;
    private double previousPrice;

    private double boxArea;

    public AccountStockMovement() {
    }

    public AccountStockMovement(
            AccountStockMovementType movementType,
            AccountInventoryKind inventoryKind,
            long inventoryItemId,
            String material,
            double quantity1,
            double quantity2,
            double price,
            double previousPrice,
            double boxArea
    ) {
        this.movementType = movementType;
        this.inventoryKind = inventoryKind;
        this.inventoryItemId = inventoryItemId;
        this.material = material;
        this.quantity1 = quantity1;
        this.quantity2 = quantity2;
        this.price = price;
        this.previousPrice = previousPrice;
        this.boxArea = boxArea;
    }

    public AccountStockMovementType getMovementType() {
        return movementType;
    }

    public AccountInventoryKind getInventoryKind() {
        return inventoryKind;
    }

    public long getInventoryItemId() {
        return inventoryItemId;
    }

    public String getMaterial() {
        return material;
    }

    public double getQuantity1() {
        return quantity1;
    }

    public double getQuantity2() {
        return quantity2;
    }

    public double getPrice() {
        return price;
    }

    public double getPreviousPrice() {
        return previousPrice;
    }

    public double getBoxArea() {
        return boxArea;
    }

    public void setMovementType(AccountStockMovementType movementType) {
        this.movementType = movementType;
    }

    public void setInventoryKind(AccountInventoryKind inventoryKind) {
        this.inventoryKind = inventoryKind;
    }

    public void setInventoryItemId(long inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setQuantity1(double quantity1) {
        this.quantity1 = quantity1;
    }

    public void setQuantity2(double quantity2) {
        this.quantity2 = quantity2;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setPreviousPrice(double previousPrice) {
        this.previousPrice = previousPrice;
    }

    public void setBoxArea(double boxArea) {
        this.boxArea = boxArea;
    }
}