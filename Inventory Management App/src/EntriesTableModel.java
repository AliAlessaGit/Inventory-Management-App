import javax.swing.table.AbstractTableModel;
import java.util.List;

public class EntriesTableModel extends AbstractTableModel {

    private final String[] cols = {
            "مدين", "دائن", "البيان", "الرصيد",
            "العدد", "الوحدة", "الإفرادي", "الإجمالي"
    };

    private List<AccountEntry> entries;

    public EntriesTableModel(List<AccountEntry> entries) {
        this.entries = entries;
    }

    public void setEntries(List<AccountEntry> entries) {
        this.entries = entries;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return entries == null ? 0 : entries.size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int c) {
        return cols[c];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return switch (col) {
            case 0, 1, 3, 4, 6, 7 -> Double.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int r, int c) {
        AccountEntry e = entries.get(r);
        return switch (c) {
            case 0 -> e.getDebit();
            case 1 -> e.getCredit();
            case 2 -> e.getDescription();
            case 3 -> runningBalance(r);
            case 4 -> e.getQty();
            case 5 -> e.getUnit();
            case 6 -> e.getUnitPrice();
            case 7 -> e.getTotal();
            default -> "";
        };
    }

    /* الرصيد التراكمي داخل الفاتورة */
    private double runningBalance(int r) {
        double bal = 0;
        for (int i = 0; i <= r; i++) {
            bal += entries.get(i).getCredit() - entries.get(i).getDebit();
        }
        return bal;
    }
}
