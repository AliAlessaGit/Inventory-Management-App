package frontEnd.account;

import backEnd.account.AccountInvoice;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class AccountInvoicesTableModel extends AbstractTableModel {

    private final String[] columns = {
            "رقم الفاتورة", "التاريخ", "أصل السند",
            "مدين", "دائن", "الرصيد", "البيان"
    };

    private List<AccountInvoice> invoices;

    public AccountInvoicesTableModel(List<AccountInvoice> invoices) {
        this.invoices = invoices;
    }

    public void setInvoices(List<AccountInvoice> invoices) {
        this.invoices = invoices;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return invoices == null ? 0 : invoices.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case 3, 4, 5 -> Double.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int row, int column) {
        AccountInvoice invoice = invoices.get(row);

        return switch (column) {
            case 0 -> invoice.getNumber();
            case 1 -> invoice.getFormattedDate();
            case 2 -> invoice.getType().getArabicName();
            case 3 -> invoice.getTotalDebit();
            case 4 -> invoice.getTotalCredit();
            case 5 -> invoice.getTotal();
            case 6 -> invoice.getDescription();
            default -> "";
        };
    }
}
