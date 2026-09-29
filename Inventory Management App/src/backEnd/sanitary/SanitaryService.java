package backEnd.sanitary;

import backEnd.inventory.InventoryItem;
import backEnd.storage.JSONUtil;
import backEnd.warehouse.Warehouse;

import java.util.ArrayList;
import java.util.List;

public class SanitaryService {

    private List<SanitaryItem> sanitaryItems = new ArrayList<>();

    private final String dataPath;

    private List<Warehouse> warehouses;

    public SanitaryService(
            String dataPath,
            List<Warehouse> warehouses) {

        this.dataPath = dataPath;
        this.warehouses = warehouses;

        load();
    }

    // =========================================================
    // الوصول إلى البيانات
    // =========================================================

    public List<SanitaryItem> getAll() {
        return sanitaryItems;
    }

    public SanitaryItem findById(long id) {

        for (SanitaryItem item : sanitaryItems) {

            if (item.getIdNumber() == id) {
                return item;
            }
        }

        return null;
    }

    // =========================================================
    // CRUD
    // =========================================================

    public void add(SanitaryItem item) {

        if (item == null) {
            return;
        }

        sanitaryItems.add(item);

        save();
    }

    public void remove(SanitaryItem item) {

        if (item == null) {
            return;
        }

        boolean removed = sanitaryItems.remove(item);

        if (removed) {
            save();
        }
    }

    public void update(
            SanitaryItem oldItem,
            SanitaryItem updatedValues) {

        if (oldItem == null || updatedValues == null) {
            return;
        }

        int index = sanitaryItems.indexOf(oldItem);

        if (index < 0) {
            return;
        }

        oldItem.applyUpdatesFrom(updatedValues);

        InventoryItem.bumpIdCounterIfNeeded(
                oldItem.getIdNumber()
        );

        save();
    }

    // =========================================================
    // الحفظ والتحميل
    // =========================================================

    public void save() {

        JSONUtil.writeAll(
                dataPath,
                sanitaryItems
        );
    }

    public void load() {

        List<SanitaryItem> loadedItems =
                JSONUtil.readAll(
                        dataPath,
                        SanitaryItem.class
                );

        if (loadedItems == null) {
            loadedItems = new ArrayList<>();
        }

        this.sanitaryItems = loadedItems;

        for (SanitaryItem item : sanitaryItems) {

            if (item != null) {

                // توحيد النخب القديمة/المستوردة: 1..5 تصبح أول..خامس.
                item.setGrade(item.getGrade());

                InventoryItem.bumpIdCounterIfNeeded(
                        item.getIdNumber()
                );
            }
        }
    }

    // =========================================================
    // الفلترة
    // =========================================================

    public List<SanitaryItem> filter(String type) {

        List<SanitaryItem> filtered =
                new ArrayList<>();

        for (SanitaryItem item : sanitaryItems) {

            if (item == null) {
                continue;
            }

            boolean match;

            if (type == null
                    || type.trim().isEmpty()
                    || type.equals("كل الأنواع")) {

                match = true;

            } else {

                match =
                        item.getType() != null
                                && item.getType()
                                .getArabicName()
                                .equals(type);
            }

            if (match) {
                filtered.add(item);
            }
        }

        return filtered;
    }

    // =========================================================
    // البحث بالاسم والنخب
    // =========================================================

    /**
     * البحث الدقيق عن الأداة الصحية
     * باستخدام الاسم والنخب.
     */
    public SanitaryItem findByNameAndGrade(
            String name,
            String grade) {

        if (name == null || grade == null) {
            return null;
        }

        String nameTrimmed = name.trim();
        String gradeTrimmed = grade.trim();

        if (nameTrimmed.isEmpty()
                || gradeTrimmed.isEmpty()) {

            return null;
        }

        for (SanitaryItem item : sanitaryItems) {

            if (item == null) {
                continue;
            }

            if (item.getName() == null
                    || item.getGrade() == null) {

                continue;
            }

            boolean sameName =
                    item.getName()
                            .equalsIgnoreCase(nameTrimmed);

            boolean sameGrade =
                    item.getGrade()
                            .equalsIgnoreCase(gradeTrimmed);

            if (sameName && sameGrade) {
                return item;
            }
        }

        return null;
    }

    // =========================================================
    // إدارة المستودعات
    // =========================================================

    public List<Warehouse> getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(List<Warehouse> warehouses) {
        this.warehouses = warehouses;
    }
}