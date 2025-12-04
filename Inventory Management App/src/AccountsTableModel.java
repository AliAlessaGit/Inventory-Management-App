import javax.swing.table.AbstractTableModel;
import java.util.List;

public class AccountsTableModel extends AbstractTableModel {
    private final List<Account> accounts;
    private final String[] cols = {"الحسابات"};

    public AccountsTableModel(List<Account> accounts) {
        this.accounts = accounts;
    }

    @Override public int getRowCount() { return accounts.size(); }
    @Override public int getColumnCount() { return 1; }
    @Override public String getColumnName(int c) { return cols[c]; }
    @Override public Object getValueAt(int row, int col) {
        return accounts.get(row).getName();
    }
}