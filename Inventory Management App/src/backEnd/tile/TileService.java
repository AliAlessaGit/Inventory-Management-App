package backEnd.tile;

import backEnd.inventory.InventoryItem;
import backEnd.storage.JSONUtil;
import backEnd.warehouse.Warehouse;

import java.util.ArrayList;
import java.util.List;

public class TileService {

    private List<TileItem> tileItems = new ArrayList<>();

    private final String dataPath;

    private List<Warehouse> warehouses;

    private final TileFilterService tileFilterService;

    public TileService(
            String dataPath,
            List<Warehouse> warehouses) {

        this.dataPath = dataPath;
        this.warehouses = warehouses;
        this.tileFilterService = new TileFilterService();

        load();
    }

    // =========================================================
    // الوصول إلى البيانات
    // =========================================================

    public List<TileItem> getAll() {
        return tileItems;
    }

    public TileItem findById(long id) {

        for (TileItem item : tileItems) {

            if (item.getIdNumber() == id) {
                return item;
            }
        }

        return null;
    }

    // =========================================================
    // CRUD
    // =========================================================

    public void add(TileItem item) {

        if (item == null) {
            return;
        }

        tileItems.add(item);
    }

    public void remove(TileItem item) {

        if (item == null) {
            return;
        }

        boolean removed = tileItems.remove(item);

        if (removed) {
            save();
        }
    }

    public void update(
            TileItem oldItem,
            TileItem updatedValues) {

        if (oldItem == null || updatedValues == null) {
            return;
        }

        int index = tileItems.indexOf(oldItem);

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
                tileItems
        );
    }

    public void load() {

        List<TileItem> loadedItems =
                JSONUtil.readAll(
                        dataPath,
                        TileItem.class
                );

        if (loadedItems == null) {
            loadedItems = new ArrayList<>();
        }

        this.tileItems = loadedItems;

        for (TileItem item : tileItems) {

            if (item != null) {

                InventoryItem.bumpIdCounterIfNeeded(
                        item.getIdNumber()
                );
            }
        }
    }

    // =========================================================
    // البحث
    // =========================================================

    /**
     * البحث القديم بالاسم أو الكود.
     * أبقيناه للتوافق مع الواجهات القديمة.
     */
    public List<TileItem> searchByNameOrCode(
            String text) {

        return tileFilterService.search(
                tileItems,
                text
        );
    }

    // =========================================================
    // الفلترة
    // =========================================================

    public List<TileItem> filter(
            TileFilter filter) {

        return tileFilterService.filter(
                tileItems,
                filter
        );
    }

    public List<TileItem> filterAndSearch(
            TileFilter filter,
            String searchText) {

        return tileFilterService.filterAndSearch(
                tileItems,
                filter,
                searchText
        );
    }

    public TileFilterService getTileFilterService() {
        return tileFilterService;
    }

    // =========================================================
    // الفلترة القديمة
    // material + warehouse + subtype
    // =========================================================

    public List<TileItem> filter(
            String material,
            String warehouse,
            String subtype) {

        TileFilter filter = new TileFilter();

        // -----------------------------------------------------
        // المادة
        // -----------------------------------------------------

        if (material != null
                && !material.trim().isEmpty()
                && !material.equals("كل الأنواع")
                && !material.equals("كل المواد")) {

            try {

                filter.addMaterial(
                        TileItem.MaterialType.valueOf(
                                material
                        )
                );

            } catch (IllegalArgumentException ignored) {
                // القيمة غير معروفة، لذلك نتجاهلها
            }
        }

        // -----------------------------------------------------
        // النوع الفرعي
        // -----------------------------------------------------

        if (subtype != null
                && !subtype.trim().isEmpty()
                && !subtype.equals("كل الأنواع")
                && !subtype.equals("كل الأنواع الفرعية")) {

            try {

                filter.addSubType(
                        TileItem.SubType.valueOf(
                                subtype
                        )
                );

            } catch (IllegalArgumentException ignored) {
                // القيمة غير معروفة، لذلك نتجاهلها
            }
        }

        // -----------------------------------------------------
        // المستودع
        // -----------------------------------------------------

        if (warehouse != null
                && !warehouse.trim().isEmpty()
                && !warehouse.equals("كل المستودعات")
                && warehouses != null) {

            for (Warehouse w : warehouses) {

                if (w == null) {
                    continue;
                }

                if (w.getDisplayName().equals(warehouse)) {

                    filter.addWarehouse(w);
                    break;
                }
            }
        }

        return filter(filter);
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