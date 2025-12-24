import java.io.Serializable;

public class AccountEntry implements Serializable {

    private String description;
    private double debit;
    private double credit;
    private double qty;
    private String unit;
    private double unitPrice;

    public AccountEntry(String description,
                        double debit, double credit,
                        double qty, String unit, double unitPrice) {
        this.description = description;
        this.debit = debit;
        this.credit = credit;
        this.qty = qty;
        this.unit = unit;
        this.unitPrice = unitPrice;
    }

    public String getDescription() { return description; }
    public double getDebit() { return debit; }
    public double getCredit() { return credit; }
    public double getQty() { return qty; }
    public String getUnit() { return unit; }
    public double getUnitPrice() { return unitPrice; }

    public double getTotal() { return qty * unitPrice; }
}
