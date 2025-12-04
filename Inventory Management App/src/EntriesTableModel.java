import javax.swing.table.AbstractTableModel;
import java.util.List;

public class EntriesTableModel extends AbstractTableModel {
    private final String[] cols = {
            "التاريخ","مدين","دائن","البيان","الرصيد",
            "العدد","الوحدة","الافرادي","الإجمالي"
    };
    private List<AccountEntry> entries;

    public EntriesTableModel(List<AccountEntry> e){ this.entries=e; }

    public void setEntries(List<AccountEntry> e){
        this.entries=e; fireTableDataChanged();
    }

    @Override public int getRowCount(){ return entries==null?0:entries.size(); }
    @Override public int getColumnCount(){ return cols.length; }
    @Override public String getColumnName(int c){ return cols[c]; }

    @Override public Class<?> getColumnClass(int col){
        return switch (col){
            case 1,2,4,5,7,8 -> Double.class;
            default -> String.class;
        };
    }

    @Override public Object getValueAt(int r,int c){
        AccountEntry e = entries.get(r);
        return switch (c){
            case 0 -> e.getDate();
            case 1 -> e.getDebit();
            case 2 -> e.getCredit();
            case 3 -> e.getDescription();
            case 4 -> runningBalance(r);        // الرصيد التراكمي
            case 5 -> e.getQty();
            case 6 -> e.getUnit();
            case 7 -> e.getUnitPrice();
            case 8 -> e.getTotal();
            default -> "";
        };
    }

    /* حساب الرصيد حتى الصفّ r */
    private double runningBalance(int r){
        double bal = 0;
        for(int i=0;i<=r;i++){
            bal += entries.get(i).getCredit() - entries.get(i).getDebit();
        }
        return bal;
    }
}