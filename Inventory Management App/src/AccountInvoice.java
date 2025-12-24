import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AccountInvoice implements Serializable {

    private static int counter = 1;

    private String number;      // FA00001
    private LocalDate date;
    private String description;

    private final List<AccountEntry> entries = new ArrayList<>();

    public AccountInvoice(String description, LocalDate date) {
        this.number = String.format("FA%05d", counter++);
        this.description = description;
        this.date = date;
    }

    // يستخدم عند التحميل
    public void setNumberWhenLoading(String number) {
        this.number = number;
    }

    public static void initCounter(List<Account> accounts) {
        int max = 0;
        for (Account acc : accounts) {
            for (AccountInvoice inv : acc.getAccountInvoices()) {
                try {
                    int n = Integer.parseInt(inv.number.substring(2));
                    if (n > max) max = n;
                } catch (Exception ignored) {}
            }
        }
        counter = max + 1;
    }

    public String getNumber() { return number; }
    public LocalDate getDate() { return date; }
    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
    public String getDescription() { return description; }
    public List<AccountEntry> getEntries() { return entries; }

    public void addEntry(AccountEntry e) { entries.add(e); }

    public double getTotalDebit() {
        return entries.stream().mapToDouble(AccountEntry::getDebit).sum();
    }

    public double getTotalCredit() {
        return entries.stream().mapToDouble(AccountEntry::getCredit).sum();
    }

    public double getTotal() {
        return getTotalCredit() - getTotalDebit();
    }
}
