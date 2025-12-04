// AccountsManager.java
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountsManager {
    private final List<Account> accounts = new ArrayList<>();
    private String storagePath; // مثال: data/accounts.json
    public AccountsManager() {}
    public AccountsManager(String storagePath) {
        this.storagePath = storagePath;
        // لا حاجة لرمي استثناءات هنا
        try {
            loadFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public List<Account> getAccounts() { return Collections.unmodifiableList(accounts); }
    public Account addAccount(String name) {
        Account a = new Account(name);
        accounts.add(a);
        return a;
    }

    public void removeAccount(Account account) { accounts.remove(account); }

    public boolean nameExists(String name) {
        return accounts.stream().anyMatch(a -> a.getName().equalsIgnoreCase(name.trim()));
    }

    // تحميل من الملف المحدد في storagePath
    @SuppressWarnings("unchecked")
    public void loadFromFile() throws IOException {
        if (storagePath == null) return;
        List<Account> loadedAccounts = JSONUtil.readAll(storagePath, Account.class);
        this.accounts.clear();
        this.accounts.addAll(loadedAccounts);
    }

    // تم التعديل: حفظ إلى ملف JSON
    public void saveToFile() throws IOException {
        if (storagePath == null) return;
        JSONUtil.writeAll(storagePath, accounts);
    }

    // للاستخدام الهادئ من الواجهة بدون إزعاج المستخدم بالأخطاء


    // للاستخدام الهادئ من الواجهة بدون إزعاج المستخدم بالأخطاء
    public void saveQuietly() {
        try { saveToFile(); } catch (Exception ignored) {}
    }
}