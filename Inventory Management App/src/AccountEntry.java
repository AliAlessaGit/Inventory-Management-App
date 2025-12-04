import java.io.Serializable;

public class AccountEntry implements Serializable {


    private String date;          // التاريخ
    private String description;   // البيان
    private double debit;         // مدين
    private double credit;        // دائن
    private double qty;           // العدد
    private String unit;          // الوحدة (كلمة مثل م²، قطعة …)
    private double unitPrice;     // الافرادي

    public AccountEntry(String date, String description,
                        double debit, double credit,
                        double qty, String unit, double unitPrice) {
        this.date        = date;
        this.description = description;
        this.debit       = debit;
        this.credit      = credit;
        this.qty         = qty;
        this.unit        = unit;
        this.unitPrice   = unitPrice;
    }

    /* getters & setters */

    public String  getDate()        { return date; }
    public String  getDescription() { return description; }
    public double  getDebit()       { return debit; }
    public double  getCredit()      { return credit; }
    public double  getQty()         { return qty; }
    public String  getUnit()        { return unit; }
    public double  getUnitPrice()   { return unitPrice; }
    public double  getTotal()       { return qty * unitPrice; }


}