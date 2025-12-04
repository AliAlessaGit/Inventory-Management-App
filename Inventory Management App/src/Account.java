// Account.java
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private final List<AccountEntry> entries = new ArrayList<>();

    public Account(String name) { this.name = name; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<AccountEntry> getEntries() { return entries; }

    public void addEntry(AccountEntry entry) { entries.add(entry); }

    public double getTotalDebit() {
        return entries.stream().mapToDouble(AccountEntry::getDebit).sum();
    }
    public double getTotalCredit() {
        return entries.stream().mapToDouble(AccountEntry::getCredit).sum();
    }
    public double getBalance() { return getTotalCredit() - getTotalDebit(); } // الدائن - المدين

    public String getSide() {
        double bal = getBalance();
        if (bal > 0) return "دائن";
        if (bal < 0) return "مدين";
        return "متزن";
    }
}