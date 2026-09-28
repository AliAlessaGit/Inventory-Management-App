package backEnd.account;

import backEnd.storage.JSONUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountsManager {

    private final List<Account> accounts =
            new ArrayList<>();

    private String storagePath;

    public AccountsManager() {
    }

    public AccountsManager(
            String storagePath
    ) {

        this.storagePath = storagePath;

        try {
            loadFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStoragePath(
            String storagePath
    ) {

        this.storagePath = storagePath;
    }

    public List<Account> getAccounts() {

        return Collections.unmodifiableList(
                accounts
        );
    }

    public Account addAccount(
            String name
    ) {

        if (nameExists(name)) {
            throw new IllegalArgumentException(
                    "هذا الحساب موجود مسبقاً"
            );
        }

        Account account =
                new Account(name);

        accounts.add(account);

        return account;
    }

    public void removeAccount(
            Account account
    ) {

        accounts.remove(account);
    }

    public boolean nameExists(
            String name
    ) {

        if (name == null) {
            return false;
        }

        String value =
                name.trim();

        if (value.isEmpty()) {
            return false;
        }

        return accounts.stream()
                .anyMatch(
                        a -> a.getName()
                                .equalsIgnoreCase(value)
                );
    }

    public Account findByName(
            String name
    ) {

        if (name == null) {
            return null;
        }

        String value =
                name.trim();

        for (Account account : accounts) {

            if (
                    account.getName()
                            .equalsIgnoreCase(value)
            ) {
                return account;
            }
        }

        return null;
    }

    public Account findByInvoiceNumber(
            String number
    ) {

        if (number == null) {
            return null;
        }

        for (Account account : accounts) {

            for (
                    AccountInvoice invoice :
                    account.getAccountInvoices()
            ) {

                if (
                        number.equalsIgnoreCase(
                                invoice.getNumber()
                        )
                ) {
                    return account;
                }
            }
        }

        return null;
    }

    public AccountInvoice findInvoice(
            String number
    ) {

        if (number == null) {
            return null;
        }

        for (Account account : accounts) {

            for (
                    AccountInvoice invoice :
                    account.getAccountInvoices()
            ) {

                if (
                        number.equalsIgnoreCase(
                                invoice.getNumber()
                        )
                ) {
                    return invoice;
                }
            }
        }

        return null;
    }

    public void loadFromFile()
            throws IOException {

        if (
                storagePath == null
                        || storagePath.trim().isEmpty()
        ) {
            return;
        }

        List<Account> loaded =
                JSONUtil.readAll(
                        storagePath,
                        Account.class
                );

        accounts.clear();

        if (loaded != null) {
            accounts.addAll(loaded);
        }

        /*
         * مهم جداً:
         * بعد تحميل الحسابات نعيد ضبط عداد
         * أرقام الفواتير.
         */
        AccountInvoice.initCounter(
                accounts
        );
    }

    public void saveToFile()
            throws IOException {

        if (
                storagePath == null
                        || storagePath.trim().isEmpty()
        ) {
            return;
        }

        JSONUtil.writeAll(
                storagePath,
                accounts
        );
    }

    public void saveQuietly() {

        try {
            saveToFile();
        } catch (Exception ignored) {
        }
    }
}