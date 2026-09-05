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


    public List<TileItem> getAll() {
        return tileItems;
    }


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

        tileItems.remove(item);
    }


    public void update(
            TileItem oldItem,
            TileItem updatedValues) {

        if (oldItem == null || updatedValues == null) {
            return;
        }

        int idx = tileItems.indexOf(oldItem);

        if (idx >= 0) {

            oldItem.applyUpdatesFrom(updatedValues);

            InventoryItem.bumpIdCounterIfNeeded(
                    oldItem.getIdNumber()
            );

            save();
        }
    }


    public void save() {

        JSONUtil.writeAll(
                dataPath,
                tileItems
        );
    }


    public void load() {

        this.tileItems =
                JSONUtil.readAll(
                        dataPath,
                        TileItem.class
                );

        if (this.tileItems == null) {
            this.tileItems = new ArrayList<>();
        }

        for (TileItem item : this.tileItems) {

            InventoryItem.bumpIdCounterIfNeeded(
                    item.getIdNumber()
            );
        }
    }


    public TileItem findById(long id) {

        for (TileItem t : tileItems) {

            if (t.getIdNumber() == id) {
                return t;
            }
        }

        return null;
    }


    /**
     * البحث القديم.
     *
     * يبقى موجودًا للتوافق مع أي واجهة قديمة.
     */
    public List<TileItem> searchByNameOrCode(
            String text) {

        return tileFilterService.search(
                tileItems,
                text
        );
    }


    /**
     * الفلترة الجديدة.
     */
    public List<TileItem> filter(
            TileFilter filter) {

        return tileFilterService.filter(
                tileItems,
                filter
        );
    }


    /**
     * الفلترة + البحث.
     *
     * الفلترة تطبق أولاً ثم البحث.
     */
    public List<TileItem> filterAndSearch(
            TileFilter filter,
            String searchText) {

        return tileFilterService.filterAndSearch(
                tileItems,
                filter,
                searchText
        );
    }


    /**
     * الوصول إلى محرك الفلترة.
     */
    public TileFilterService getTileFilterService() {

        return tileFilterService;
    }


    /**
     * النظام القديم:
     *
     * material + warehouse + subtype
     *
     * أبقيناه حتى لا نكسر الكود القديم.
     */
    public List<TileItem> filter(
            String material,
            String warehouse,
            String subtype) {

        TileFilter filter = new TileFilter();


        /*
         * المادة
         */
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
                // تجاهل القيمة غير المعروفة
            }
        }


        /*
         * النوع
         */
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
                // تجاهل القيمة غير المعروفة
            }
        }


        /*
         * المستودع
         */
        if (warehouse != null
                && !warehouse.trim().isEmpty()
                && !warehouse.equals("كل المستودعات")) {

            if (warehouses != null) {

                for (Warehouse w : warehouses) {

                    if (w != null
                            && w.getDisplayName()
                            .equals(warehouse)) {

                        filter.addWarehouse(w);
                        break;
                    }
                }
            }
        }


        return filter(filter);
    }
}