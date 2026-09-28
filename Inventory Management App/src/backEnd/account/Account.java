package backEnd.account;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private final List<AccountInvoice>
            accountInvoices =
            new ArrayList<>();

    public Account(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {

        if (
                name == null
                        || name.trim().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "اسم الشركة لا يمكن أن يكون فارغاً"
            );
        }

        this.name = name.trim();
    }

    public List<AccountInvoice>
    getAccountInvoices() {

        return accountInvoices;
    }

    public void addAccountInvoice(
            AccountInvoice accountInvoice
    ) {

        if (accountInvoice == null) {
            throw new IllegalArgumentException(
                    "الفاتورة مطلوبة"
            );
        }

        accountInvoices.add(accountInvoice);
    }

    public void removeAccountInvoice(
            AccountInvoice accountInvoice
    ) {

        accountInvoices.remove(
                accountInvoice
        );
    }

    public double getTotalDebit() {

        return accountInvoices.stream()
                .mapToDouble(
                        AccountInvoice::getTotalDebit
                )
                .sum();
    }

    public double getTotalCredit() {

        return accountInvoices.stream()
                .mapToDouble(
                        AccountInvoice::getTotalCredit
                )
                .sum();
    }

    /**
     * رصيد الشركة:
     *
     * المدين - الدائن.
     */
    public double getBalance() {

        return getTotalDebit()
                - getTotalCredit();
    }

    public String getSide() {

        double balance =
                getBalance();

        if (balance > 0) {
            return "مدين";
        }

        if (balance < 0) {
            return "دائن";
        }

        return "متزن";
    }
}