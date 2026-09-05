import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * يمثل حالة الفلترة الحالية لعناصر البلاط.
 *
 * جميع القوائم اختيارية:
 * - إذا كانت القائمة فارغة => لا يوجد فلتر على هذا الحقل.
 * - إذا احتوت على قيم => يجب أن يطابق العنصر إحدى هذه القيم.
 *
 * يمكن الاحتفاظ بنفس الكائن أثناء استخدام الشاشة،
 * وبالتالي لا تضيع حالة الفلترة بعد التعديل أو التخريج.
 */
public class TileFilter {

    private final Set<TileItem.MaterialType> materials = new LinkedHashSet<>();
    private final Set<TileItem.SubType> subTypes = new LinkedHashSet<>();
    private final Set<Warehouse> warehouses = new LinkedHashSet<>();
    private final Set<TileItem.Location> locations = new LinkedHashSet<>();
    private final Set<String> grades = new LinkedHashSet<>();
    private final Set<Double> boxAreas = new LinkedHashSet<>();

    /**
     * المادة
     */
    public Set<TileItem.MaterialType> getMaterials() {
        return materials;
    }

    public void setMaterials(Collection<TileItem.MaterialType> values) {
        materials.clear();

        if (values != null) {
            materials.addAll(values);
        }
    }

    public void addMaterial(TileItem.MaterialType value) {
        if (value != null) {
            materials.add(value);
        }
    }

    public void removeMaterial(TileItem.MaterialType value) {
        materials.remove(value);
    }

    /**
     * النوع
     */
    public Set<TileItem.SubType> getSubTypes() {
        return subTypes;
    }

    public void setSubTypes(Collection<TileItem.SubType> values) {
        subTypes.clear();

        if (values != null) {
            subTypes.addAll(values);
        }
    }

    public void addSubType(TileItem.SubType value) {
        if (value != null) {
            subTypes.add(value);
        }
    }

    public void removeSubType(TileItem.SubType value) {
        subTypes.remove(value);
    }

    /**
     * المستودعات
     */
    public Set<Warehouse> getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(Collection<Warehouse> values) {
        warehouses.clear();

        if (values != null) {
            warehouses.addAll(values);
        }
    }

    public void addWarehouse(Warehouse value) {
        if (value != null) {
            warehouses.add(value);
        }
    }

    public void removeWarehouse(Warehouse value) {
        warehouses.remove(value);
    }

    /**
     * المواقع
     */
    public Set<TileItem.Location> getLocations() {
        return locations;
    }

    public void setLocations(Collection<TileItem.Location> values) {
        locations.clear();

        if (values != null) {
            locations.addAll(values);
        }
    }

    public void addLocation(TileItem.Location value) {
        if (value != null) {
            locations.add(value);
        }
    }

    public void removeLocation(TileItem.Location value) {
        locations.remove(value);
    }

    /**
     * النخب
     */
    public Set<String> getGrades() {
        return grades;
    }

    public void setGrades(Collection<String> values) {
        grades.clear();

        if (values != null) {
            for (String value : values) {
                if (value != null && !value.trim().isEmpty()) {
                    grades.add(value.trim());
                }
            }
        }
    }

    public void addGrade(String value) {
        if (value != null && !value.trim().isEmpty()) {
            grades.add(value.trim());
        }
    }

    public void removeGrade(String value) {
        grades.remove(value);
    }

    /**
     * سعة الصندوق / مساحة الصندوق
     */
    public Set<Double> getBoxAreas() {
        return boxAreas;
    }

    public void setBoxAreas(Collection<Double> values) {
        boxAreas.clear();

        if (values != null) {
            for (Double value : values) {
                if (value != null) {
                    boxAreas.add(value);
                }
            }
        }
    }

    public void addBoxArea(Double value) {
        if (value != null) {
            boxAreas.add(value);
        }
    }

    public void removeBoxArea(Double value) {
        boxAreas.remove(value);
    }

    /**
     * إزالة جميع الفلاتر.
     */
    public void clear() {
        materials.clear();
        subTypes.clear();
        warehouses.clear();
        locations.clear();
        grades.clear();
        boxAreas.clear();
    }

    /**
     * هل لا يوجد أي فلتر؟
     */
    public boolean isEmpty() {
        return materials.isEmpty()
                && subTypes.isEmpty()
                && warehouses.isEmpty()
                && locations.isEmpty()
                && grades.isEmpty()
                && boxAreas.isEmpty();
    }

    /**
     * إنشاء نسخة مستقلة من الفلتر.
     */
    public TileFilter copy() {
        TileFilter copy = new TileFilter();

        copy.materials.addAll(materials);
        copy.subTypes.addAll(subTypes);
        copy.warehouses.addAll(warehouses);
        copy.locations.addAll(locations);
        copy.grades.addAll(grades);
        copy.boxAreas.addAll(boxAreas);

        return copy;
    }

    @Override
    public String toString() {
        return "TileFilter{" +
                "materials=" + materials +
                ", subTypes=" + subTypes +
                ", warehouses=" + warehouses +
                ", locations=" + locations +
                ", grades=" + grades +
                ", boxAreas=" + boxAreas +
                '}';
    }
}