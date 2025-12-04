import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TileService {
    private List<TileItem> tileItems = new ArrayList<>();
    private final String dataPath; // اسم أعم
    private List<Warehouse> warehouses;
    public TileService(String dataPath, List<Warehouse> warehouses) {
        this.dataPath = dataPath; // e.g., "data/tiles.json"
        this.warehouses = warehouses;
        load(); // تحميل البيانات عند الإنشاء
    }

    public List<TileItem> getAll() { return tileItems; }
    public void add(TileItem item) { tileItems.add(item); }
    public void remove(TileItem item) { tileItems.remove(item); }
    public void update(TileItem oldItem, TileItem updatedValues) {
        if (oldItem == null || updatedValues == null) return;
        int idx = tileItems.indexOf(oldItem);
        if (idx >= 0) {
            oldItem.applyUpdatesFrom(updatedValues);
            InventoryItem.bumpIdCounterIfNeeded(oldItem.getIdNumber());
            save();
        }
    }    // داخل TileService
    // تم التعديل: الحفظ باستخدام JSON
    public void save() {
        JSONUtil.writeAll(dataPath, tileItems);
    }

    // تم التعديل: التحميل باستخدام JSON (أبسط بكثير!)
    public void load() {
        this.tileItems = JSONUtil.readAll(dataPath, TileItem.class);

        // تحديث عداد ID العام
        for (TileItem item : this.tileItems) {
            InventoryItem.bumpIdCounterIfNeeded(item.getIdNumber());
        }
    }
    // اختياري: تسهيل البحث بالـ idNumber
    public TileItem findById(long id) {
        for (TileItem t : tileItems) {
            if (t.getIdNumber() == id) return t;
        }
        return null;
    }


    public List<TileItem> searchByNameOrCode(String text) {
        if (text == null || text.trim().isEmpty()) return getAll();
        String txtLower = text.trim().toLowerCase();
        return tileItems.stream()
                .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(txtLower)) ||
                        (item.getCode() != null && item.getCode().toLowerCase().contains(txtLower)))
                .collect(Collectors.toList());
    }

    public List<TileItem> filter(String material, String warehouse, String subtype) {
        List<TileItem> filtered = new ArrayList<>();
        for (TileItem t : tileItems) {
            boolean match = (material == null || material.equals("كل الأنواع") || t.getMaterialType().name().equalsIgnoreCase(material)) &&
                    (warehouse == null || warehouse.equals("كل المستودعات") || ("المستودع " + t.getWarehouseNumber()).equals(warehouse)) &&
                    (subtype == null || subtype.equals("كل الأنواع") || t.getSubType().name().equalsIgnoreCase(subtype));
            if (match) filtered.add(t);
        }
        return filtered;
    }
}