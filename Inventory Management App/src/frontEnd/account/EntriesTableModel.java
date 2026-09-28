package frontEnd.account;

import backEnd.account.AccountEntry;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class EntriesTableModel extends AbstractTableModel {

    private final String[] columns = {
            "المادة", "الكمية 1", "الوحدة", "السعر", "الإجمالي", "الكمية 2"
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
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case 1, 3, 4, 5 -> Double.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int row, int column) {
        AccountEntry entry = entries.get(row);
        return switch (column) {
            case 0 -> entry.getMaterial();
            case 1 -> entry.getQuantity1();
            case 2 -> entry.getUnit();
            case 3 -> entry.getPrice();
            case 4 -> entry.getTotal();
            case 5 -> entry.getQuantity2();
            default -> "";
        };
    }
}
