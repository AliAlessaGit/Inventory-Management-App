// Account.java
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Account implements Serializable {

    private String name;

    // حركات الحساب (AccountInvoice) — ليست Invoice
    private final List<AccountInvoice> AccountInvoices = new ArrayList<>();

    public Account(String name) {
        this.name = name;
    }

    /* ===== بيانات الحساب ===== */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* ===== إدارة AccountInvoices ===== */

    public List<AccountInvoice> getAccountInvoices() {
        return AccountInvoices;
    }

    public void addAccountInvoice(AccountInvoice accountInvoice) {
        AccountInvoices.add(accountInvoice);
    }

    /* ===== الرصيد ===== */

    public double getBalance() {
        return AccountInvoices.stream()
                .mapToDouble(AccountInvoice::getTotal)
                .sum();
    }

    public String getSide() {
        double bal = getBalance();
        if (bal > 0) return "دائن";
        if (bal < 0) return "مدين";
        return "متزن";
    }
}
