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
            case 5 -> getRunningBalance(row);
            case 6 -> invoice.getDescription();
            default -> "";
        };
    }

    /**
     * الرصيد التراكمي حتى هذه الفاتورة.
     * الفاتورة الحالية تضيف حركتها إلى رصيد الفواتير السابقة.
     */
    private double getRunningBalance(int row) {

        double balance = 0.0;

        if (invoices == null) {
            return 0.0;
        }

        for (int i = 0; i <= row && i < invoices.size(); i++) {

            AccountInvoice invoice =
                    invoices.get(i);

            if (invoice != null) {

                balance += invoice.getTotal();

                // تقريب الرصيد بعد كل فاتورة إلى منزلتين
                balance =
                        Math.round(
                                balance * 100.0
                        ) / 100.0;
            }
        }

        return balance;
    }
}
