import java.util.ArrayList;
import java.util.List;

public class WarehouseManager {
    private List<Warehouse> warehouses = new ArrayList<>();
    private String filePath = "data/warehouses.json"; // تم تغيير الامتداد إلى .json

    private int nextId = 1;

    public WarehouseManager(String s) {
        // تأكد من تحميل البيانات عند إنشاء الكائن
        load();
    }

    public List<Warehouse> getWarehouses() {
        return warehouses;
    }

    private int generateId() {
        return nextId++;
    }

    public void addWarehouse(Warehouse w) {
        if (w.getId() <= 0) {
            w.setId(generateId());
        }
        warehouses.add(w);
        save();
    }

    public void deleteWarehouse(int id) {
        warehouses.removeIf(w -> w.getId() == id);
        save();
    }

    public void updateWarehouse(Warehouse updated) {
        for (int i = 0; i < warehouses.size(); i++) {
            if (warehouses.get(i).getId() == updated.getId()) {
                warehouses.set(i, updated);
                break;
            }
        }
        save();
    }

    // تم التعديل: تحميل من JSON
    public void load() {
        this.warehouses = JSONUtil.readAll(filePath, Warehouse.class);

        // تحديث nextId ليكون أكبر من أعلى id موجود
        int maxId = 0;
        for (Warehouse w : warehouses) {
            if (w.getId() > maxId) {
                maxId = w.getId();
            }
        }
        this.nextId = maxId + 1;
    }

    // تم التعديل: حفظ إلى JSON
    private void save() {
        JSONUtil.writeAll(filePath, warehouses);
    }
}