import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Account implements Serializable {

    private String name;

    // ⚠ لا final
    private List<AccountInvoice> AccountInvoices;

    public Account(String name) {
        this.name = name;
        this.AccountInvoices = new ArrayList<>();
    }

    /* ===== بيانات الحساب ===== */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* ===== حماية من null (مهم جدًا مع Gson) ===== */

    public List<AccountInvoice> getAccountInvoices() {
        if (AccountInvoices == null) {
            AccountInvoices = new ArrayList<>();
        }
        return AccountInvoices;
    }

    public void addAccountInvoice(AccountInvoice accountInvoice) {
        getAccountInvoices().add(accountInvoice);
    }

    /* ===== الرصيد ===== */

    public double getBalance() {
        return getAccountInvoices().stream()
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
