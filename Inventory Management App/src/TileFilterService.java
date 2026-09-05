import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * مسؤول عن تطبيق الفلاتر والبحث على عناصر البلاط.
 *
 * لا يحتوي على أي كود Swing.
 * لذلك يمكن استخدامه من أكثر من واجهة.
 */
public class TileFilterService {

    /**
     * تطبيق الفلاتر فقط.
     */
    public List<TileItem> filter(
            List<TileItem> source,
            TileFilter filter) {

        if (source == null) {
            return new ArrayList<>();
        }

        if (filter == null || filter.isEmpty()) {
            return new ArrayList<>(source);
        }

        return source.stream()
                .filter(item -> matches(item, filter))
                .collect(Collectors.toList());
    }

    /**
     * تطبيق الفلاتر ثم البحث.
     *
     * الترتيب مهم جدًا:
     *
     * البيانات الأصلية
     *       ↓
     * الفلاتر
     *       ↓
     * البحث
     *
     * وبالتالي البحث يتم فقط ضمن العناصر التي اجتازت الفلاتر.
     */
    public List<TileItem> filterAndSearch(
            List<TileItem> source,
            TileFilter filter,
            String searchText) {

        List<TileItem> filtered = filter(source, filter);

        if (searchText == null || searchText.trim().isEmpty()) {
            return filtered;
        }

        String text = searchText.trim().toLowerCase();

        return filtered.stream()
                .filter(item ->
                        containsIgnoreCase(item.getName(), text)
                                || containsIgnoreCase(item.getCode(), text)
                )
                .collect(Collectors.toList());
    }

    /**
     * التحقق من توافق عنصر واحد مع جميع الفلاتر.
     */
    public boolean matches(
            TileItem item,
            TileFilter filter) {

        if (item == null) {
            return false;
        }

        if (filter == null || filter.isEmpty()) {
            return true;
        }

        /*
         * المادة
         */
        if (!filter.getMaterials().isEmpty()) {

            if (item.getMaterialType() == null
                    || !filter.getMaterials().contains(item.getMaterialType())) {

                return false;
            }
        }

        /*
         * النوع
         */
        if (!filter.getSubTypes().isEmpty()) {

            if (item.getSubType() == null
                    || !filter.getSubTypes().contains(item.getSubType())) {

                return false;
            }
        }

        /*
         * المستودع
         */
        if (!filter.getWarehouses().isEmpty()) {

            if (item.getWarehouse() == null
                    || !containsWarehouse(
                    filter.getWarehouses(),
                    item.getWarehouse())) {

                return false;
            }
        }

        /*
         * الموقع
         */
        if (!filter.getLocations().isEmpty()) {

            if (item.getLocation() == null
                    || !filter.getLocations().contains(item.getLocation())) {

                return false;
            }
        }

        /*
         * النخب
         */
        if (!filter.getGrades().isEmpty()) {

            if (item.getGrade() == null
                    || !containsIgnoreCase(
                    filter.getGrades(),
                    item.getGrade())) {

                return false;
            }
        }

        /*
         * سعة الصندوق
         */
        if (!filter.getBoxAreas().isEmpty()) {

            if (!containsDouble(
                    filter.getBoxAreas(),
                    item.getBoxArea())) {

                return false;
            }
        }

        return true;
    }

    /**
     * الحصول على المواد الموجودة فعليًا في البيانات.
     */
    public List<TileItem.MaterialType> getAvailableMaterials(
            List<TileItem> items) {

        if (items == null) {
            return new ArrayList<>();
        }

        return items.stream()
                .map(TileItem::getMaterialType)
                .filter(value -> value != null)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * الحصول على الأنواع الموجودة فعليًا.
     */
    public List<TileItem.SubType> getAvailableSubTypes(
            List<TileItem> items) {

        if (items == null) {
            return new ArrayList<>();
        }

        return items.stream()
                .map(TileItem::getSubType)
                .filter(value -> value != null)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * الحصول على المستودعات الموجودة فعليًا.
     */
    public List<Warehouse> getAvailableWarehouses(
            List<TileItem> items) {

        if (items == null) {
            return new ArrayList<>();
        }

        List<Warehouse> result = new ArrayList<>();

        for (TileItem item : items) {

            if (item == null || item.getWarehouse() == null) {
                continue;
            }

            Warehouse warehouse = item.getWarehouse();

            boolean exists = result.stream()
                    .anyMatch(existing ->
                            existing.getId() == warehouse.getId());

            if (!exists) {
                result.add(warehouse);
            }
        }

        return result;
    }

    /**
     * الحصول على المواقع الموجودة فعليًا.
     */
    public List<TileItem.Location> getAvailableLocations(
            List<TileItem> items) {

        if (items == null) {
            return new ArrayList<>();
        }

        return items.stream()
                .map(TileItem::getLocation)
                .filter(value -> value != null)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * الحصول على النخب الموجودة فعليًا.
     */
    public List<String> getAvailableGrades(
            List<TileItem> items) {

        if (items == null) {
            return new ArrayList<>();
        }

        return items.stream()
                .map(TileItem::getGrade)
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    /**
     * الحصول على سعات الصناديق الموجودة فعليًا.
     */
    public List<Double> getAvailableBoxAreas(
            List<TileItem> items) {

        if (items == null) {
            return new ArrayList<>();
        }

        return items.stream()
                .map(TileItem::getBoxArea)
                .filter(value -> value > 0)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * تطبيق البحث فقط.
     *
     * أبقيناه هنا حتى تكون كل عمليات التصفية والبحث
     * في نفس الخدمة.
     */
    public List<TileItem> search(
            List<TileItem> source,
            String searchText) {

        if (source == null) {
            return new ArrayList<>();
        }

        if (searchText == null || searchText.trim().isEmpty()) {
            return new ArrayList<>(source);
        }

        String text = searchText.trim().toLowerCase();

        return source.stream()
                .filter(item ->
                        containsIgnoreCase(item.getName(), text)
                                || containsIgnoreCase(item.getCode(), text))
                .collect(Collectors.toList());
    }

    /**
     * مقارنة نصية بدون حساسية لحالة الأحرف.
     */
    private boolean containsIgnoreCase(
            String value,
            String searchText) {

        if (value == null || searchText == null) {
            return false;
        }

        return value.toLowerCase().contains(searchText.toLowerCase());
    }

    /**
     * البحث عن نص داخل مجموعة نصوص.
     */
    private boolean containsIgnoreCase(
            Set<String> values,
            String target) {

        if (values == null || target == null) {
            return false;
        }

        for (String value : values) {

            if (value != null
                    && value.equalsIgnoreCase(target)) {

                return true;
            }
        }

        return false;
    }

    /**
     * مقارنة المستودع بواسطة ID.
     *
     * لا نعتمد على equals() في Warehouse
     * حتى لا تتأثر الفلترة بطريقة تنفيذ Warehouse.
     */
    private boolean containsWarehouse(
            Set<Warehouse> warehouses,
            Warehouse target) {

        if (warehouses == null || target == null) {
            return false;
        }

        for (Warehouse warehouse : warehouses) {

            if (warehouse != null
                    && warehouse.getId() == target.getId()) {

                return true;
            }
        }

        return false;
    }

    /**
     * مقارنة Double مع هامش صغير لتجنب مشاكل floating point.
     */
    private boolean containsDouble(
            Set<Double> values,
            double target) {

        final double EPSILON = 0.000001;

        for (Double value : values) {

            if (value != null
                    && Math.abs(value - target) < EPSILON) {

                return true;
            }
        }

        return false;
    }
}