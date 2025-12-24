import javax.swing.table.AbstractTableModel;
import java.util.List;

public class InvoicesTableModel extends AbstractTableModel {

    private List<AccountInvoice> invoices;
    private final String[] cols = {"رقم", "تاريخ", "بيان", "الإجمالي"};

    public InvoicesTableModel(List<AccountInvoice> invoices) {
        this.invoices = invoices;
    }

    public void setInvoices(List<AccountInvoice> invoices) {
        this.invoices = invoices;
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return invoices == null ? 0 : invoices.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override
    public Object getValueAt(int r, int c) {
        AccountInvoice inv = invoices.get(r);
        return switch (c) {
            case 0 -> inv.getNumber();
            case 1 -> inv.getFormattedDate();
            case 2 -> inv.getDescription();
            case 3 -> String.format("%.2f", inv.getTotal());
            default -> "";
        };
    }
}
