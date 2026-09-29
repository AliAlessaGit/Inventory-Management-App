package frontEnd.account;

import backEnd.account.Account;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class AccountsTableModel
        extends AbstractTableModel {

    private final List<Account> accounts;

    private final String[] columns = {
            "اسم الحساب"
    };

    public AccountsTableModel(
            List<Account> accounts
    ) {
        this.accounts = accounts;
    }

    @Override
    public int getRowCount() {

        return accounts == null
                ? 0
                : accounts.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(
            int column
    ) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(
            int column
    ) {
        return String.class;
    }

    @Override
    public Object getValueAt(
            int row,
            int column
    ) {

        return accounts
                .get(row)
                .getName();
    }
}